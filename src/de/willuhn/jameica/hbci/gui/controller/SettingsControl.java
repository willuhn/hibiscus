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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.experiments.Feature;
import de.willuhn.jameica.hbci.experiments.FeatureService;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.parts.RangeList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypTree;
import de.willuhn.jameica.hbci.server.Range;
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

	private Input buchungSollFg     				= null;
	private Input buchungHabenFg    				= null;
  private CheckboxInput boldValues        = null;
	private CheckboxInput colorValues       = null;

  private UmsatzTypTree umsatzTypTree     = null;
  private Map<String,RangeList> ranges    = new HashMap<String,RangeList>();

  private Input ueberweisungLimit         = null;

  private CheckboxInput exFeatures        = null;
  private List<CheckboxInput> experiments = null;
  
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
   * Liefert eine Liste mit den verfuegbaren Zeitraum-Auswahlen fuer die angegebene Kategorie.
   * @param category Zeitraumkagegorie (Range.CATEGORY_ZAHLUNGSVERKEHR oder Range.CATEGORY_AUSWERTUNG)
   * @return Liste mit den Zeitr‰umen der gegebenen Kategorie.
   * @throws RemoteException 
   * */
  public RangeList getRanges(final String category) throws RemoteException
  {
    RangeList list = this.ranges.get(category);
    if (list != null)
      return list;
    
    list = new RangeList(category);
    this.ranges.put(category,list);
    return list;
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
   * Checkbox mit der festgelegt werden kann, dass nur der Betrag eingef‰rbt wird.
   * @return Checkbox.
   */
  public CheckboxInput getColorValues()
  {
    if (colorValues == null)
    {
      colorValues = new CheckboxInput(Settings.getColorValues());
      colorValues.setComment(i18n.tr("Deaktivieren, um die ganze Zeile farbig anzuzeigen"));
    }
    return colorValues;
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
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob die experimentellen Features aktiv sein sollen.
   * @return Checkbox.
   */
  public CheckboxInput getExFeatures()
  {
    if (this.exFeatures != null)
      return this.exFeatures;

    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final FeatureService fs = bs.get(FeatureService.class);

    this.exFeatures = new CheckboxInput(fs.enabled());
    this.exFeatures.setName(i18n.tr("Experimentelle Funktionen aktivieren"));
    final Listener l = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        boolean enabled = ((Boolean) getExFeatures().getValue()).booleanValue();
        for (CheckboxInput c:getExperiments())
        {
          c.setEnabled(enabled);
        }
      }
    };
    this.exFeatures.addListener(l);
    
    l.handleEvent(null); // Einmal initial ausloesen
    
    return this.exFeatures;
  }
  
  /**
   * Liefert eine Liste mit Checkboxen zum Einstellen experimenteller Funktionen.
   * @return eine Liste mit Checkboxen zum Einstellen experimenteller Funktionen.
   * Liefert NULL, wenn es kein Nightly-Build ist.
   */
  public List<CheckboxInput> getExperiments()
  {
    if (this.experiments != null)
      return this.experiments;
    
    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final FeatureService fs = bs.get(FeatureService.class);
    
    this.experiments = new ArrayList<CheckboxInput>();
    
    for (Feature f:fs.getFeatures())
    {
      CheckboxInput c = new CheckboxInput(fs.isEnabled(f));
      c.setData("feature",f);
      c.setData("description",f.getDescription() + "\n\n" + i18n.tr("Vorgabewert: {0}",i18n.tr(f.getDefault() ? "aktiviert" : "deaktiviert")));
      c.setName(f.getName());
      this.experiments.add(c);
    }
    
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
    boolean haveError = false;
    
		Color hf = (Color)getBuchungHabenForeground().getValue();
		Color sf = (Color)getBuchungSollForeground().getValue();

		Settings.setBuchungHabenForeground(hf.getRGB());
		Settings.setBuchungSollForeground(sf.getRGB());

    Settings.setDecimalGrouping(((Boolean)getDecimalGrouping().getValue()).booleanValue());
    Settings.setBoldValues(((Boolean)getBoldValues().getValue()).booleanValue());
    Settings.setColorValues(((Boolean)getColorValues().getValue()).booleanValue());
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
    final boolean ex = ((Boolean) this.getExFeatures().getValue()).booleanValue();
    boolean changed = ex != fs.enabled();
    fs.setEnabled(ex);
		if (ex)
		{
		  try
		  {
	      for (CheckboxInput c:this.getExperiments())
	      {
	        Feature f = (Feature) c.getData("feature");
	        fs.setEnabled(f,(Boolean) c.getValue());
	      }
		  }
		  catch (Exception e)
		  {
		    Logger.error("unable to store feature",e);
		    Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Speichern der experimentellen Features fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
		    haveError = true;
		  }
		}
		
    for (Entry<String,RangeList> n:this.ranges.entrySet())
    {
      try
      {
        final String cat = n.getKey();
        final RangeList table = n.getValue();
        Range.setActiveRanges(cat,table.getItems(true));
      }
      catch (Exception e)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Speichern der Zeitr‰ume fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
        haveError = true;
      }
    }

    if (changed)
    {
      try
      {
        GUI.getCurrentView().reload();
      }
      catch (Exception e)
      {
        Logger.error("unable to reload view",e);
      }
    }

    if (!haveError)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert."),StatusBarMessage.TYPE_SUCCESS));
  }
}

