/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/02/20 20:45:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory {

	private static AbstractDialog dialog = null;

  /**
	 * Erzeugt einen simplen Dialog mit einem OK-Button.
   * @param headline Ueberschrift des Dialogs.
   * @param text Text des Dialogs.
   */
  public static void openSimple(final String headline, final String text)
	{
		SimpleDialog d = new SimpleDialog(AbstractDialog.POSITION_CENTER);
		d.setTitle(headline);
		d.setText(text);
		dialog = (AbstractDialog) d;
		d.open();
	}

	/**
	 * Erzeugt einen Passwort-Dialog.
	 * @param headline Ueberschrift des Dialogs.
	 * @param text Text des Dialogs.
	 */
	public static String openPassword(final String headline, final String text)
	{
		PasswordDialog d = new PasswordDialog(AbstractDialog.POSITION_CENTER);
		d.setTitle(headline);
		d.setText(text);
		dialog = (AbstractDialog) d;
		return d.getPassword();
	}


	/**
   * Schliesst den gerade offenen Dialog.
   */
  public static void close()
	{
		if (dialog == null)
			return;
		dialog.close();
	}

}


/**********************************************************************
 * $Log: DialogFactory.java,v $
 * Revision 1.4  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 **********************************************************************/