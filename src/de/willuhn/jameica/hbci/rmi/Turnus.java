/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Turnus.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/11 16:14:29 $
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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet einen Turnus bei wiederkehrenden Zahlungen ab.
 */
public interface Turnus extends DBObject
{

	/**
	 * Intervall fuer monatliche Zahlung.
	 */
	public final static int INTERVALL_MONATLICH   			=  1;
	
	/**
	 * Intervall fuer Zahlung alle 2 Monate.
	 */
	public final static int INTERVALL_2MONATLICH  			=  2;

	/**
	 * Intervall fuer vierteljaehrliche Zahlung.
	 */
	public final static int INTERVALL_VIERTELJAEHRLICH  =  4;

	/**
	 * Intervall fuer jaehrliche Zahlung.
	 */
	public final static int INTERVALL_JAEHRLICH         = 12;	

	/**
	 * Liefert das Intervall der Zahlung.
	 * Siehe auch die Konstanten INTERVALL_*.
   * @return Intervall der Zahlung.
   * @throws RemoteException
   */
  public int getIntervall() throws RemoteException;
	
	/**
	 * Liefert die sprechende Bezeichnung des Intervalls.
   * @return Bezeichnung des Intervalls.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

}


/**********************************************************************
 * $Log: Turnus.java,v $
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/