/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.BPDUtil.Query;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszugPdf;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragList;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaLastschrift;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
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
  
  private final static int WINDOW_WIDTH = 500;
  
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
  private CheckboxInput useCamt         = null;
  private LabelInput error              = null;
  private Button apply                  = null;
  
  private Map<SynchronizeBackend,List<Input>> properties = new HashMap<SynchronizeBackend,List<Input>>();

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
          List<Input> props = new ArrayList<Input>();
          this.properties.put(backend,props);
          for (String name:names)
          {
            props.add(this.createCustomProperty(name));
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
   * @return das erzeugte Eingabefeld.
   * @throws RemoteException
   */
  private Input createCustomProperty(String name) throws RemoteException
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
    return t;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);

    group.addHeadline(this.konto.getLongName());

    group.addText(i18n.tr("Bitte wählen Sie aus, welche Geschäftsvorfälle bei der " +
    		                  "Synchronisierung des Kontos ausgeführt werden sollen."),true);
    
    this.apply = new Button(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        
        if (!offline || syncAvail) // Entweder bei Online-Konten oder bei welchen mit neuem Scripting-Support
        {
          options.setSyncSaldo(((Boolean)getSyncSaldo().getValue()).booleanValue());
          options.setSyncKontoauszuege(((Boolean)getSyncUmsatz().getValue()).booleanValue());
          
          Support support = BPDUtil.getSupport(konto,Query.UmsatzCamt);
          if (support != null && support.isSupported())
          {
            try
            {
              Boolean value = (Boolean) getUseCamt().getValue();
              MetaKey.UMSATZ_CAMT.set(konto,value.toString());
            }
            catch (RemoteException re)
            {
              Logger.error("unable to save changes",re);
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Übernehmen der Einstellungen fehlgeschlagen: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
            }
          }
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
          for (List<Input> l:properties.values())
          {
            for (Input prop:l)
            {
              Object value = prop.getValue();
              konto.setMeta(prop.getName(),value != null ? value.toString() : null);
            }
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
    
    Input i1  = this.getSyncSaldo();
    Input i2  = this.getSyncUmsatz();
    Input i3  = this.getSyncOffline();
    Input i4  = this.getSyncAueb();
    Input i5  = this.getSyncSepaLast();
    Input i6  = this.getSyncSepaDauer();
    Input i7  = this.getSyncMessages();
    Input i8  = this.getSyncKontoauszug();

    Input camt = null;

    if (!offline || syncAvail)
    {
      // Wir stellen die Option nur zur Verfuegung, wenn das Konto es prinzipiell unterstuetzt
      Support support = BPDUtil.getSupport(this.konto,Query.UmsatzCamt);
      
      // Wichtig: Das Input muss erzeugt werden, bevor getSyncUmsatz gezeichnet wird. Sonst wird der Listener nicht mehr registriert
      if (!offline && support != null && support.isSupported())
        camt = this.getUseCamt();
      
      group.addInput(i1);
      group.addInput(i2);
    }
    
    if (offline)
    {
      group.addInput(i3);
    }
    else
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine = service.get(SynchronizeEngine.class);
      
      if (engine.supports(SynchronizeJobKontoauszugPdf.class,this.konto)) group.addInput(i8);
      if (engine.supports(SynchronizeJobSepaUeberweisung.class,this.konto)) group.addInput(i4);
      if (engine.supports(SynchronizeJobSepaLastschrift.class,this.konto)) group.addInput(i5);
      if (engine.supports(SynchronizeJobSepaDauerauftragList.class,this.konto)) group.addInput(i6);
      
      // Abrufen der Nachrichten lassen wir immer zu.
      group.addInput(i7);
    }

    if (camt != null && (!offline || syncAvail))
    {
      group.addHeadline(i18n.tr("Erweiterte Einstellungen: FinTS"));
      group.addInput(camt);
    }

    if (this.properties.size() > 0)
    {
      for (Entry<SynchronizeBackend,List<Input>> e:this.properties.entrySet())
      {
        group.addHeadline(i18n.tr("Erweiterte Einstellungen: {0}",e.getKey().getName()));
        for (Input prop:e.getValue())
        {
          group.addInput(prop);
        }
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
   * Liefert eine Checkbox fuer die Aktivierung/Deaktivierung des CAMT-Formats.
   * @return Checkbox.
   */
  private CheckboxInput getUseCamt()
  {
    if (this.useCamt == null)
    {
      this.useCamt = new CheckboxInput(KontoUtil.useCamt(this.konto,false));
      this.useCamt.setName(i18n.tr("Umsätze im neuen SEPA CAMT-Format abrufen"));
      
      final CheckboxInput syncUms = this.getSyncUmsatz();
      
      final Listener l = new Listener() {
        
        @Override
        public void handleEvent(Event event)
        {
          // Option nur freischalten, wenn Umsatz-Abruf aktiviert ist
          useCamt.setEnabled((Boolean)syncUms.getValue());
        }
      };
      
      syncUms.addListener(l);
      l.handleEvent(null);
    }
    
    return this.useCamt;
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


  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Offline und Umsatz-Abruf schliessen sich gegenseitig aus.
   */
  private class OfflineListener implements Listener
  {
    @Override
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
