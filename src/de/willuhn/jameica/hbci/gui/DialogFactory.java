/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/03/06 18:25:10 $
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
   * @param headline Ueberschrift des Dialogs.
   * @param text Text des Dialogs.
   */
  public static synchronized void openSimple(final String headline, final String text)
	{
		check();
		SimpleDialog d = new SimpleDialog(AbstractDialog.POSITION_CENTER);
		d.setTitle(headline);
		d.setText(text);
		dialog = d;
		try {
			d.open();
		}
		catch (Exception e)
		{
			Application.getLog().error(e.getLocalizedMessage(),e);
			GUI.setActionText(e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
		finally
		{
			close();
		}
	}

	/**
	 * Erzeugt den PIN-Dialog.
	 * Hinweis: Wirft eine RuntimeException, wenn der PIN-Dialog abgebrochen
	 * oder die PIN drei mal falsch eingegeben wurde (bei aktivierter Checksummen-Pruefung).
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
	 * @return die eingegebene PIN.
	 */
	public static synchronized String getPIN()
	{
		check();
		dialog = new PINDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		catch (Exception e)
		{
			Application.getLog().error(e.getLocalizedMessage(),e);
			GUI.setActionText(e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
		finally
		{
			close();
		}
	}

	/**
   * Prueft, ob der Dialog geoeffnet werden kann.
   */
  private static synchronized void check()
	{
		if (dialog == null)
			return;

		Application.getLog().error("alert: there's another opened dialog");
		throw new RuntimeException("alert: there's another opened dialog");
	}

	/**
   * Schliesst den gerade offenen Dialog.
   */
  public static synchronized void close()
	{
		if (dialog == null)
			return;
		try {
			dialog.close();
		}
		finally
		{
			dialog = null;
		}
	}

}


/**********************************************************************
 * $Log: DialogFactory.java,v $
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.7  2004/02/24 22:47:05  willuhn
 * @N GUI refactoring
 *
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