/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SettingsControl.java,v $
 * $Revision: 1.15 $
 * $Date: 2004/04/27 22:23:56 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.NewPassportDialog;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen.
 */
public class SettingsControl extends AbstractControl {

	// Eingabe-Felder
	private CheckboxInput onlineMode     		= null;
	private CheckboxInput checkPin     			= null;

	private AbstractInput buchungSollFg     = null;
	private AbstractInput buchungSollBg     = null;
	private AbstractInput buchungHabenFg    = null;
	private AbstractInput buchungHabenBg    = null;

	private AbstractInput ueberfaelligFg    = null;
	private AbstractInput ueberfaelligBg		= null;

	private TablePart passportList 							= null;
	
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
  public TablePart getPassportListe() throws RemoteException
	{
    if (passportList != null)
      	return passportList;

    DBIterator list = Settings.getDatabase().createList(Passport.class);

		passportList = new TablePart(list,this);
		passportList.addColumn(i18n.tr("Bezeichnung"),"name");
		passportList.addColumn(i18n.tr("Typ"),"passport_type_id");
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
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von Soll-Buchungen. 
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public AbstractInput getBuchungSollForeground() throws RemoteException
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
	public AbstractInput getBuchungSollBackground() throws RemoteException
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
	public AbstractInput getBuchungHabenForeground() throws RemoteException
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
	public AbstractInput getBuchungHabenBackground() throws RemoteException
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
	public AbstractInput getUeberfaelligBackground() throws RemoteException
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
	public AbstractInput getUeberfaelligForeground() throws RemoteException
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

			// Wir gehen nochmal auf Nummer sicher, dass die Pruefsummen-Algorithmen vorhanden sind
			new CheckPinListener().handleEvent(null);
			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while storing settings",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen"));
		}
  }

	/**
   * Loescht den gegebenenfalls vorhandenen gespeicherten Pin-Hash.
   */
  public void handleDeleteCheckSum()
	{
		Settings.setCheckSum(null);
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  	// Hier wird ein neuer Passport erstellt.
		NewPassportDialog d = new NewPassportDialog(NewPassportDialog.POSITION_MOUSE);
		PassportType pt = null;
		try {
			pt = (PassportType) d.open();
		}
		catch (Exception e)
		{
			// Der User hat "abbrechen" im Dialog gedrueckt
			return;
		}
		try {
			// wir erzeugen einen neuen Passport
			Passport p = (Passport) de.willuhn.jameica.hbci.Settings.getDatabase().createObject(Passport.class,null);
			
			// weisen ihm den korrekten Typ zu
			p.setPassportType(pt);

			// Lassen ihn anschliessend auf die passende Impl casten
			p = HBCIFactory.getInstance().findImplementor(p);

			// und oeffnen ihn in dem Dialog, der fuer diesen Typ hinterlegt ist
			GUI.startView(pt.getAbstractView(),p);
		}
		catch (Exception e)
		{
			Application.getLog().error("error while creating new passport",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden des Sicherheitsmediums"));
		}

  }

	/**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o)
	{
		// Hier wird der ausgewaehlte Passport geoeffnet.
		try {
			Passport p = (Passport) o;
			PassportType pt = p.getPassportType();
			
			// bevor wir den Passport an die View geben, muessen wir ihn noch
			// auf die korrekte Impl casten lassen
			GUI.startView(pt.getAbstractView(),HBCIFactory.getInstance().findImplementor(p));
		}
		catch (Exception e)
		{
			Application.getLog().error("error while opening passport",e);
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