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

import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;

/**
 * Implementierung einer einzelnen Buchung einer Sammel-Ueberweisung.
 * @author willuhn
 */
public class SammelUeberweisungBuchungImpl extends AbstractSammelTransferBuchungImpl implements SammelUeberweisungBuchung
{

  /**
   * @throws java.rmi.RemoteException
   */
  public SammelUeberweisungBuchungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "sueberweisungbuchung";
  }

  @Override
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("sueberweisung_id".equals(arg0))
      return SammelUeberweisung.class;

    return super.getForeignObject(arg0);
  }

  @Override
  public SammelTransfer getSammelTransfer() throws RemoteException
  {
    return (SammelUeberweisung) getAttribute("sueberweisung_id");
  }

  @Override
  public void setSammelTransfer(SammelTransfer s) throws RemoteException
  {
    setAttribute("sueberweisung_id",s);
  }
}
