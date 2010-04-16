/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypUtil.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/04/16 12:46:40 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse fuer die Umsatzkategorien.
 */
public class UmsatzTypUtil
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Liefert einen sprechenden Namen fuer den Kategorie-Typ.
   * @param type Typ
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @see UmsatzTyp#TYP_EGAL
   * @return sprechender Name.
   */
  public static String getNameForType(int type)
  {
    
    switch (type)
    {
      case UmsatzTyp.TYP_AUSGABE:
        return i18n.tr("Ausgabe");
      case UmsatzTyp.TYP_EINNAHME:
        return i18n.tr("Einnahme");
    }
    return i18n.tr("egal");
  }
  
  /**
   * Liefert eine Liste aller Umsatz-Kategorien, sortiert nach Nummer und Name.
   * @return Liste aller Umsatz-Kategorien.
   * @throws RemoteException
   */
  public static DBIterator getAll() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(UmsatzTyp.class);
    list.setOrder("ORDER BY nummer,name");
    return list;
  }

  /**
   * Liefert eine Liste der Umsatz-Kategorien oberster Ebene.
   * @return Liste der Umsatz-Kategorien oberster Ebene.
   * @throws RemoteException
   */
  public static DBIterator getRootElements() throws RemoteException
  {
    DBIterator list = getAll();
    // die mit ungueltiger Parent-ID sind quasi Leichen - steht nur sicherheitshalber
    // mit hier drin. Eigentlich sollte die DB sowas via Constraint verhindern
    list.addFilter("parent_id is null or parent_id not in (select id from umsatztyp)");
    return list;
  }

}


/*********************************************************************
 * $Log: UmsatzTypUtil.java,v $
 * Revision 1.5  2010/04/16 12:46:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2010/04/16 12:46:03  willuhn
 * @B Parent-ID beim Import von Kategorien beruecksichtigen und neu mappen - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=66546#66546
 *
 * Revision 1.3  2010/03/05 23:59:31  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.2  2010/03/05 23:29:18  willuhn
 * @N Statische Basis-Funktion zum Laden der Kategorien in der richtigen Reihenfolge
 *
 * Revision 1.1  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 **********************************************************************/