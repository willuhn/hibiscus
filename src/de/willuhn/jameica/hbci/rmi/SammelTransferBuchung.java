/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SammelTransferBuchung.java,v $
 * $Revision: 1.6 $
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

/**
 * Interface fuer eine einzelne Buchung eines Sammel-Transfers.
 */
public interface SammelTransferBuchung extends Transfer, HibiscusDBObject
{
	/**
	 * Liefert den zugeordneten Sammel-Transfer.
   * @return Sammel-Transfer.
   * @throws RemoteException
   */
  public SammelTransfer getSammelTransfer() throws RemoteException;

	/**
	 * Legt den zugehoerigen Sammel-Transfer fest.
   * @param s Sammel-Transfer.
   * @throws RemoteException
   */
  public void setSammelTransfer(SammelTransfer s) throws RemoteException;

	/**
	 * Speichert die Kontonummer des Gegenkontos.
   * @param kontonummer
   * @throws RemoteException
   */
  public void setGegenkontoNummer(String kontonummer) throws RemoteException;
	
	/**
	 * Speichert die BLZ des Gegenkontos.
   * @param blz
   * @throws RemoteException
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException;

	/**
	 * Speichert den Namen des Kontoinhabers des Gegenkontos.
   * @param name
   * @throws RemoteException
   */
  public void setGegenkontoName(String name) throws RemoteException;

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
  
  /**
   * Liefert den Textschluessel der Buchung.
   * @return Textschluessel.
   * @throws RemoteException
   */
  public String getTextSchluessel() throws RemoteException;
  
  /**
   * Speichert den Textschluessel der Buchung.
   * @param schluessel Textschluessel.
   * @throws RemoteException
   */
  public void setTextSchluessel(String schluessel) throws RemoteException;

  /**
   * Speichert eine Liste erweiterter Verwendungszwecke.
   * @param list Liste erweiterter Verwendungszwecke.
   * @throws RemoteException
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException;
}


/**********************************************************************
 * $Log: SammelTransferBuchung.java,v $
 * Revision 1.6  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.5  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.4  2007/10/14 23:26:59  willuhn
 * @N Textschluessel in Sammelauftraegen - wird noch nicht persistiert
 *
 * Revision 1.3  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.2  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/