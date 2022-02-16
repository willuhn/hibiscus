/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Containers fuer Sammellastschrift-Buchungen.
 */
public class SammelLastschriftImpl extends AbstractSammelTransferImpl
  implements SammelLastschrift
{

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SammelLastschriftImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "slastschrift";
  }

  @Override
  public DBIterator getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SammelLastBuchung.class);
    list.addFilter("slastschrift_id = " + this.getID());
    list.setOrder("order by gegenkonto_name,id");
    return list;
  }

  @Override
  public SammelTransferBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SammelLastBuchung b = (SammelLastBuchung) this.getService().createObject(SammelLastBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }

}
