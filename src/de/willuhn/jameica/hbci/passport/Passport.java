/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passport/Passport.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/05 22:14:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passport;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Basis-Interface aller Passports.
 * Insofern ein Passport (Sicherheitsmedium) beim Start der Anwendung
 * gefunden werden soll, muss er dieses Interface implementieren.<br>
 * Als Referenz kann hierbei der bereits existierende Passport fuer
 * die Unterstuetzung von DDV-Chipkarten vorhandene
 * <i>de.willuhn.jameica.passports.ddv.server.PassportImpl</i> dienen.
 */
public interface Passport extends Remote {

	/**
	 * Liefert den sprechenden Namen des Passports.
	 * Dieser Name wird dem Benutzer zum Beispiel in Combo-Boxen angezeigt.
   * @return Name des Passports.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
	
	/**
	 * Liefert das Passport-Handle.
	 * Das ist im Prinzip die direkte Schnittstelle zu HBCI4Java.
   * @return Handle.
   * @throws RemoteException
   */
  public PassportHandle getHandle() throws RemoteException;

	/**
	 * Liefert die Klasse des Konfigurationsdialogs.
	 * Oeffnet der Anwender den Konfigurations-Dialog durch
	 * Doppelklick auf die Liste der vorhandenen Passports,
	 * dann wird eine Instanz dieser Klasse erzeugt und
	 * in der GUI angezeigt. Als Referenz kann hierzu
	 * <i>de.willuhn.jameica.passports.ddv.View</i> dienen.
   * @return Die Klasse des Konfig-Dialogs.
   * Muss von <code>AbstractView</code> abgeleitet sein.
   * @throws RemoteException
   */
  public Class getConfigDialog() throws RemoteException;

}


/**********************************************************************
 * $Log: Passport.java,v $
 * Revision 1.1  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/05/04 23:08:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.7  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.6  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.5  2004/02/25 23:11:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/