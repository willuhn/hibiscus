/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.PassportChange;
import de.willuhn.jameica.hbci.gui.action.PassportSync;
import de.willuhn.jameica.hbci.gui.action.PassportTest;
import de.willuhn.jameica.hbci.gui.dialogs.NewKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportPropertyDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.HBCIVersionInput;
import de.willuhn.jameica.hbci.passport.PassportChangeRequest;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
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

  // Liste aller Schluessel
  private TablePart keyList           = null;

  // Benutzerdaten
  private Input benutzerkennung       = null;
  private Input kundenkennung         = null;
  private Input blz                   = null;
  
  // Verbindungsdaten
  private Input hbciUrl               = null;
  private Input hbciPort              = null;
  private Input hbciVersion           = null;
  
  // Erweiterte Einstellungen
  private Input alias                 = null;
  private Input path                  = null;

  // Fach-Daten
  private RDHKey key                  = null;
  private HBCIPassport passport       = null;

  // BUGZILLA 173
  // Liste der zugeordneten Kunden
  private TablePart kontoList         = null;

  /**
   * ct.
   * @param view
   */
  public Controller(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert den Schluessel.
   * @return Schluessel.
   */
  public RDHKey getKey()
  {
    if (this.key != null)
      return this.key;
    Object o = getCurrentObject();
    if (o instanceof RDHKey)
      this.key = (RDHKey) o;
    return this.key;
  }
  
  /**
   * Liefert den HBCI-Passport.
   * @return Passport.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public HBCIPassport getHBCIPassport() throws RemoteException, ApplicationException, OperationCanceledException
  {
    if (this.passport != null)
      return this.passport;
    RDHKey key = getKey();
    if (key == null)
      throw new ApplicationException(i18n.tr("Kein Schlüssel ausgewählt"));
    this.passport = key.load();
    return this.passport;
  }

  /**
   * Liefert ein Anzeigefeld fuer die Benutzerkennung.
   * @return Anzeigefeld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getBenutzerkennung() throws RemoteException, ApplicationException
  {
    if (this.benutzerkennung != null)
      return this.benutzerkennung;
    this.benutzerkennung = new TextInput(getHBCIPassport().getUserId(),30);
    this.benutzerkennung.setName(i18n.tr("Benutzerkennung"));
    return this.benutzerkennung;
  }
  
  /**
   * Liefert ein Anzeigefeld fuer die Kundenkennung.
   * @return Anzeigefeld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getKundenkennung() throws RemoteException, ApplicationException
  {
    if (this.kundenkennung != null)
      return this.kundenkennung;
    this.kundenkennung = new TextInput(getHBCIPassport().getCustomerId(),30);
    this.kundenkennung.setName(i18n.tr("Kundenkennung"));
    return this.kundenkennung;
  }
  
  /**
   * Liefert ein Anzeigefeld fuer die BLZ.
   * @return Anzeigefeld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getBLZ() throws RemoteException, ApplicationException
  {
    if (this.blz != null)
      return this.blz;
    this.blz = new BLZInput(getHBCIPassport().getBLZ());
    this.blz.setEnabled(false);
    this.blz.setName(i18n.tr("Bankleitzahl"));
    return this.blz;
  }
  
  
  /**
   * Liefert einen zusaetzlichen Alias-Namen, an dem der User mehrere Schluessel
   * unterscheiden kan.
   * @return Alias-Name.
   * @throws RemoteException
   */
  public Input getAlias() throws RemoteException
  {
    if (this.alias != null)
      return this.alias;
    this.alias = new TextInput(getKey().getAlias());
    this.alias.setName(i18n.tr("Alias-Name des Schlüssels"));
    return this.alias;
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
      this.kontoList = new KontoList(this.getKey());
    return kontoList;
  }
  
  
  /**
   * Liefert ein Eingabe-Feld zur Eingabe des Pfads zum Schluessel.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPath() throws RemoteException
  {
    if (this.path == null)
    {
      // Das Aendern des Pfad macht ueberhaupt keine Sinn mehr.
      // Da kann man den Schluessel auch einfach loeschen
      // und neu importieren.
      this.path = new FileInput(getKey().getFilename());
      this.path.setEnabled(false);
      this.path.setName(i18n.tr("Pfad zu Schlüsseldatei"));
    }
    return this.path;
  }
  
  /**
   * Liefert eine Auswahl-Box fuer die HBCI-Version.
   * @return Auswahl-Box.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getHBCIVersion() throws RemoteException, ApplicationException
  {
    if (this.hbciVersion != null)
      return this.hbciVersion;
    this.hbciVersion = new HBCIVersionInput(getHBCIPassport(),getKey().getHBCIVersion());
    this.hbciVersion.setName(i18n.tr("HBCI-Version"));
    return this.hbciVersion;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer die URL.
   * @return Eingabe-Feld.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public Input getHBCIUrl() throws ApplicationException, RemoteException
  {
    if (this.hbciUrl != null)
      return this.hbciUrl;

    this.hbciUrl = new TextInput(getHBCIPassport().getHost());
    this.hbciUrl.setName(i18n.tr("Hostname des Bankservers"));
    return this.hbciUrl;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den TCP-Port.
   * @return Eingabe-Feld.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public Input getHBCIPort() throws ApplicationException, RemoteException
  {
    if (this.hbciPort != null)
      return this.hbciPort;

    Integer i = getHBCIPassport().getPort();
    this.hbciPort = new IntegerInput(i != null ? i.intValue() : 3000);
    this.hbciPort.setName(i18n.tr("TCP-Port des Bankservers"));
    this.hbciPort.setComment(i18n.tr("meist \"3000\""));
    return this.hbciPort;
  }

  /**
   * Liefert eine Liste mit den importierten Schluesseln.
   * @return Liste der Schluessel.
   * @throws RemoteException
   */
  public TablePart getKeyList() throws RemoteException
  {
    if (keyList != null)
      return keyList;

    keyList = new TablePart(RDHKeyFactory.getKeys(),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        GUI.startView(Detail.class,context);
      }
    });


    // Spalte Datei
    keyList.addColumn(i18n.tr("Schlüsseldatei"),"file");
    keyList.addColumn(i18n.tr("Alias-Name"),"alias");
    keyList.addColumn(i18n.tr("Format"),"format");



    ContextMenu ctx = new ContextMenu();

    // Kontext: Details.
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),new Action()
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
          Logger.error("error while loading rdh key",e);
        }
      }
    },"document-open.png"));

    ctx.addItem(new ContextMenuItem(i18n.tr("Neuer Schlüssel..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        startCreate();
      }
    }, "document-new.png"));
    ctx.addItem(new ContextMenuItem(i18n.tr("Schlüssel importieren..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        startImport();
      }
    },"stock_keyring.png"));

    // Kontext: Aktivieren/Deaktivieren
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new ActivateKey(true));
    ctx.addItem(new ActivateKey(false));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          String q = i18n.tr("Wollen Sie diesen Schlüssel wirklich löschen?\n" +
                             "Hierbei wird nur die Verknüpfung aus Hibiscus " +
                             "entfernt. Die Schlüsseldatei selbst bleibt erhalten.");
        
          if (!Application.getCallback().askUser(q))
            return;

          RDHKey k = (RDHKey) context;
          RDHKeyFactory.removeKey(k);
          GUI.startView(View.class,null);
        }
        catch (OperationCanceledException e)
        {
          Logger.info("operation cancelled");
          return;
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e2)
        {
          Logger.error("unable to delete key",e2);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen des Schlüssels"),StatusBarMessage.TYPE_ERROR));
        }
      }
    },"user-trash-full.png"));

    keyList.setContextMenu(ctx);
    


    // Format fuer aktiv/inaktiv
    keyList.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        try
        {
          RDHKey key = (RDHKey) item.getData();
          if (!key.isEnabled())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("error while formatting key",e);
        }
      }
    });

    keyList.setMulti(false);
    keyList.setRememberColWidths(true);
    keyList.setRememberOrder(true);
    keyList.setSummary(false);
    return keyList;
  }

  /**
   * Startet die Erzeugung eines INI-Briefs.
   */
  public synchronized void startIniLetter()
  {
    RDHKey key = getKey();
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
    
    HBCIPassport passport = null;
    try
    {
      passport = key.load();
      NewKeysDialog d = new NewKeysDialog(passport);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled: " + oce.getMessage());
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(ae.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while creating ini letter",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Erzeugen des INI-Briefs: {0}",e.getMessage()));
    }
    finally
    {
      if (passport != null)
      {
        try
        {
          passport.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close passport",e);
        }
      }
    }
  }

  /**
   * Aendert das Passwort der Schluesseldatei.
   */
  public synchronized void changePassword()
  {
    HBCIPassport passport = null;
    HBCICallback callback = null;
    
    try
    {
      passport = getHBCIPassport();
      
      // muessen wir zwingend machen, weil sonst der Callback nicht bei uns landet
      // Das wuerde bewirken, dass Hibiscus ein zufaelliges neues Passwort erzeugt,
      // welches der User aber nicht mehr kennt.
      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
      callback = ((HBCI)plugin).getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl());
      
      passport.changePassphrase();
      
      // Passwort-Cache leeren
      DialogFactory.clearPINCache(passport);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Passwort geändert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to change password",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ändern des Passwortes: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(null);
    }
  }
  
  /**
   * Synchronisiert den Bankzugang.
   */
  public synchronized void handleSync()
  {
    RDHKey key = getKey();
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
    
    HBCIPassport passport = null;
    try
    {
      if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher?")))
        return;

      passport = key.load();
      passport.syncSigId();
      
      new PassportSync().handleAction(new PassportHandleImpl(key));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (Exception e)
    {
      Logger.error("error while testing passport",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Synchronisieren des Bank-Zugangs: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Startet einen Schluessel-Import.
   */
  public synchronized void startImport()
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.OPEN);
    fd.setText(i18n.tr("Bitte wählen Sie die zu importierende Schlüsseldatei"));

    String importFile = fd.open();
    if (importFile == null || importFile.length() == 0)
      return;
    RDHKeyFactory.importKey(new File(importFile));
    GUI.startView(View.class,null); // Reload
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
      new PassportTest().handleAction(new PassportHandleImpl(getKey()));
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
   * Speichert die Einstellungen fuer den aktuellen Schluessel.
   * @return true, wenn das Speichern erfolgreich war.
   */
  public synchronized boolean handleStore()
  {
    RDHKey key = getKey();
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return false;
    }

    try
    {
      Konto[] konten = null;
      List checked = getKontoAuswahl().getItems();
      if (checked != null && checked.size() > 0)
        konten = (Konto[]) checked.toArray(new Konto[checked.size()]);
      key.setKonten(konten);
      
      key.setHBCIVersion((String)getHBCIVersion().getValue());
      key.setAlias((String)getAlias().getValue());

      
      HBCIPassport p = getHBCIPassport();
      if (p != null)
      {
        String s = (String) getHBCIUrl().getValue();
        if (s != null && s.length() > 0)
          p.setHost(s);
        Integer i = (Integer) getHBCIPort().getValue();
        if (i != null)
          p.setPort(i);
        p.saveChanges();
      }
      
      PassportChangeRequest change = new PassportChangeRequest((AbstractHBCIPassport) p,(String)getKundenkennung().getValue(),(String)getBenutzerkennung().getValue());
      new PassportChange().handleAction(change);
      
      GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert"));
      return true;
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled: " + oce.getMessage());
    }
    catch (ApplicationException e2)
    {
      Logger.error("error while exporting key",e2);
      GUI.getStatusBar().setErrorText(i18n.tr(e2.getMessage()));
    }
    catch (Exception e)
    {
      Logger.error("error while storing settings",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen"));
    }
    return false;
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
   * Erstellt einen neuen Schluessel.
   */
  public synchronized void startCreate()
  {
    try
    {
      // Wir fragen den User, wo er den Schluessel hinhaben will.
      FileDialog dialog = new FileDialog(GUI.getShell(), SWT.SAVE);
      dialog.setOverwrite(true);
      dialog.setText(Application.getI18n().tr("Bitte wählen einen Pfad und Dateinamen, an dem der Schlüssel gespeichert werden soll."));
      dialog.setFileName("hibiscus-" + System.currentTimeMillis() + ".rdh");
      dialog.setFilterPath(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath());
      String newFile = dialog.open();
      if (newFile == null || newFile.length() == 0)
        throw new OperationCanceledException("no key file choosen");
      
      File newKey = new File(newFile);
      
      if (RDHKeyFactory.createKey(newKey))
        GUI.startView(View.class,null);
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled: " + oce.getMessage());
    }
    catch (Throwable t)
    {
      Logger.error("error while creating key",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erstellen der Schlüsseldatei"),StatusBarMessage.TYPE_ERROR));
    }

  }
  

  /**
   * Hilfsklasse zum Aktivieren des Schluessels.
   */
  private class ActivateKey extends CheckedContextMenuItem
  {

    private boolean activate;

    /**
     * @param activate
     */
    public ActivateKey(final boolean activate)
    {
      super((activate ? i18n.tr("Schlüssel aktivieren") : i18n.tr("Schlüssel deaktivieren")),
        new Action()
        {
          public void handleAction(Object context) throws ApplicationException
          {
            if (context == null)
              return;
            try
            {
              ((RDHKey) context).setEnabled(activate);
              GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
            }
            catch (Exception e)
            {
              Logger.error("error while activating rdh key",e);
            }
          }
        },activate ? "network-transmit-receive.png" : "network-offline.png");
      this.activate = activate;
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null)
        return false;
      try
      {
        boolean active = ((RDHKey) o).isEnabled();
        if (activate)
          return !active;
        return active;
      }
      catch (Exception e)
      {
        Logger.error("error while checking key state",e);
        return false;
      }
    }
  }
}
