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
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.HibiscusAddressUpdate;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BICInput;
import de.willuhn.jameica.hbci.gui.input.IBANInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.PurposeCodeInput;
import de.willuhn.jameica.hbci.gui.input.ReminderIntervalInput;
import de.willuhn.jameica.hbci.gui.input.StoreAddressInput;
import de.willuhn.jameica.hbci.gui.input.TerminInput;
import de.willuhn.jameica.hbci.gui.input.ZweckInput;
import de.willuhn.jameica.hbci.gui.parts.AuslandsUeberweisungList;
import de.willuhn.jameica.hbci.reminder.ReminderUtil;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer Auslandsueberweisungen.
 */
public class AuslandsUeberweisungControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private AuslandsUeberweisungList list      = null;

  private AuslandsUeberweisung transfer      = null;
  
  // Eingabe-Felder
  private KontoInput kontoAuswahl            = null;
  private Input betrag                       = null;
  private ZweckInput zweck                   = null;

  private AddressInput empfName              = null;
  private TextInput empfkto                  = null;
  private TextInput bic                      = null;
  private TextInput endToEndId               = null;
  private TextInput pmtInfId                 = null;
  private PurposeCodeInput purposeCode       = null;

  private TerminInput termin                 = null;
  private SelectInput typ                    = null;
  private ReminderIntervalInput interval     = null;

  private CheckboxInput storeEmpfaenger      = null;
  private HibiscusAddressUpdate aUpdate      = new HibiscusAddressUpdate();
  
  private Listener terminListener            = new TerminListener();
  
  
  /**
   * ct.
   * @param view
   */
  public AuslandsUeberweisungControl(AbstractView view)
  {
    super(view);
  }
  
  /**
   * @return der Auftrag
   * @throws RemoteException
   */
  public AuslandsUeberweisung getTransfer() throws RemoteException
  {
    if (this.transfer != null)
      return this.transfer;
    
    Object o = getCurrentObject();
    if (o instanceof AuslandsUeberweisung)
    {
      this.transfer = (AuslandsUeberweisung) o;
      return this.transfer;
    }
    
    this.transfer = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
    return this.transfer;
  }
  
  /**
   * Liefert die Liste der Auslandsueberweisungen.
   * @return Liste der Auslandsueberweisungen.
   * @throws RemoteException
   */
  public AuslandsUeberweisungList getAuslandsUeberweisungListe() throws RemoteException
  {
    if (this.list == null)
      this.list = new AuslandsUeberweisungList(new AuslandsUeberweisungNew());
    return this.list;
  }

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
   * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AddressInput getEmpfaengerName() throws RemoteException
  {
    if (empfName != null)
      return empfName;
    empfName = new AddressInput(getTransfer().getGegenkontoName(), AddressFilter.FOREIGN);
    empfName.setMandatory(true);
    empfName.addListener(new EmpfaengerListener());
    empfName.setEnabled(!getTransfer().ausgefuehrt());
    return empfName;
  }

  
  /**
   * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getEmpfaengerKonto() throws RemoteException
  {
    if (empfkto != null)
      return empfkto;

    empfkto = new IBANInput(getTransfer().getGegenkontoNummer(),this.getEmpfaengerBic());
    empfkto.setMandatory(true);
    empfkto.setEnabled(!getTransfer().ausgefuehrt());
    return empfkto;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerBic() throws RemoteException
  {
    if (this.bic == null)
    {
      this.bic = new BICInput(getTransfer().getGegenkontoBLZ());
      this.bic.setEnabled(!getTransfer().ausgefuehrt());
    }
    return this.bic;
  }

  /**
   * Liefert das Eingabe-Feld fuer die End2End-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEndToEndId() throws RemoteException
  {
    if (this.endToEndId == null)
    {
      this.endToEndId = new TextInput(getTransfer().getEndtoEndId(),HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      this.endToEndId.setName(i18n.tr("End-to-End ID"));
      this.endToEndId.setValidChars(HBCIProperties.HBCI_SEPA_VALIDCHARS);
      this.endToEndId.setEnabled(!getTransfer().ausgefuehrt());
      this.endToEndId.setHint(i18n.tr("freilassen wenn nicht benötigt"));
      this.endToEndId.setMandatory(false);
    }
    return this.endToEndId;
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
   * Liefert das Eingabe-Feld fuer den Purpose-Code.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPurposeCode() throws RemoteException
  {
    if (this.purposeCode != null)
      return this.purposeCode;

    this.purposeCode = new PurposeCodeInput(getTransfer().getPurposeCode());
    this.purposeCode.setEnabled(!getTransfer().ausgefuehrt());
    this.purposeCode.setMandatory(false);
    return this.purposeCode;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Verwendungszweck.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getZweck() throws RemoteException
  {
    if (zweck != null)
      return zweck;
    zweck = new ZweckInput(getTransfer().getZweck());
    zweck.setEnabled(!getTransfer().ausgefuehrt());
    return zweck;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Betrag.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBetrag() throws RemoteException
  {
    if (betrag != null)
      return betrag;
    HibiscusTransfer t = getTransfer();
    double d = t.getBetrag();
    if (d == 0.0d) d = Double.NaN;
    betrag = new DecimalInput(d,HBCI.DECIMALFORMAT);

    Konto k = t.getKonto();
    betrag.setComment(k == null ? "" : k.getWaehrung());
    betrag.setMandatory(true);
    betrag.setEnabled(!getTransfer().ausgefuehrt());
    
    new KontoListener().handleEvent(null);

    return betrag;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TerminInput getTermin() throws RemoteException
  {
    if (this.termin != null)
      return this.termin;
    
    this.termin = new TerminInput((Terminable) getTransfer());
    this.termin.setName(this.termin.getName() + "  "); // ein kleines bisschen extra Platz lassen, damit auch "Ausführungstermin" hin passt
    this.termin.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          if (!termin.hasChanged())
            return;
          
          Date date = (Date) termin.getValue();
          if (date == null)
            return;
          
          // Wenn das Datum eine Woche in der Zukunft liegt, fragen wir den User, ob es vielleicht
          // eine Terminueberweisung werden soll. Muessen wir aber nicht fragen, wenn
          // der User nicht ohnehin schon eine Termin-Ueberweisung ausgewaehlt hat
          Typ typ = (Typ) getTyp().getValue();
          if (typ == null || typ == Typ.TERMIN)
            return;

          Calendar cal = Calendar.getInstance();
          cal.setTime(DateUtil.startOfDay(new Date()));
          cal.add(Calendar.DATE,6);
          if (DateUtil.startOfDay(date).after(cal.getTime()))
          {
            String q = i18n.tr("Soll der Auftrag als bankseitig geführte SEPA-Terminüberweisung ausgeführt werden?");
            if (Application.getCallback().askUser(q))
              getTyp().setValue(Typ.TERMIN);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to check for termueb",e);
        }
        
      }
    });
    
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
   * Liefert eine CheckBox ueber die ausgewaehlt werden kann,
   * ob der Empfaenger mitgespeichert werden soll.
   * @return CheckBox.
   * @throws RemoteException
   */
  public CheckboxInput getStoreEmpfaenger() throws RemoteException
  {
    if (storeEmpfaenger != null)
      return storeEmpfaenger;

    this.storeEmpfaenger = new StoreAddressInput(this.getTransfer());
    return this.storeEmpfaenger;
  }
  
  /**
   * Liefert eine Combobox zur Auswahl des Auftragstyps.
   * Zur Wahl stehen Ueberweisung, Termin-Ueberweisung und Umbuchung.
   * @return die Combobox.
   * @throws RemoteException
   */
  public SelectInput getTyp() throws RemoteException
  {
    if (this.typ != null)
      return this.typ;
    final AuslandsUeberweisung u = getTransfer();
    
    this.typ = new SelectInput(Typ.values(),Typ.determine(u));
    this.typ.setName(i18n.tr("Auftragstyp"));
    this.typ.setAttribute("description");
    this.typ.setEnabled(!u.ausgefuehrt());
    this.typ.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Wir muessen die Entscheidung, ob es eine Termin-Ueberweisung ist,
        // sofort im Objekt speichern, denn die Information wird von
        // "getTermin()" gebraucht, um zu erkennen, ob der Auftrag faellig ist
        try
        {
          Typ t = (Typ) getTyp().getValue();
          u.setTerminUeberweisung(t != null && t == Typ.TERMIN);
        }
        catch (Exception e)
        {
          Logger.error("unable to set flag",e);
        }
      }
    });
    
    this.typ.addListener(this.terminListener);
    this.terminListener.handleEvent(null); // einmal initial ausloesen
    return this.typ;
  }

  /**
   * Speichert den Geld-Transfer.
   * @return true, wenn das Speichern erfolgreich war, sonst false.
   */
  public synchronized boolean handleStore()
  {
    AuslandsUeberweisung t = null;
    
    try
    {
      t = this.getTransfer();
      if (t.ausgefuehrt()) // BUGZILLA 1197
        return true;
      
      t.transactionBegin();

      Double d = (Double) getBetrag().getValue();
      t.setBetrag(d == null ? Double.NaN : d.doubleValue());
      
      t.setKonto((Konto)getKontoAuswahl().getValue());
      t.setZweck((String)getZweck().getValue());
      t.setTermin((Date) getTermin().getValue());
      t.setEndtoEndId((String) getEndToEndId().getValue());
      t.setPmtInfId((String) getPmtInfId().getValue());
      t.setPurposeCode((String)getPurposeCode().getValue());

      Typ typ = (Typ) getTyp().getValue();
      if (typ != null)
        typ.apply(t);

      String kto  = (String)getEmpfaengerKonto().getValue();
      String name = getEmpfaengerName().getText();
      String bic  = (String) getEmpfaengerBic().getValue();

      t.setGegenkontoNummer(kto);
      t.setGegenkontoName(name);
      t.setGegenkontoBLZ(bic);
      
      t.store();

      // Reminder-Intervall speichern
      ReminderIntervalInput input = this.getReminderInterval();
      if (input.containsInterval())
        ReminderUtil.apply(t,(ReminderInterval) input.getValue(), input.getEnd());

      {
        final Boolean store = (Boolean) getStoreEmpfaenger().getValue();
        this.aUpdate.setCreate(store.booleanValue());
        HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
        e.setIban(kto);
        e.setName(name);
        e.setBic(bic);
        this.aUpdate.handleAction(e);
      }
      GUI.getStatusBar().setSuccessText(i18n.tr("Auftrag gespeichert"));
      t.transactionCommit();

      if (t.getBetrag() > Settings.getUeberweisungLimit())
        GUI.getView().setErrorText(i18n.tr("Warnung: Auftragslimit überschritten: {0} ", HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + getTransfer().getKonto().getWaehrung()));
      
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
   * Eigener ueberschriebener Kontofilter.
   */
  private class MyKontoFilter extends KontoFilter
  {
    // Wir leiten die Anfrage an den weiter
    private KontoFilter foreign = KontoFilter.createForeign(SynchronizeJobSepaUeberweisung.class);

    private boolean found = false;

    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
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
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

      try {
        Object o = getKontoAuswahl().getValue();
        if (o == null || !(o instanceof Konto))
        {
          getBetrag().setComment("");
          return;
        }

        Konto konto = (Konto) o;
        getBetrag().setComment(konto.getWaehrung());
        getTransfer().setKonto(konto);
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating currency",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei Ermittlung der Währung"));
      }
    }
  }

  /**
   * Listener, der bei Auswahl des Empfaengers die restlichen Daten vervollstaendigt.
   */
  private class EmpfaengerListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
      if (event == null)
        return;
      
      if (!(event.data instanceof Address))
        return;
      
      Address a = (Address) event.data;
      aUpdate.setAddress(a);

      try {
        getEmpfaengerName().setText(a.getName());
        getEmpfaengerKonto().setValue(a.getIban());
        getEmpfaengerBic().setValue(a.getBic());

        // Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
        getStoreEmpfaenger().setValue(Boolean.FALSE);
        
        try
        {
          String zweck = (String) getZweck().getValue();
          if ((zweck != null && zweck.length() > 0))
            return;
          
          DBIterator list = getTransfer().getList();
          list.addFilter("empfaenger_konto = ?",a.getIban());
          list.addFilter("empfaenger_name = ?",a.getName());
          list.setOrder("order by id desc");
          if (list.hasNext())
          {
            HibiscusTransfer t = (HibiscusTransfer) list.next();
            getZweck().setValue(t.getZweck());
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to autocomplete subject",e);
        }

          
      }
      catch (RemoteException er)
      {
        Logger.error("error while choosing empfaenger",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Empfängers"));
      }
    }
  }
  
  /**
   * Listener, der das Label vor dem Termin aendert, wenn es eine Bank-seitig gefuehrte Termin-Ueberweisung ist.
   */
  private class TerminListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      // BUGZILLA 1778 - Das Label wurde unter Ubuntu nicht sofort aktualisiert.
      // Eventuell hilft die asynchrone Ausfuehrung.
      GUI.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run()
        {
          try
          {
            TerminInput input = getTermin();
            Typ typ = (Typ) getTyp().getValue();
            if (typ != null && typ == Typ.TERMIN)
            {
              input.setName(i18n.tr("Ausführungstermin"));
              
              // Pruefen, ob es sich um eine Termin-Ueberweisung handelt. Wenn
              // das Ausfuehrungsdatum in der Vergangenheit liegt, dann Hinweis-Text anzeigen
              Date date = (Date) input.getValue();
              if (date != null)
              {
                if (!DateUtil.startOfDay(date).after(DateUtil.startOfDay(new Date())))
                  Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Ausführungstermin der Terminüberweisung liegt in der Vergangenheit"),StatusBarMessage.TYPE_INFO));
              }
            }
            else
            {
              input.setName(i18n.tr("Erinnerungstermin"));
            }
            
            // Kommentar vom Termin-Eingabefeld aktualisieren.
            input.updateComment();
          }
          catch (Exception e)
          {
            Logger.error("unable to update label",e);
          }
        }
      });
    }
  }
  
  /**
   * Hilfsklasse fuer den Auftragstyp.
   */
  public enum Typ
  {
    /**
     * Ueberweisung
     */
    UEBERWEISUNG("Überweisung"),
    
    /**
     * Terminueberweisung
     */
    TERMIN("Bankseitige SEPA-Terminüberweisung"),

    /**
     * Umbuchung
     */
    UMBUCHUNG("Interne Umbuchung (Übertrag)"),
    
    /**
     * Echtzeitueberweisung
     */
    INSTANT("Echtzeitüberweisung (SEPA Instant-Payment)"),
    
    ;
    
    private String description = null;
    
    /**
     * ct.
     * @param description der Beschreibungstext.
     */
    private Typ(String description)
    {
      this.description = description;
    }
    
    /**
     * Liefert den Beschreibungstext.
     * @return description
     */
    public String getDescription()
    {
      return i18n.tr(description);
    }
    
    /**
     * Ermittelt den Typ anhand des Auftrages.
     * @param u der Auftrag.
     * @return der Typ.
     * @throws RemoteException
     */
    public static Typ determine(AuslandsUeberweisung u) throws RemoteException
    {
      if (u.isInstantPayment())
        return INSTANT;
      
      if (u.isTerminUeberweisung())
        return TERMIN;
      
      if (u.isUmbuchung())
        return UMBUCHUNG;
      
      return UEBERWEISUNG;
    }
    
    /**
     * Uebernimmt den Typ in den Auftrag.
     * @param u der Auftrag.
     * @throws RemoteException
     */
    public void apply(AuslandsUeberweisung u) throws RemoteException
    {
      u.setInstantPayment(this == INSTANT);
      u.setUmbuchung(this == UMBUCHUNG);
      u.setTerminUeberweisung(this == TERMIN);
    }
  }

}
