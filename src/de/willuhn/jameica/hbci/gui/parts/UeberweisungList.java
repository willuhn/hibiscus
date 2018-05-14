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
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Ueberweisungen.
 */
public class UeberweisungList extends AbstractTransferList
{

  /**
   * @param action
   * @throws RemoteException
   */
  public UeberweisungList(Action action) throws RemoteException
  {
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UeberweisungList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getList(java.lang.Object, java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    DBIterator list = super.getList(konto, from, to, text);
    if (text != null && text.length() > 0)
    {
      String s = "%" + text.toLowerCase() + "%";
      list.addFilter("(LOWER(empfaenger_konto) like ? or LOWER(empfaenger_name) like ? or LOWER(zweck) like ? or LOWER(zweck2) like ? or LOWER(zweck3) like ?)", new Object[]{s,s,s,s,s});
    }
    
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return Ueberweisung.class;
  }
}
