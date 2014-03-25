/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/SynchronizeOptionsDialog.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/05/20 16:22:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
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
  
  private Konto konto                = null;
  private boolean offline            = false;
  private boolean syncAvail          = false;
  private SynchronizeOptions options = null;
  private CheckboxInput syncOffline  = null;
  private CheckboxInput syncSaldo    = null;
  private CheckboxInput syncUmsatz   = null;
  private CheckboxInput syncUeb      = null;
  private CheckboxInput syncLast     = null;
  private CheckboxInput syncDauer    = null;
  private CheckboxInput syncAueb     = null;
  private CheckboxInput syncSepaLast = null;
  private LabelInput error           = null;
  private Button apply               = null;
  
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
    
    if (this.offline)
    {
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
          options.setSyncUeberweisungen(((Boolean)getSyncUeb().getValue()).booleanValue());
          options.setSyncLastschriften(((Boolean)getSyncLast().getValue()).booleanValue());
          options.setSyncDauerauftraege(((Boolean)getSyncDauer().getValue()).booleanValue());
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
    
    
    if (!offline || syncAvail)
    {
      group.addInput(getSyncSaldo());
      group.addInput(getSyncUmsatz());
    }

    if (offline)
    {
      group.addInput(getSyncOffline());
    }
    else
    {
      group.addInput(getSyncUeb());
      group.addInput(getSyncAueb());
      group.addInput(getSyncLast());
      group.addInput(getSyncSepaLast());
      group.addInput(getSyncDauer());
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
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Ueberweisungen.
   * @return Checkbox.
   */
  private CheckboxInput getSyncUeb()
  {
    if (this.syncUeb == null)
    {
      this.syncUeb = new CheckboxInput(options.getSyncUeberweisungen());
      this.syncUeb.setName(i18n.tr("Fällige Überweisungen absenden"));
    }
    return this.syncUeb;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Lastschriften.
   * @return Checkbox.
   */
  private CheckboxInput getSyncLast()
  {
    if (this.syncLast == null)
    {
      this.syncLast = new CheckboxInput(options.getSyncLastschriften());
      this.syncLast.setName(i18n.tr("Fällige Lastschriften einziehen"));
    }
    return this.syncLast;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Dauerauftraege.
   * @return Checkbox.
   */
  private CheckboxInput getSyncDauer()
  {
    if (this.syncDauer == null)
    {
      this.syncDauer = new CheckboxInput(options.getSyncDauerauftraege());
      this.syncDauer.setName(i18n.tr("Daueraufträge synchronisieren"));
    }
    return this.syncDauer;
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
