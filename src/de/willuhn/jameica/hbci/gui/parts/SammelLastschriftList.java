/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SammelLastschriftList.java,v $
 * $Revision: 1.8 $
 * $Date: 2008/06/30 13:04:10 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
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
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SammelLastschriftList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractSammelTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return SammelLastschrift.class;
  }
}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.8  2008/06/30 13:04:10  willuhn
 * @N Von-Bis-Filter auch in Sammel-Auftraegen
 *
 * Revision 1.7  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
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