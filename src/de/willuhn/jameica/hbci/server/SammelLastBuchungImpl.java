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

import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;

/**
 * Implementierung einer einzelnen Buchung einer Sammellastschrift.
 * @author willuhn
 */
public class SammelLastBuchungImpl extends AbstractSammelTransferBuchungImpl implements SammelLastBuchung
{

  /**
   * @throws java.rmi.RemoteException
   */
  public SammelLastBuchungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "slastbuchung";
  }

  @Override
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("slastschrift_id".equals(arg0))
      return SammelLastschrift.class;

    return super.getForeignObject(arg0);
  }

  @Override
  public SammelTransfer getSammelTransfer() throws RemoteException
  {
    return (SammelLastschrift) getAttribute("slastschrift_id");
  }

  @Override
  public void setSammelTransfer(SammelTransfer s) throws RemoteException
  {
    setAttribute("slastschrift_id",s);
  }
}
