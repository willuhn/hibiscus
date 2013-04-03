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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den die Synchronisierungs-Optionen fuer ein Konto eingestellt werden koennen.
 */
public class SynchronizeOptionsDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private boolean offline            = false;
  private SynchronizeOptions options = null;
  private CheckboxInput syncOffline  = null;
  private CheckboxInput syncSaldo    = null;
  private CheckboxInput syncUmsatz   = null;
  private CheckboxInput syncUeb      = null;
  private CheckboxInput syncLast     = null;
  private CheckboxInput syncDauer    = null;
  private CheckboxInput syncAueb     = null;

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
    this.options = new SynchronizeOptions(konto);
    this.offline = konto.hasFlag(Konto.FLAG_OFFLINE);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);

    if (offline)
    {
      group.addText(i18n.tr("Bitte wählen Sie die Synchronisierungsoptionen für das Offline-Konto."),false);
      group.addInput(getSyncOffline());
    }
    else
    {
      group.addText(i18n.tr("Bitte wählen Sie aus, welche Geschäftsvorfälle bei\nder Synchronisierung des Kontos ausgeführt werden sollen."),false);
      group.addInput(getSyncSaldo());
      group.addInput(getSyncUmsatz());
      group.addInput(getSyncUeb());
      group.addInput(getSyncAueb());
      group.addInput(getSyncLast());
      group.addInput(getSyncDauer());
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (offline)
        {
          options.setSyncOffline(((Boolean)getSyncOffline().getValue()).booleanValue());
        }
        else
        {
          options.setSyncSaldo(((Boolean)getSyncSaldo().getValue()).booleanValue());
          options.setSyncKontoauszuege(((Boolean)getSyncUmsatz().getValue()).booleanValue());
          options.setSyncUeberweisungen(((Boolean)getSyncUeb().getValue()).booleanValue());
          options.setSyncLastschriften(((Boolean)getSyncLast().getValue()).booleanValue());
          options.setSyncDauerauftraege(((Boolean)getSyncDauer().getValue()).booleanValue());
          options.setSyncAuslandsUeberweisungen(((Boolean)getSyncAueb().getValue()).booleanValue());
        }
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"process-stop.png");
    
    group.addButtonArea(buttons);
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
      this.syncUmsatz.setName(i18n.tr("Kontoauszüge (Umsätze) abrufen"));
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
    }
    return this.syncOffline;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
}
