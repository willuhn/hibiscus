/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SettingsControl.java,v $
 * $Revision: 1.61 $
 * $Date: 2011/06/30 16:29:41 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.parts.PassportList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypTree;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
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
  private CheckboxInput storePin          = null;
  private CheckboxInput decimalGrouping   = null;
  private CheckboxInput kontoCheck        = null;
  private CheckboxInput excludeAddresses  = null;

	private Input buchungSollFg     				= null;
	private Input buchungHabenFg    				= null;

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
    {
      kontoCheck = new CheckboxInput(Settings.getKontoCheck());
      Listener l = new Listener() {
        public void handleEvent(Event event)
        {
          getKontoCheckExcludeAddressbook().setEnabled(((Boolean)kontoCheck.getValue()).booleanValue());
        }
      };
      kontoCheck.addListener(l);
      
      // einmal initial ausloesen
      l.handleEvent(null);
    }
    return kontoCheck;
  }
  
  /**
   * Checkbox, mit der Bankverbindungen aus dem Adressbuch aus der Pruefung ausgenommen werden koennen.
   * @return Checkbox.
   */
  public CheckboxInput getKontoCheckExcludeAddressbook()
  {
    if (this.excludeAddresses == null)
      this.excludeAddresses = new CheckboxInput(Settings.getKontoCheckExcludeAddressbook());
    return this.excludeAddresses;
  }

  /**
   * Liefert eine Checkbox zur Aktivierung oder Deaktivierung des Pin-Caches.
   * @return Checkbox.
   */
  public CheckboxInput getCachePin()
  {
    if (this.cachePin != null)
      return this.cachePin;

    Listener l = new Listener() {
      public void handleEvent(Event event)
      {
        boolean b1 = (Boolean) getCachePin().getValue();
        boolean b2 = Application.getStartupParams().getPassword() == null;
        getStorePin().setEnabled(b1 && b2);
        
        if (!b2)
          getStorePin().setComment(i18n.tr("Deaktiviert. Master-Passwort nicht manuell eingegeben"));
      }
    };
    this.cachePin = new CheckboxInput(Settings.getCachePin());
    this.cachePin.addListener(l);
    
    // einmal ausloesen
    l.handleEvent(null);
    return this.cachePin;
  }
  
  /**
   * Liefert eine Checkbox zum Aktivieren der permanenten Speicherung der PINs.
   * @return true, wenn das Speichern der PINs aktiviert ist.
   */
  public CheckboxInput getStorePin()
  {
    if (storePin != null)
      return storePin;
    
    storePin = new CheckboxInput(Settings.getStorePin());
    storePin.setComment("");
    storePin.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Wir loesen nur bei dem Selection-Event aus, nicht bei FocusOut/FocusIn
        if (event.type != SWT.Selection)
          return;
        
        boolean enabled = ((Boolean) storePin.getValue()).booleanValue();
        if (enabled)
        {
          boolean b = false;
          try
          {
            YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
            d.setTitle(i18n.tr("Warnung"));
            d.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
            d.setText(i18n.tr("Mit der permanenten Speicherung der PIN verstoﬂen Sie unter Umst‰nden\n" +
                              "gegen die Onlinebanking-AGB Ihres Geldinstitutes. Bitte wenden Sie sich\n" +
                              "an Ihre Bank und fragen Sie diese, ob das Speichern der PIN zul‰ssig ist.\n\n" +
                              "Nach Aktivierung dieser Funktion erhalten Sie vom Programm-Autor von\n" +
                              "Hibiscus keine Hilfe mehr bei Fragen oder Problemen.\n\n" +
                              "PIN-Speicherung wirklich aktivieren?"));
            b = ((Boolean) d.open()).booleanValue();
          }
          catch (OperationCanceledException oce)
          {
            // ignore
          }
          catch (Exception e)
          {
            Logger.error("unable to open dialog",e);
          }
          getStorePin().setValue(b);
        }
      }
    });
    return storePin;
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
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {
		Color hf = (Color)getBuchungHabenForeground().getValue();
		Color sf = (Color)getBuchungSollForeground().getValue();

		Settings.setBuchungHabenForeground(hf.getRGB());
		Settings.setBuchungSollForeground(sf.getRGB());

		Settings.setOnlineMode(((Boolean)getOnlineMode().getValue()).booleanValue());
    Settings.setDecimalGrouping(((Boolean)getDecimalGrouping().getValue()).booleanValue());
    Settings.setKontoCheck(((Boolean)getKontoCheck().getValue()).booleanValue());
    Settings.setKontoCheckExcludeAddressbook(((Boolean)getKontoCheckExcludeAddressbook().getValue()).booleanValue());
    Settings.setCancelSyncOnError(((Boolean)getCancelSyncOnError().getValue()).booleanValue());

    boolean storeEnabled = ((Boolean)getStorePin().getValue()).booleanValue();
    boolean cacheEnabled = ((Boolean)getCachePin().getValue()).booleanValue();
    
    Settings.setCachePin(cacheEnabled);
    Settings.setStorePin(storeEnabled);

    // Cache und Store leeren, wenn das Feature deaktiviert wurde
    if (!cacheEnabled) DialogFactory.clearPINCache(null);
    if (!storeEnabled) DialogFactory.clearPINStore(null);
    
    Double limit = (Double) getUeberweisungLimit().getValue();
		Settings.setUeberweisungLimit(limit == null ? 0.0d : limit.doubleValue());

		GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
  }
}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.61  2011/06/30 16:29:41  willuhn
 * @N Unterstuetzung fuer neues UnreadCount-Feature
 *
 * Revision 1.60  2011-05-25 10:05:49  willuhn
 * @N Im Fehlerfall nur noch die PINs/Passwoerter der betroffenen Passports aus dem Cache loeschen. Wenn eine PIN falsch ist, muss man jetzt nicht mehr alle neu eingeben
 *
 * Revision 1.59  2011-05-25 08:53:31  willuhn
 * @N Cache und Store leeren, wenn die Features deaktiviert wurden
 *
 * Revision 1.58  2011-05-23 12:57:37  willuhn
 * @N optionales Speichern der PINs im Wallet. Ich announce das aber nicht. Ich hab das nur eingebaut, weil mir das Gejammer der User auf den Nerv ging und ich nicht will, dass sich User hier selbst irgendwelche Makros basteln, um die PIN dennoch zu speichern
 *
 * Revision 1.57  2011-04-28 07:33:23  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.56  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 **********************************************************************/