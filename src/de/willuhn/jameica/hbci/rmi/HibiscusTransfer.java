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
 * Basis-Interface fuer Geld-Transfers zwischen Konten.
 */
public interface HibiscusTransfer extends Transfer, HibiscusDBObject
{

	/**
	 * Liefert das Konto, ueber das bezahlt wurde.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;

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
  public void setGegenkonto(Address e) throws RemoteException;

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
   * Speichert eine Liste erweiterter Verwendungszwecke.
   * @param list Liste erweiterter Verwendungszwecke.
   * @throws RemoteException
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException;
}

/**********************************************************************
 * $Log: HibiscusTransfer.java,v $
 * Revision 1.3  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.2  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.1  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
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