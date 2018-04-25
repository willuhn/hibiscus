/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.PasswordInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den die Synchronisierungs-Optionen fuer ein Konto eingestellt werden koennen.
 */
public class SynchronizeOptionsDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final static int WINDOW_WIDTH = 400;
  
  private Konto konto                   = null;
  private boolean offline               = false;
  private boolean syncAvail             = false;
  private SynchronizeOptions options    = null;
  private CheckboxInput syncOffline     = null;
  private CheckboxInput syncSaldo       = null;
  private CheckboxInput syncUmsatz      = null;
  private CheckboxInput syncKontoauszug = null;
  private CheckboxInput syncAueb        = null;
  private CheckboxInput syncSepaLast    = null;
  private CheckboxInput syncSepaDauer   = null;
  private CheckboxInput syncMessages    = null;
  private LabelInput error              = null;
  private Button apply                  = null;
  
  private List<Input> properties = new ArrayList<Input>();

  /**
   * ct.
   * @param konto das Konto.
   * @param position
   * @throws RemoteException
   */
  public SynchronizeOptionsDialog(Konto konto, int position) throws RemoteException
  {
    super(position);
    this.setTitle(i18n.tr("Synchronisierungsoptionen"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.konto = konto;
    this.options = new SynchronizeOptions(konto);
    this.offline = konto.hasFlag(Konto.FLAG_OFFLINE);
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine = service.get(SynchronizeEngine.class);
    this.syncAvail = engine.supports(SynchronizeJobKontoauszug.class,konto);
    
    // checken, ob wir Addon-Properties haben
    if (this.syncAvail)
    {
      try
      {
        SynchronizeBackend backend = engine.getBackend(SynchronizeJobKontoauszug.class,konto);
        List<String> names = backend.getPropertyNames(konto);
        if (names != null && names.size() > 0)
        {
          for (String name:names)
          {
            this.createCustomProperty(name);
          }
        }
      }
      catch (ApplicationException ae)
      {
        Logger.error(ae.getMessage());
      }
    }
  }
  
  /**
   * Erzeugt ein Custom-Property-Input fuer den angegebenen Property-Namen.
   * @param name der Name des Custom-Property.
   * @throws RemoteException
   */
  private void createCustomProperty(String name) throws RemoteException
  {
    Input t = null;
    if (name.endsWith("(true/false)"))
    {
      String newName = name.replace("(true/false)","").trim();
      String value = konto.getMeta(newName,null);
      t = new CheckboxInput(value != null && Boolean.valueOf(value).booleanValue());
      t.setName(newName);
    }
    else if (name.endsWith("(pwd)") || name.endsWith("(password)"))
    {
      String newName = name.replace("(pwd)","").replace("(password)","").trim();
      t = new PasswordInput(konto.getMeta(newName,null));
      t.setName(newName);
    }
    else
    {
      t = new TextInput(konto.getMeta(name,null));
      t.setName(name);
    }
    this.properties.add(t);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);

    group.addText(i18n.tr("Bitte wählen Sie aus, welche Geschäftsvorfälle bei der " +
    		                  "Synchronisierung des Kontos ausgeführt werden sollen."),true);
    
    group.addHeadline(this.konto.getLongName());
    
    this.apply = new Button(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        
        if (!offline || syncAvail) // Entweder bei Online-Konten oder bei welchen mit neuem Scripting-Support
        {
          options.setSyncSaldo(((Boolean)getSyncSaldo().getValue()).booleanValue());
          options.setSyncKontoauszuege(((Boolean)getSyncUmsatz().getValue()).booleanValue());
        }
        
        if (offline)
        {
          options.setSyncOffline(((Boolean)getSyncOffline().getValue()).booleanValue());
        }
        else
        {
          options.setSyncKontoauszuegePdf(((Boolean)getSyncKontoauszug().getValue()).booleanValue());
          options.setSyncMessages(((Boolean)getSyncMessages().getValue()).booleanValue());
          options.setSyncSepaDauerauftraege(((Boolean)getSyncSepaDauer().getValue()).booleanValue());
          options.setSyncAuslandsUeberweisungen(((Boolean)getSyncAueb().getValue()).booleanValue());
          options.setSyncSepaLastschriften(((Boolean)getSyncSepaLast().getValue()).booleanValue());
        }
        
        try
        {
          for (Input prop:properties)
          {
            Object value = prop.getValue();
            konto.setMeta(prop.getName(),value != null ? value.toString() : null);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to apply properties",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Übernehmen der Optionen fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
        close();
      }
    },null,true,"ok.png");
    
    Input i1 = this.getSyncSaldo();
    Input i2 = this.getSyncUmsatz();
    Input i3 = this.getSyncOffline();
    Input i4 = this.getSyncAueb();
    Input i5 = this.getSyncSepaLast();
    Input i6 = this.getSyncSepaDauer();
    Input i7 = this.getSyncMessages();
    Input i8 = this.getSyncKontoauszug();
    
    if (!offline || syncAvail)
    {
      group.addInput(i1);
      group.addInput(i2);
    }
    
    if (offline)
    {
      group.addInput(i3);
    }
    else
    {
      group.addInput(i8);
      group.addInput(i4);
      group.addInput(i5);
      group.addInput(i6);
      group.addInput(i7);
    }
    
    if (this.properties.size() > 0)
    {
      group.addHeadline(i18n.tr("Erweiterte Einstellungen"));
      for (Input prop:this.properties)
      {
        group.addInput(prop);
      }
    }
    
    group.addInput(getErrorLabel());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.apply);
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"process-stop.png");
    
    group.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Salden.
   * @return Checkbox.
   */
  private CheckboxInput getSyncSaldo()
  {
    if (this.syncSaldo == null)
    {
      this.syncSaldo = new CheckboxInput(options.getSyncSaldo());
      this.syncSaldo.setName(i18n.tr("Saldo abrufen"));
    }
    return this.syncSaldo;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Umsaetze.
   * @return Checkbox.
   */
  private CheckboxInput getSyncUmsatz()
  {
    if (this.syncUmsatz == null)
    {
      this.syncUmsatz = new CheckboxInput(options.getSyncKontoauszuege());
      this.syncUmsatz.setName(i18n.tr("Umsätze abrufen"));
      if (this.offline)
        this.syncUmsatz.addListener(new OfflineListener());
    }
    return this.syncUmsatz;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Kontoauszuege im PDF-Format.
   * @return Checkbox.
   */
  private CheckboxInput getSyncKontoauszug()
  {
    if (this.syncKontoauszug != null)
      return this.syncKontoauszug;
    
    this.syncKontoauszug = new CheckboxInput(options.getSyncKontoauszuegePdf());
    this.syncKontoauszug.setName(i18n.tr("Elektronischen Kontoauszug abrufen"));
    
    // Option fuer die PDF-Kontoauszuege nur aktivieren, wenn es laut BPD unterstuetzt wird
    boolean supported = KontoauszugPdfUtil.supported(this.konto);
    this.syncKontoauszug.setEnabled(supported);
    
    // Wenn es nicht unterstuetzt wird, nehmen wir auch das Haekchen raus. Egal, was der User eingestellt hatte
    if (!supported)
      this.syncKontoauszug.setValue(supported); 
    
    return this.syncKontoauszug;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der SEPA-Dauerauftraege.
   * @return Checkbox.
   */
  private CheckboxInput getSyncSepaDauer()
  {
    if (this.syncSepaDauer == null)
    {
      this.syncSepaDauer = new CheckboxInput(options.getSyncSepaDauerauftraege());
      this.syncSepaDauer.setName(i18n.tr("SEPA-Daueraufträge synchronisieren"));
    }
    return this.syncSepaDauer;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der SEPA-Ueberweisungen.
   * @return Checkbox.
   */
  private CheckboxInput getSyncAueb()
  {
    if (this.syncAueb == null)
    {
      this.syncAueb = new CheckboxInput(options.getSyncAuslandsUeberweisungen());
      this.syncAueb.setName(i18n.tr("Fällige SEPA-Überweisungen absenden"));
    }
    return this.syncAueb;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der SEPA-Lastschriften.
   * @return Checkbox.
   */
  private CheckboxInput getSyncSepaLast()
  {
    if (this.syncSepaLast == null)
    {
      this.syncSepaLast = new CheckboxInput(options.getSyncSepaLastschriften());
      this.syncSepaLast.setName(i18n.tr("Fällige SEPA-Lastschriften einziehen"));
    }
    return this.syncSepaLast;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung des Abrufs der Banknachrichten.
   * @return Checkbox.
   */
  private CheckboxInput getSyncMessages()
  {
    if (this.syncMessages != null)
      return this.syncMessages;
    
    this.syncMessages = new CheckboxInput(options.getSyncMessages());
    this.syncMessages.setName(i18n.tr("Banknachrichten abrufen"));
    
    final Input i1 = getSyncSaldo();
    final Input i2 = getSyncUmsatz();
    final Input i3 = getSyncAueb();
    final Input i4 = getSyncSepaLast();
    final Input i5 = getSyncSepaDauer();
    final Input i6 = getSyncKontoauszug();
    
    // Wir haengen hier noch einen Listener dran, der bewirkt, dass die Option nur dann auswaehlbar ist,
    // wenn wenigstens ein HBCI-Geschaeftsvorfall durchgefuehrt wird
    final Listener l = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        boolean b = ((Boolean) i1.getValue()).booleanValue();
        b |= ((Boolean) i2.getValue()).booleanValue();
        b |= ((Boolean) i3.getValue()).booleanValue();
        b |= ((Boolean) i4.getValue()).booleanValue();
        b |= ((Boolean) i5.getValue()).booleanValue();
        b |= ((Boolean) i6.getValue()).booleanValue();
        syncMessages.setEnabled(b);
      }
    };

    i1.addListener(l);
    i2.addListener(l);
    i3.addListener(l);
    i4.addListener(l);
    i5.addListener(l);
    i6.addListener(l);
    
    // einmal initial ausloesen
    l.handleEvent(null);
    
    return this.syncMessages;
  }

  /**
   * Liefert eine Checkbox, mit der die automatische Synchronisierung
   * von Offline-Konten aktiviert werden kann.
   * @return Checkbox.
   */
  private CheckboxInput getSyncOffline()
  {
    if (this.syncOffline == null)
    {
      this.syncOffline = new CheckboxInput(this.options.getSyncOffline());
      this.syncOffline.setName(i18n.tr("Passende Gegenbuchungen automatisch anlegen"));
      this.syncOffline.addListener(new OfflineListener());
    }
    return this.syncOffline;
  }

  /**
   * Liefert ein Label fuer Fehlermeldungen.
   * @return Label.
   */
  private LabelInput getErrorLabel()
  {
    if (this.error == null)
    {
      this.error = new LabelInput("\n");
      this.error.setColor(Color.ERROR);
    }
    return this.error;
  }


  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Offline und Umsatz-Abruf schliessen sich gegenseitig aus.
   */
  private class OfflineListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (offline && syncAvail)
      {
        // Wir checken, ob beides aktiv ist und bringen einen Warnhinweis
        boolean a = ((Boolean)getSyncOffline().getValue()).booleanValue();
        boolean b = ((Boolean)getSyncUmsatz().getValue()).booleanValue();
        if (a && b)
          getErrorLabel().setValue(i18n.tr("Umsatzabruf und Anlegen von Gegenbuchungen\nkönnen nicht zusammen aktiviert werden."));
        else
          getErrorLabel().setValue("\n");
        
        apply.setEnabled(!(a && b));
      }
    }
  }
}
