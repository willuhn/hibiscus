/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
	/**
	 * Land.
	 */
	public String country 		= "DE";

	/**
	 * BLZ.
	 */
	public String blz 				= null;

  /**
	 * Host.
	 */
	public String host 				= null;
	
  /**
   * TCP-Port.
   */
  public int 		port				= 3000;
	
  /**
   * Filter.
   */
  public String filter			= null;
	
  /**
   * Benutzer-Kennung.
   */
  public String userid			= null;
	
  /**
   * Kunden-Kennung.
   */
  public String customerid	= null;
}


/**********************************************************************
 * $Log: AccountContainer.java,v $
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/