/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/12 00:38:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportDDV;

/**
 * Factory zum Laden der Passports.
 */
public class PassportFactory {

	/**
	 * Erzeugt eine konkrete Implementierung des Passports
	 * fuer das angegebene Konto.
   * @param k das Konto.
   * @return die konkrete Passport-Implementierung.
   */
  protected static Passport create(Konto k) throws RemoteException
	{
		if (k == null)
			return null;
		
		Passport p = (Passport) k.getField("passport_id");
		
		PassportImpl kp = null;

		if (new Integer(p.getID()).intValue() == Passport.TYPE_DDV)
			kp = (PassportImpl) Settings.getDatabase().createObject(PassportDDV.class,p.getID());
		// hier spaeter weitere Passports hinzufuegen
		
		kp.setKonto(k);
		return kp;
	}

}


/**********************************************************************
 * $Log: PassportFactory.java,v $
 * Revision 1.1  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/