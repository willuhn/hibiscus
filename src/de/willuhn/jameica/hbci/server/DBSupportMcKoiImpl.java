/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/DBSupportMcKoiImpl.java,v $
 * $Revision: 1.10 $
 * $Date: 2008/12/30 15:21:40 $
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

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des Datenbank-Supports fuer McKoi.
 */
public class DBSupportMcKoiImpl extends AbstractDBSupportImpl
{
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

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    // brauchen wir bei McKoi nicht
  }
}


/*********************************************************************
 * $Log: DBSupportMcKoiImpl.java,v $
 * Revision 1.10  2008/12/30 15:21:40  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.9  2007/12/06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 * Revision 1.8  2007/07/18 09:45:18  willuhn
 * @B Neue Version 1.8 in DB-Checks nachgezogen
 *
 * Revision 1.7  2007/05/30 09:41:51  willuhn
 * @N SQL-Update fuer Nightly-Build nun generisch
 *
 * Revision 1.6  2007/05/30 09:34:55  willuhn
 * @B Seit Support fuer MySQL wurde die DB-Checksummen-Pruefung sowie das automatische SQL-Update bei Nightly-Builds vergessen
 *
 * Revision 1.5  2007/05/07 09:27:25  willuhn
 * @N Automatisches Neuerstellen der JDBC-Connection bei MySQL
 *
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