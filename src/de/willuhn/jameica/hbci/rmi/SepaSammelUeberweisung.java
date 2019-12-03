/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Interface fuer SEPA-Sammelueberweisungen.
 */
public interface SepaSammelUeberweisung extends SepaSammelTransfer<SepaSammelUeberweisungBuchung>
{
  /**
   * Prueft, ob es sich um einen bankseitige Termin-Ueberweisung handelt.
   * @return true, wenn es eine bankseitige Termin-Ueberweisung ist.
   * @throws RemoteException
   */
  public boolean isTerminUeberweisung() throws RemoteException;
  
  /**
   * Legt fest, ob es sich um eine bankseitige Termin-Ueberweisung handelt.
   * @param termin true, wenn es eine bankseitige Terminueberweisung sein soll.
   * @throws RemoteException
   */
  public void setTerminUeberweisung(boolean termin) throws RemoteException;

}
