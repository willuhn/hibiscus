/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Ueberweisung.java,v $
 * $Revision: 1.18 $
 * $Date: 2009/05/12 22:53:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;


/**
 * Bildet eine Ueberweisung ab.
 */
public interface Ueberweisung extends BaseUeberweisung
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

}


/**********************************************************************
 * $Log: Ueberweisung.java,v $
 * Revision 1.18  2009/05/12 22:53:33  willuhn
 * @N BUGZILLA 189 - Ueberweisung als Umbuchung
 *
 * Revision 1.17  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.16  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 **********************************************************************/