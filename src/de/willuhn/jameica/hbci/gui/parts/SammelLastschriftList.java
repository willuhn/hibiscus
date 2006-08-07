/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SammelLastschriftList.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/08/07 14:31:59 $
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
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sammel-Lastschriften.
 */
public class SammelLastschriftList extends AbstractSammelTransferList implements Part
{

  /**
   * @param action
   * @throws RemoteException
   */
  public SammelLastschriftList(Action action) throws RemoteException
  {
    super(init(), action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SammelLastschriftList());
  }

  // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
  /**
   * Initialisiert die Liste der Sammel-Lastschriften.
   * @return Initialisiert die Liste der Sammel-Lastschriften.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(SammelLastschrift.class);
    list.setOrder("ORDER BY TONUMBER(termin) DESC");
    return list;
  }
}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.6  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.5  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.4  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.3  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.2  2005/06/23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/