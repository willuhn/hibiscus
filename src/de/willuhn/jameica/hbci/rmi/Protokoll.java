/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Protokoll.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/25 23:23:17 $
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

/**
 * Speichert HBCI-Protokoll-Informationen zu jedem Konto.
 * Hintergrund: Ueber diese Klasse kann jederzeit geprueft werden,
 * wann welche Art von HBCI-Aktion (z.Bsp. Umsaetze abrufen oder Ueberweisung)
 * ausgefuehrt wurde.
 */
public interface Protokoll extends DBObject {

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
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/