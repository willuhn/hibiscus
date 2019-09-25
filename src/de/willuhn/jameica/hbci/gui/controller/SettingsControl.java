/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.experiments.Feature;
import de.willuhn.jameica.hbci.experiments.FeatureService;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypTree;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
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
  private CheckboxInput cachePin          = null;
  private CheckboxInput storePin          = null;
  private CheckboxInput decimalGrouping   = null;
  private CheckboxInput kontoCheck        = null;
  private CheckboxInput excludeAddresses  = null;
  private CheckboxInput boldValues        = null;

	private Input buchungSollFg     				= null;
	private Input buchungHabenFg    				= null;

  private UmsatzTypTree umsatzTypTree     = null;

  private Input ueberweisungLimit         = null;
  
  private TablePart experiments           = null;
  
  /**
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
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
   * Checkbox zur fett gedruckten Darstellung von Geldbetraegen.
   * @return Checkbox.
   */
  public CheckboxInput getBoldValues()
  {
    if (boldValues == null)
      boldValues = new CheckboxInput(Settings.getBoldValues());
    return boldValues;
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
   * Liefert eine Tabelle zum Einstellen experimenteller Funktionen im Nightly-Build.
   * @return eine Tabelle zum Einstellen experimenteller Funktionen im Nightly-Build.
   * Liefert NULL, wenn es kein Nightly-Build ist.
   * @throws RemoteException
   */
  public TablePart getExperiments() throws RemoteException
  {
    if(this.experiments != null)
      return this.experiments;
    
    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final FeatureService fs = bs.get(FeatureService.class);
    if (!fs.enabled())
      return null;
    
    this.experiments = new TablePart(null);
    this.experiments.setCheckable(true);
    this.experiments.addColumn(i18n.tr("Name"),"name");
    this.experiments.addColumn(i18n.tr("Beschreibung"),"description");
    this.experiments.setRememberColWidths(true);
    this.experiments.setRememberOrder(true);
    this.experiments.removeFeature(FeatureSummary.class);
    
    for (Feature f:fs.getFeatures())
    {
      this.experiments.addItem(f);
    }
    
    this.experiments.setFormatter(new TableFormatter() {
      
      public void format(TableItem item)
      {
        if (item == null)
          return;
        
        Feature f = (Feature) item.getData();
        if (f == null)
          return;
        
        item.setChecked(fs.isEnabled(f));
      }
    });
    
    return this.experiments;
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

    Settings.setDecimalGrouping(((Boolean)getDecimalGrouping().getValue()).booleanValue());
    Settings.setBoldValues(((Boolean)getBoldValues().getValue()).booleanValue());
    Settings.setKontoCheck(((Boolean)getKontoCheck().getValue()).booleanValue());
    Settings.setKontoCheckExcludeAddressbook(((Boolean)getKontoCheckExcludeAddressbook().getValue()).booleanValue());

    boolean storeEnabled = ((Boolean)getStorePin().getValue()).booleanValue();
    boolean cacheEnabled = ((Boolean)getCachePin().getValue()).booleanValue();
    
    Settings.setCachePin(cacheEnabled);
    Settings.setStorePin(storeEnabled);

    // Cache und Store leeren, wenn das Feature deaktiviert wurde
    if (!cacheEnabled) DialogFactory.clearPINCache(null);
    if (!storeEnabled) DialogFactory.clearPINStore(null);
    
    Double limit = (Double) getUeberweisungLimit().getValue();

    Settings.setUeberweisungLimit(limit == null ? 0.0d : limit.doubleValue());
		
    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final FeatureService fs = bs.get(FeatureService.class);
		if (fs.enabled())
		{
		  try
		  {
	      final TablePart table = this.getExperiments();
	      final List<Feature> all = table.getItems(false);
	      final List<Feature> selected = table.getItems(true);
	      for (Feature f:all)
	      {
	        fs.setEnabled(f,selected.contains(f));
	      }
		  }
		  catch (Exception e)
		  {
		    Logger.error("unable to store feature",e);
		    Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Speichern der experimentellen Features fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
		  }
		}

		GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
  }
}

