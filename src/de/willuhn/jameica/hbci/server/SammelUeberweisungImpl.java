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
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Containers fuer Sammellastschrift-Buchungen.
 */
public class SammelUeberweisungImpl extends AbstractSammelTransferImpl
  implements SammelUeberweisung
{

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SammelUeberweisungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "sueberweisung";
  }

  @Override
  public DBIterator getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SammelUeberweisungBuchung.class);
    list.addFilter("sueberweisung_id = " + this.getID());
    list.setOrder("order by gegenkonto_name,id");
    return list;
  }

  @Override
  public SammelTransferBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SammelUeberweisungBuchung b = (SammelUeberweisungBuchung) this.getService().createObject(SammelUeberweisungBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }
}
