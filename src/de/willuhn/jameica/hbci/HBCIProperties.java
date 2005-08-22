/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCIProperties.java,v $
 * $Revision: 1.11 $
 * $Date: 2005/08/22 10:36:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * enthaelt HBCI-Parameter.
 */
public class HBCIProperties
{

	private static Settings settings = new Settings(HBCIProperties.class);
  private static I18N i18n = null;
  
  static
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    settings.setStoreWhenRead(false);
  }

	/**
	 * Liste der in DTAUS erlaubten Zeichen.
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

  // BUGZILLA #49 http://www.willuhn.de/bugzilla/show_bug.cgi?id=49
  /**
   * Reservierter Tag fuer "Monatsletzten".
   */
  public final static int HBCI_LAST_OF_MONTH =
    settings.getInt("hbci.lastofmonth",99);

  /**
   * Laenge von Bankleitzahlen.
   */
  public final static int HBCI_BLZ_LENGTH =
    settings.getInt("hbci.blz.maxlength",8);

	/**
	 * Maximale Text-Laenge fuer Namen.
	 */
	public final static int HBCI_TRANSFER_NAME_MAXLENGTH =
		settings.getInt("hbci.transfer.name.maxlength",27);

  // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
  /**
   * Default-Waehrungs-Bezeichnung in Deutschland. 
   */
  public final static String CURRENCY_DEFAULT_DE = 
    settings.getString("currency.default.de","EUR");

	
  // BUGZILLA 28 http://www.willuhn.de/bugzilla/show_bug.cgi?id=28
	/**
	 * Maximale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MAXLENGTH =
		settings.getInt("hbci.pin.maxlength",6);
	
	/**
	 * Minimale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MINLENGTH =
	  settings.getInt("hbci.pin.minlength",5);
	
	/**
   * Prueft die uebergebenen Strings auf Vorhandensein nicht erlaubter Zeichen.
   * @param chars zu testende Zeichen.
   * @throws ApplicationException
   */
  public final static void checkChars(String chars) throws ApplicationException
  {
    if (chars == null || chars.length() == 0)
      return;
    char[] c = chars.toCharArray();
    for (int i=0;i<c.length;++i)
    {
      if (HBCIProperties.HBCI_DTAUS_VALIDCHARS.indexOf(c[i]) == -1)
        throw new ApplicationException(i18n.tr("Das Zeichen \"{0}\" darf nicht verwendet werden",""+c[i])); 
    }
  }

  /**
   * Prueft die Gueltigkeit der BLZ/Kontonummer-Kombi anhand von Pruefziffern.
   * @see HBCIUtils#checkAccountCRC(java.lang.String, java.lang.String)
   * @param blz
   * @param kontonummer
   * @return true, wenn die Kombi ok ist.
   */
  public final static boolean checkAccountCRC(String blz, String kontonummer)
  {
    if (!de.willuhn.jameica.hbci.Settings.getKontoCheck())
      return true;
    try
    {
      return HBCIUtils.checkAccountCRC(blz, kontonummer);
    }
    catch (Exception e)
    {
      Logger.warn("HBCI4Java subsystem seems to be not initialized for this thread group, adding thread group");
      HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
      HBCIUtils.initThread(null,null,plugin.getHBCICallback());
      return HBCIUtils.checkAccountCRC(blz, kontonummer);
    }
  }

	// disabled
	private HBCIProperties()
	{
	}

}


/**********************************************************************
 * $Log: HBCIProperties.java,v $
 * Revision 1.11  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.10  2005/06/07 22:19:57  web0
 * @B bug 49
 *
 * Revision 1.9  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.8  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.7  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.6  2005/03/25 23:08:44  web0
 * @B bug 28
 *
 * Revision 1.5  2005/03/09 01:16:17  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.3  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 **********************************************************************/