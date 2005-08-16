/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/Adresse.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/08/16 21:33:13 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet einen Empfaenger ab.
 */
public interface Adresse extends DBObject {

	/**
	 * Liefert die Kontonummer des Empfaengers.
   * @return Kontonummer.
   * @throws RemoteException
   */
  public String getKontonummer() throws RemoteException;
	
	/**
	 * Liefert die BLZ des Empfaengers.
   * @return BLZ.
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;
	
	/**
	 * Liefert den Namen des Empfaengers.
   * @return Name.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
	
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
   */
  public void setBLZ(String blz) throws RemoteException;
	
	/**
	 * Speichert den Namen des Empfaengers.
   * @param name Name.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
  
  /**
   * Liefert eine Liste der Ueberweisungen, die an diesen Empfaenger
   * getaetigt wurden.
   * @return Liste mit Ueberweisungen.
   * @throws RemoteException
   */
  public DBIterator getUeberweisungen() throws RemoteException;

  /**
   * Liefert einen zusaetzlichen Kommentar fuer den Adressbuch-Eintrag.
   * @return Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;
  
  /**
   * Speichert einen zusaetzlichen Kommentar fuer den Adressbuch-Eintrag.
   * @param kommentar
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;
}


/**********************************************************************
 * $Log: Adresse.java,v $
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