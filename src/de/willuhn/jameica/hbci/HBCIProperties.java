/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCIProperties.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/02 18:48:32 $
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

  /**
   * Maximale Text-Laenge einer Verwendungszweck-Zeile.
   */
  public final static int HBCI_TRANSFER_USAGE_MAXLENGTH =
    settings.getInt("hbci.transfer.usage.maxlength",27);

	// disabled
	private HBCIProperties()
	{
	}

}


/**********************************************************************
 * $Log: HBCIProperties.java,v $
 * Revision 1.2  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 **********************************************************************/