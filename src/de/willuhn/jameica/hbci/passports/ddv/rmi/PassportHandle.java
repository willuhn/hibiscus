/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/rmi/Attic/PassportHandle.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/27 22:23:56 $
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
public interface PassportHandle extends de.willuhn.jameica.hbci.rmi.hbci.PassportHandle {

}


/**********************************************************************
 * $Log: PassportHandle.java,v $
 * Revision 1.1  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/