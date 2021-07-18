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

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;

/**
 * Implementierung einer einzelnen Buchung einer SEPA-Sammelueberweisung.
 */
public class SepaSammelUeberweisungBuchungImpl extends AbstractSepaSammelTransferBuchungImpl<SepaSammelUeberweisung> implements SepaSammelUeberweisungBuchung
{
  /**
   * @throws java.rmi.RemoteException
   */
  public SepaSammelUeberweisungBuchungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sepasuebbuchung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("sepasueb_id".equals(arg0))
      return SepaSammelUeberweisung.class;

    return super.getForeignObject(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung#getSammelTransfer()
   */
  public SepaSammelUeberweisung getSammelTransfer() throws RemoteException
  {
    return (SepaSammelUeberweisung) getAttribute("sepasueb_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung#setSammelTransfer(de.willuhn.jameica.hbci.rmi.SepaSammelTransfer)
   */
  public void setSammelTransfer(SepaSammelUeberweisung s) throws RemoteException
  {
    setAttribute("sepasueb_id",s);
  }
}
