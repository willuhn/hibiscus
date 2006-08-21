/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Umsatz.java,v $
 * $Revision: 1.14 $
 * $Date: 2006/08/21 23:15:01 $
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

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

/**
 * Bildet eine Zeile in den Kontoauszuegen ab.
 * Auch wenn das Interface Set-Methoden zum Speichern von
 * Werten besitzt, so macht es keinen Sinn, manuell derartige Objekte
 * zu erzeugen und zu speichern oder zu aendern. Umsatz-Objekte werden
 * ueber HBCI-Geschaeftsvorfaelle von der Bank geliefert und nur von
 * dort geschrieben.
 */
public interface Umsatz extends DBObject, Checksum
{

	/**
	 * Liefert das Konto, auf welches sich diese Umsaetze beziehen.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;
	
	/**
	 * Liefert den Namen des Empfaengers der Zahlung.
   * @return Empfaenger der Zahlung.
   * @throws RemoteException
   */
  public String getEmpfaengerName() throws RemoteException;
	
  /**
   * Liefert die Kontonummer des Empfaengers der Zahlung.
   * @return Kontonummer des Empfaengers der Zahlung.
   * @throws RemoteException
   */
  public String getEmpfaengerKonto() throws RemoteException;
  
  /**
   * Liefert die BLZ des Empfaengers der Zahlung.
   * @return BLZ des Empfaengers der Zahlung.
   * @throws RemoteException
   */
  public String getEmpfaengerBLZ() throws RemoteException;
  
  /**
	 * Betrag der Buchung. Soll-Buchungen werden durch negative
	 * Werte dargestellt, Haben-Buchungen durch positive Werte.
   * @return Buchungsbetrag.
   * @throws RemoteException
   */
  public double getBetrag() throws RemoteException;
	
	/**
	 * Text im Verwendungszweck.
   * @return Verwendungszweck.
   * @throws RemoteException
   */
  public String getZweck() throws RemoteException;
	
	/**
	 * Fortsetzung des Verwendungszwecks.
	 * Das Feld enthaelt alle restlichen Zeilen.
   * @return weiterer Verwendungszweck.
   * @throws RemoteException
   */
  public String getZweck2() throws RemoteException;
	
	/**
	 * Liefert das Datum der Buchung.
   * @return Datum der Buchung.
   * @throws RemoteException
   */
  public Date getDatum() throws RemoteException;
	
	/**
	 * Datum der Wert-Stellung. 
	 * Das ist das Datum, ab dem der gebuchte Betrag
	 * finanzmathematisch Geltung findet.
	 * Oft stimmt der mit dem Datum der Buchung ueberein.
   * @return Valuta.
   * @throws RemoteException
   */
  public Date getValuta() throws RemoteException;

	/**
	 * Liefert den Saldo des Kontos nach dieser Buchung.
   * @return Saldo.
   * @throws RemoteException
   */
  public double getSaldo() throws RemoteException;

	/**
	 * Liefert das Primanota-Kennzeichen der Buchung.
   * @return PrimaNota-Kennzeichen.
   * @throws RemoteException
   */
  public String getPrimanota() throws RemoteException;
	
	/**
	 * Liefert einen Text, der die Art der Buchung beschreibt.
   * @return Art der Buchung.
   * @throws RemoteException
   */
  public String getArt() throws RemoteException;
	
	/**
	 * Liefert die Kundenreferenz.
   * @return Kundenreferenz.
   * @throws RemoteException
   */
  public String getCustomerRef() throws RemoteException;

  /**
   * Liefert einen optionalen Kommentar, den der User zu dem Umsatz eintragen kann.
   * @return optionaler Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;

  /**
   * Speichert einen optionalen Kommentar zu dem Umsatz.
   * @param kommentar Kommentar.
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;

  /**
	 * Speichert das Konto, auf welches sich der Umsatz bezieht.
   * @param k das Konto.
   * @throws RemoteException
   */
  void setKonto(Konto k) throws RemoteException;

	/**
	 * Speichert Name,BLZ und Konto des Empfaengers en bloc.
   * @param empf der zu speichernde Empfaenger.
   * @throws RemoteException
   */
  void setEmpfaenger(Adresse empf) throws RemoteException;
	
	/**
	 * Speichert den Namen des Empfaengers.
   * @param name des Empfaenger.
   * @throws RemoteException
   */
  public void setEmpfaengerName(String name) throws RemoteException;
	
  /**
   * Speichert die Kontonummer des Empfaengers.
   * @param konto Kontonummer des Empfaenger.
   * @throws RemoteException
   */
	public void setEmpfaengerKonto(String konto) throws RemoteException;

  /**
   * Speichert die BLZ des Empfaengers.
   * @param blz BLZ des Empfaenger.
   * @throws RemoteException
   */
	public void setEmpfaengerBLZ(String blz) throws RemoteException;
  
  /**
	 * Betrag der Buchung. Soll-Buchungen werden durch negative Werte dargestellt.
   * @param d Betrag der Buchung.
   * @throws RemoteException
   */
	public void setBetrag(double d) throws RemoteException;
	
	/**
	 * Verwendungszweck.
   * @param zweck
   * @throws RemoteException
   */
	public void setZweck(String zweck) throws RemoteException;
	
	/**
	 * weiterer Verwendungszweck. Darf <code>null</code> sein.
   * @param zweck2
   * @throws RemoteException
   */
	public void setZweck2(String zweck2) throws RemoteException;
	
	/**
	 * Datum der Buchung.
   * @param d
   * @throws RemoteException
   */
	public void setDatum(Date d) throws RemoteException;
	
  /**
   * Datum der Wertstellung.
   * @param d
   * @throws RemoteException
   */
	public void setValuta(Date d) throws RemoteException;

	/**
	 * Speichert den Saldo des Kontos nach dieser Buchung.
	 * @param s
	 * @throws RemoteException
	 */
	public void setSaldo(double s) throws RemoteException;

	/**
	 * Speichert das Primanota-Kennzeichen der Buchung.
	 * @param primanota
	 * @throws RemoteException
	 */
	public void setPrimanota(String primanota) throws RemoteException;
	
	/**
	 * Speichert einen Text, der die Art der Buchung beschreibt.
	 * @param art
	 * @throws RemoteException
	 */
	public void setArt(String art) throws RemoteException;
	
	/**
	 * Speichert die Kundenreferenz.
	 * @param ref
	 * @throws RemoteException
	 */
	public void setCustomerRef(String ref) throws RemoteException;

  /**
   * Prueft, ob der Umsatz vom User geaendert wurde.
   * @return true, wenn der Umsatz geaendert wurde.
   * @throws RemoteException
   */
  public boolean hasChangedByUser() throws RemoteException;
  
  /**
   * Markiert den Umsatz als "durch den Benutzer geaendert".
   * Der Vorgang kann nach dem Speichern nicht rueckgaengig gemacht werden.
   * @throws RemoteException
   */
  public void setChangedByUser() throws RemoteException;
  
  /**
   * Erlaubt das Setzen von Attributen des Umsatzes ueber diese generische Funktion.
   * @param name Name des Attributs.
   * @param value Wert.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void setGenericAttribute(String name, String value) throws RemoteException, ApplicationException;
}


/**********************************************************************
 * $Log: Umsatz.java,v $
 * Revision 1.14  2006/08/21 23:15:01  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.13  2005/12/29 01:22:12  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.12  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.11  2005/11/14 23:47:21  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.10  2005/06/30 21:48:56  web0
 * @B bug 75
 *
 * Revision 1.9  2005/06/13 23:11:01  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.7  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.5  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.4  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 **********************************************************************/