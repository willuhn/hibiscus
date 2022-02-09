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
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit SEPA-Sammel-Lastschriften.
 */
public class SepaSammelLastschriftList extends AbstractSepaSammelTransferList implements Part
{

  /**
   * @param action
   * @throws RemoteException
   */
  public SepaSammelLastschriftList(Action action) throws RemoteException
  {
    super(action);
    addColumn(i18n.tr("Zieltermin"),"targetdate", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("Art"),"sepatype");
    addColumn(i18n.tr("Sequenz"),"sequencetype");
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SepaSammelLastschriftList());
  }

  @Override
  protected Class getObjectType()
  {
    return SepaSammelLastschrift.class;
  }
}
