/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
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
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SammelUeberweisungList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractSammelTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return SammelUeberweisung.class;
  }
}

/**********************************************************************
 * $Log: SammelUeberweisungList.java,v $
 * Revision 1.3  2008/06/30 13:04:10  willuhn
 * @N Von-Bis-Filter auch in Sammel-Auftraegen
 *
 * Revision 1.2  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/