/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/PassportDDV.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/12 00:38:40 $
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
 * Passport fuer das Sicherheitsmedium "Chipkarte" (DDV).
 */
public interface PassportDDV extends Passport {

	// Parameter fuer den Port (meist 0)
	public final static String PORT 		= "client.passport.DDV.port";
	public final static String[] PORTS = new String[] {"COM1","COM2","COM3","COM4","USB"};

	// Parameter fuer den Index (normalerweise 0)
	public final static String CTNUMBER = "client.passport.DDV.ctnumber";

	// Parameter ober Biometrie verwendet wird (meist 0)
	public final static String USEBIO	  = "client.passport.DDV.usebio";

	// Parameter ob die Tastatur zur Pin-Eingabe verwendet werden soll
	public final static String SOFTPIN  = "client.passport.DDV.softpin";

	// Parameter fuer den Index des HBCI-Zugangs (meist 1)
	public final static String ENTRYIDX = "client.passport.DDV.entryidx";

	/**
	 * Liefert den Port des Kartenlesers.
	 * "0" steht hierbei z.Bsp. fuer COM1, "1" fuer COM2 usw.
   * @return Portnummer.
   * @throws RemoteException
   */
  public int getPort() throws RemoteException;

	/**
	 * Speichert den zu verwendenden Port.
	 * "0" steht hierbei z.Bsp. fuer COM1, "1" fuer COM2 usw.
   * @param port Portnummer.
   * @throws RemoteException
   */
  public void setPort(int port) throws RemoteException;

	/**
	 * Liefert die Index-Nummer des Readers.
   * @return Index-Nummer des Readers.
   * @throws RemoteException
   */
  public int getCTNumber() throws RemoteException;

	/**
	 * Speichert die Index-Nummer des Readers.
   * @param ctNumber Index-Nummer des Readers.
   * @throws RemoteException
   */
  public void setCTNumber(int ctNumber) throws RemoteException;

	/**
	 * Prueft, ob der Reader biometrische Authentifizierung unterstuetzt.
   * @return true, wenn er es unterstuetzt, sonst false.
   * @throws RemoteException
   */
  public boolean useBIO() throws RemoteException;

	/**
	 * Legt fest, ob der Reader biometrische Authentifizierung verwenden soll.
   * @param bio true, wenn er es verwenden soll, sonst false.
   * @throws RemoteException
   */
  public void setBIO(boolean bio) throws RemoteException;

	/**
	 * Prueft, ob die Tastatur zur Eingabe des PINs verwendet werden soll.
   * @return true, wenn die Tastatur des PCs verwendet werden soll.
   * @throws RemoteException
   */
  public boolean useSoftPin() throws RemoteException;

	/**
	 * Legt fest, ob die Tastatur des PCs zur Eingabe der PIN verwendet werden soll.
   * @param softPin true, wenn die Tastatur des PCs verwendet werden soll.
   * @throws RemoteException
   */
  public void setSoftPin(boolean softPin) throws RemoteException;

	/**
	 * Liefert den Index des HBCI-Zugangs.
   * @return Index des HBCI-Zugangs.
   * @throws RemoteException
   */
  public int getEntryIndex() throws RemoteException;

	/**
	 * Speichert den Index des HBCI-Zugangs.
   * @param index Index des HBCI-Zugangs.
   * @throws RemoteException
   */
  public void setEntryIndex(int index) throws RemoteException;

}


/**********************************************************************
 * $Log: PassportDDV.java,v $
 * Revision 1.1  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 **********************************************************************/