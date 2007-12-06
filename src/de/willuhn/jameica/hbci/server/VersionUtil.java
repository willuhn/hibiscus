/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/VersionUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/06 17:57:21 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse zum Laden und Erzeugen von Versionen.
 */
public class VersionUtil
{
  /**
   * Liefert ein Versionsobjekt fuer die genannte Version.
   * Bei Bedarf wird sie automatisch erstellt.
   * @param name Name der Version.
   * @return das Versionsobjekt.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Version getVersion(String name) throws RemoteException, ApplicationException
  {
    if (name == null || name.length() == 0)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Keine Versionsbezeichnung angegeben"));
    }
    DBService service = Settings.getDBService();
    DBIterator list = service.createList(Version.class);
    list.addFilter("name = ?", new String[]{name});
    if (list.hasNext())
      return (Version) list.next();
    
    // Neue Version erstellen
    Version v = (Version) service.createObject(Version.class,null);
    v.setName(name);
    v.store();
    return v;
  }
}


/*********************************************************************
 * $Log: VersionUtil.java,v $
 * Revision 1.1  2007/12/06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 **********************************************************************/