/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Transfer.java,v $
 * $Revision: 1.13 $
 * $Date: 2010/04/14 17:44:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Basis-Interface fuer eine Geld-Bewegung.
 */
public interface Transfer extends Remote
{
  /**
   * Typ Ueberweisung
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_UEBERWEISUNG         = 1;
  
  /**
   * Typ Lastschrift
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_LASTSCHRIFT          = 2;
  
  /**
   * Typ Dauerauftrag
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_DAUERAUFTRAG         = 3;

  /**
   * Typ Umsatz
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_UMSATZ               = 4;
  
  /**
   * Typ Buchung einer Sammel-Ueberweisung
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_SUEB_BUCHUNG         = 5;
  
  /**
   * Typ Buchung einer Sammel-Lastschrift
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_SLAST_BUCHUNG        = 6;

  /**
   * Typ Auslandsueberweisung
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public final static int TYP_AUSLANDSUEBERWEISUNG = 7;

  /**
	 * Liefert die Kontonummer des Gegenkontos.
   * @return Kontonummer des Empfaengers.
   * @throws RemoteException
   */
  public String getGegenkontoNummer() throws RemoteException;

	/**
	 * Liefert die BLZ des Gegenkontos.
	 * @return BLZ des Gegenkontos.
	 * @throws RemoteException
	 */
	public String getGegenkontoBLZ() throws RemoteException;
	
	/**
	 * Liefert den Namen des Kontoinhabers des Gegenkontos.
	 * @return Name des Kontoinhabers des Gegenkontos.
	 * @throws RemoteException
	 */
	public String getGegenkontoName() throws RemoteException;

	/**
	 * Liefert den Betrag.
   * @return Betrag.
   * @throws RemoteException
   */
  public double getBetrag() throws RemoteException;
	
	/**
	 * Liefert die Zeile 1 des Verwendungszwecks.
   * @return Zeile 1 des Verwendungszwecks.
   * @throws RemoteException
   */
  public String getZweck() throws RemoteException;
	
	/**
	 * Liefert die Zeile 2 des Verwendungszwecks.
	 * @return Zeile 2 des Verwendungszwecks.
	 * @throws RemoteException
	 */
	public String getZweck2() throws RemoteException;
  
  /**
   * Liefert den Transfer-Typ.
   * @return Transfer-Typ.
   * @throws RemoteException
   * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
   */
  public int getTransferTyp() throws RemoteException;
  
  /**
   * Liefert eine Liste erweiterter Verwendungszwecke.
   * @return Liste erweiterter Verwendungszwecke.
   * @throws RemoteException
   */
  public String[] getWeitereVerwendungszwecke() throws RemoteException;
}


/**********************************************************************
 * $Log: Transfer.java,v $
 * Revision 1.13  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.12  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 * Revision 1.11  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.10  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.9  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 **********************************************************************/