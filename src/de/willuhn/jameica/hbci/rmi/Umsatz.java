/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Umsatz.java,v $
 * $Revision: 1.27 $
 * $Date: 2011/10/18 09:28:14 $
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
   * Liefert true, wenn der Umsatz nicht vorgemerkt ist.
   * @return true, wenn der Umsatz nicht vorgemerkt ist.
   * @throws RemoteException
   */
  public boolean isBooked() throws RemoteException;
  
  /**
   * Liefert true, wenn der Umsatz geprueft ist.
   * @return true, wenn der Umsatz geprueft ist.
   * @throws RemoteException
   */
  public boolean isChecked() throws RemoteException;
}


/**********************************************************************
 * $Log: Umsatz.java,v $
 * Revision 1.27  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.26  2011-07-25 17:17:19  willuhn
 * @N BUGZILLA 1065 - zusaetzlich noch addkey
 *
 * Revision 1.25  2011-07-25 14:42:41  willuhn
 * @N BUGZILLA 1065
 *
 * Revision 1.24  2010/04/22 12:42:02  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.23  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.22  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.21  2009/02/23 17:01:58  willuhn
 * @C Kein Abgleichen mehr bei vorgemerkten Buchungen sondern stattdessen vorgemerkte loeschen und neu abrufen
 *
 * Revision 1.20  2009/02/12 18:37:18  willuhn
 * @N Erster Code fuer vorgemerkte Umsaetze
 *
 * Revision 1.19  2009/02/12 16:14:34  willuhn
 * @N HBCI4Java-Version mit Unterstuetzung fuer vorgemerkte Umsaetze
 *
 * Revision 1.18  2009/02/04 23:06:24  willuhn
 * @N BUGZILLA 308 - Umsaetze als "geprueft" markieren
 *
 * Revision 1.17  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.16  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.15  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
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