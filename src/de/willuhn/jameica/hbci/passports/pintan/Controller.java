/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/Controller.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/07/22 12:37:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.PassportTest;
import de.willuhn.jameica.hbci.gui.dialogs.PassportPropertyDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.HBCIVersionInput;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.passports.pintan.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class Controller extends AbstractControl {


  private PinTanConfig config   = null;
  private HBCIPassport passport = null;

	private I18N i18n;

  private TablePart configList  = null;

  private Input url             = null;
  private Input blz             = null;
  private Input port            = null;
  private Input filterType      = null;
  private Input hbciVersion     = null;
  private Input customerId      = null;
  private Input userId          = null;
  private Input bezeichnung     = null;
  private CheckboxInput saveTan = null;
  private CheckboxInput showTan = null;

  // BUGZILLA 173
  private TablePart kontoList   = null;
  
  /**
   * ct.
   * @param view
   */
  public Controller(AbstractView view) {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert die aktuelle Config.
   * @return config
   */
  public PinTanConfig getConfig()
  {
    if (config == null)
      config = (PinTanConfig) getCurrentObject();
    return config; 
  }

  /**
   * Liefert eine Liste mit den existierenden Konfigurationen.
   * @return Liste der Konfigurationen.
   * @throws RemoteException
   */
  public TablePart getConfigList() throws RemoteException
  {
    if (configList != null)
      return configList;

    configList = new TablePart(PinTanConfigFactory.getConfigs(),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        GUI.startView(Detail.class,context);
      }
    });

    configList.addColumn(i18n.tr("Name der Bank"),"bank");
    configList.addColumn(i18n.tr("Alias-Name"),"bezeichnung");
    configList.addColumn(i18n.tr("Bankleitzahl"),"blz");
    configList.addColumn(i18n.tr("URL"),"url");

    ContextMenu ctx = new ContextMenu();

    // Kontext: Erstellen
    ctx.addItem(new ContextMenuItem(i18n.tr("Neue Konfiguration erstellen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleCreate();
      }
    }));

    // Kontext: Details.
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Details anzeigen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null)
          return;
        try
        {
          GUI.startView(Detail.class,context);
        }
        catch (Exception e)
        {
          Logger.error("error while loading config",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Anlegen der Konfiguration"));
        }
      }
    }));

    // Kontext: Loeschen.
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Konfiguration löschen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleDelete((PinTanConfig)context);
      }
    }));

    configList.setContextMenu(ctx);

    return configList;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die BLZ.
   * @return BLZ
   * @throws RemoteException
   */
  public Input getBLZ() throws RemoteException
  {
    if (this.blz != null)
      return this.blz;
    this.blz = new BLZInput(getConfig().getBLZ());
    this.blz.setEnabled(false);
    this.blz.setName(i18n.tr("Bankleitzahl"));
    this.blz.setMandatory(true);
    return this.blz;
  }

  /**
   * BUGZILLA 173
   * BUGZILLA 314
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
   * Liefert ein Eingabe-Feld fuer die URL.
   * @return URL
   * @throws RemoteException
   */
  public Input getURL() throws RemoteException
  {
    if (url != null)
      return url;
    url = new TextInput(getConfig().getURL());
    url.setEnabled(false);
    url.setName(i18n.tr("URL des Bank-Servers"));
    url.setMandatory(true);
    return url;
  }

  /**
   * Liefert eine Checkbox zur TAN-Speicherung.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getSaveTAN() throws RemoteException
  {
    if (saveTan != null)
      return saveTan;
    saveTan = new CheckboxInput(getConfig().getSaveUsedTan());
    return saveTan;
  }

  /**
   * Liefert eine Checkbox zur Aktivierung oder Deaktivierung der TAN-Anzeige waehrend der Eingabe.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getShowTan() throws RemoteException
  {
    if (showTan != null)
      return showTan;
    showTan = new CheckboxInput(getConfig().getShowTan());
    return showTan;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Bezeichnung.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
      return bezeichnung;
    bezeichnung = new TextInput(getConfig().getBezeichnung());
    bezeichnung.setComment(i18n.tr("Angabe optional"));
    bezeichnung.setName(i18n.tr("Alias-Name"));
    return bezeichnung;
  }

  /**
   * Liefert ein Eingabe-Feld fuer den TCP-Port.
   * @return Port
   * @throws RemoteException
   */
  public Input getPort() throws RemoteException
  {
    if (port != null)
      return port;
    port = new IntegerInput(getConfig().getPort());
    port.setEnabled(false);
    port.setName(i18n.tr("TCP-Port des Bank-Servers"));
    port.setMandatory(true);
    return port;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Benutzerkennung.
   * @return Benutzerkennung.
   * @throws RemoteException
   */
  public Input getCustomerId() throws RemoteException
  {
    if (customerId != null)
      return customerId;
    customerId = new TextInput(getConfig().getCustomerId());
    customerId.setName(i18n.tr("Kundenkennung"));
    customerId.setEnabled(false);
    customerId.setMandatory(true);
    return customerId;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Userkennung.
   * @return Userkennung.
   * @throws RemoteException
   */
  public Input getUserId() throws RemoteException
  {
    if (userId != null)
      return userId;
    userId = new TextInput(getConfig().getUserId());
    userId.setName(i18n.tr("Benutzerkennung"));
    userId.setEnabled(false);
    userId.setMandatory(true);
    return userId;
  }

  /**
   * Liefert ein Eingabe-Feld fuer den Transport-Filter.
   * @return Filter
   * @throws RemoteException
   */
  public Input getFilterType() throws RemoteException
  {
    if (filterType != null)
      return filterType;
    filterType = new SelectInput(
      new String[]{"Base64","None"},
      getConfig().getFilterType());
    filterType.setComment(i18n.tr("meist Base64"));
    filterType.setName(i18n.tr("Filter für Übertragung"));
    filterType.setMandatory(true);
    return filterType;
  }

  /**
   * Liefert eine Auswahl-Box fuer die HBCI-Version.
   * @return Auswahl-Box.
   * @throws RemoteException
   */
  public Input getHBCIVersion() throws RemoteException
  {
    if (hbciVersion != null)
      return hbciVersion;
    String current = getConfig().getHBCIVersion();
    hbciVersion = new HBCIVersionInput(getHBCIPassport(),current);
    hbciVersion.setMandatory(true);
    hbciVersion.setName(i18n.tr("HBCI-Version"));
    return hbciVersion;
  }
  
  /**
   * Liefert den HBCI-Passport.
   * @return der HBCI-Passport.
   * @throws RemoteException
   */
  private HBCIPassport getHBCIPassport() throws RemoteException
  {
    if (this.passport == null)
      this.passport = PinTanConfigFactory.load(new File(config.getFilename()));
    return this.passport;
  }

  /**
   * BUGZILLA 218
   * Loescht die automatische Zuordnung eines TAN-Verfahrens fuer die PINTAN-Config.
   */
  public void handleDeleteSecMech()
  {
    try
    {
      getConfig().setSecMech(null);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Automatische Vorauswahl des TAN-Verfahrens gelöscht"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while deleting ptsechmech preselection",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der Vorauswahl"),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Zeigt eine Liste der benutzten TANs an.
   */
  public void handleShowUsedTans()
  {
    try
    {
      UsedTanDialog d = new UsedTanDialog(getConfig());
      d.open();
    }
    catch (Exception e)
    {
      Logger.error("error while loading used TANs",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der TAN-Liste"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Zeigt die BPD/UPD des Passports an.
   */
  public synchronized void handleDisplayProperties()
  {
    try
    {
      new PassportPropertyDialog(PassportPropertyDialog.POSITION_CENTER,this.getHBCIPassport()).open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (ApplicationException e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    catch (Throwable t)
    {
      Logger.error("error while displaying BPD/UPD",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler Anzeigen der BPD/UPD"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Loescht die Config.
   * @param config
   */
  public synchronized void handleDelete(PinTanConfig config)
  {
    if (config == null)
      return;
    try
    {
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle(i18n.tr("Sicher?"));
      d.setText(i18n.tr("Wollen Sie diese Konfiguration wirklich löschen?"));
      
      Boolean b = (Boolean) d.open();
      if (b.booleanValue())
      {
        PinTanConfigFactory.delete((PinTanConfig) config);
      }
      GUI.startView(View.class,null);
      GUI.getStatusBar().setSuccessText(i18n.tr("Konfiguration gelöscht"));
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(i18n.tr(ae.getMessage()));
    }
    catch (Exception e)
    {
      Logger.error("error while deleting config",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Konfiguration"));
    }
  }

  /**
   * Testet die Konfiguration.
   */
  public synchronized void handleTest()
  {

    // Speichern, damit sicher ist, dass wir vernuenftige Daten fuer den
    // Test haben und die auch gespeichert sind
    handleStore();
    if (!stored)
      return;

    try
    {
      new PassportTest().handleAction(new PassportHandleImpl(getConfig()));
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(ae.getMessage());
    }
    catch (RemoteException e)
    {
      Logger.error("error while testing passport",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Testen der Konfiguration. Bitte prüfen Sie das Protokoll. ") + e.getMessage());
    }
  }

  /**
   * Erstellt eine neue Config.
   */
  public synchronized void handleCreate()
  {
    PinTanConfig conf = null;
    try
    {
      Logger.info("creating new pin/tan config");
      conf = PinTanConfigFactory.create();
      GUI.startView(Detail.class,conf);
      
      GUI.getStatusBar().setSuccessText(i18n.tr("Konfiguration erfolgreich erstellt"));
    }
    catch (ApplicationException e)
    {
      Logger.error("error while creating config",e);
      GUI.getStatusBar().setErrorText(i18n.tr(e.getMessage()));
    }
    catch (Throwable t)
    {
      // Fehlertext nur anzeigen, wenn der Vorgang nicht durch den User abgebrochen wurde
      if (HBCIFactory.getCause(t,OperationCanceledException.class) == null)
      {
        Logger.error("error while creating config",t);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Erstellen der Konfiguration"));
      }
    }
  }

  private boolean stored = false;

  /**
   * Speichert die Konfiguration.
   */
  public synchronized void handleStore()
  {
    try
    {
      stored = false;
			Logger.info("storing pin/tan config");

    	PinTanConfig config  = getConfig();
      
      Konto[] konten = null;
      List checked = getKontoAuswahl().getItems();
      if (checked != null && checked.size() > 0)
        konten = (Konto[]) checked.toArray(new Konto[checked.size()]);
      config.setKonten(konten);
      
      config.setFilterType((String) getFilterType().getValue());
      config.setBezeichnung((String) getBezeichnung().getValue());
      config.setSaveUsedTan(((Boolean)getSaveTAN().getValue()).booleanValue());
      config.setShowTan(((Boolean)getShowTan().getValue()).booleanValue());
			config.setHBCIVersion((String) getHBCIVersion().getValue());


      PinTanConfigFactory.store(config);
      this.passport = null; // force reload

      GUI.getStatusBar().setSuccessText(i18n.tr("Konfiguration gespeichert"));
      stored = true;
    }
    catch (ApplicationException e)
    {
      Logger.error("error while storing config",e);
      GUI.getStatusBar().setErrorText(i18n.tr(e.getMessage()));
    }
    catch (Throwable t)
    {
      Logger.error("error while creating config",t);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Konfiguration"));
    }
  }

}


/**********************************************************************
 * $Log: Controller.java,v $
 * Revision 1.3  2010/07/22 12:37:41  willuhn
 * @N GUI poliert
 *
 * Revision 1.2  2010-07-22 11:31:50  willuhn
 * @B Fehlertext nur anzeigen, wenn der Erstell-Vorgang nicht durch den User abgebrochen wurde
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.22  2009/06/29 11:04:17  willuhn
 * @N Beim Speichern existierender Konfigurationen wird der BPD-Cache geloescht. Das soll Fehler bei VR-Banken vermeiden, nachdem dort die HBCI-Version geaendert wurde
 *
 * Revision 1.21  2009/06/16 15:32:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2009/06/16 14:04:34  willuhn
 * @N Dialog zum Anzeigen der BPD/UPD
 *
 * Revision 1.19  2008/07/29 08:30:04  willuhn
 * @B Compile-Fix
 *
 * Revision 1.18  2007/11/19 21:54:58  willuhn
 * @B BUGZILLA 506
 *
 * Revision 1.17  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 * Revision 1.16  2006/08/03 15:31:35  willuhn
 * @N Bug 62 completed
 *
 * Revision 1.15  2006/08/03 13:51:38  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.14  2006/08/03 11:27:36  willuhn
 * @N Erste Haelfte von BUG 62 (Speichern verbrauchter TANs)
 *
 * Revision 1.13  2006/03/28 22:52:31  willuhn
 * @B bug 218
 *
 * Revision 1.12  2006/03/28 22:35:14  willuhn
 * @B bug 218
 *
 * Revision 1.11  2006/03/28 17:51:08  willuhn
 * @B bug 218
 *
 * Revision 1.10  2006/01/10 22:34:07  willuhn
 * @B bug 173
 *
 * Revision 1.9  2005/07/18 12:53:30  web0
 * @B bug 96
 *
 * Revision 1.8  2005/04/27 00:30:12  web0
 * @N real test connection
 * @N all hbci versions are now shown in select box
 * @C userid and customerid are changable
 *
 * Revision 1.7  2005/04/05 23:42:12  web0
 * @C moved HBCIVersionInput into Hibiscus source tree
 *
 * Revision 1.6  2005/03/11 02:43:59  web0
 * @N PIN/TAN works ;)
 *
 * Revision 1.5  2005/03/11 00:49:30  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/10 18:38:48  web0
 * @N more PinTan Code
 *
 * Revision 1.3  2005/03/09 17:24:40  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/08 18:44:57  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/07 12:06:12  web0
 * @N initial import
 *
 **********************************************************************/