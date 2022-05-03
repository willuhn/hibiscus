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
 * Bildet eine Auslands-Ueberweisung ab.
 */
public interface AuslandsUeberweisung extends BaseUeberweisung, Duplicatable, SepaPayment, SepaBooking
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
  
  /**
   * Prueft, ob es sich um einen bankinterne Umbuchung handelt.
   * @return true, wenn es eine bankinterne Umbuchung ist.
   * @throws RemoteException
   */
  public boolean isUmbuchung() throws RemoteException;
  
  /**
   * Legt fest, ob es sich um eine bankinterne Umbuchung handelt.
   * @param b true, wenn es eine bankinterne Umbuchung sein soll.
   * @throws RemoteException
   */
  public void setUmbuchung(boolean b) throws RemoteException;
  
  /**
   * Prueft, ob es eine Echtzeitueberweisung ist.
   * @return true, wenn es eine Echtzeitueberweisung ist.
   * @throws RemoteException
   */
  public boolean isInstantPayment() throws RemoteException;
  
  /**
   * Legt fest, ob es eine Echtzeitueberweisung ist.
   * @param b true, wenn es eine Echtzeitueberweisung ist.
   * @throws RemoteException
   */
  public void setInstantPayment(boolean b) throws RemoteException;
  
}
