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
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.BatchBookInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.ReminderIntervalInput;
import de.willuhn.jameica.hbci.gui.input.TerminInput;
import de.willuhn.jameica.hbci.reminder.ReminderUtil;
import de.willuhn.jameica.hbci.rmi.BatchBookType;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des Controllers fuer die Dialog Liste der SEPA-Sammellastschriften/SEPA-Sammelüberweisungen.
 * @author willuhn
 * @param <T> der konkrete Typ des Sammel-Auftrages.
 */
public abstract class AbstractSepaSammelTransferControl<T extends SepaSammelTransfer> extends AbstractControl
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private KontoInput kontoAuswahl	       = null;
  private Input name                     = null;
  private TerminInput termin             = null;
  private ReminderIntervalInput interval = null;
  private BatchBookInput batchbook       = null;
  private TextInput pmtInfId             = null;

  /**
   * ct.
   * @param view
   */
  public AbstractSepaSammelTransferControl(AbstractView view)
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
   * Liefert die Synchronize-Job-Art.
   * @return die Synchronize-Job-Art.
   */
  public abstract Class<? extends SynchronizeJob> getSynchronizeJobType();

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
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    KontoListener kl = new KontoListener();
    MyKontoFilter filter = new MyKontoFilter();
    this.kontoAuswahl = new KontoInput(getTransfer().getKonto(),filter);
    this.kontoAuswahl.setName(i18n.tr("Persönliches Konto"));
    this.kontoAuswahl.setRememberSelection("auftraege",false); // BUGZILLA 1362 - zuletzt ausgewaehltes Konto gleich uebernehmen
    this.kontoAuswahl.setMandatory(true);
    this.kontoAuswahl.addListener(kl);
    this.kontoAuswahl.setEnabled(!getTransfer().ausgefuehrt());

    // einmal ausloesen
    kl.handleEvent(null);

    if (!filter.found)
      this.kontoAuswahl.setComment(i18n.tr("Bitte tragen Sie IBAN/BIC in Ihrem Konto ein"));

    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Auswahlfeld fuer den Batchbook-Mode.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public BatchBookInput getBatchBook() throws RemoteException
  {
    if (this.batchbook != null)
      return this.batchbook;
    
    T t = this.getTransfer();
    this.batchbook = new BatchBookInput(t.getKonto(),t);
    this.batchbook.setEnabled(!getTransfer().ausgefuehrt());
    return this.batchbook;
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
   * @throws RemoteException
   */
  public ReminderIntervalInput getReminderInterval() throws RemoteException
  {
    if (this.interval != null)
      return this.interval;
    
    this.interval = new ReminderIntervalInput((Terminable) getTransfer(),(Date)getTermin().getValue());
    return this.interval;
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
   * Liefert das Eingabe-Feld fuer die PmtInf-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPmtInfId() throws RemoteException
  {
    if (this.pmtInfId != null)
      return this.pmtInfId;

    this.pmtInfId = new TextInput(getTransfer().getPmtInfId(),HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
    this.pmtInfId.setName(i18n.tr("Referenz (Payment-Information ID)"));
    this.pmtInfId.setValidChars(HBCIProperties.HBCI_SEPA_PMTINF_VALIDCHARS);
    this.pmtInfId.setEnabled(!getTransfer().ausgefuehrt());
    this.pmtInfId.setHint(i18n.tr("freilassen wenn nicht benötigt"));
    this.pmtInfId.setMandatory(false);
    return this.pmtInfId;
  }

  /**
   * Speichert den Auftrag.
   * @throws Exception
   */
  public synchronized void store() throws Exception
  {
    SepaSammelTransfer t = this.getTransfer();
    if (t.ausgefuehrt())
      return;

    Konto k = (Konto)getKontoAuswahl().getValue();
    t.setKonto(k);
    t.setBezeichnung((String)getName().getValue());
    t.setTermin((Date)getTermin().getValue());
    t.setPmtInfId((String) getPmtInfId().getValue());
    t.store();

    // Batchbook-Mode speichern. Sowohl im Auftrag als auch im Konto als Preset fuer den naechsten Auftrag
    BatchBookType batch = (BatchBookType) this.getBatchBook().getValue();
    String value = batch != null ? batch.getValue() : null;
    MetaKey.SEPA_BATCHBOOK.set(t,value);
    MetaKey.SEPA_BATCHBOOK.set(k,t.getClass().getSimpleName(),value);
    
    // Reminder-Intervall speichern
    ReminderIntervalInput input = this.getReminderInterval();
    if (input.containsInterval())
      ReminderUtil.apply(t,(ReminderInterval) input.getValue(), input.getEnd());

    Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("SEPA-Sammelauftrag gespeichert"),StatusBarMessage.TYPE_SUCCESS));
  }

  /**
   * Speichert den Auftrag.
   * @return true, wenn das Speichern erfolgreich war, sonst false.
   */
  public synchronized boolean handleStore()
  {
    try
    {
      SepaSammelTransfer t = this.getTransfer();
      if (t.ausgefuehrt())
        return true;
      
      this.store();
      
      return true;
    }
    catch (Exception e)
    {
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
   * Eigener ueberschriebener Kontofilter.
   */
  private class MyKontoFilter extends KontoFilter
  {
    // Wir leiten die Anfrage an den weiter
    private KontoFilter foreign = KontoFilter.createForeign(getSynchronizeJobType());

    private boolean found = false;

    @Override
    public boolean accept(Konto konto) throws RemoteException
    {
      boolean b = foreign.accept(konto);
      found |= b;
      return b;
    }
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
        T transfer  = getTransfer();

        // Wird u.a. benoetigt, damit anhand des Auftrages ermittelt werden
        // kann, wieviele Zeilen Verwendungszweck jetzt moeglich sind
        transfer.setKonto(konto);
        
        // Fuer den Fall, dass der Auftrag noch keinen Batchbook-Type hat,
        // das neu ausgewaehlte Konto jedoch einen anderen als den momentanen.
        getBatchBook().update(konto,transfer);
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
      super(i18n.tr("Buchung(en) löschen..."),new DBObjectDelete(), "user-trash-full.png");
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
          try
          {
            if (handleStore())
              action.handleAction(getTransfer());
          }
          catch (OperationCanceledException oce)
          {
            // ignore
          }
          catch (ApplicationException ae)
          {
            throw ae;
          }
          catch (Exception e)
          {
            Logger.error("unable to store transfer",e);
            throw new ApplicationException(i18n.tr("Erstellen der Buchung fehlgeschlagen: {0}",e.getMessage()));
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
