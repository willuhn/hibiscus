/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Ueberweisung.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/10/19 23:33:31 $
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

import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Bildet eine Ueberweisung ab.
 */
public interface Ueberweisung extends Transfer
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
	 * Speichert den Termin, an dem die Ueberweisung ausgefuehrt werden soll.
   * @param termin Termin der Ueberweisung.
   * @throws RemoteException
   */
  public void setTermin(Date termin) throws RemoteException;

  /**
	 * Fuehrt die Ueberweisung aus zum definierten Termin aus.
	 * Ist dieser Termin nicht definiert, wird sie sofort ausgefuehrt.
	 * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public void execute() throws RemoteException, ApplicationException, OperationCanceledException;
  
  /**
   * Dupliziert die Ueberweisung.
   * @return eine neue Ueberweisung mit den Eigenschaften dieser.
   * @throws RemoteException
   */
  public Ueberweisung duplicate() throws RemoteException;
  
  /**
   * Prueft, ob die Ueberweisung ueberfaellig ist.
   * @return true, wenn sie ueberfaellig ist.
   * @throws RemoteException
   */
  public boolean ueberfaellig() throws RemoteException;
	
}


/**********************************************************************
 * $Log: Ueberweisung.java,v $
 * Revision 1.8  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 * Revision 1.6  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.5  2004/04/05 23:28:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.3  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/