/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/02/22 20:04:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory {

	private static AbstractDialog dialog = null;

  /**
	 * Erzeugt einen simplen Dialog mit einem OK-Button.
	 * Hinweis: Wirft eine RuntimeException, wenn der Dialog eine
	 * Exception wirft. Hintergrund: Der Dialog wurde aus dem HBCICallBack
	 * heraus aufgerufen und soll im Fehlerfall den HBCI-Vorgang abbrechen.
   * @param headline Ueberschrift des Dialogs.
   * @param text Text des Dialogs.
   */
  public static void openSimple(final String headline, final String text)
	{
		SimpleDialog d = new SimpleDialog(AbstractDialog.POSITION_CENTER);
		d.setTitle(headline);
		d.setText(text);
		dialog = (AbstractDialog) d;
		try {
			d.open();
		}
		catch (Exception e)
		{
			Application.getLog().error(e.getLocalizedMessage(),e);
			GUI.setActionText(e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt den PIN-Dialog.
	 */
	public static String getPIN()
	{
		PINDialog d = new PINDialog(AbstractDialog.POSITION_CENTER);
		dialog = (AbstractDialog) d;
		try {
			return d.getPassword();
		}
		catch (Exception e)
		{
			Application.getLog().error(e.getLocalizedMessage(),e);
			GUI.setActionText(e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
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
 * Revision 1.6  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
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