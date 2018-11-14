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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.util.Base64;

/**
 * Abstrakte Basisklasse fuer den Datenbank-Support.
 */
public abstract class AbstractDBSupportImpl implements DBSupport
{
  private final static String PREFIX_ENC = "encrypted:";

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#execute(java.sql.Connection, java.io.File)
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException
  {
    if (sqlScript == null)
      return;

    // Wir schreiben unseren Prefix davor.
    sqlScript = new File(sqlScript.getParent(),getScriptPrefix() + sqlScript.getName());
    if (!sqlScript.exists())
    {
      Logger.debug("file " + sqlScript + " does not exist, skipping");
      return;
    }
    
    if (!sqlScript.canRead() || !sqlScript.exists())
      return;

    Logger.info("executing sql script: " + sqlScript.getAbsolutePath());
    
    Reader reader = null;

    try
    {
      reader  = new InputStreamReader(new BufferedInputStream(new FileInputStream(sqlScript)),"iso-8859-1");
      ScriptExecutor.execute(reader,conn);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("error while executing sql script " + sqlScript,e);
    }
    finally
    {
      try
      {
        if (reader != null)
          reader.close();
      }
      catch (Exception e3)
      {
        Logger.error("error while closing file " + sqlScript,e3);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getTransactionIsolationLevel()
   */
  public int getTransactionIsolationLevel() throws RemoteException
  {
    return -1;
  }
  
  private long lastCheck = 0;

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    long newCheck = System.currentTimeMillis();
    if ((newCheck - lastCheck) < (10 * 1000L))
      return; // Wir checken hoechstens aller 10 Sekunden
    
    Statement s  = null;
    ResultSet rs = null;
    try
    {
      s = conn.createStatement();
      rs = s.executeQuery("select 1");
      lastCheck = newCheck;
    }
    catch (SQLException e)
    {
      // das Ding liefert in getMessage() den kompletten Stacktrace mit, den brauchen wir
      // nicht (das muellt uns nur das Log voll) Also fangen wir sie und werfen eine neue
      // saubere mit kurzem Fehlertext
      String msg = e.getMessage();
      if (msg != null && msg.indexOf("\n") != -1)
        msg = msg.substring(0,msg.indexOf("\n"));
      throw new RemoteException(msg);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
        if (s != null)  s.close();
      }
      catch (Exception e)
      {
        throw new RemoteException("unable to close statement/resultset",e);
      }
    }
  }
  
  /**
   * Liefert einen verschluesselten Parameter aus der Config-Datei.
   * Wenn der Parameter noch nicht verschluesselt in der Konfigurationsdatei
   * liegt, wird er automatisch verschluesselt. Das wird daran festgemacht,
   * wenn der Wert des Parameters mit "encrypted:" beginnt
   * @param key der Name des Parameters.
   * @return der entschluesselte Parameter.
   */
  String getEncrypted(String key)
  {
    String value = HBCIDBService.SETTINGS.getString(key,null);
    
    // Parameter nicht definiert, dann muessen wir auch nichts machen
    if (value == null)
      return value;
    
    // Wir machen das Verhalten sicherheitshalber konfigurierbar. Fuer den
    // Fall, dass es doch User gibt, die das nicht wollen.
    // Falls der Wert bereits verschluesselt war, speichern wir ihn wieder
    // unverschluesselt
    boolean encrypted = value.startsWith(PREFIX_ENC);
    boolean doEncrypt = HBCIDBService.SETTINGS.getBoolean("encrypt",false);

    try
    {
      if (encrypted)
      {
        Logger.debug("decrypting value for key " + key);
        // Prefix abschneiden
        value = value.substring(PREFIX_ENC.length());
        // Wert ist schon verschluesselt - wir entschluesseln ihn und liefern ihn zurueck
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Application.getSSLFactory().decrypt(new ByteArrayInputStream(Base64.decode(value)),bos);
        String result = bos.toString("UTF-8");
        
        // Wert wieder unverschluesselt speichern, wenn der User keine Verschluesselung (mehr) moechte
        if (!doEncrypt)
        {
          Logger.info("undo encryption for key " + key);
          HBCIDBService.SETTINGS.setAttribute(key,result);
        }
        
        return result;
      }
      
      
      // Wert ist noch nicht verschluesselt. Dann verschluesseln wir ihn bei der Gelegenheit
      // Aber nur, wenn der User das will
      if (!doEncrypt)
        return value;
      
      Logger.info("encrypting value for key " + key);
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().encrypt(new ByteArrayInputStream(value.getBytes("UTF-8")),bos);
      HBCIDBService.SETTINGS.setAttribute(key,PREFIX_ENC + Base64.encode(bos.toByteArray()));
      
      return value;
    }
    catch (Exception e)
    {
      throw new RuntimeException("error while determining encrypted parameter",e);
    }
  }


}
