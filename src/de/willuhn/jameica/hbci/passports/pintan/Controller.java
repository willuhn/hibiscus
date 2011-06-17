/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/Controller.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/06/17 08:49:18 $
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
public class Controller extends AbstractControl
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  private PinTanConfig config   = null;
  private HBCIPassport passport = null;

  private TablePart configList  = null;

  private Input url             = null;
  private Input blz             = null;
  private Input port            = null;
  private Input filterType      = null;
  private Input hbciVersion     = null;
  private Input customerId      = null;
  private Input userId          = null;
  private Input bezeichnung     = null;
  private CheckboxInput showTan = null;

  // BUGZILLA 173
  private TablePart kontoList   = null;
  
  /**
   * ct.
   * @param view
   */
  public Controller(AbstractView view) {
    super(view);
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

    ctx.addItem(new ContextMenuItem(i18n.tr("PIN/TAN-Zugang anlegen"),new Action() {
      public void handleAction(Object context) throws ApplicationException {handleCreate();}
    },"document-new.png"));

    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),new Action() {
      public void handleAction(Object context) throws ApplicationException {handleDelete((PinTanConfig)context);}
    },"user-trash-full.png"));

    configList.setContextMenu(ctx);
    configList.setMulti(false);
    configList.setRememberColWidths(true);
    configList.setRememberOrder(true);
    configList.setSummary(false);

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
   * Loescht die Vorauswahlen bei den TAN-Verfahren.
   */
  public void handleDeleteTanSettings()
  {
    try
    {
      getConfig().setSecMech(null);
      getConfig().setTanMedia(null);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Vorauswahl der TAN-Verfahren zurückgesetzt"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while deleting tan settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zurücksetzen der TAN-Verfahren"),StatusBarMessage.TYPE_ERROR));
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
   * @param config die zu loeschende Config.
   */
  public synchronized void handleDelete(PinTanConfig config)
  {
    if (config == null)
      return;
    try
    {
      if (!Application.getCallback().askUser(i18n.tr("Wollen Sie diese Konfiguration wirklich löschen?")))
        return;

      PinTanConfigFactory.delete(config);
      GUI.startView(View.class,null);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konfiguration gelöscht"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while deleting config",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Löschen fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Testet die Konfiguration.
   */
  public synchronized void handleTest()
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

  /**
   * Speichert die Konfiguration.
   * @return true, wenn die Config gespeichert werden konnte.
   */
  public synchronized boolean handleStore()
  {
    try
    {
			Logger.info("storing pin/tan config");

    	PinTanConfig config  = getConfig();
      
      Konto[] konten = null;
      List checked = getKontoAuswahl().getItems();
      if (checked != null && checked.size() > 0)
        konten = (Konto[]) checked.toArray(new Konto[checked.size()]);
      config.setKonten(konten);
      
      config.setFilterType((String) getFilterType().getValue());
      config.setBezeichnung((String) getBezeichnung().getValue());
      config.setShowTan(((Boolean)getShowTan().getValue()).booleanValue());
			config.setHBCIVersion((String) getHBCIVersion().getValue());


      PinTanConfigFactory.store(config);
      this.passport = null; // force reload

      GUI.getStatusBar().setSuccessText(i18n.tr("Konfiguration gespeichert"));
      return true;
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
    return false;
  }

}


/**********************************************************************
 * $Log: Controller.java,v $
 * Revision 1.10  2011/06/17 08:49:18  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.9  2011-05-23 10:47:29  willuhn
 * @R BUGZILLA 62 - Speichern der verbrauchten TANs ausgebaut. Seit smsTAN/chipTAN gibt es zum einen ohnehin keine TAN-Listen mehr. Zum anderen kann das jetzt sogar Fehler ausloesen, wenn ueber eines der neuen TAN-Verfahren die gleiche TAN generiert wird, die frueher irgendwann schonmal zufaellig generiert wurde. TANs sind inzwischen fluechtige und werden dynamisch erzeugt. Daher ist es unsinnig, die zu speichern. Zumal es das Wallet sinnlos aufblaeht.
 *
 * Revision 1.8  2011-05-11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.7  2011-05-09 09:35:15  willuhn
 * @N BUGZILLA 827
 *
 * Revision 1.6  2011-04-29 11:38:57  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.5  2011-04-28 07:34:43  willuhn
 * @R Summen-Zeile nicht mehr anzeigen - unnuetz
 **********************************************************************/