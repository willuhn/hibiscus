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
import java.util.Date;

/**
 * Bildet eine Zeile in den Kontoauszuegen ab.
 * Auch wenn das Interface Set-Methoden zum Speichern von
 * Werten besitzt, so macht es keinen Sinn, manuell derartige Objekte
 * zu erzeugen und zu speichern oder zu aendern. Umsatz-Objekte werden
 * ueber HBCI-Geschaeftsvorfaelle von der Bank geliefert und nur von
 * dort geschrieben.
 */
public interface Umsatz extends HibiscusTransfer, HibiscusDBObject, Checksum, Flaggable, Duplicatable<Umsatz>
{
  /**
   * Flag "kein Flag".
   */
  public final static int FLAG_NONE    = 0;

  /**
   * Flag "Geprueft".
   */
  public final static int FLAG_CHECKED = 1 << 0;

  /**
   * Flag "Vorgemerkt".
   */
  public final static int FLAG_NOTBOOKED = 1 << 1;

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
   * Liefert den 3-stelligen Geschaeftsvorfall-Code.
   * @return der 3-stellige Geschaeftsvorfall-Code.
   * @throws RemoteException
   */
  public String getGvCode() throws RemoteException;
  
  /**
   * Speichert den 3-stelligen Geschaeftsvorfall-Code.
   * @param code der 3-stellige Geschaeftsvorfall-Code.
   * @throws RemoteException
   */
  public void setGvCode(String code) throws RemoteException;

  /**
   * Liefert die 3-stellige Textschluessel-Ergaenzung.
   * @return die 3-stellige Textschluessel-Ergaenzung.
   * @throws RemoteException
   */
  public String getAddKey() throws RemoteException;
  
  /**
   * Speichert die 3-stellige Textschluessel-Ergaenzung.
   * @param key die 3-stellige Textschluessel-Ergaenzung.
   * @throws RemoteException
   */
  public void setAddKey(String key) throws RemoteException;

  /**
   * Speichert einen optionalen Kommentar zu dem Umsatz.
   * @param kommentar Kommentar.
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;

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
   * Liefert einen ggf manuell zugeordneten Umsatz-Typ oder <code>null</code> wenn keiner zugeordnet ist.
   * @return Umsatz-Typ.
   * @throws RemoteException
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException;
  
  /**
   * Speichert einen manuell zugeordneten Umsatz-Typ.
   * @param ut zugeordneter Umsatztyp oder <code>null</code> zum Entfernen der Zuordnung.
   * @throws RemoteException
   */
  public void setUmsatzTyp(UmsatzTyp ut) throws RemoteException;
  
  /**
   * Liefert true, wenn der Umsatz einer Kategorie zugeordnet ist.
   * @return true, wenn der Umsatz einer Kategorie zugeordnet ist.
   * @throws RemoteException
   */
  public boolean isAssigned() throws RemoteException;
  
  /**
   * Liefert eine optionale Transaktions-ID, anhand derer der Umsatz eindeutig identifiziert werden kann.
   * Die ID ist nur bei Umsaetzen vorhanden, die per CAMT abgerufen wurden.
   * Wichtig: Hibiscus garantiert NICHT, dass die ID eindeutig ist. Weder durch Programmlogik noch durch einen Unique-Key.
   * Denn wuerde es das tun - und es wuerde tatsaechlich zu einer doppelten ID kommen, koennte der Umsatz
   * nicht angelegt werden. Die ID ist lediglich ein zusaetzliches Kriterium bei der Doppler-Erkennung.
   * @return optionale Transaktions-ID, anhand derer der Umsatz eindeutig identifiziert werden kann.
   * @throws RemoteException
   */
  public String getTransactionId() throws RemoteException;
  
  /**
   * Speichert eine optionale Transaktions-ID, anhand derer der Umsatz eindeutig identifiziert werden kann.
   * Wichtig: Hibiscus garantiert NICHT, dass die ID eindeutig ist. Weder durch Programmlogik noch durch einen Unique-Key.
   * Denn wuerde es das tun - und es wuerde tatsaechlich zu einer doppelten ID kommen, koennte der Umsatz
   * nicht angelegt werden. Die ID ist lediglich ein zusaetzliches Kriterium bei der Doppler-Erkennung.
   * @param id die eindeutige Transaktions-ID.
   * @throws RemoteException
   */
  public void setTransactionId(String id) throws RemoteException;
  
  /**
   * Liefert den Purpose-Code der Buchung.
   * Nur bei Umsaetzen vorhanden, die per CAMT abgerufen wurden.
   * @return der Purpose-Code der Buchung.
   * @throws RemoteException
   */
  public String getPurposeCode() throws RemoteException;
  
  /**
   * Speichern den Purpose-Code der Buchung.
   * @param code der Purpose-Code der Buchung.
   * @throws RemoteException
   */
  public void setPurposeCode(String code) throws RemoteException;

  /**
   * Liefert die EndToEnd-ID der Buchung.
   * Nur bei Umsaetzen vorhanden, die per CAMT abgerufen wurden.
   * @return der EndToEnd-ID der Buchung.
   * @throws RemoteException
   */
  public String getEndToEndId() throws RemoteException;
  
  /**
   * Speichern die EndToEnd-ID der Buchung.
   * @param id die EndToEnd-ID der Buchung.
   * @throws RemoteException
   */
  public void setEndToEndId(String id) throws RemoteException;

  /**
   * Liefert die Mandatsreferenz der Buchung.
   * Nur bei Umsaetzen vorhanden, die per CAMT abgerufen wurden.
   * @return der Mandatsreferenz der Buchung.
   * @throws RemoteException
   */
  public String getMandateId() throws RemoteException;
  
  /**
   * Speichern die Mandatsreferenz der Buchung.
   * @param id die Mandatsreferenz der Buchung.
   * @throws RemoteException
   */
  public void setMandateId(String id) throws RemoteException;

  /**
   * Liefert den Namen des ultimativen Empfaengers.
   * Nur bei Umsaetzen vorhanden, die per CAMT abgerufen wurden.
   * @return Name des ultimativen Empfaengers
   * @throws RemoteException
   */
  public String getGegenkontoName2() throws RemoteException;

  /**
   * Setzt den Namen des ultimativen Empfaengers.
   * @param name Name des ultimativen Empfaengers
   * @throws RemoteException
   */
  public void setGegenkontoName2(String name) throws RemoteException;

  /**
   * Liefert die Gläubiger-ID des Gegenkontos.
   * Nur bei Umsaetzen vorhanden, die per CAMT abgerufen werden.
   * @return Gläubiger-ID des Gegenkontos
   * @throws RemoteException
   */
  public String getCreditorId() throws RemoteException;

  
  /**
   * Setzt die Gläubiger-ID des Gegenkontos.
   * @param id Gläubiger-ID des Gegenkontos
   * @throws RemoteException
   */
  public void setCreditorId(String id) throws RemoteException;
}
