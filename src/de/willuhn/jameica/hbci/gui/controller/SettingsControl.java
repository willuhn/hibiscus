/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SettingsControl.java,v $
 * $Revision: 1.57 $
 * $Date: 2011/04/28 07:33:23 $
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

import org.eclipse.swt.graphics.Color;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.parts.PassportList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypTree;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen.
 */
public class SettingsControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	// Eingabe-Felder
	private CheckboxInput onlineMode     		= null;
  private CheckboxInput cancelSyncOnError = null;
  private CheckboxInput cachePin          = null;
  private CheckboxInput decimalGrouping   = null;
  private CheckboxInput kontoCheck        = null;

	private Input buchungSollFg     				= null;
	private Input buchungHabenFg    				= null;
  private Input ueberfaelligFg            = null;

  private TablePart passportList          = null;
  private UmsatzTypTree umsatzTypTree     = null;

  private Input ueberweisungLimit         = null;

  /**
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert eine Tabelle mit den existierenden Passports.
   * @return Tabelle mit den Passports.
   * @throws RemoteException
   */
  public TablePart getPassportListe() throws RemoteException
	{
    if (passportList == null)
      passportList = new PassportList(new PassportDetail());
		return passportList;
  }

  /**
   * Liefert einen Tree mit den existierenden Umsatz-Kategorien.
   * @return Tree mit den Umsatz-Kategorien.
   * @throws RemoteException
   */
  public UmsatzTypTree getUmsatzTypTree() throws RemoteException
  {
    if (umsatzTypTree == null)
      umsatzTypTree = new UmsatzTypTree(new UmsatzTypNew());
    return umsatzTypTree;
  }

  /**
	 * Checkbox zur Auswahl des Online-Mode.
   * @return Checkbox.
   */
  public CheckboxInput getOnlineMode()
	{
		if (onlineMode == null)
      onlineMode = new CheckboxInput(Settings.getOnlineMode());
		return onlineMode;
	}

  /**
   * Checkbox zur Auswahl das Abbruches der Synchronisierung bei Fehler.
   * @return Checkbox.
   */
  public CheckboxInput getCancelSyncOnError()
  {
    if (cancelSyncOnError == null)
      cancelSyncOnError = new CheckboxInput(Settings.getCancelSyncOnError());
    return cancelSyncOnError;
  }

  /**
   * Checkbox zur Auswahl von Dezimal-Trennzeichen in Betraegen.
   * @return Checkbox.
   */
  public CheckboxInput getDecimalGrouping()
  {
    //  BUGZILLA 101 http://www.willuhn.de/bugzilla/show_bug.cgi?id=101
    if (decimalGrouping == null)
      decimalGrouping = new CheckboxInput(Settings.getDecimalGrouping());
    return decimalGrouping;
  }

  /**
   * Checkbox zur Aktivierung der Pruefziffernberechnung bei Kontonummern.
   * @return Checkbox.
   */
  public CheckboxInput getKontoCheck()
  {
    if (kontoCheck == null)
      kontoCheck = new CheckboxInput(Settings.getKontoCheck());
    return kontoCheck;
  }

  /**
   * Liefert eine Checkbox zur Aktivierung oder Deaktivierung des Pin-Caches.
   * @return Checkbox.
   */
  public CheckboxInput getCachePin()
  {
    if (cachePin == null)
      cachePin = new CheckboxInput(Settings.getCachePin());
    return cachePin;
  }

  /**
	 * Eingabe-Feld fuer ein Limit bei Ueberweisungen.
   * @return Eingabe-Feld.
   */
  public Input getUeberweisungLimit()
	{
		if (ueberweisungLimit == null)
		{
	    ueberweisungLimit = new DecimalInput(Settings.getUeberweisungLimit(),HBCI.DECIMALFORMAT);
	    ueberweisungLimit.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
		}
		return ueberweisungLimit;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von Soll-Buchungen. 
   * @return Auswahlfeld.
   */
  public Input getBuchungSollForeground()
	{
		if (buchungSollFg == null)
  		buchungSollFg = new ColorInput(Settings.getBuchungSollForeground(),true);
		return buchungSollFg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von Haben-Buchungen. 
	 * @return Auswahlfeld.
	 */
	public Input getBuchungHabenForeground()
	{
		if (buchungHabenFg == null)
  		buchungHabenFg = new ColorInput(Settings.getBuchungHabenForeground(),true);
		return buchungHabenFg;
	}

	/**
	 * Liefert ein Auswahlfeld fuer die Vordergrundfarbe von ueberfaelligen Ueberweisungen.
	 * @return Auswahlfeld.
	 */
	public Input getUeberfaelligForeground()
	{
		if (ueberfaelligFg == null)
      ueberfaelligFg = new ColorInput(Settings.getUeberfaelligForeground(),true);
		return ueberfaelligFg;
	}

  /**
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {
		Color hf = (Color)getBuchungHabenForeground().getValue();
		Color sf = (Color)getBuchungSollForeground().getValue();
		Color uf = (Color)getUeberfaelligForeground().getValue();

		Settings.setBuchungHabenForeground(hf.getRGB());
		Settings.setBuchungSollForeground(sf.getRGB());
		Settings.setUeberfaelligForeground(uf.getRGB());

		Settings.setOnlineMode(((Boolean)getOnlineMode().getValue()).booleanValue());
    Settings.setCachePin(((Boolean)getCachePin().getValue()).booleanValue());
    Settings.setDecimalGrouping(((Boolean)getDecimalGrouping().getValue()).booleanValue());
    Settings.setKontoCheck(((Boolean)getKontoCheck().getValue()).booleanValue());
    Settings.setCancelSyncOnError(((Boolean)getCancelSyncOnError().getValue()).booleanValue());
		
    Double limit = (Double) getUeberweisungLimit().getValue();
		Settings.setUeberweisungLimit(limit == null ? 0.0d : limit.doubleValue());

		GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
  }
}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.57  2011/04/28 07:33:23  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.56  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 **********************************************************************/