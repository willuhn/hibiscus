/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Statische Hilfsklasse zum Auslesen von Versionsnummern.
 */
public class VersionUtil
{
  /**
   * Liefert die Version zum angegebenen Namen.
   * Wenn das Versions-Objekt noch nicht existiert, wird es automatisch erstellt.
   * @param service der Datenbank-Service.
   * @param name Name der Version.
   * @return die Version.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Version getVersion(HBCIDBService service, String name) throws RemoteException, ApplicationException
  {
    if (name == null || name.length() == 0)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Keine Versionsbezeichnung angegeben"));
    }
    DBIterator list = service.createList(Version.class);
    list.addFilter("name = ?",name);
    if (list.hasNext())
      return (Version) list.next();
    
    // Neue Version erstellen
    Version v = (Version) service.createObject(Version.class,null);
    v.setName(name);
    v.store();
    return v;
  }

  /**
   * Loescht alle Versionen, deren Namen mit dem angegebenen Prefix beginnt.
   * @param service der Datenbank-Service.
   * @param prefix der prefix.
   * @return die Anzahl der geloeschten Datensaetze.
   * @throws RemoteException
   */
  public static int deleteAll(HBCIDBService service, String prefix) throws RemoteException
  {
    if (prefix == null || prefix.length() == 0)
      throw new RemoteException("no prefix given");

    if (prefix.indexOf("%") != -1 || prefix.indexOf("_") != -1)
      throw new RemoteException("no wildcards allowed in prefix");
    
    return service.executeUpdate("delete from version where name like ?",prefix + ".%");
  }


  /**
   * Loescht die Version.
   * @param service der Datenbank-Service.
   * @param name der Name der Version.
   * @return die Anzahl der geloeschten Datensaetze.
   * @throws RemoteException
   */
  public static int delete(HBCIDBService service, String name) throws RemoteException
  {
    if (name == null || name.length() == 0)
      throw new RemoteException("no name given");

    int i = service.executeUpdate("delete from version where name = ?",name);
    return i;
  }
}
