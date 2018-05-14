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

/**
 * Bildet einen Empfaenger ab.
 */
public interface HibiscusAddress extends Address, HibiscusDBObject
{

	/**
	 * Speichert die Kontonummer des Empfaengers.
   * @param kontonummer Kontonummer.
   * @throws RemoteException
   */
  public void setKontonummer(String kontonummer) throws RemoteException;
	
  /**
   * Speichert die BLZ des Empfaengers.
   * @param blz BLZ.
   * @throws RemoteException
   * BUGZILLA 534
   */
  public void setBlz(String blz) throws RemoteException;

  /**
	 * Speichert den Namen des Empfaengers.
   * @param name Name.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
  
  /**
   * Speichert einen zusaetzlichen Kommentar fuer den Adressbuch-Eintrag.
   * @param kommentar
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;
  
  /**
   * Liefert den Namen der Bank.
   * Ist nur fuer auslaendische Banken sinnvoll, da HBCI4Java fuer
   * deutsche Banken eine Mapping-Tabelle BLZ->Bankname mitbringt.
   * @return Name der Bank.
   * @throws RemoteException
   */
  public String getBank() throws RemoteException;
  
  /**
   * Speichert den Namen der Bank.
   * Ist nur fuer auslaendische Banken sinnvoll, da HBCI4Java fuer
   * deutsche Banken eine Mapping-Tabelle BLZ->Bankname mitbringt.
   * @param name Name der Bank.
   * @throws RemoteException
   */
  public void setBank(String name) throws RemoteException;
  
  /**
   * Speichert die BIC.
   * @param bic die BIC.
   * @throws RemoteException
   */
  public void setBic(String bic) throws RemoteException;
  
  /**
   * Speichert die IBAN.
   * @param iban die IBAN.
   * @throws RemoteException
   */
  public void setIban(String iban) throws RemoteException;
  
  /**
   * Speichert die Kategorie.
   * @param kategorie die Kategorie.
   * @throws RemoteException
   */
  public void setKategorie(String kategorie) throws RemoteException;
}


/**********************************************************************
 * $Log: HibiscusAddress.java,v $
 * Revision 1.9  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.8  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.7  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.6  2009/05/07 09:58:40  willuhn
 * @R deprecated Funktionen getBLZ/setBLZ entfernt - bitte nur noch getBlz/setBlz nutzen!
 *
 * Revision 1.5  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 * Revision 1.4  2008/11/05 09:26:57  willuhn
 * @B tag typo
 *
 * Revision 1.3  2008/01/09 23:32:54  willuhn
 * @B Bug 534
 *
 * Revision 1.2  2007/04/23 21:03:48  willuhn
 * @R "getTransfers" aus Address entfernt - hat im Adressbuch eigentlich nichts zu suchen
 *
 * Revision 1.1  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.6  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.5  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 * Revision 1.4  2005/10/03 16:17:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.2  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.1  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/