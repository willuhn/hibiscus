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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.HibiscusAddressUpdate;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragExecute;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragNew;
import de.willuhn.jameica.hbci.gui.dialogs.TurnusDialog;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BICInput;
import de.willuhn.jameica.hbci.gui.input.IBANInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.PurposeCodeInput;
import de.willuhn.jameica.hbci.gui.input.StoreAddressInput;
import de.willuhn.jameica.hbci.gui.input.ZweckInput;
import de.willuhn.jameica.hbci.gui.parts.SepaDauerauftragList;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragStore;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.TypedProperties;

/**
 * Controller fuer SEPA-Dauerauftraege.
 */
public class SepaDauerauftragControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Listener nextDate          = new NextDateListener();
	private Input orderID				       = null;
	private DialogInput turnus	       = null;
	private DateInput ersteZahlung	   = null;
	private DateInput letzteZahlung	   = null;
	
  private KontoInput kontoAuswahl    = null;
  private DecimalInput betrag        = null;
  private ZweckInput zweck           = null;
  private AddressInput empfName      = null;
  private TextInput empfkto          = null;
  private TextInput bic              = null;
  private PurposeCodeInput purposeCode = null;

  private CheckboxInput storeEmpfaenger = null;
  private HibiscusAddressUpdate aUpdate = new HibiscusAddressUpdate();
	
  private SepaDauerauftrag transfer  = null;
  private TypedProperties bpd        = null;

  private SepaDauerauftragList list  = null;
  
  /**
   * ct.
   * @param view
   */
  public SepaDauerauftragControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert den aktuellen SEPA-Dauerauftrag.
	 * @return
	 * @throws RemoteException
	 */
	public SepaDauerauftrag getTransfer() throws RemoteException
	{
    if (transfer != null)
      return transfer;

    Object o = getCurrentObject();
    if (o != null && (o instanceof SepaDauerauftrag))
      return (SepaDauerauftrag) o;
      
    transfer = (SepaDauerauftrag) Settings.getDBService().createObject(SepaDauerauftrag.class,null);
    return transfer;
	}
	
	/**
	 * Liefert die passenden BPD-Parameter fuer den Auftrag.
	 * @return die BPD.
	 * @throws RemoteException
	 */
	private TypedProperties getBPD() throws RemoteException
	{
	  if (this.bpd != null)
	    return this.bpd;
	  
	  SepaDauerauftrag auftrag = this.getTransfer();
	  if (auftrag.isActive())
	  {
	    Support support = BPDUtil.getSupport(auftrag.getKonto(),BPDUtil.Query.DauerEdit);
      this.bpd = support != null ? support.getBpd() : null;
	  }

	  
    // Der Auftrag ist noch nicht aktiv oder wir konnten keine Einschraenkungen ermitteln
	  if (this.bpd == null)
	    this.bpd = new TypedProperties();
	  
	  return this.bpd;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen SEPA-Dauerauftraegen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public SepaDauerauftragList getDauerauftragListe() throws RemoteException
	{
    if (list != null)
      return list;
    list = new de.willuhn.jameica.hbci.gui.parts.SepaDauerauftragList(new SepaDauerauftragNew());
    return list;
	}

	/**
	 * Liefert ein Auswahlfeld fuer den Zahlungsturnus.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public DialogInput getTurnus() throws RemoteException
	{
		if (turnus != null)
			return turnus;

		TurnusDialog td = new TurnusDialog(TurnusDialog.POSITION_MOUSE);
		td.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Turnus choosen = (Turnus) event.data;
				try
				{
					getTransfer().setTurnus(choosen);
					getTurnus().setText(choosen.getBezeichnung());
          nextDate.handleEvent(null);
				}
				catch (RemoteException e)
				{
					Logger.error("error while choosing turnus",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Zahlungsturnus"));
				}
			}
		});

		SepaDauerauftrag da = getTransfer();
		Turnus t = da.getTurnus();
		turnus = new DialogInput(t == null ? "" : t.getBezeichnung(),td);
		turnus.setValue(t);
    turnus.setMandatory(true);
    
    if (da.isActive())
    {
      boolean changable = getBPD().getBoolean("turnuseditable",true) && getBPD().getBoolean("timeuniteditable",true);
      turnus.setEnabled(changable);
    }
    
    turnus.disableClientControl(); // Client-Control generell deaktivieren - auch wenn Aenderungen erlaubt sind
		return turnus;
	}

	/**
	 * Liefert ein Anzeige-Feld fuer die Order-ID des Dauerauftrages.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getOrderID() throws RemoteException
	{
		 if (orderID != null)
		 	return orderID;
		 orderID = new LabelInput(getTransfer().getOrderID());
		 return orderID;
	}

	/**
	 * Liefert ein Datums-Feld fuer die erste Zahlung.
   * @return Datums-Feld.
   * @throws RemoteException
   */
  public Input getErsteZahlung() throws RemoteException
	{
		if (ersteZahlung != null)
			return ersteZahlung;
    
    final SepaDauerauftrag t = getTransfer();
    Date d = t.getErsteZahlung();
    if (d == null)
    {
      d = new Date();
      t.setErsteZahlung(d);
    }

    ersteZahlung = new DateInput(d);
    ersteZahlung.setComment("");
		ersteZahlung.setTitle(i18n.tr("Datum der ersten Zahlung"));
    ersteZahlung.setText(i18n.tr("Bitte geben Sie das Datum der ersten Zahlung ein"));
    ersteZahlung.setMandatory(true);

    ersteZahlung.addListener(this.nextDate);
    
    if (t.isActive())
      ersteZahlung.setEnabled(this.canEditErsteZahlung());
    
    this.nextDate.handleEvent(null); // einmal ausloesen fuer initialen Text
		return ersteZahlung;
	}
  
  /**
   * Prueft, ob das Datum der ersten Zahlung beim Aendern von Dauerauftraegen geaendert werden darf.
   * @return true, wenn es geaendert werden darf.
   * BUGZILLA 1783
   */
  private boolean canEditErsteZahlung() throws RemoteException
  {
    return getBPD().getBoolean("firstexeceditable",true);
  }

	/**
	 * Liefert ein Datums-Feld fuer die letzte Zahlung.
	 * @return Datums-Feld.
	 * @throws RemoteException
	 */
	public Input getLetzteZahlung() throws RemoteException
	{
		if (letzteZahlung != null)
			return letzteZahlung;

		SepaDauerauftrag t = getTransfer();
    Date d = t.getLetzteZahlung();

    letzteZahlung = new DateInput(d,HBCI.DATEFORMAT);
    letzteZahlung.setComment("");
    letzteZahlung.setTitle(i18n.tr("Datum der letzten Zahlung"));
    letzteZahlung.setText(i18n.tr("Bitte geben Sie das Datum der letzten Zahlung ein"));
    letzteZahlung.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Nur, um den Parser zu triggern
        letzteZahlung.getValue();
      }
    
    });

    if (t.isActive())
      letzteZahlung.setEnabled(getBPD().getBoolean("lastexeceditable",true));
    
    return letzteZahlung;
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
    this.kontoAuswahl.setRememberSelection("auftraege",false);
    this.kontoAuswahl.setMandatory(true);
    this.kontoAuswahl.addListener(kl);
    this.kontoAuswahl.setEnabled(!getTransfer().isActive());

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
    
    SepaDauerauftrag t = getTransfer();

    empfName = new AddressInput(t.getGegenkontoName(), AddressFilter.FOREIGN);
    empfName.setValidChars(HBCIProperties.HBCI_SEPA_VALIDCHARS);
    empfName.setMandatory(true);
    empfName.addListener(new EmpfaengerListener());
    if (t.isActive())
    {
      boolean changable = getBPD().getBoolean("recnameeditable",true) && getBPD().getBoolean("recktoeditable",true);
      empfName.setEnabled(changable);
    }
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

    SepaDauerauftrag t = getTransfer();
    empfkto = new IBANInput(t.getGegenkontoNummer(),this.getEmpfaengerBic());
    empfkto.setMandatory(true);
    if (t.isActive())
      empfkto.setEnabled(getBPD().getBoolean("recktoeditable",true));
    return empfkto;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getEmpfaengerBic() throws RemoteException
  {
    if (this.bic != null)
      return this.bic;
    
    SepaDauerauftrag t = getTransfer();
    this.bic = new BICInput(t.getGegenkontoBLZ());
    if (t.isActive())
      this.bic.setEnabled(getBPD().getBoolean("recktoeditable",true));
    return this.bic;
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
   * Liefert das Eingabe-Feld fuer den Betrag.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
      return betrag;
    
    SepaDauerauftrag t = getTransfer();
    double d = t.getBetrag();
    if (d == 0.0d) d = Double.NaN;
    betrag = new DecimalInput(d,HBCI.DECIMALFORMAT);

    Konto k = t.getKonto();
    betrag.setComment(k == null ? "" : k.getWaehrung());
    betrag.setMandatory(true);
    if (t.isActive())
      betrag.setEnabled(getBPD().getBoolean("valueeditable",true));
    
    new KontoListener().handleEvent(null);

    return betrag;
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
    this.purposeCode.setMandatory(false);
    return this.purposeCode;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Verwendungszweck.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public ZweckInput getZweck() throws RemoteException
  {
    if (zweck != null)
      return zweck;
    
    SepaDauerauftrag t = getTransfer();
    zweck = new ZweckInput(getTransfer().getZweck());
    zweck.setMandatory(true);
    if (t.isActive())
      zweck.setEnabled(getBPD().getBoolean("usageeditable",true));
    return zweck;
  }
  
  /**
   * Fuehrt den Dauerauftrag aus.
   */
  public synchronized void handleExecute()
  {
    try
    {
      SepaDauerauftrag t = this.getTransfer();
      
      if (this.handleStore())
      {
        // BUGZILLA 1740 - Beim Aendern das Datum der ersten Zahlung auf den naechsten Zahlungstermin basierend auf heute setzen
        // Aber nur, wenn als Termin nicht ohnehin bereits ein zukuenftiger Termin angegeben ist
        // BUGZILLA 1783 - und auch nur dann, wenn es der User ueberhaupt aendern darf.
        if (t.isActive() && this.canEditErsteZahlung())
        {
          Date ersteZahlung = t.getErsteZahlung();
          Date now = DateUtil.endOfDay(new Date());
          if (ersteZahlung == null || !ersteZahlung.after(now))
          {
            String msg = i18n.tr("Das Datum der ersten Zahlung befindet sich in der Vergangenheit.\n" +
                                 "Manche Banken verlangen auch bei der Änderung existierender Daueraufträge\n" +
                                 "die Angabe eines zukünftigen Datums für die erste (nächste) Zahlung.\n\n" +
                                 "Der Auftrag könnte von der Bank eventuell abgelehnt werden.\n\n" +
                                 "Möchten Sie den Vorgang dennoch fortsetzen?\n" +
                                 "Klicken Sie alternativ auf \"Nein\" und ändern Sie das Datum der ersten Zahlung.");
            if (!Application.getCallback().askUser(msg))
              return;
          }
        }
        new SepaDauerauftragExecute().handleAction(t);
      }
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while executing order",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Speichert den Geld-Transfer.
   * @return true, wenn das Speichern erfolgreich war, sonst false.
   */
  public synchronized boolean handleStore()
  {
    SepaDauerauftrag t = null;
    
    try
    {
      t = this.getTransfer();
      
      t.transactionBegin();

      Double d = (Double) getBetrag().getValue();
      t.setBetrag(d == null ? Double.NaN : d.doubleValue());
      
      t.setKonto((Konto)getKontoAuswahl().getValue());
      t.setErsteZahlung((Date)getErsteZahlung().getValue());
      t.setLetzteZahlung((Date)getLetzteZahlung().getValue());
      t.setTurnus((Turnus)getTurnus().getValue());
      t.setZweck((String)getZweck().getValue());
      t.setPurposeCode((String)getPurposeCode().getValue());

      String kto  = (String)getEmpfaengerKonto().getValue();
      String name = getEmpfaengerName().getText();
      String bic  = (String) getEmpfaengerBic().getValue();

      t.setGegenkontoNummer(kto);
      t.setGegenkontoName(name);
      t.setGegenkontoBLZ(bic);

      t.store();
      
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
   * Listener, der das Datum der naechsten Zahlung aktualisiert.
   */
  private class NextDateListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try
      {
        Date first = (Date) getErsteZahlung().getValue();
        Date last  = (Date) getLetzteZahlung().getValue();
        Turnus t   = (Turnus) getTurnus().getValue();
        if (first == null || t == null)
          return;

        Date next = TurnusHelper.getNaechsteZahlung(first,last,t,new Date());
        if (next != null)
          ersteZahlung.setComment(i18n.tr("Nächste: {0}", HBCI.DATEFORMAT.format(next)));
        else
          ersteZahlung.setComment("");
      }
      catch (Exception e)
      {
        Logger.error("unable to apply first payment date",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ermitteln der nächsten Zahlung"), StatusBarMessage.TYPE_ERROR));
      }
    }
  }
  
  
  /**
   * Eigener ueberschriebener Kontofilter.
   */
  private class MyKontoFilter extends KontoFilter
  {
    // Wir leiten die Anfrage an den weiter
    private KontoFilter foreign = KontoFilter.createForeign(SynchronizeJobSepaDauerauftragStore.class);

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


}
