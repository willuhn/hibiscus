/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/04/27 22:23:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;
import de.willuhn.jameica.hbci.gui.views.Welcome;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.I18N;

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
			GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
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
			GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
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

  /**
   * Die Funktion ueberprueft, ob schon ein Passport eingerichtet ist.
   * Ist dies nicht der Fall, wird ein Dialog zur Erstellung eines
   * neuen erzeugt. Diese Funktion sollte ueberall dort aufgerufen
   * werden, wo vorm Aufbau des Dialogs feststehen muss, ob ein Passport
   * schon existiert.
   * @return true, wenn ein Passport existiert.
   */
  public static synchronized boolean checkPassport()
	{
		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		try {
			DBIterator passports = Settings.getDatabase().createList(Passport.class);
			if (passports.size() > 0)
				return true;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Kein Sicherheitsmedium"));
			d.setText(i18n.tr("Es ist noch kein Sicherheitsmedium eingerichtet. " +
				"Möchten Sie dies jetzt einrichten?"));
			Boolean choice = (Boolean) d.open();
			if (choice.booleanValue())
			{
				SettingsControl settings = new SettingsControl(null);
				settings.handleCreate();
			}
			else {
				GUI.startView(Welcome.class.getName(),null);
			}
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to check/create passport",e);
			GUI.getStatusBar().setErrorText("Fehler beim Prüfen des Sicherheitsmediums");
		}
		return false;
	}

}


/**********************************************************************
 * $Log: DialogFactory.java,v $
 * Revision 1.11  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.10  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
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