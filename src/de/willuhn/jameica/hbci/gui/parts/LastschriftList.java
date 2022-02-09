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
import de.willuhn.jameica.hbci.rmi.Lastschrift;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Lastschriften.
 */
public class LastschriftList extends AbstractTransferList
{

  /**
   * @param action
   * @throws RemoteException
   */
  public LastschriftList(Action action) throws RemoteException
  {
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.LastschriftList());
  }

  @Override
  protected Class getObjectType()
  {
    return Lastschrift.class;
  }

  @Override
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    DBIterator list = super.getList(konto, from, to, text);
    if (text != null && text.length() > 0)
    {
      String s = "%" + text.toLowerCase() + "%";
      list.addFilter("(LOWER(empfaenger_konto) like ? or LOWER(empfaenger_name) like ? or LOWER(zweck) like ? or LOWER(zweck2) like ? or LOWER(zweck3) like ?)", s, s, s, s, s);
    }

    return list;
  }
}
