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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.HibiscusAddressUpdate;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftNew;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BICInput;
import de.willuhn.jameica.hbci.gui.input.IBANInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.ReminderIntervalInput;
import de.willuhn.jameica.hbci.gui.input.StoreAddressInput;
import de.willuhn.jameica.hbci.gui.input.TerminInput;
import de.willuhn.jameica.hbci.gui.input.ZweckInput;
import de.willuhn.jameica.hbci.gui.parts.SepaLastschriftList;
import de.willuhn.jameica.hbci.reminder.ReminderUtil;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaLastschrift;
import de.willuhn.jameica.messaging.MessageBus;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer SEPA-Lastschriften.
 */
public class SepaLastschriftControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private SepaLastschriftList list           = null;

  private SepaLastschrift transfer           = null;
  
  // Eingabe-Felder
  private KontoInput kontoAuswahl            = null;
  private Input betrag                       = null;
  private ZweckInput zweck                   = null;

  private AddressInput empfName              = null;
  private TextInput empfkto                  = null;
  private TextInput bic                      = null;
  private TextInput endToEndId               = null;
  private TextInput pmtInfId                 = null;
  private TextInput creditorId               = null;
  private TextInput mandateId                = null;
  private DateInput signature                = null;
  private SelectInput sequenceType           = null;
  private SelectInput type                   = null;
  private DateInput targetDate               = null;

  private TerminInput termin                 = null;
  private ReminderIntervalInput interval     = null;

  private CheckboxInput storeEmpfaenger      = null;
  private HibiscusAddressUpdate aUpdate      = new HibiscusAddressUpdate();
  
  private HibiscusAddress address            = null;
  
  
  /**
   * ct.
   * @param view
   */
  public SepaLastschriftControl(AbstractView view)
  {
    super(view);
  }
  
  /**
   * @return der Auftrag
   * @throws RemoteException
   */
  public SepaLastschrift getTransfer() throws RemoteException
  {
    if (this.transfer != null)
      return this.transfer;
    
    Object o = getCurrentObject();
    if (o instanceof SepaLastschrift)
    {
      this.transfer = (SepaLastschrift) o;
      return this.transfer;
    }
    
    this.transfer = (SepaLastschrift) Settings.getDBService().createObject(SepaLastschrift.class,null);
    return this.transfer;
  }
  
  /**
   * Liefert die Liste der SEPA-Lastschriften.
   * @return Liste der SEPA-Lastschriften.
   * @throws RemoteException
   */
  public SepaLastschriftList getSepaLastschriftListe() throws RemoteException
  {
    if (this.list == null)
      this.list = new SepaLastschriftList(new SepaLastschriftNew());
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
    
    // Wir schauen mal, ob wir in dem Auftrag schon eine Adress-ID haben
    // Wenn das der Fall ist, bearbeitet der User scheinbar gerade eine
    // existierende Lastschrift. Wir laden dann die Adresse, damit die
    // Mandats-Daten beim Speichern wieder dieser zugeordnet werden.
    try
    {
      SepaLastschrift s = this.getTransfer();
      if (!s.isNewObject())
      {
        String id = StringUtils.trimToNull(MetaKey.ADDRESS_ID.get(s));
        if (id != null)
          this.address = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,id);
      }
    }
    catch (ObjectNotFoundException e)
    {
      // Die Adresse gibts nicht mehr
    }
    catch (RemoteException re)
    {
      Logger.error("unable to restore linked address from transfer",re);
    }
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
    empfkto.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
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
   * Liefert das Eingabe-Feld fuer die Glaeubiger-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getCreditorId() throws RemoteException
  {
    if (this.creditorId == null)
    {
      this.creditorId = new TextInput(getTransfer().getCreditorId(),HBCIProperties.HBCI_SEPA_CREDITORID_MAXLENGTH);
      this.creditorId.setName(i18n.tr("Gläubiger-Identifikation"));
      this.creditorId.setValidChars(HBCIProperties.HBCI_SEPA_VALIDCHARS);
      this.creditorId.setEnabled(!getTransfer().ausgefuehrt());
      this.creditorId.setMandatory(true);
    }
    return this.creditorId;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Mandate-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getMandateId() throws RemoteException
  {
    if (this.mandateId == null)
    {
      this.mandateId = new TextInput(getTransfer().getMandateId(),HBCIProperties.HBCI_SEPA_MANDATEID_MAXLENGTH);
      this.mandateId.setName(i18n.tr("Mandats-Referenz"));
      this.mandateId.setValidChars(HBCIProperties.HBCI_SEPA_MANDATE_VALIDCHARS);
      this.mandateId.setEnabled(!getTransfer().ausgefuehrt());
      this.mandateId.setMandatory(true);
    }
    return this.mandateId;
  }

  /**
   * Liefert das Eingabe-Feld fuer das Unterschriftsdatum.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getSignatureDate() throws RemoteException
  {
    if (this.signature == null)
    {
      this.signature = new DateInput(getTransfer().getSignatureDate(),DateUtil.DEFAULT_FORMAT);
      this.signature.setName(i18n.tr("Unterschriftsdatum des Mandats"));
      this.signature.setEnabled(!getTransfer().ausgefuehrt());
      this.signature.setMandatory(true);
    }
    return this.signature;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Sequenztyp.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getSequenceType() throws RemoteException
  {
    if (this.sequenceType == null)
    {
      this.sequenceType = new SelectInput(SepaLastSequenceType.values(),getTransfer().getSequenceType());
      this.sequenceType.setName(i18n.tr("Sequenz-Typ"));
      this.sequenceType.setEnabled(!getTransfer().ausgefuehrt());
      this.sequenceType.setMandatory(true);
    }
    return this.sequenceType;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Typ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getType() throws RemoteException
  {
    if (this.type == null)
    {
      this.type = new SelectInput(SepaLastType.values(),getTransfer().getType());
      this.type.setName(i18n.tr("Lastschrift-Art"));
      this.type.setEnabled(!getTransfer().ausgefuehrt());
    }
    return this.type;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer das Ausfuehrungsdatum.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getTargetDate() throws RemoteException
  {
    if (this.targetDate == null)
    {
      this.targetDate = new DateInput(getTransfer().getTargetDate(),DateUtil.DEFAULT_FORMAT);
      this.targetDate.setName(i18n.tr("Zieltermin"));
      this.targetDate.setEnabled(!getTransfer().ausgefuehrt());
      this.targetDate.setMandatory(true);
    }
    return this.targetDate;
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
   * Speichert den Geld-Transfer.
   * @return true, wenn das Speichern erfolgreich war, sonst false.
   */
  public synchronized boolean handleStore()
  {
    SepaLastschrift t = null;
    
    try
    {
      t = this.getTransfer();
      if (t.ausgefuehrt())
        return true;
      
      t.transactionBegin();

      Double d = (Double) getBetrag().getValue();
      t.setBetrag(d == null ? Double.NaN : d.doubleValue());
      
      Konto k = (Konto)getKontoAuswahl().getValue();
      t.setKonto(k);
      t.setZweck((String)getZweck().getValue());
      t.setTermin((Date) getTermin().getValue());
      t.setEndtoEndId((String) getEndToEndId().getValue());
      t.setPmtInfId((String) getPmtInfId().getValue());
      t.setCreditorId(StringUtils.trimToNull((String) getCreditorId().getValue()));
      t.setMandateId((String) getMandateId().getValue());
      t.setSignatureDate((Date) getSignatureDate().getValue());
      t.setSequenceType((SepaLastSequenceType)getSequenceType().getValue());
      t.setType((SepaLastType)getType().getValue());
      t.setTargetDate((Date) getTargetDate().getValue());

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
        
        // wenn sie in der Action gespeichert wurde, sollte sie jetzt eine ID haben und wir koennen die Meta-Daten dran haengen
        if (e.getID() != null)
          this.address = e;
      }
      
      // Glaeubiger-ID im Konto speichern, damit wir sie beim naechsten Mal parat haben
      MetaKey.SEPA_CREDITOR_ID.set(k,t.getCreditorId());
      
      // Daten des Mandats als Meta-Daten an der Adresse speichern
      if (this.address != null)
      {
        MetaKey.SEPA_MANDATE_ID.set(this.address,t.getMandateId());
        MetaKey.SEPA_SEQUENCE_CODE.set(this.address,t.getSequenceType().name());
        MetaKey.SEPA_MANDATE_SIGDATE.set(this.address,DateUtil.DEFAULT_FORMAT.format(t.getSignatureDate()));
        
        // Adress-ID am Auftrag speichern, damit wir nach erfolgreicher Ausfuehrung des Auftrages den
        // Sequence-Typ von FRST auf RCUR setzen koennen
        MetaKey.ADDRESS_ID.set(t,this.address.getID());
      }
      
      t.transactionCommit();

      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftrag gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      MessageBus.send("hibiscus.transfer.check",t);
      
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
    private KontoFilter foreign = KontoFilter.createForeign(SynchronizeJobSepaLastschrift.class);

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
        
        // Checken, ob wir im Konto eine Glaeubiger-ID haben
        String creditorId = StringUtils.trimToNull(MetaKey.SEPA_CREDITOR_ID.get(konto));
        if (creditorId != null)
          getCreditorId().setValue(creditorId);
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
          String zweck = StringUtils.trimToNull((String) getZweck().getValue());
          if (zweck == null)
          {
            // Verwendungszweck vervollstaendigen
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
        }
        catch (Exception e)
        {
          Logger.error("unable to autocomplete subject",e);
        }

        // Checken, ob wir in der Adresse Mandats-Daten haben
        if (a instanceof HibiscusAddress)
        {
          HibiscusAddress addressCur = address;
          
          // Wir merken uns die ausgewaehlte Adresse fuer die spaetere Speicherung dieser Daten an der Adresse.
          address = (HibiscusAddress) a;
          
          String miNew = StringUtils.trimToNull(MetaKey.SEPA_MANDATE_ID.get(address));
          String sdNew = StringUtils.trimToNull(MetaKey.SEPA_MANDATE_SIGDATE.get(address));
          String scNew = StringUtils.trimToNull(MetaKey.SEPA_SEQUENCE_CODE.get(address));
          String miCur               = StringUtils.trimToNull((String)getMandateId().getValue());
          Date sdCur                 = (Date) getSignatureDate().getValue();
          
          if (miNew != null)
            getMandateId().setValue(miNew);
          
          if (sdNew != null)
            getSignatureDate().setValue(sdNew);
          
          if (scNew != null)
          {
            try
            {
              SepaLastSequenceType type = SepaLastSequenceType.valueOf(scNew);
              getSequenceType().setValue(type);
            }
            catch (Exception e)
            {
              Logger.error("unable to determine enum value of SepaLastSequenceType for " + scNew,e);
            }
          }
          
          // Hatten wir vorher Eingaben drin?
          boolean haveCur = miCur != null || sdCur != null;
          
          // Haben wir jetzt komplett neue Eingaben drin?
          boolean haveNew = miNew != null && sdNew != null && scNew != null;
          
          // Wenn eine *andere* Adresse ausgewaehlt wurde und vorher schon Mandatsdaten drin
          // standen und wir die nicht komplett ueberschrieben haben, zeigen wir einen Warnhinweis an
          if (addressCur != null && !BeanUtil.equals(address,addressCur) && haveCur && !haveNew)
          {
            String msg = i18n.tr("Sie haben eine neue Adresse ausgewählt zu der noch keine vollständigen Mandatsdaten\n" +
            		                 "hinterlegt sind. Die Daten des Mandats stammen u.U. noch von der vorher ausgewählten\n" +
            		                 "Adresse.\n\nMandats-Referenz und Unterschriftsdatum entfernen und neu eingeben?");
            
            boolean clear = Application.getCallback().askUser(msg);
            if (clear)
            {
              getMandateId().setValue("");
              getSignatureDate().setValue(null);
              getSequenceType().setValue(SepaLastSequenceType.FRST);
              getMandateId().focus();
            }
          }
          
        }
        
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while choosing empfaenger",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Auswahl des Gegenkontos"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }

}
