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
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit SEPA-Sammelueberweisungen.
 */
public class SepaSammelUeberweisungList extends AbstractSepaSammelTransferList implements Part
{

  /**
   * @param action
   * @throws RemoteException
   */
  public SepaSammelUeberweisungList(Action action) throws RemoteException
  {
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SepaSammelUeberweisungList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractSammelTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return SepaSammelUeberweisung.class;
  }
}
