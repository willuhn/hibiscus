/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SammelUeberweisungList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sammel-Ueberweisungen.
 */
public class SammelUeberweisungList extends AbstractSammelTransferList implements Part
{

  /**
   * @param action
   * @throws RemoteException
   */
  public SammelUeberweisungList(Action action) throws RemoteException
  {
    super(init(), action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SammelUeberweisungList());
  }

  /**
   * Initialisiert die Liste der Sammel-Ueberweisungen.
   * @return Initialisiert die Liste der Sammel-Ueberweisungen.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(SammelUeberweisung.class);
    list.setOrder("ORDER BY TONUMBER(termin) DESC");
    return list;
  }

}


/**********************************************************************
 * $Log: SammelUeberweisungList.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/