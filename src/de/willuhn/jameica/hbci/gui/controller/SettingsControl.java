/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SettingsControl.java,v $
 * $Revision: 1.27 $
 * $Date: 2004/07/09 00:04:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.GenericObject;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Controller fuer die Einstellungen.
 */
public class SettingsControl extends AbstractControl {

	// Eingabe-Felder
	private CheckboxInput onlineMode     		= null;
	private CheckboxInput checkPin     			= null;

	private Input buchungSollFg     = null;
	private Input buchungSollBg     = null;
	private Input buchungHabenFg    = null;
	private Input buchungHabenBg    = null;

	private Input ueberfaelligFg    = null;
	private Input ueberfaelligBg		= null;

	private TablePart passportList 					= null;

	private Input ueberweisungLimit = null;

	private I18N i18n;

  /**
   * @param view
   */
  public SettingsControl(AbstractView view) {
    super(view);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert eine Tabelle mit den existierenden Passports.
   * @return Tabelle mit den Passports.
   * @throws RemoteException
   */
  public Part getPassportListe() throws RemoteException
	{
    if (passportList != null)
      	return passportList;

		Passport[] passports = PassportRegistry.getPassports();

		GenericObject[] p = new GenericObject[passports.length];
		for (int i=0;i<passports.length;++i)
		{
			p[i] = new PassportObject(passports[i]);
		}
		passportList = new TablePart(PseudoIterator.fromArray(p),this);
		passportList.addColumn(i18n.tr("Bezeichnung"),"name");
		return passportList;
	}

	/**
	 * Checkbox zur Auswahl des Online-Mode.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getOnlineMode() throws RemoteException
	{
		if (onlineMode != null)
			return onlineMode;
		onlineMode = new CheckboxInput(Settings.getOnlineMode());
		return onlineMode;
	}

	/**
	 * Liefert eine Checkbox zur Aktivierung oder Deaktivierung der Pin-Pruefung via Checksumme.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getCheckPin() throws RemoteException
	{
		if (checkPin != null)
			return checkPin;
		checkPin = new CheckboxInput(Settings.getCheckPin());
		checkPin.addListener(new CheckPinListener());
		return checkPin;
	}

	/**
	 * Eingabe-Feld fuer ein Limit bei Ueberweisungen.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getUeberweisungLimit() throws RemoteException
	{
		if (ueberweisungLimit != null)
			return ueberweisungLimit;
		ueberweisungLimit = new DecimalInput(Settings.getUeberweisungLimit(),HBCI.DECIMALFORMAT);
		ueberweisungLimit.setComment(i18n.tr("in der Währung des jeweiligen Kontos"));
		return ueberweisungLimit;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von Soll-Buchungen. 
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public Input getBuchungSollForeground() throws RemoteException
	{
		if (buchungSollFg != null)
			return buchungSollFg;
		buchungSollFg = new ColorInput(Settings.getBuchungSollForeground());
		return buchungSollFg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Hintergrundfarbe von Soll-Buchungen. 
	 * @return Auswahlfeld.
	 * @throws RemoteException
	 */
	public Input getBuchungSollBackground() throws RemoteException
	{
		if (buchungSollBg != null)
			return buchungSollBg;
		buchungSollBg = new ColorInput(Settings.getBuchungSollBackground());
		return buchungSollBg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von Haben-Buchungen. 
	 * @return Auswahlfeld.
	 * @throws RemoteException
	 */
	public Input getBuchungHabenForeground() throws RemoteException
	{
		if (buchungHabenFg != null)
			return buchungHabenFg;
		buchungHabenFg = new ColorInput(Settings.getBuchungHabenForeground());
		return buchungHabenFg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Hintergrundfarbe von Haben-Buchungen. 
	 * @return Auswahlfeld.
	 * @throws RemoteException
	 */
	public Input getBuchungHabenBackground() throws RemoteException
	{
		if (buchungHabenBg != null)
			return buchungHabenBg;
		buchungHabenBg = new ColorInput(Settings.getBuchungHabenBackground());
		return buchungHabenBg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Hintergrundfarbe von ueberfaelligen Ueberweisungen.
	 * @return Auswahlfeld.
	 * @throws RemoteException
	 */
	public Input getUeberfaelligBackground() throws RemoteException
	{
		if (ueberfaelligBg != null)
			return ueberfaelligBg;
		ueberfaelligBg = new ColorInput(Settings.getUeberfaelligBackground());
		return ueberfaelligBg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von ueberfaelligen Ueberweisungen.
	 * @return Auswahlfeld.
	 * @throws RemoteException
	 */
	public Input getUeberfaelligForeground() throws RemoteException
	{
		if (ueberfaelligFg != null)
			return ueberfaelligFg;
		ueberfaelligFg = new ColorInput(Settings.getUeberfaelligForeground());
		return ueberfaelligFg;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
//  	GUI.startView(Welcome.class.getName(),null);
		GUI.startPreviousView();

  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
		try {
			Color hb = (Color)getBuchungHabenBackground().getValue();
			Color hf = (Color)getBuchungHabenForeground().getValue();
			Color sb = (Color)getBuchungSollBackground().getValue();
			Color sf = (Color)getBuchungSollForeground().getValue();

			Color ub = (Color)getUeberfaelligBackground().getValue();
			Color uf = (Color)getUeberfaelligForeground().getValue();

			Settings.setBuchungHabenBackground(hb.getRGB());
			Settings.setBuchungHabenForeground(hf.getRGB());
			Settings.setBuchungSollBackground(sb.getRGB());
			Settings.setBuchungSollForeground(sf.getRGB());
			Settings.setUeberfaelligBackground(ub.getRGB());
			Settings.setUeberfaelligForeground(uf.getRGB());

			Settings.setOnlineMode(((Boolean)getOnlineMode().getValue()).booleanValue());
			Settings.setCheckPin(((Boolean)getCheckPin().getValue()).booleanValue());
			
			Settings.setUeberweisungLimit(((Double)getUeberweisungLimit().getValue()).doubleValue());

			// Wir gehen nochmal auf Nummer sicher, dass die Pruefsummen-Algorithmen vorhanden sind
			new CheckPinListener().handleEvent(null);
			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while storing settings",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen"));
		}
  }

	/**
   * Loescht den gegebenenfalls vorhandenen gespeicherten Pin-Hash.
   */
  public void handleDeleteCheckSum()
	{
		if (Settings.getCheckSum() == null)
			return; // noch keine definiert
		YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
		d.setTitle(i18n.tr("Sicher?"));
		d.setText(i18n.tr("Möchten Sie die gespeicherte Checksumme wirklich löschen?"));
		try {
			if (!((Boolean)d.open()).booleanValue())
				return;
		}
		catch (Exception e)
		{
			Logger.error("error while getting data from yes/no dialog",e);
			return;
		}
		Settings.setCheckSum(null);
		GUI.getStatusBar().setSuccessText(i18n.tr("Checksumme gelöscht."));
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

	/**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o)
	{
		try {
			PassportObject p = (PassportObject) o;
			GUI.startView(p.getPassport().getConfigDialog().getName(),p.getPassport());
		}
		catch (Exception e)
		{
			Logger.error("error while opening passport",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden des Sicherheitsmediums"));
		}
	}

	/**
	 * Listener, der prueft, ob die Hash-Algorithmen zur Checksummen-Bildung
	 * verfuegbar sind.
   */
  private class CheckPinListener implements Listener
	{

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
			try {
				MessageDigest.getInstance("MD5");
				MessageDigest.getInstance("SHA1");
			}
			catch (NoSuchAlgorithmException e)
			{
				Settings.setCheckPin(false);
				try {
					getCheckPin().disable();
				}
				catch (RemoteException e1) {/*useless*/}
				GUI.getStatusBar().setErrorText(i18n.tr("Algorithmen zur Prüfsummenbildung auf diesem System nicht vorhanden"));
			}
    }
	}

}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.27  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.26  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/06/18 19:47:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/06/17 22:06:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.22  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.20  2004/05/11 23:31:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/05/11 21:11:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/05/09 17:39:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.15  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.14  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/04/13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.12  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.11  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.7  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.6  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.5  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/