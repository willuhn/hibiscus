/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SettingsControl.java,v $
 * $Revision: 1.38 $
 * $Date: 2005/06/06 09:54:39 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Color;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen.
 */
public class SettingsControl extends AbstractControl {

	// Eingabe-Felder
	private CheckboxInput onlineMode     		= null;
	private CheckboxInput checkPin     			= null;
  private CheckboxInput showTan           = null;

	private Input buchungSollFg     				= null;
	private Input buchungHabenFg    				= null;
	private Input ueberfaelligFg    				= null;

	private TablePart passportList 					= null;

	private Input ueberweisungLimit 				= null;

	private I18N i18n;

  /**
   * @param view
   */
  public SettingsControl(AbstractView view) {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
		passportList = new TablePart(PseudoIterator.fromArray(p),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	// Wir haben hier nicht direkt die Passports sondern noch einen
      	// GenericObject-Wrapper drum rum (Passport-Objekt). Die Huelle
      	// entfernen wir vorher noch
      	PassportObject o = (PassportObject) context;
      	if (o == null)
      		return;
				new PassportDetail().handleAction(o.getPassport());
      }
    });
		passportList.addColumn(i18n.tr("Bezeichnung"),"name");
		return passportList;
	}

	/**
	 * Checkbox zur Auswahl des Online-Mode.
   * @return Checkbox.
   */
  public CheckboxInput getOnlineMode()
	{
		if (onlineMode != null)
			return onlineMode;
		onlineMode = new CheckboxInput(Settings.getOnlineMode());
		return onlineMode;
	}

	/**
	 * Liefert eine Checkbox zur Aktivierung oder Deaktivierung der Pin-Pruefung via Checksumme.
   * @return Checkbox.
   */
  public CheckboxInput getCheckPin()
	{
		if (checkPin != null)
			return checkPin;
		checkPin = new CheckboxInput(Settings.getCheckPin());
		return checkPin;
	}

  /**
   * Liefert eine Checkbox zur Aktivierung oder Deaktivierung der TAN-Anzeige waehrend der Eingabe.
   * @return Checkbox.
   */
  public CheckboxInput getShowTan()
  {
    if (showTan != null)
      return showTan;
    showTan = new CheckboxInput(Settings.getShowTan());
    return showTan;
  }

	/**
	 * Eingabe-Feld fuer ein Limit bei Ueberweisungen.
   * @return Eingabe-Feld.
   */
  public Input getUeberweisungLimit()
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
   */
  public Input getBuchungSollForeground()
	{
		if (buchungSollFg != null)
			return buchungSollFg;
		buchungSollFg = new ColorInput(Settings.getBuchungSollForeground());
		return buchungSollFg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von Haben-Buchungen. 
	 * @return Auswahlfeld.
	 */
	public Input getBuchungHabenForeground()
	{
		if (buchungHabenFg != null)
			return buchungHabenFg;
		buchungHabenFg = new ColorInput(Settings.getBuchungHabenForeground());
		return buchungHabenFg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von ueberfaelligen Ueberweisungen.
	 * @return Auswahlfeld.
	 */
	public Input getUeberfaelligForeground()
	{
		if (ueberfaelligFg != null)
			return ueberfaelligFg;
		ueberfaelligFg = new ColorInput(Settings.getUeberfaelligForeground());
		return ueberfaelligFg;
	}

  /**
   * Speichert die Einstellungen.
   */
  public void handleStore() {
		Color hf = (Color)getBuchungHabenForeground().getValue();
		Color sf = (Color)getBuchungSollForeground().getValue();
		Color uf = (Color)getUeberfaelligForeground().getValue();

		Settings.setBuchungHabenForeground(hf.getRGB());
		Settings.setBuchungSollForeground(sf.getRGB());
		Settings.setUeberfaelligForeground(uf.getRGB());

		Settings.setOnlineMode(((Boolean)getOnlineMode().getValue()).booleanValue());
		Settings.setCheckPin(((Boolean)getCheckPin().getValue()).booleanValue());
    Settings.setShowTan(((Boolean)getShowTan().getValue()).booleanValue());
		
		Settings.setUeberweisungLimit(((Double)getUeberweisungLimit().getValue()).doubleValue());

		GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
  }

	/**
   * Loescht den gegebenenfalls vorhandenen gespeicherten Pin-Hash.
   */
  public void handleDeleteCheckSum()
	{
		YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
		d.setTitle(i18n.tr("Sicher?"));
		d.setText(i18n.tr("Möchten Sie die gespeicherten Checksummen wirklich löschen?"));
		try {
			if (!((Boolean)d.open()).booleanValue())
				return;
		}
		catch (Exception e)
		{
			Logger.error("error while getting data from yes/no dialog",e);
			return;
		}

		try
		{
			Settings.getWallet().delete("hbci.passport.pinchecksum");
			GUI.getStatusBar().setSuccessText(i18n.tr("Checksummen gelöscht."));
		}
		catch (Exception e)
		{
			Logger.error("error while deleting checksums",e);
		}
	}
}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.38  2005/06/06 09:54:39  web0
 * *** empty log message ***
 *
 * Revision 1.37  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.36  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/10/21 13:59:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.32  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.30  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.29  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
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