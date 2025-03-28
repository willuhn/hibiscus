/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;

/**
 * Implementierung des Datenbank-Supports fuer H2-Database (http://www.h2database.com).
 */
public class DBSupportH2Impl extends AbstractDBSupportImpl
{
  private final static String DRIVER_FORK = "org.h14199.Driver";
  private final static String DRIVER      = "org.h2.Driver";
  
  private boolean haveFork = false;
  private boolean haveNew  = false;

  /**
   * ct.
   */
  public DBSupportH2Impl()
  {
    // H2-Datenbank verwendet uppercase Identifier
    Logger.info("switching dbservice to uppercase");
    System.setProperty(HBCIDBServiceImpl.class.getName() + ".uppercase","true");
    
    try
    {
      
      Method m = Class.forName("org.h14199.engine.Constants").getMethod("getVersion",(Class[]) null);
      Logger.info("h2 (1.4.199-fork) version: " + m.invoke(null,(Object[])null));
      this.haveFork = true;
    }
    catch (Throwable t)
    {
      Logger.info("h2 1.4.199-fork not present");
    }

    try
    {
      Method m = Class.forName("org.h2.engine.Constants").getMethod("getVersion",(Class[]) null);
      final Object version = m.invoke(null,(Object[])null);
      Logger.info("h2 version: " + version);
    }
    catch (Throwable t)
    {
      try
      {
        // Neuere H2-Versionen haben keine Methode "getVersion" mehr sondern ein Feld "VERSION"
        final Object o = Class.forName("org.h2.engine.Constants").getField("VERSION").get(null);
        final String version = o != null ? o.toString() : null;
        this.haveNew = version != null && version.startsWith("2");
        Logger.info("h2 version: " + version);
      }
      catch (Throwable t2)
      {
        Logger.info("h2 not present");
      }
    }
    
    Logger.info("have h2 fork: " + this.haveFork + ", have new h2: " + this.haveNew);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
   */
  public String getJdbcDriver()
  {
    if (this.isNewH2())
    {
      Logger.info("hibiscus migrated to new h2 version (2.x)");
      return DRIVER;
    }
    
    // Wir sind noch nicht migriert. Checken, ob wir auf einer Jameica-Version laufen,
    // welche schon beide H2-Treiber-Versionen enth�lt
    try
    {
      Class.forName(DRIVER_FORK);
      Logger.info("hibiscus not yet migrated to new h2 version, using fork-driver");
      return DRIVER_FORK;
    }
    catch (Throwable t)
    {
    }
    Logger.info("running in jameica version, that does not contain fork-driver");
    return DRIVER;
  }
  
  /**
   * Liefert true, wenn die neue H2-Version verwendet werden soll.
   * @return true, wenn die neue H2-Version verwendet werden soll.
   */
  private boolean isNewH2()
  {
    return HBCIDBService.SETTINGS.getString("h2.migration",null) != null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
   */
  public String getJdbcPassword()
  {
    String password = HBCIDBService.SETTINGS.getString("database.driver.h2.encryption.encryptedpassword",null);
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
        byte[] data = new byte[20];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(data);
        
        // Jetzt noch verschluesselt abspeichern
        Logger.info("encrypting password with system certificate");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Application.getSSLFactory().encrypt(new ByteArrayInputStream(data),bos);

        // Verschluesseltes Passwort als Base64 speichern
        HBCIDBService.SETTINGS.setAttribute("database.driver.h2.encryption.encryptedpassword",Base64.encode(bos.toByteArray()));
        
        // Entschluesseltes Passwort als Base64 zurueckliefern, damit keine Binaer-Daten drin sind.
        // Die Datenbank will es doppelt mit Leerzeichen getrennt haben.
        // Das erste ist fuer den User. Das zweite fuer die Verschluesselung.
        String encoded = Base64.encode(data);
        return encoded + " " + encoded;
      }

      Logger.debug("decrypting database password");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().decrypt(new ByteArrayInputStream(Base64.decode(password)),bos);
      
      String encoded = Base64.encode(bos.toByteArray());
      return encoded + " " + encoded;
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
    final String dbname = this.isNewH2() ? "hibiscus2" : "hibiscus";
    String url = "jdbc:h2:" + Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/h2db/" + dbname;

    if (HBCIDBService.SETTINGS.getBoolean("database.driver.h2.encryption",true))
    {
      final String cipher = HBCIDBService.SETTINGS.getString("database.driver.h2.encryption.algorithm","XTEA");
      Logger.info("database encryption: " + cipher);
      url += ";CIPHER=" + cipher;
      Logger.info("jdbc url: " + url);
    }
    if (HBCIDBService.SETTINGS.getBoolean("database.driver.h2.recover",false))
    {
      Logger.warn("#############################################################");
      Logger.warn("## DATABASE RECOVERY ACTIVATED                             ##");
      Logger.warn("#############################################################");
      url += ";RECOVER=1";
    }
    
    String addon = HBCIDBService.SETTINGS.getString("database.driver.h2.parameters",null);
    if (addon != null)
      url += ";" + addon;

    return url;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    return "hibiscus";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getScriptPrefix()
   */
  public String getScriptPrefix() throws RemoteException
  {
    return this.isNewH2() ? "h2new-" : "h2-";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    // Nicht noetig
    // return MessageFormat.format("DATEDIFF('MS','1970-01-01 00:00',{0})", new Object[]{content});
    return content;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    // brauchen wir bei nicht, da Embedded
  }
}
