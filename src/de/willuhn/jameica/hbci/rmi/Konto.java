/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Konto.java,v $
 * $Revision: 1.14 $
 * $Date: 2004/07/09 00:04:40 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.passport.*;
import de.willuhn.util.ApplicationException;

/**
 * Bildet eine Bankverbindung in HBCI ab.
 */
public interface Konto extends DBObject {

	/**
	 * Liefert die Kontonummer fuer diese Bankverbindung.
   * @return Kontonummer.
   * @throws RemoteException
   */
  public String getKontonummer() throws RemoteException;
	
	/**
	 * Liefert die Bankleitzahl fuer diese Bankverbindung.
   * @return Bankleitzahl.
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;
	
	/**
	 * Liefert den Namen des Konto-Inhabers.
   * @return Name des Konto-Inhabers.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
	
	/**
	 * Liefert die Bezeichnung des Kontos.
   * @return Bezeichnung des Kontos.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

	/**
	 * Liefert die Kundennummer bei der Bank.
   * @return Kundennummer.
   * @throws RemoteException
   */
  public String getKundennummer() throws RemoteException;

	/**
	 * Liefert den Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public Passport getPassport() throws RemoteException;
	
  /**
   * Liefert die Waehrungs-Bezeichnung der Bankverbindung.
   * @return Waehrungsbezeichnung.
   * @throws RemoteException
   */
  public String getWaehrung() throws RemoteException;

	/**
	 * Speichert die Kontonummer der Bankverbindung.
   * @param kontonummer Kontonummer.
   * @throws RemoteException
   */
  public void setKontonummer(String kontonummer) throws RemoteException;
	
	/**
	 * Speichert die Bankleitzahl der Bankverbindung.
   * @param blz Bankleitzahl.
   * @throws RemoteException
   */
  public void setBLZ(String blz) throws RemoteException;
	
	/**
	 * Speichert den Namen des Konto-Inhabers.
   * @param name Name des Konto-Inhaber.s
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;

	/**
	 * Speichert die Bezeichnung des Kontos.
   * @param bezeichnung Bezeichnung.
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;

  /**
   * Speichert die Waehrungsbezeichnung.
   * @param waehrung Bezeichnung.
   * @throws RemoteException
   */
  public void setWaehrung(String waehrung) throws RemoteException;

	/**
	 * Speichert den zu verwendenden Passport.
   * @param passport Passport.
   * @throws RemoteException
   */
  public void setPassport(Passport passport) throws RemoteException;

	/**
	 * Speichert die Kundennummer.
   * @param kundennummer Kundennummer.
   * @throws RemoteException
   */
  public void setKundennummer(String kundennummer) throws RemoteException;

	/**
	 * Liefert den Saldo des Kontos oder <code>0.0</code> wenn er noch nie
	 * abgefragt wurde.
   * @return Saldo des Kontos.
   * @throws RemoteException
   */
  public double getSaldo() throws RemoteException;

	/**
	 * Liefert das Datum des aktuellen Saldos oder <code>null</code> wenn er
	 * noch nie abgefragt wurde.
   * @return Datum des Saldos.
   * @throws RemoteException
   */
  public Date getSaldoDatum() throws RemoteException;

	/**
	 * Aktualisiert den Saldo online.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public void refreshSaldo() throws ApplicationException,RemoteException;

	/**
	 * Aktualisiert die Umsaetze des Kontos online.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public void refreshUmsaetze() throws ApplicationException,RemoteException;

	/**
	 * Liefert eine Liste aller Umsaetze fuer das Konto.
   * @return Umsatzliste.
   * @throws RemoteException
   */
  public DBIterator getUmsaetze() throws RemoteException;

	/**
	 * Liefert eine Liste aller Ueberweisungen, die ueber dieses Konto getaetigt wurden.
	 * @return Ueberweisungsliste.
	 * @throws RemoteException
	 */
	public DBIterator getUeberweisungen() throws RemoteException;
	
	/**
	 * Liefert die HBCI-Protokollierung des Kontos in Form einer Liste von Protokoll-Objekten.
   * @return Liste von Protokoll-Objekten.
   * @throws RemoteException
   */
  public DBIterator getProtokolle() throws RemoteException;

	/**
	 * Fuegt den uebergebenen Text zum Konto-Protokoll hinzu.
   * @param kommentar der hinzuzufuegende Text.
   * @param protokollTyp Typ des Protokoll-Eintrags.
   * Siehe <code>de.willuhn.jameica.hbci.rmi.Protokoll</code>.
   * @throws RemoteException
   */
  public void addToProtokoll(String kommentar, int protokollTyp) throws RemoteException;
	
	/**
	 * Loescht alle Umsaetze des Kontos.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void deleteUmsaetze() throws ApplicationException, RemoteException;
}


/**********************************************************************
 * $Log: Konto.java,v $
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.12  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/05 23:28:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.7  2004/02/25 23:11:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.4  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/