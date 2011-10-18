/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SammelTransfer.java,v $
 * $Revision: 1.5 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer Sammellastschriften und -ueberweisungen.
 */
public interface SammelTransfer extends HibiscusDBObject, Terminable, Duplicatable
{
	/**
	 * Liefert eine Liste der Buchungen fuer diesen Transfer.
   * Das sind Objekte des Typs <code>SammelTransferBuchung</code>.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public DBIterator getBuchungen() throws RemoteException;

  /**
   * Liefert die Buchungen des Sammeltransfers als Array.
   * Convenience-Funktion fuer Velocity (fuer den Export). Das versteht leider nur Arrays/List,
   * kann also nicht mit einem DBIterator umgehen.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public SammelTransferBuchung[] getBuchungenAsArray() throws RemoteException;
  
  /**
   * Liefert die Summe der enthaltenen Buchungen.
   * @return Summe der enthaltenen Buchungen.
   * @throws RemoteException
   */
  public double getSumme() throws RemoteException;

  /**
	 * Liefert das Konto, ueber das der Transfer gebucht wird.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;

	/**
	 * Speichert das Konto, ueber das der Transfer gebucht werden soll.
   * @param konto Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;

  /**
   * Liefert eine Bezeichnung des Transfers.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

  /**
   * Speichert die Bezeichnung.
   * @param bezeichnung
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;
  
  /**
   * Erzeugt eine neue Buchung auf dem Sammeltransfer.
   * @return die neu erzeugte Buchung.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public SammelTransferBuchung createBuchung() throws RemoteException, ApplicationException;
}


/**********************************************************************
 * $Log: SammelTransfer.java,v $
 * Revision 1.5  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.4  2008/12/17 22:48:17  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.3  2006/08/17 10:06:32  willuhn
 * @B Fehler in HTML-Export von Sammeltransfers
 *
 * Revision 1.2  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/