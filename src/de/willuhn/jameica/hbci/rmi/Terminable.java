/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Terminable.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/19 16:49:32 $
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
import java.util.Date;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer clientseitig terminierte Transfers.
 */
public interface Terminable
{

	/**
	 * Liefert den Termin der Ueberweisung.
   * @return Termin der Ueberweisung.
   * @throws RemoteException
   */
  public Date getTermin() throws RemoteException;
	
	/**
	 * Prueft, ob die Ueberweisung ausgefuehrt wurde.
   * @return true, wenn die Ueberweisung bereits ausgefuehrt wurde.
   * @throws RemoteException
   */
  public boolean ausgefuehrt() throws RemoteException;
	
	/**
	 * Markiert die Ueberweisung als ausgefuehrt und speichert die Aenderung
   * unmittelbar.
   * @throws RemoteException
   */
  public void setAusgefuehrt() throws RemoteException, ApplicationException;

	/**
	 * Speichert den Termin, an dem die Ueberweisung ausgefuehrt werden soll.
   * @param termin Termin der Ueberweisung.
   * @throws RemoteException
   */
  public void setTermin(Date termin) throws RemoteException;

  /**
   * Prueft, ob die Ueberweisung ueberfaellig ist.
   * @return true, wenn sie ueberfaellig ist.
   * @throws RemoteException
   */
  public boolean ueberfaellig() throws RemoteException;
	
}


/**********************************************************************
 * $Log: Terminable.java,v $
 * Revision 1.1  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.1  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 **********************************************************************/