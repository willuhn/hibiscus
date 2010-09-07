/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/rmi/Passport.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/09/07 15:28:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.rmi;


/**
 * Passport fuer das Sicherheitsmedium "Chipkarte" (DDV).
 */
public interface Passport extends de.willuhn.jameica.hbci.passport.Passport {

	/**
	 * Parameter fuer den Port (meist 0)
	 */
	public final static String PORT 		= "client.passport.DDV.port";

	/**
	 * Parameter fuer den Index (normalerweise 0)
	 */
	public final static String CTNUMBER = "client.passport.DDV.ctnumber";

	/**
	 * Parameter ober Biometrie verwendet wird (meist 0)
	 */
	public final static String USEBIO	  = "client.passport.DDV.usebio";

	/**
	 * Parameter ob die Tastatur zur Pin-Eingabe verwendet werden soll.
	 */
	public final static String SOFTPIN  = "client.passport.DDV.softpin";

	/**
	 * Parameter fuer den Index des HBCI-Zugangs (meist 1).
	 */
	public final static String ENTRYIDX = "client.passport.DDV.entryidx";

	/**
	 * Parameter fuer den den Pfad und Dateinamen des CTAPI-Treibers.
	 */
	public final static String CTAPI = "client.passport.DDV.libname.ctapi";
}


/**********************************************************************
 * $Log: Passport.java,v $
 * Revision 1.5  2010/09/07 15:28:06  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.4  2010-07-22 22:36:24  willuhn
 * @N Code-Cleanup
 *
 * Revision 1.3  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.16  2007/07/24 13:50:27  willuhn
 * @N BUGZILLA 61
 *
 * Revision 1.15  2006/04/05 15:15:43  willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 *
 * Revision 1.14  2005/11/14 11:35:16  willuhn
 * @B bug 148
 *
 * Revision 1.13  2005/06/27 15:29:43  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/06/27 11:24:30  web0
 * @N HBCI-Version aenderbar
 *
 * Revision 1.11  2005/03/09 01:07:27  web0
 * @D javadoc fixes
 *
 * Revision 1.10  2005/02/01 18:26:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/18 23:12:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/17 14:06:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/17 13:17:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/17 12:52:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 * Revision 1.4  2004/07/19 22:37:28  willuhn
 * @B gna - Chipcard funktioniert ja doch ;)
 *
 * Revision 1.2  2004/05/05 22:14:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/04 23:24:34  willuhn
 * @N separated passports into eclipse project
 *
 * Revision 1.1  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.1  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 **********************************************************************/