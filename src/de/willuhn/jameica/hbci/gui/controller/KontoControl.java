/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/KontoControl.java,v $
 * $Revision: 1.99 $
 * $Date: 2011/05/27 11:33:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoFetchFromPassport;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeOptionsDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.PassportInput;
import de.willuhn.jameica.hbci.gui.parts.ProtokollList;
import de.willuhn.jameica.hbci.gui.parts.SaldoChart;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller der fuer den Dialog "Bankverbindungen" zustaendig ist.
 */
public class KontoControl extends AbstractControl
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  // Fachobjekte
	private Konto konto 			 		        = null;
	
	// Eingabe-Felder
	private TextInput kontonummer  		    = null;
  private TextInput unterkonto          = null;
	private TextInput blz          		    = null;
	private Input name				 		        = null;
	private Input bezeichnung	 		        = null;
	private Input passportAuswahl         = null;
  private Input kundennummer 		        = null;
  private Input kommentar               = null;
  
  private TextInput bic                 = null;
  private TextInput iban                = null;
  
  private DecimalInput saldo			      = null;
  private DecimalInput avail            = null;
  private SaldoMessageConsumer consumer = null;
  
  private Button synchronizeOptions     = null;

	private TablePart kontoList						= null;
	private TablePart protokoll						= null;
  private UmsatzList umsatzList         = null;
  private SaldoChart saldoChart         = null;
  
  private CheckboxInput offline         = null;

  /**
   * ct.
   * @param view
   */
  public KontoControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert die aktuelle Bankverbindung.
   * @return Bankverbindung.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;
		
		try
		{
			konto = (Konto) getCurrentObject();
			if (konto != null)
			{
			  // BUGZILLA 1299 reload Konto
			  if (!konto.isNewObject())
         konto = (Konto) Settings.getDBService().createObject(Konto.class,konto.getID());
			  
        return konto;
			}
		}
		catch (ClassCastException e)
		{
			// Falls wir von 'nem anderen Dialog kommen, kann es durchaus sein,
			// das getCurrentObject() was falsches liefert. Das ist aber nicht
			// weiter schlimm. Wir erstellen dann einfach ein neues.
		}
		
		// Kein Konto verfuegbar - wir bauen ein neues.
		konto = (Konto) Settings.getDBService().createObject(Konto.class,null);
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit dem Protokoll des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getProtokoll() throws RemoteException
	{
		if (protokoll != null)
			return protokoll;

		protokoll = new ProtokollList(getKonto(),null);
		return protokoll;
	}

  /**
   * Liefert eine Tabelle mit den Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzList() throws RemoteException
  {
    if (umsatzList != null)
      return umsatzList;

    umsatzList = new UmsatzList(getKonto(),HBCIProperties.UMSATZ_DEFAULT_DAYS,new UmsatzDetail());
    umsatzList.setFilterVisible(false);
    return umsatzList;
  }

  /**
   * Liefert einen Chart mit dem Saldo des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getSaldoChart() throws RemoteException
  {
    if (saldoChart != null)
      return saldoChart;

    saldoChart = new SaldoChart(getKonto());
    saldoChart.setTinyView(true);
    return saldoChart;
  }

  /**
	 * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKontonummer() throws RemoteException
	{
		if (kontonummer != null)
			return kontonummer;
		kontonummer = new TextInput(getKonto().getKontonummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    // BUGZILLA 280
    kontonummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    kontonummer.setMandatory(true);
		return kontonummer;
	}
  
  /**
   * Liefert eine Checkbox, mit der festgelegt werden kann, ob es sich um ein Offlinekonto handelt.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getOffline() throws RemoteException
  {
    if (this.offline != null)
      return this.offline;
    
    this.offline = new CheckboxInput((this.getKonto().getFlags() & Konto.FLAG_OFFLINE) != 0);
    this.offline.setName(i18n.tr("Offline-Konto"));
    
    Listener l = new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          boolean b = ((Boolean) getOffline().getValue()).booleanValue();
          getSaldo().setEnabled(b);
          getPassportAuswahl().setEnabled(!b);
          
          // Wir muessen die Aenderung sofort ins Konto uebernehmen, damit
          // der richtige Sync-Options-Dialog angezeigt wird.
          applyOfflineState(b);
        }
        catch (Exception e)
        {
          Logger.error("error while updating offline status",e);
        }
      }
    };
    
    this.offline.addListener(l);
    
    // einmal initial ausloesen
    l.handleEvent(null);

    return this.offline;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Unterkontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getUnterkonto() throws RemoteException
  {
    if (unterkonto != null)
      return unterkonto;
    unterkonto = new TextInput(getKonto().getUnterkonto(),HBCIProperties.HBCI_ID_MAXLENGTH);
    unterkonto.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    unterkonto.setComment(i18n.tr("Kann meist frei gelassen werden"));
    return unterkonto;
  }

  /**
   * Liefert einen Button, ueber den die Synchronisierungsdetails konfiguriert
   * werden.
   * @return Button.
   * @throws RemoteException
   */
  public Button getSynchronizeOptions() throws RemoteException
  {
    if (this.synchronizeOptions != null)
      return this.synchronizeOptions;

    this.synchronizeOptions = new Button(i18n.tr("Synchronisierungsoptionen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          SynchronizeOptionsDialog d = new SynchronizeOptionsDialog(getKonto(),SynchronizeOptionsDialog.POSITION_CENTER);
          d.open();
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(oce.getMessage());
          return;
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to configure synchronize options");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Konfigurieren der Synchronisierungsoptionen"),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    },getKonto(),false,"document-properties.png");
    this.synchronizeOptions.setEnabled((getKonto().getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED);
    return this.synchronizeOptions;
  }
  
  /**
	 * Liefert das Eingabe-Feld fuer die Bankleitzahl.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new BLZInput(getKonto().getBLZ());
    blz.setMandatory(true);
    blz.addListener(new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        completeBic();
      }
    });
		return blz;
	}

	/**
	 * Liefert den Namen des Konto-Inhabers.
   * @return Name des Konto-Inhabers.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getKonto().getName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    name.setMandatory(true);
		return name;
	}

	/**
	 * Liefert die Bezeichnung des Kontos.
	 * @return Bezeichnung des Kontos.
	 * @throws RemoteException
	 */
	public Input getBezeichnung() throws RemoteException
	{
		if (bezeichnung != null)
			return bezeichnung;
		bezeichnung = new TextInput(getKonto().getBezeichnung(),255);
		return bezeichnung;
	}

	/**
	 * Liefert das Eingabefeld fuer die Kundennummer.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getKundennummer() throws RemoteException
	{
		if (kundennummer != null)
			return kundennummer;
		kundennummer = new TextInput(getKonto().getKundennummer());
    kundennummer.setMandatory(true);
		return kundennummer;
	}

	/**
	 * Liefert das Auswahl-Feld fuer das Sicherheitsmedium.
   * @return Eingabe-Feld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getPassportAuswahl() throws RemoteException, ApplicationException
	{
		if (passportAuswahl != null)
			return passportAuswahl;

		passportAuswahl = new PassportInput(getKonto());
		return passportAuswahl;
	}

	/**
	 * Liefert ein Feld zur Anzeige des Saldos.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSaldo() throws RemoteException
	{
		if (saldo != null)
			return saldo;

		saldo = new DecimalInput(this.getKonto().getSaldo(),HBCI.DECIMALFORMAT);
    saldo.setComment(""); // Platz fuer Kommentar reservieren
    saldo.setEnabled((this.getKonto().getFlags() & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE);
    // Einmal ausloesen, damit das Feld mit Inhalt gefuellt wird.
    this.consumer = new SaldoMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
    try
    {
      this.consumer.handleMessage(null);
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh saldo",e);
    }
    return saldo;
	}
  
  /**
   * Liefert ein Feld fuer den verfuegbaren Betrag.
   * @return Feld mit dem verfuegbaren Betrag.
   * @throws RemoteException
   */
  public Input getSaldoAvailable() throws RemoteException
  {
    if (avail != null)
      return avail;

    // Bei Offline-Konten gibts keinen verfuegbaren Betrag
    if ((this.getKonto().getFlags() & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
      return null;
    
    // Wenn wir noch keinen verfuegbaren Betrag haben, zeigen wir auch nichts an
    double d = this.getKonto().getSaldoAvailable();
    if (Double.isNaN(d))
      return null;

    avail = new DecimalInput(d,HBCI.DECIMALFORMAT);
    String w = this.getKonto().getWaehrung();
    if (w == null) w = HBCIProperties.CURRENCY_DEFAULT_DE;
    avail.setComment(w);
    avail.setEnabled(false);
    return avail;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer einen Kommentar.
   * @return Kommentar.
   * @throws RemoteException
   */
  public Input getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    this.kommentar = new TextAreaInput(getKonto().getKommentar());
    return this.kommentar;
  }

  /**
   * Liefert das Eingabe-Feld fuer die IBAN.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getIban() throws RemoteException
  {
    if (this.iban == null)
    {
      this.iban = new TextInput(getKonto().getIban(),HBCIProperties.HBCI_IBAN_MAXLENGTH + 5); // max. 5 Leerzeichen
      this.iban.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS + " ");
      this.iban.addListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          String s = (String) iban.getValue();
          if (s == null || s.length() == 0 || s.indexOf(" ") == -1)
            return;
          iban.setValue(s.replaceAll(" ",""));
        }
      });
    }
    return this.iban;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBic() throws RemoteException
  {
    if (this.bic == null)
    {
      this.bic = new TextInput(getKonto().getBic(),HBCIProperties.HBCI_BIC_MAXLENGTH);
      this.bic.setValidChars(HBCIProperties.HBCI_BIC_VALIDCHARS);
      this.completeBic();
    }
    return this.bic;
  }

  /**
   * Liefert einen Saldo-MessageConsumer.
   * @return Consumer.
   */
  public MessageConsumer getSaldoMessageConsumer()
  {
    return this.consumer;
  }
  
  /**
	 * Liefert eine Tabelle mit allen vorhandenen Bankverbindungen.
   * @return Tabelle mit Bankverbindungen.
   * @throws RemoteException
   */
  public TablePart getKontoListe() throws RemoteException
	{
		if (kontoList != null)
			return kontoList;

    kontoList = new de.willuhn.jameica.hbci.gui.parts.KontoList(new KontoNew());
    // BUGZILLA 81 http://www.willuhn.de/bugzilla/show_bug.cgi?id=81
    kontoList.addColumn(i18n.tr("Umsätze"),"numumsaetze");
		return kontoList;
	}

  /**
   * Speichert das Konto.
   */
  public synchronized void handleStore() {
		try {

      boolean offline = ((Boolean)getOffline().getValue()).booleanValue();

			if (offline)
			{
			  getKonto().setPassportClass(null);
			  getKonto().setSaldo(((Double)getSaldo().getValue()).doubleValue());
			}
			else
			{
	      Passport p = (Passport) getPassportAuswahl().getValue();
			  if (p == null)
			  {
	        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie ein Sicherheitsmedium aus"), StatusBarMessage.TYPE_ERROR));
	        return;
			  }
        getKonto().setPassportClass(p.getClass().getName());
			}
			
			applyOfflineState(offline);

			getKonto().setKontonummer((String)getKontonummer().getValue());
      getKonto().setUnterkonto((String)getUnterkonto().getValue());
			getKonto().setBLZ((String)getBlz().getValue());
			getKonto().setName((String)getName().getValue());
			getKonto().setBezeichnung((String)getBezeichnung().getValue());
      getKonto().setKundennummer((String)getKundennummer().getValue());
      getKonto().setKommentar((String) getKommentar().getValue());
      getKonto().setIban((String)getIban().getValue());
      getKonto().setBic((String)getBic().getValue());
      
      // und jetzt speichern wir.
			getKonto().store();
			GUI.getStatusBar().setSuccessText(i18n.tr("Konto gespeichert."));
      GUI.getView().setSuccessText("");
		}
		catch (ApplicationException e1)
		{
			GUI.getView().setErrorText(i18n.tr(e1.getLocalizedMessage()));
		}
		catch (RemoteException e)
		{
			Logger.error("unable to store konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Kontos."));
		}
  }
  
  /**
   * Uebernimmt den Offline-Status in das Konto.
   * @param offline true, wenn es offline ist.
   * @throws RemoteException
   */
  private void applyOfflineState(boolean offline) throws RemoteException
  {
    int flags = getKonto().getFlags();
    boolean have = (flags & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE;
    if (offline && !have)
      getKonto().setFlags(flags | Konto.FLAG_OFFLINE);
    else if (!offline && have)
      getKonto().setFlags(flags ^ Konto.FLAG_OFFLINE);
  }

	/**
   * Liest alle ueber das Sicherheitsmedium verfuegbaren Konten
   * aus und speichert sie (insofern Konten mit identischer kto-Nummer/BLZ nicht schon existieren).
   */
  public synchronized void handleReadFromPassport()
	{

		try 
		{
		  // Checken, ob wir ein Konto ausgewaehlt haben
		  Object selection = getKontoListe().getSelection();
		  Konto k = null;
		  if (selection != null && (selection instanceof Konto))
		    k = (Konto) selection;
      PassportAuswahlDialog d = new PassportAuswahlDialog(k,PassportAuswahlDialog.POSITION_CENTER);
      Passport p = (Passport) d.open();

      new KontoFetchFromPassport().handleAction(p);
		}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
		catch (ApplicationException ae)
		{
			GUI.getStatusBar().setErrorText(ae.getMessage());
		}
		catch (Exception e)
		{
			Logger.error("error while reading passport from select box",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Auslesen der Konto-Informationen"));
		}
	}

  /**
   * Laedt die Tabelle mit den Umsaetzen neu.
   */
  public void handleReload()
  {
    GUI.startSync(new Runnable() {
      public void run()
      {
        try
        {
          UmsatzList list = ((UmsatzList)getUmsatzList());
          list.removeAll();
          Konto k = getKonto();
          DBIterator i = k.getUmsaetze(HBCIProperties.UMSATZ_DEFAULT_DAYS);
          while (i.hasNext())
            list.addItem(i.next());
          list.sort();
          if (consumer != null)
            consumer.handleMessage(null);
        }
        catch (IllegalArgumentException iae)
        {
          // Fliegt, wenn der Dialog zwischenzeitlich verlassen
          // wurde und die Tabelle disposed ist.
          // Dann brechen wir ab und ignorieren den Fehler.
          Logger.warn("umsatz table has be disposed in the meantime, skip reload");
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while reloading umsatz list",e);
        }
      }
    });
  }
  
  /**
   * Auto-vervollstaendigt die BIC, falls sie noch nicht eingegeben wurde aber die BLZ bekannt ist.
   */
  private void completeBic()
  {
    try
    {
      String bic = (String) getBic().getValue();
      if (bic != null && bic.length() > 0)
        return; // schon eingegeben
      
      String blz = (String) getBlz().getValue();
      if (blz == null || blz.length() == 0)
        return; // keine BLZ bekannt
      
      bic = HBCIUtils.getBICForBLZ(blz);
      if (bic != null && bic.length() > 0)
        getBic().setValue(bic);
    }
    catch (Exception e)
    {
      Logger.write(Level.WARN,"unable to autocomplete BIC",e);
    }
  }

  /**
   * Wird beim Eintreffen neuer Salden benachrichtigt und aktualisiert ggf die Anzeige.
   */
  private class SaldoMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SaldoMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable() {
        
        public void run()
        {
          try
          {
            if (saldo == null)
              return;
            
            Konto k = getKonto();
            double s  = k.getSaldo();
            saldo.setValue(Double.isNaN(s) ? null : s);
            
            double s2 = k.getSaldoAvailable();
            if (getSaldoAvailable() != null)
              getSaldoAvailable().setValue(Double.isNaN(s2) ? null : s2);

            Date d = k.getSaldoDatum();
            if (d == null)
              saldo.setComment(i18n.tr("noch kein Saldo ermittelt"));
            else
              saldo.setComment(i18n.tr("vom {0}",HBCI.LONGDATEFORMAT.format(d)));
          }
          catch (Exception e)
          {
            Logger.error("unable to refresh saldo",e);
          }
        }
      
      });
    }
    
  }


}
