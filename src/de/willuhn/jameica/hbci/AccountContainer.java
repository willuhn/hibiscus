/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/AccountContainer.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/02 16:15:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

/**
 * Dieser Container fasst alle fuer eine HBCI-Bankverbindung
 * notwendigen Daten (BLZ, Kontonummer, Kundennummer, etc.) zusammen.
 * Hintegrund: Bei Erstellung eines neuen RDH-Passports fragt
 * der HBCICallback jeden Wert einzeln vom Benutzer ab. Das wollen
 * wir ihm nicht zumuten. Daher oeffnen wir anfangs einen Dialog,
 * in dem der User alle Daten auf einmal eingeben kann.
 */
public class AccountContainer
{
	public String country 		= "DE";
	public String blz 				= null;
	public String host 				= null;
	public int 		port				= 3000;
	public String filter			= null;
	public String userid			= null;
	public String customerid	= null;
}


/**********************************************************************
 * $Log: AccountContainer.java,v $
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/