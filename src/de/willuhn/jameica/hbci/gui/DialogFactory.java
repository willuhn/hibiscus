/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/12 23:46:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import de.willuhn.jameica.gui.views.Dialog;
import de.willuhn.jameica.gui.views.SimpleDialog;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory {

	private static Dialog dialog = null;

  /**
	 * Erzeugt einen simplen Dialog mit einem OK-Button.
   * @param headline Ueberschrift des Dialogs.
   * @param text Text des Dialogs.
   */
  public static void openSimple(final String headline, final String text)
	{
		SimpleDialog d = new SimpleDialog();
		d.setTitle(headline);
		d.setText(text);
		dialog = (Dialog) d;
		d.open();
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
 * Revision 1.1  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 **********************************************************************/