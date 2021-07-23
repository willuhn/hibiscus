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
 * Speichert HBCI-Protokoll-Informationen zu jedem Konto.
 * Hintergrund: Ueber diese Klasse kann jederzeit geprueft werden,
 * wann welche Art von HBCI-Aktion (z.Bsp. Umsaetze abrufen oder Ueberweisung)
 * ausgefuehrt wurde.
 */
public interface Protokoll extends HibiscusDBObject
{

	/**
	 * Protokoll-Typ unbekannt (Default).
	 */
	public final static int TYP_UNKNOWN = 0;

	/**
	 * Protokoll-Typ bei Erfolg.
	 */
	public final static int TYP_SUCCESS = 1;

	/**
	 * Protokoll-Typ bei einem Fehler.
	 */
	public final static int TYP_ERROR   = 2;

	/**
	 * Liefert das Konto, zu dem dieser Protokoll-Eointrag gehoert.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;
	
	/**
	 * Liefert den Kommentar des Log-Eintrages.
   * @return Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;
	
	/**
	 * Liefert das Datum, an dem der Log-Eintrag erzeugt wurde.
   * @return Datum.
   * @throws RemoteException
   */
  public Date getDatum() throws RemoteException;
	
	/**
	 * Liefert den Typ des Log-Eintrages.
	 * Zur Codierung siehe die Konstanten TYP_*.
   * @return Typ.
   * @throws RemoteException
   */
  public int getTyp() throws RemoteException;
	
	/**
	 * Speichert das Konto, zu dem dieser Log-Eintrag gehoert.
   * @param konto Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;
	
	/**
	 * Speichert den Kommentar zu diesem Log-Eintrag.
   * @param kommentar Kommentar.
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;
	
	/**
	 * Speichert den Typ des Log-Eintrages.
	 * Zur Codierung siehe die Konstanten TYP_*.
   * @param typ Typ.
   * @throws RemoteException
   */
  public void setTyp(int typ) throws RemoteException;
	
	
}


/**********************************************************************
 * $Log: Protokoll.java,v $
 * Revision 1.2  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/