/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCIProperties.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/01 23:10:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import de.willuhn.jameica.system.Settings;


/**
 * enthaelt HBCI-Parameter.
 */
public class HBCIProperties
{

	private static Settings settings = new Settings(HBCIProperties.class);

	/**
	 * Liste der erlaubten Zeichen (z.Bsp. fuer den Verwendungszweck.).
	 */
	public final static String HBCI_DTAUS_VALIDCHARS =
		settings.getString("hbci.dtaus.validchars",
											 "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.&-+*%/$üöäÜÖÄß"
		); 

	// disabled
	private HBCIProperties()
	{
	}

}


/**********************************************************************
 * $Log: HBCIProperties.java,v $
 * Revision 1.1  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 **********************************************************************/