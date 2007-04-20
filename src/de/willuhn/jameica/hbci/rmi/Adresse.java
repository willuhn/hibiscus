/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/Adresse.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/04/20 14:49:05 $
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
import de.willuhn.util.ApplicationException;

/**
 * Bildet einen Empfaenger ab.
 */
public interface Adresse extends Address, DBObject
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
   */
  public void setBLZ(String blz) throws RemoteException;
	
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
   * Liefert eine Liste von Umsaetzen, die von/an diese Adresse getaetigt wurden.
   * @return Liste von Umsaetzen.
   * @throws RemoteException
   */
  public DBIterator getUmsaetze() throws RemoteException;

  /**
   * Liefert eine Liste von Buchungen aus Sammellastschriften, die von dieser
   * Adresse eingezogen wurden.
   * @return Liste von Buchungen.
   * @throws RemoteException
   */
  public DBIterator getSammellastBuchungen() throws RemoteException;
  
  /**
   * Liefert eine Liste von Buchungen aus Sammelueberweisungen, die an diese
   * Adresse ueberweisen wurden.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public DBIterator getSammelUeberweisungBuchungen() throws RemoteException;

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
 * $Log: Adresse.java,v $
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