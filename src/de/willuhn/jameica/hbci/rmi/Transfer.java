/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Transfer.java,v $
 * $Revision: 1.8 $
 * $Date: 2005/03/02 17:59:30 $
 * $Author: web0 $
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
 * Basis-Interface fuer Geld-Transfers zwischen Konten.
 */
public interface Transfer extends DBObject
{

	/**
	 * Liefert das Konto, ueber das bezahlt wurde.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;
	
	/**
	 * Liefert die Kontonummer des Gegenkontos.
   * @return Kontonummer des Empfaengers.
   * @throws RemoteException
   */
  public String getGegenkontoNummer() throws RemoteException;

	/**
	 * Liefert die BLZ des Gegenkontos.
	 * @return BLZ des Gegenkontos.
	 * @throws RemoteException
	 */
	public String getGegenkontoBLZ() throws RemoteException;
	
	/**
	 * Liefert den Namen des Kontoinhabers des Gegenkontos.
	 * @return Name des Kontoinhabers des Gegenkontos.
	 * @throws RemoteException
	 */
	public String getGegenkontoName() throws RemoteException;

	/**
	 * Liefert den Betrag.
   * @return Betrag.
   * @throws RemoteException
   */
  public double getBetrag() throws RemoteException;
	
	/**
	 * Liefert die Zeile 1 des Verwendungszwecks.
   * @return Zeile 1 des Verwendungszwecks.
   * @throws RemoteException
   */
  public String getZweck() throws RemoteException;
	
	/**
	 * Liefert die Zeile 2 des Verwendungszwecks.
	 * @return Zeile 2 des Verwendungszwecks.
	 * @throws RemoteException
	 */
	public String getZweck2() throws RemoteException;
	
	/**
	 * Speichert das Konto, das zur Bezahlung verwendet werden soll.
   * @param konto Konto, das verwendet werden soll.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;
	
	/**
	 * Speichert die Kontonummer des Gegenkontos.
   * @param konto Kontonummer des Gegenkontos.
   * @throws RemoteException
   */
  public void setGegenkontoNummer(String konto) throws RemoteException;
	
	/**
	 * Speichert die BLZ des Gegenkontos.
	 * @param blz BLZ des Gegenkontos.
	 * @throws RemoteException
	 */
	public void setGegenkontoBLZ(String blz) throws RemoteException;

	/**
	 * Speichert den Namen des Kontoinhabers des Gegenkontos.
	 * @param name Name des Kontoinhabers des Gegenkontos.
	 * @throws RemoteException
	 */
	public void setGegenkontoName(String name) throws RemoteException;

	/**
	 * Setzt alle drei oben genannten Gegenkonto-Eigenschaften auf einmal.
   * @param e
   * @throws RemoteException
   */
  public void setGegenkonto(Adresse e) throws RemoteException;

	/**
	 * Speichert den zu ueberweisenden Betrag.
   * @param betrag Betrag.
   * @throws RemoteException
   */
  public void setBetrag(double betrag) throws RemoteException;
	
	/**
	 * Speichert den Zweck der Ueberweisung.
   * @param zweck Zweck der Ueberweisung.
   * @throws RemoteException
   */
  public void setZweck(String zweck) throws RemoteException;
	
	/**
	 * Speichert Zeile 2 des Verwendungszwecks.
   * @param zweck2 Zeile 2 des Verwendungszwecks.
   * @throws RemoteException
   */
  public void setZweck2(String zweck2) throws RemoteException;
}


/**********************************************************************
 * $Log: Transfer.java,v $
 * Revision 1.8  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.7  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.6  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.5  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.4  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.3  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.2  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/