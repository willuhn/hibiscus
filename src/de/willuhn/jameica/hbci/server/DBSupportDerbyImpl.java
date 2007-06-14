/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/DBSupportDerbyImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/14 18:04:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.logging.LoggerOutputStream;
import de.willuhn.sql.CheckSum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des Datenbank-Supports fuer Apache Derby.
 */
public class DBSupportDerbyImpl extends AbstractDBSupportImpl
{
  /**
   * Log-Handler fuer Derby.
   */
  public final static OutputStream os = new LoggerOutputStream(Logger.getLevel());
  
  // Mapper von Datenbank-Hash zu Versionsnummer
  private static HashMap DBMAPPING = new HashMap();
  // private boolean whileInstall = false;

  static
  {
    DBMAPPING.put("1B2M2Y8AsgTpgAmY7PhCfg==",new Double(1.7));
    
    Logger.info("setting derby system properties");
    // Liste moeglicher Parameter:
    // http://developers.sun.com/docs/javadb/10.2.2/tuning/ctunproper22250.html
    System.setProperty("derby.stream.error.field",DBSupportDerbyImpl.class.getName() + ".os");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
   */
  public String getJdbcDriver()
  {
    return "org.apache.derby.jdbc.EmbeddedDriver";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
   */
  public String getJdbcPassword()
  {
    String password = HBCIDBService.SETTINGS.getString("database.driver.derby.encryption.encryptedpassword",null);
    try
    {
      // Existiert noch nicht. Also neu erstellen.
      if (password == null)
      {
        // Wir koennen als Passwort nicht so einfach das Masterpasswort
        // nehmen, weil der User es aendern kann. Wir koennen zwar
        // das Passwort der Datenbank aendern. Allerdings kriegen wir
        // hier nicht mit, wenn sich das Passwort geaendert hat.
        // Daher erzeugen wir ein selbst ein Passwort.
        Logger.info("generating new random password for database");
        byte[] data = new byte[16];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed((long) (new Date().getTime()));
        random.nextBytes(data);
        
        // Jetzt noch verschluesselt abspeichern
        Logger.info("encrypting password with system certificate");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Application.getSSLFactory().encrypt(new ByteArrayInputStream(data),bos);
        
        // Wird kodieren es noch als Base64, damit die properties-Datei nicht
        // zur Binaer-Datei wird ;)
        HBCIDBService.SETTINGS.setAttribute("database.driver.derby.encryption.encryptedpassword",Base64.encode(bos.toByteArray()));
        return Base64.encode(data); // Base64 damit es in der JDBC-URL verwendet werden kann
      }

      Logger.debug("decrypting database password");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().decrypt(new ByteArrayInputStream(Base64.decode(password)),bos);
      return Base64.encode(bos.toByteArray()); // Base64 damit es in der JDBC-URL verwendet werden kann
    }
    catch (Exception e)
    {
      throw new RuntimeException("error while determining database password",e);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
   */
  public String getJdbcUrl()
  {
    String url = "jdbc:derby:" + Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/derby";

    if (HBCIDBService.SETTINGS.getBoolean("database.driver.derby.encryption",true))
    {
      String algo     = HBCIDBService.SETTINGS.getString("database.driver.derby.encryption.algorithm","AES/CBC/NoPadding");
      String provider = HBCIDBService.SETTINGS.getString("database.driver.derby.encryption.provider",BouncyCastleProvider.class.getName());
      
      url += ";dataEncryption=true"
          +  ";bootPassword=" + getJdbcPassword()
          +  ";encryptionAlgorithm=" + algo
          +  ";encryptionProvider=" + provider;
    }
    if (!HBCIDBService.SETTINGS.getBoolean("database.driver.derby.databasecreated",false))
    {
      Logger.info("creating new database");
      url += ";create=true";
    }
    return url;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    // Wir verwenden "app" als Usernamen damit das Default-Schema verwendet wird. 
    return "hibiscus";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#checkConsistency(java.sql.Connection)
   */
  public void checkConsistency(Connection conn) throws RemoteException, ApplicationException
  {

    ////////////////////////////////////////////////////////////////////////////
    // Damit wir die Updates nicht immer haendisch nachziehen muessen, rufen wir
    // das letzte Update-Script ggf. nochmal auf.
    if (!Application.inClientMode())
    {
      try
      {
        PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
        de.willuhn.jameica.system.Settings s = res.getSettings();
        double size = s.getDouble("sql-update-size",-1);
        
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH); // Punkt als Dezimal-Trenner
        df.setMaximumFractionDigits(1);
        df.setMinimumFractionDigits(1);
        df.setGroupingUsed(false);

        double version    = Application.getPluginLoader().getManifest(HBCI.class).getVersion();
        double oldVersion = version - 0.1d;

        File f = new File(res.getPath() + File.separator + "sql",
            "update_" + df.format(oldVersion) + "-" + df.format(version) + ".sql");

        if (f.exists())
        {
          long length = f.length();
          if (length != size)
          {
            s.setAttribute("sql-update-size",(double)f.length());
            execute(conn, f);
          }
          else
            Logger.info("database up to date");
        }
      }
      catch (Exception e2)
      {
        Logger.error("unable to execute sql update script",e2);
      }
    }
    ////////////////////////////////////////////////////////////////////////////
    
    if (!Settings.getCheckDatabase())
      return;

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
      ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
      monitor.setStatusText(i18n.tr("Prüfe Datenbank-Integrität"));

      String checkSum = CheckSum.md5(conn,null,"APP");
      if (DBMAPPING.get(checkSum) == null)
        throw new ApplicationException(i18n.tr("Datenbank-Checksumme ungültig: {0}. Datenbank-Version nicht kompatibel zur Hibiscus-Version?",checkSum));
      monitor.setStatusText(i18n.tr("Datenbank-Checksumme korrekt"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Prüfen der Datenbank"),e);
    }
  }

  /**
   * Ueberschrieben, weil SQL-Scripts bei Derby mit einem Prefix versehen werden.
   * Das soll der Admin sicherheitshalber manuell durchfuehren. Wir hinterlassen stattdessen
   * nur einen Hinweistext mit den auszufuehrenden SQL-Scripts.
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#execute(java.sql.Connection, java.io.File)
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException
  {
    if (sqlScript == null)
      return; // Ignore

    // Wir schreiben unseren Prefix davor.
    String prefix = HBCIDBService.SETTINGS.getString("database.driver.derby.scriptprefix","derby-");
    sqlScript = new File(sqlScript.getParent(),prefix + sqlScript.getName());
    if (!sqlScript.exists())
    {
      Logger.debug("file " + sqlScript + " does not exist, skipping");
      return;
    }
    super.execute(conn,sqlScript);
    
    // Wir markieren die Datenbank als erstellt
    HBCIDBService.SETTINGS.setAttribute("database.driver.derby.databasecreated",true);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return content; // Wie lautet die SQL-Funktion bei Derby, um UNIXTIME zu erzeugen?
    // return MessageFormat.format("tonumber({0})", new Object[]{content});
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    // brauchen wir bei nicht, da Embedded
  }
}


/*********************************************************************
 * $Log: DBSupportDerbyImpl.java,v $
 * Revision 1.1  2007/06/14 18:04:22  willuhn
 * @N Apache-Derby Treiber. Ich werde nicht auf Derby umstellen (siehe http://www.willuhn.de/blog/index.php?/archives/291-Tschuess-Apache-Derby.html) - der Code ist nur fuer's Archiv, um spaeter nochmal drin stoebern zu koennen
 *
 **********************************************************************/