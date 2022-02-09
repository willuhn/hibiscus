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
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.ReminderIntervalInput;
import de.willuhn.jameica.hbci.gui.input.TerminInput;
import de.willuhn.jameica.hbci.reminder.ReminderUtil;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des Controllers fuer die Dialog Liste der Sammellastschriften/Sammel-Überweisungen.
 * @param <T> der konkrete Typ des Sammel-Auftrages.
 */
public abstract class AbstractSammelTransferControl<T extends SammelTransfer> extends AbstractControl
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private KontoInput kontoAuswahl	       = null;
  private Input name                     = null;
  private TerminInput termin             = null;
  private ReminderIntervalInput interval = null;
  private Input summe                    = null;

  /**
   * ct.
   * @param view
   */
  public AbstractSammelTransferControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert den aktuellen Sammel-Auftrag.
   * @return Sammel-Auftrag.
   * @throws RemoteException
   */
  public abstract T getTransfer() throws RemoteException;

  /**
   * Liefert eine Tabelle mit den existierenden Sammel-Auftraegen.
   * @return Liste der Sammellastschriften.
   * @throws RemoteException
   */
  public abstract TablePart getListe() throws RemoteException;

  /**
   * Liefert eine Liste mit den in diesem Sammel-Auftrag enthaltenen Buchungen.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public abstract TablePart getBuchungen() throws RemoteException;

  /**
   * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    Konto k = getTransfer().getKonto();
    KontoListener kl = new KontoListener();
    this.kontoAuswahl = new KontoInput(k,getTransfer().isNewObject() ? KontoFilter.ONLINE : KontoFilter.ALL); // Falls nachtraeglich das Konto deaktiviert wurde
    this.kontoAuswahl.setRememberSelection("auftraege",false); // BUGZILLA 1362 - zuletzt ausgewaehltes Konto gleich uebernehmen
    this.kontoAuswahl.setMandatory(true);
    this.kontoAuswahl.addListener(kl);
    this.kontoAuswahl.setEnabled(!getTransfer().ausgefuehrt());
    
    // einmal ausloesen
    kl.handleEvent(null);

    
    return this.kontoAuswahl;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TerminInput getTermin() throws RemoteException
  {
    if (this.termin == null)
      this.termin = new TerminInput((Terminable) getTransfer());
    return termin;
  }

  /**
   * Liefert das Intervall fuer die zyklische Ausfuehrung.
   * @return Auswahlfeld.
   * @throws Exception
   */
  public ReminderIntervalInput getReminderInterval() throws Exception
  {
    if (this.interval != null)
      return this.interval;
    
    this.interval = new ReminderIntervalInput((Terminable) getTransfer(),(Date)getTermin().getValue());
    return this.interval;
  }

  /**
   * Liefert ein Anzeige-Feld mit der Gesamt-Summe der Buchungen.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSumme() throws RemoteException
  {
    if (this.summe != null)
      return this.summe;
    this.summe = new LabelInput(HBCI.DECIMALFORMAT.format(getTransfer().getSumme()));
    Konto k = getTransfer().getKonto();
    this.summe.setComment(k != null ? k.getWaehrung() : "");
    return this.summe;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den Namen des Sammel-Auftrages.
   * @return Name des Sammel-Auftrages.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
  {
    if (name != null)
      return name;
    name = new TextInput(getTransfer().getBezeichnung());
    name.setMandatory(true);
    name.setEnabled(!getTransfer().ausgefuehrt());
    return name;
  }

  /**
   * Speichert den Auftrag.
   * @return true, wenn der Auftrag erfolgreich gespeichert werden konnte.
   */
  public synchronized boolean handleStore()
  {
    SammelTransfer t = null;
    
    try
    {
      t = this.getTransfer();
      if (t.ausgefuehrt()) // BUGZILLA 1197
        return true;
      t.transactionBegin();
      
      t.setKonto((Konto)getKontoAuswahl().getValue());
      t.setBezeichnung((String)getName().getValue());
      t.setTermin((Date)getTermin().getValue());
      t.store();

      // Reminder-Intervall speichern
      ReminderIntervalInput input = this.getReminderInterval();
      if (input.containsInterval())
        ReminderUtil.apply(t,(ReminderInterval) input.getValue(), input.getEnd());

      t.transactionCommit();
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sammel-Auftrag gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (Exception e)
    {
      if (t != null) {
        try {
          t.transactionRollback();
        }
        catch (Exception xe) {
          Logger.error("rollback failed",xe);
        }
      }
      
      if (e instanceof ApplicationException)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      else
      {
        Logger.error("error while saving order",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      }
    }
    return false;
  }
  
  /**
   * Listener, der die Auswahl des Kontos ueberwacht und die Waehrungsbezeichnung
   * hinter dem Betrag abhaengig vom ausgewaehlten Konto anpasst.
   */
  private class KontoListener implements Listener
  {
    @Override
    public void handleEvent(Event event) {

      try {
        Object o = getKontoAuswahl().getValue();
        if (o == null || !(o instanceof Konto))
          return;

        Konto konto = (Konto) o;

        // Wird u.a. benoetigt, damit anhand des Auftrages ermittelt werden
        // kann, wieviele Zeilen Verwendungszweck jetzt moeglich sind
        getTransfer().setKonto(konto);
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating konto",er);
      }
    }
  }

  /**
   * Contextmenu-Item zum Loeschen von Buchungen.
   */
  class DeleteMenuItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     */
    public DeleteMenuItem()
    {
      super(i18n.tr("Buchung(en) löschen..."),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          new DBObjectDelete().handleAction(context);
          try
          {
            getSumme().setValue(HBCI.DECIMALFORMAT.format(getTransfer().getSumme()));
          }
          catch (RemoteException e)
          {
            Logger.error("unable to refresh summary",e);
          }
        }
      }, "user-trash-full.png");
    }
    
    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o == null)
        return false;
      try
      {
        return !getTransfer().ausgefuehrt() && super.isEnabledFor(o);
      }
      catch (Exception e)
      {
        Logger.error("error while checking menu item",e);
      }
      return false;
    }
  }
  
  /**
   * Contextmenu-Item zum Erstellen einer Buchung.
   */
  class CreateMenuItem extends ContextMenuItem
  {
    /**
     * ct.
     * @param action
     */
    public CreateMenuItem(final Action action)
    {
      super(i18n.tr("Neue Buchung..."),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          if (handleStore())
          {
            try
            {
              action.handleAction(getTransfer());
            }
            catch (RemoteException e)
            {
              Logger.error("unable to load sammelueberweisung",e);
              throw new ApplicationException(i18n.tr("Fehler beim Laden des Sammel-Auftrages"));
            }
          }
        }
      },"text-x-generic.png");
    }
    
    @Override
    public boolean isEnabledFor(Object o)
    {
      try
      {
        return !getTransfer().ausgefuehrt() && super.isEnabledFor(o);
      }
      catch (Exception e)
      {
        Logger.error("error while checking menu item",e);
      }
      return false;
    }
  }
}
