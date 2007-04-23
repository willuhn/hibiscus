/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/DBSupportMcKoiImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/04/23 18:07:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.HashMap;

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.sql.CheckSum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des Datenbank-Supports fuer McKoi.
 */
public class DBSupportMcKoiImpl extends AbstractDBSupportImpl
{
  // Mapper von Datenbank-Hash zu Versionsnummer
  private static HashMap DBMAPPING = new HashMap();

  static
  {
    DBMAPPING.put("KvynDJyxe6D1XUvSCkNAFA==",new Double(1.0));
    DBMAPPING.put("Oj3JSimz84VKq44EEzQOZQ==",new Double(1.1));
    DBMAPPING.put("NhTl6Nt8RmaRNz49M/SGiA==",new Double(1.2));
    DBMAPPING.put("kwi5vy1fvgOOVtoTYJYjuA==",new Double(1.3));
    DBMAPPING.put("JtkHZYFRtWpxGR6nE8TYFw==",new Double(1.4));
    DBMAPPING.put("a4VHFRr69c+LynZiczIICg==",new Double(1.5));
    DBMAPPING.put("a4VHFRr69c+LynZiczIICg==",new Double(1.6));
    DBMAPPING.put("EdG6qLQ0SXRgJ8QBtz5Vrg==", new Double(1.7));
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
   */
  public String getJdbcDriver()
  {
    return "com.mckoi.JDBCDriver";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
   */
  public String getJdbcPassword()
  {
    return "hibiscus";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
   */
  public String getJdbcUrl()
  {
    return ":jdbc:mckoi:local://" +
           Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() +
           "/db/db.conf";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    return "hibiscus";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#checkConsistency(java.sql.Connection)
   */
  public void checkConsistency(Connection conn) throws RemoteException, ApplicationException
  {
    if (!Settings.getCheckDatabase())
      return;

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
      ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
      monitor.setStatusText(i18n.tr("Prüfe Datenbank-Integrität"));

      String checkSum = CheckSum.md5(conn,null,"APP");
      if (DBMAPPING.get(checkSum) == null)
      {
        throw new ApplicationException(i18n.tr("Datenbank-Checksumme ungültig: {0}. Datenbank-Version nicht kompatibel zur Hibiscus-Version?",checkSum));
      }
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
   * Ueberschrieben, um die Datenbank anzulegen.
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#install()
   */
  public void install() throws RemoteException
  {
    try
    {
      EmbeddedDatabase db = new EmbeddedDatabase(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/db",
                                                 getJdbcUsername(),
                                                 getJdbcPassword());
      if (!db.exists())
      {
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
        monitor.setStatusText(i18n.tr("Erstelle Hibiscus-Datenbank"));
        db.create();
      }
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to create embedded database",e);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return MessageFormat.format("tonumber({0})", new Object[]{content});
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException
  {
    return true;
  }
}


/*********************************************************************
 * $Log: DBSupportMcKoiImpl.java,v $
 * Revision 1.4  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.3  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.2  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.1  2007/04/18 17:03:06  willuhn
 * @N Erster Code fuer Unterstuetzung von MySQL
 *
 **********************************************************************/