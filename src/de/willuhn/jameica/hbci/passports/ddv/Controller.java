/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv;

import java.io.File;
import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.passport.HBCIPassportChipcard;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.PassportTest;
import de.willuhn.jameica.hbci.gui.dialogs.AccountContainerDialog;
import de.willuhn.jameica.hbci.gui.input.HBCIVersionInput;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.passports.ddv.server.PassportHandleImpl;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class Controller extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private TablePart configList      = null;
  private TablePart kontoList       = null;

  // Eingabe-Felder
  private TextInput pcscName        = null;
  private TextInput bezeichnung     = null;
  private SelectInput port          = null;
  private Input ctNumber            = null;
  private Input entryIndex          = null;
  private FileInput ctapi           = null;
  private CheckboxInput useSoftPin  = null;
  private SelectInput readerPresets = null;
  private SelectInput hbciVersion   = null;

  /**
   * ct.
   * @param view
   */
  public Controller(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert die aktuelle Config.
   * @return die aktuelle Config.
   */
  private DDVConfig getConfig()
  {
    Object o = getCurrentObject();
    if (o instanceof DDVConfig)
      return (DDVConfig) o;
    return null;
  }

  /**
   * Liefert eine Liste mit den existierenden Konfigurationen.
   * @return Liste der Konfigurationen.
   * @throws RemoteException
   */
  public TablePart getConfigList() throws RemoteException
  {
    if (this.configList != null)
      return this.configList;

    this.configList = new TablePart(DDVConfigFactory.getConfigs(),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        GUI.startView(Detail.class,context);
      }
    });
    this.configList.addColumn(i18n.tr("Alias-Name"),"name");
    this.configList.addColumn(i18n.tr("Kartenleser"),"readerPreset");
    this.configList.addColumn(i18n.tr("Index des HBCI-Zugangs"),"entryIndex");

    ContextMenu ctx = new ContextMenu();

    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (context == null)
          return;
        try
        {
          GUI.startView(Detail.class,context);
        }
        catch (Exception e) {
          Logger.error("error while loading config",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Anlegen der Konfiguration"));
        }
      }
    },"document-open.png"));

    ctx.addItem(new ContextMenuItem(i18n.tr("Neue Konfiguration..."),new Action() {
      public void handleAction(Object context) throws ApplicationException {handleCreate();}
    },"document-new.png"));

    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),new Action() {
      public void handleAction(Object context) throws ApplicationException {handleDelete((DDVConfig)context);}
    },"user-trash-full.png"));

    this.configList.setContextMenu(ctx);
    this.configList.setMulti(false);
    this.configList.setRememberColWidths(true);
    this.configList.setRememberOrder(true);
    this.configList.setSummary(false);
    return this.configList;
  }

  /**
   * Liefert eine Tabelle mit festzuordenbaren Konten.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public TablePart getKontoAuswahl() throws RemoteException
  {
    if (kontoList == null)
      this.kontoList = new KontoList(this.getConfig());
    return kontoList;
  }

  /**
   * Liefert eine Auswahl-Box fuer die HBCI-Version.
   * @return Auswahl-Box.
   * @throws RemoteException
   */
  public SelectInput getHBCIVersion() throws RemoteException
  {
    if (this.hbciVersion == null)
      this.hbciVersion = new HBCIVersionInput(null,getConfig().getHBCIVersion());
    return this.hbciVersion;
  }

  /**
   * Liefert eine Auswahl von vorkonfigurierten Chipkartenlesern.
   * @return Auswahl von vorkonfigurierten Lesern.
   */
  public SelectInput getReaderPresets()
  {
    if (this.readerPresets != null)
      return this.readerPresets;
    
    Reader reader = getConfig().getReaderPreset();
    this.readerPresets = new SelectInput(DDVConfigFactory.getReaderPresets(),reader);
    this.readerPresets.setAttribute("name");
    this.readerPresets.setName(i18n.tr("Kartenleser"));
    this.readerPresets.setEditable(false);
    this.readerPresets.addListener(new PresetListener());
    return this.readerPresets;
  }
  
  /**
   * Liefert true, wenn das aktuelle Preset ein PCSC-Kartenleser ist.
   * @return true, wenn das aktuelle Preset ein PCSC-Kartenleser ist.
   */
  private boolean isPCSC()
  {
    Reader r = (Reader) getReaderPresets().getValue();
    return r.getType().isPCSC();
  }
  
  /**
   * Liefert eine Datei-Auswahl fuer den CTAPI-Treiber.
   * @return Auswahl-Feld.
   */
  public Input getCTAPI()
  {
    if (this.ctapi != null)
      return this.ctapi;
    this.ctapi = new FileInput(getConfig().getCTAPIDriver());
    this.ctapi.setName(i18n.tr("CTAPI Treiber-Datei"));
    this.ctapi.setEnabled(!isPCSC());
    this.ctapi.setMandatory(!isPCSC());
    return this.ctapi;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Port.
   * @return Eingabe-Feld.
   */
  public SelectInput getPort()
  {
    if (this.port != null)
      return this.port;

    this.port = new SelectInput(DDVConfig.PORTS, getConfig().getPort());
    this.port.setEnabled(!isPCSC());
    this.port.setComment(i18n.tr("meist COM/USB"));
    this.port.setName(i18n.tr("Port des Lesers"));
    return this.port;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Nummer des Lesers.
   * @return Eingabe-Feld.
   */
  public Input getCTNumber()
  {
    if (this.ctNumber != null)
      return this.ctNumber;

    this.ctNumber = new SpinnerInput(0,4,getConfig().getCTNumber());
    this.ctNumber.setEnabled(!isPCSC());
    this.ctNumber.setName(i18n.tr("Index des Lesers"));
    this.ctNumber.setComment(i18n.tr("meist 0"));
    return this.ctNumber;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Bezeichnung.
   * @return Bezeichnung.
   */
  public Input getBezeichnung()
  {
    if (this.bezeichnung != null)
      return this.bezeichnung;
    this.bezeichnung = new TextInput(getConfig().getName());
    this.bezeichnung.setComment(i18n.tr("Angabe optional"));
    this.bezeichnung.setName(i18n.tr("Alias-Name"));
    return this.bezeichnung;
  }

  /**
   * Liefert ein Eingabe-Feld fuer den Namen des Kartenlesers bei PCSC.
   * @return Bezeichnung.
   */
  public Input getPCSCName()
  {
    if (this.pcscName != null)
      return this.pcscName;
    this.pcscName = new TextInput(getConfig().getPCSCName());
    this.pcscName.setHint(i18n.tr("nur eingeben, wenn mehrere Leser vorhanden"));
    this.pcscName.setEnabled(isPCSC());
    this.pcscName.setName(i18n.tr("Identifier des PC/SC-Kartenlesers"));
    return this.pcscName;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Index-Nummer des HBCI-Zugangs.
   * @return Eingabe-Feld.
   */
  public Input getEntryIndex()
  {
    if (this.entryIndex != null)
      return this.entryIndex;

    this.entryIndex = new SpinnerInput(1,4,getConfig().getEntryIndex());
    this.entryIndex.setName(i18n.tr("Index des HBCI-Zugangs"));
    this.entryIndex.setComment(i18n.tr("meist 1"));
    return this.entryIndex;
  }

  /**
   * Liefert die Checkbox fuer die Auswahl der Tastatur als PIN-Eingabe.
   * @return Checkbox.
   */
  public CheckboxInput getSoftPin()
  {
    if (this.useSoftPin != null)
      return this.useSoftPin;

    this.useSoftPin = new CheckboxInput(getConfig().useSoftPin());
    this.useSoftPin.setName(i18n.tr("Tastatur des PCs zur PIN-Eingabe verwenden"));
    return this.useSoftPin;
  }
  
  /**
   * Versucht, den Kartenleser automatisch zu ermitteln.
   */
  public void handleScan()
  {
    try
    {
      String ask = i18n.tr("Legen Sie Ihre HBCI-Chipkarte vor dem Test in das Lesegerät.\nDie Suchen kann einige Minuten in Anspruch nehmen. Vorgang fortsetzen?");
      if (!Application.getCallback().askUser(ask))
        return;
    }
    catch (Exception e)
    {
      Logger.error("unable to open confirm dialog",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Suchen des Kartenlesers: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      return;
    }
    

    BackgroundTask task = new BackgroundTask()
    {
      private boolean stop = false;
      
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(final ProgressMonitor monitor) throws ApplicationException
      {
        final DDVConfig config = DDVConfigFactory.scan(monitor,this);
        
        if (getConfig() == null)
        {
          // Wir sind nicht in der Detail-Ansicht sondern in der Liste.
          // Daher starten wir die List-View einfach neu, damit die Liste
          // aktualisiert wird.
          DDVConfigFactory.store(config);
          GUI.startView(GUI.getCurrentView().getClass(),null);
          return;
        }

        if (config != null)
        {
          // wenn wir einen gefunden haben, uebernehmen wir die Daten.
          GUI.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
              Reader reader = config.getReaderPreset();
              getReaderPresets().setValue(reader);
  
              getBezeichnung().setValue(config.getName());
              getPCSCName().setValue(config.getPCSCName());
              getCTAPI().setValue(config.getCTAPIDriver());
              getPort().setValue(config.getPort());
              getCTNumber().setValue(config.getCTNumber());
              getEntryIndex().setValue(config.getEntryIndex());
              getSoftPin().setValue(config.useSoftPin());
              
              try
              {
                getHBCIVersion().setValue(config.getHBCIVersion());
              }
              catch (RemoteException re)
              {
                Logger.error("unable to apply hbci version",re);
                // Den Fehler koennen wir tolerieren
              }
            }
          });
        }
      }

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
        this.stop = true;
      }

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return this.stop;
      }
    };
    Application.getController().start(task);
  }

  /**
   * Erstellt eine neue Kartenleser-Config.
   */
  public void handleCreate()
  {
    GUI.startView(Detail.class,DDVConfigFactory.create());
  }
  
  /**
   * Loescht die angegebene Kartenleser-Config.
   * @param config die zu loeschende Config.
   */
  public void handleDelete(DDVConfig config)
  {
    if (config == null)
      return;
    try
    {
      if (!Application.getCallback().askUser(i18n.tr("Wollen Sie diese Konfiguration wirklich löschen?")))
        return;

      DDVConfigFactory.delete(config);
      getConfigList().removeItem(config);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konfiguration gelöscht"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      GUI.getStatusBar().setErrorText(i18n.tr(ae.getMessage()));
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (Exception e)
    {
      Logger.error("error while deleting config",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der Konfiguration: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Speichert die Einstellungen.
   * @return true, wenn die Einstellungen gespeichert werden konnten.
   */
  public boolean handleStore()
  {
    try
    {
      if (!isPCSC())
      {
        getConfig().setCTNumber((Integer) getCTNumber().getValue());
        getConfig().setPort((String) getPort().getValue());
        getConfig().setCTAPIDriver((String) getCTAPI().getValue());
      }
      else
      {
        getConfig().setPCSCName((String) getPCSCName().getValue());
      }

      getConfig().setEntryIndex((Integer) getEntryIndex().getValue());
      getConfig().setSoftPin((Boolean) getSoftPin().getValue());

      getConfig().setKonten(getKontoAuswahl().getItems());
      getConfig().setReaderPreset((Reader) getReaderPresets().getValue());
      getConfig().setHBCIVersion((String) getHBCIVersion().getValue());
      getConfig().setName((String) getBezeichnung().getValue());
      DDVConfigFactory.store(getConfig());

      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (Exception e)
    {
      Logger.error("error while storing ddv config", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern der Einstellungen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return false;
  }

  /**
   * Testet die Einstellungen.
   */
  public void handleTest()
  {

    // Speichern, damit sicher ist, dass wir vernuenftige Daten fuer den
    // Test haben und die auch gespeichert sind
    if (!handleStore())
      return;

    try
    {
      new PassportTest().handleAction(new PassportHandleImpl(getConfig()));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while testing passport",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Testen der Konfiguration: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Aendert BLZ, Hostname usw. auf der Karte.
   */
  public void handleChangeBankData()
  {
    if (!handleStore())
      return;

    Application.getController().start(new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        HBCIPassportChipcard passport = null;
        try
        {
          // Ist hier etwas umstaendlich, weil wir das Handle
          // nicht aufmachen duerfen. Wuerden wir das tun, dann
          // wuerde HBCI4Java automatisch die UPD abrufen wollen,
          // was fehlschlagen wird, wenn wir ungueltige Daten
          // auf der Karte haben. Auf diese Weise hier koennen
          // wir aber die Daten ohne Bank-Kontakt aendern
          AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
          HBCICallback callback = ((HBCI)plugin).getHBCICallback();
          if (callback != null && (callback instanceof HBCICallbackSWT))
            ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl(getConfig()));

          passport = DDVConfigFactory.createPassport(getConfig());

          AccountContainerDialog d = new AccountContainerDialog(passport);
          AccountContainer container = (AccountContainer) d.open();
          
          passport.setBLZ(container.blz);
          passport.setUserId(container.userid);
          passport.setCustomerId(container.customerid);
          passport.setHost(container.host);
          passport.setFilterType(container.filter);
          passport.setCountry(container.country);
          
          passport.saveChanges();
          
          
          passport.saveBankData();
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        catch (OperationCanceledException oce)
        {
          Logger.info("operation cancelled");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Vorgang abgebrochen"),StatusBarMessage.TYPE_ERROR));
        }
        catch (Exception e)
        {
          if (HBCIProperties.getCause(e,OperationCanceledException.class) != null)
          {
            Logger.info("operation cancelled");
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Vorgang abgebrochen"),StatusBarMessage.TYPE_ERROR));
          }
          else
          {
            Logger.error("error while changing bank data",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ändern der Bankdaten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
        finally
        {
          if (passport != null)
          {
            try
            {
              Logger.info("closing passport");
              passport.close();
            }
            catch (Exception e)
            {
              Logger.error("unable to close passport",e);
            }
          }
        }
      }
      
      public boolean isInterrupted(){return false;}
      public void interrupt(){}
    });
  }

  /**
   * Listener, der nach Auswahl eines Kartenleser-Presets ausgeloest wird.
   */
  private class PresetListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try {
        Reader r = (Reader) getReaderPresets().getValue();
        if (r == null)
          return;

        if (!r.isSupported())
        {
          GUI.getView().setErrorText(i18n.tr("Der ausgewählte Kartenleser wird von Hibiscus nicht unterstützt"));
          return;
        }
        
        boolean pcsc = isPCSC();
        getCTAPI().setEnabled(!pcsc);
        getCTNumber().setEnabled(!pcsc);
        getPort().setEnabled(!pcsc);
        getPCSCName().setEnabled(pcsc);

        if (!pcsc)
        {
          String s = r.getCTAPIDriver();
          if (s != null && s.length() > 0)
          {
            File f = new File(s);
            if (!f.exists())
              GUI.getView().setErrorText(i18n.tr("CTAPI-Treiber nicht gefunden. Bitte Treiber installieren."));
          }
          
          String port = r.getPort();
          if (port != null)
            getPort().setPreselected(port);
          
          int ctNumber = r.getCTNumber();
          if (ctNumber >= 0)
            getCTNumber().setValue(new Integer(ctNumber));

          getCTAPI().setValue(s);
        }
        else
        {
          getCTAPI().setValue("");
        }
        
        getSoftPin().setValue(new Boolean(r.useSoftPin()));
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while applying reader preset",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der Einstellungen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    	}
    }
  }
}
