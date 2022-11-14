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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.dialogs.VerwendungszweckDialog;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.AccountInput;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.StoreAddressInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakter Basis-Controler fuer die Zahlungen.
 */
public abstract class AbstractTransferControl extends AbstractControl
{

	// Fach-Objekte
	private Address gegenkonto 							   = null;
	private Konto konto											   = null;
	
	// Eingabe-Felder
	private KontoInput kontoAuswahl			       = null;
	private Input betrag										   = null;
	private TextInput zweck									   = null;
	private DialogInput zweck2							   = null;
  private VerwendungszweckDialog zweckDialog = null;

  private AddressInput empfName              = null;
	private TextInput empfkto 						     = null;
	private TextInput empfblz								   = null;

	private CheckboxInput storeEmpfaenger 	   = null;
	
	final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param view
   */
  public AbstractTransferControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert den Transfer.
   * @return der Transfer oder <code>null</code> wenn keiner existiert.
   * @throws RemoteException
   */
  public abstract HibiscusTransfer getTransfer() throws RemoteException;

	/**
	 * Liefert das Konto der Ueberweisung.
   * @return das Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;

		konto = getTransfer().getKonto();
		return konto;
	}

	/**
	 * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public KontoInput getKontoAuswahl() throws RemoteException
	{
		if (this.kontoAuswahl != null)
		  return this.kontoAuswahl;
		
    Konto k = getKonto();
    KontoListener kl = new KontoListener();
		this.kontoAuswahl = new KontoInput(k,getTransfer().isNewObject() ? KontoFilter.ONLINE : KontoFilter.ALL); // Falls nachtraeglich das Konto deaktiviert wurde
    this.kontoAuswahl.setRememberSelection("auftraege",false); // BUGZILLA 1362 - zuletzt ausgewaehltes Konto gleich uebernehmen
		this.kontoAuswahl.setName(i18n.tr("Persönliches Konto"));
		this.kontoAuswahl.setMandatory(true);
    this.kontoAuswahl.addListener(kl);
    
    // einmal ausloesen
    kl.handleEvent(null);

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
    empfName = new AddressInput(getTransfer().getGegenkontoName(), AddressFilter.INLAND);
    empfName.setName(i18n.tr("Name"));
    empfName.setMandatory(true);
    empfName.addListener(new EmpfaengerListener());
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

		empfkto = new AccountInput(getTransfer().getGegenkontoNummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_SOFT);
		empfkto.setName(i18n.tr("Kontonummer"));
		empfkto.setComment("");
    empfkto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    empfkto.setMandatory(true);
    empfkto.addListener(new KontonummerListener());
		return empfkto;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getEmpfaengerBlz() throws RemoteException
	{
		if (empfblz != null)
			return empfblz;
		empfblz = new BLZInput(getTransfer().getGegenkontoBLZ());
    empfblz.addListener(new KontonummerListener());
    empfblz.setMandatory(true);
		return empfblz;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public TextInput getZweck() throws RemoteException
	{
		if (zweck != null)
			return zweck;
		// BUGZILLA #10 http://www.willuhn.de/bugzilla/show_bug.cgi?id=10
		zweck = new TextInput(getTransfer().getZweck(),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
		zweck.setName(i18n.tr("Verwendungszweck"));
		zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    zweck.setMandatory(true);
		return zweck;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den "weiteren" Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public DialogInput getZweck2() throws RemoteException
	{
		if (zweck2 != null)
			return zweck2;
		// BUGZILLA #10 http://www.willuhn.de/bugzilla/show_bug.cgi?id=10
    final String buttonText = "weitere Zeilen ({0})...";
    this.zweckDialog = new VerwendungszweckDialog(getTransfer(),VerwendungszweckDialog.POSITION_MOUSE);
    this.zweckDialog.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          String[] newLines = (String[]) zweckDialog.getData();
          if (newLines != null) // andernfalls wurde "Abbrechen" gedrueckt
            zweck2.setButtonText(i18n.tr(buttonText,String.valueOf(newLines.length)));
        }
        catch (Exception e)
        {
          Logger.error("unable to update line count",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Zeilen-Anzahl"), StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
		zweck2 = new DialogInput(getTransfer().getZweck2(),this.zweckDialog);
		zweck2.setName(i18n.tr("weiterer Verwendungszweck"));
    zweck2.setButtonText(i18n.tr(buttonText,String.valueOf(getTransfer().getWeitereVerwendungszwecke().length)));
    zweck2.setMaxLength(HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    zweck2.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
		return zweck2;
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

		betrag.setName(i18n.tr("Betrag"));
		betrag.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    betrag.setMandatory(true);
		return betrag;
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
   * @return true, wenn das Speichern erfolgreich war.
   */
	public synchronized boolean handleStore()
	{
	  HibiscusTransfer t = null;
		try
		{
  		t = this.getTransfer();
			t.transactionBegin();

			Double d = (Double) getBetrag().getValue();
      t.setBetrag(d == null ? Double.NaN : d.doubleValue());
			
			t.setKonto((Konto)getKontoAuswahl().getValue());
			t.setZweck((String)getZweck().getValue());
			t.setZweck2(getZweck2().getText());  // "getText()" ist wichtig, weil das ein DialogInput ist

			String kto  = (String)getEmpfaengerKonto().getValue();
			String blz  = (String)getEmpfaengerBlz().getValue();
			String name = getEmpfaengerName().getText();

			t.setGegenkontoNummer(kto);
			t.setGegenkontoBLZ(blz);
			t.setGegenkontoName(name);


      // Geaenderte Verwendungszwecke uebernehmen. Allerdings nur, wenn
      // der Dialog tatsaechlich geoffnet und auf "Uebernehmen" geklickt wurde
      String[] lines = (String[]) this.zweckDialog.getData();
      if (lines != null)
        t.setWeitereVerwendungszwecke(lines);
        
      t.store();
      
			Boolean store = (Boolean) getStoreEmpfaenger().getValue();
			if (store.booleanValue())
			{
				HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
				e.setBlz(blz);
				e.setKontonummer(kto);
				e.setName(name);
        
        // Zu schauen, ob die Adresse bereits existiert, ueberlassen wir der Action
        new EmpfaengerAdd().handleAction(e);
			}
      t.transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftrag gespeichert"),StatusBarMessage.TYPE_SUCCESS));

      if (t.getBetrag() > Settings.getUeberweisungLimit())
        GUI.getView().setErrorText(i18n.tr("Warnung: Auftragslimit überschritten: {0} ", HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + getKonto().getWaehrung()));
      
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
	 * Listener, der die Auswahl des Kontos ueberwacht.
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
          return;

        // Wird u.a. benoetigt, damit anhand des Auftrages ermittelt werden
        // kann, wieviele Zeilen Verwendungszweck jetzt moeglich sind
        getTransfer().setKonto(konto);
			}
			catch (RemoteException er)
			{
				Logger.error("error while updating currency",er);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei Ermittlung der Währung"),StatusBarMessage.TYPE_ERROR));
			}
		}
	}
  
  /**
   * Listener, der die CRC-Pruefung von Kontonummer und BLZ vornimmt.
   */
  private class KontonummerListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try
      {
        String kto = StringUtils.trimToNull((String) getEmpfaengerKonto().getValue());
        String blz = StringUtils.trimToNull((String) getEmpfaengerBlz().getValue());
        if (kto == null || blz == null)
        {
          getEmpfaengerKonto().setComment("");
          return;
        }
        getEmpfaengerKonto().setComment(i18n.tr(HBCIProperties.checkAccountCRC(blz,kto) ? "Konto OK" : "BLZ/Kto ungültig, bitte prüfen"));
      }
      catch (RemoteException er)
      {
        Logger.error("error while checking konto/blz",er);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen der Bankverbindung"),StatusBarMessage.TYPE_ERROR));
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
			gegenkonto = (Address) event.data;
			if (gegenkonto == null)
				return;
			try {
        getEmpfaengerName().setText(gegenkonto.getName());
				getEmpfaengerKonto().setValue(gegenkonto.getKontonummer());
				getEmpfaengerBlz().setValue(gegenkonto.getBlz());
				
        // Listener zum Pruefen der Bankverbindung ausloesen
        new KontonummerListener().handleEvent(null);

				// Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreEmpfaenger().setValue(Boolean.FALSE);
        
        // BUGZILLA 408
        // Verwendungszweck automatisch vervollstaendigen
        try
        {
          String zweck = (String) getZweck().getValue();
          String zweck2 = getZweck2().getText(); // "getText()" ist wichtig, weil das ein DialogInput ist
          if ((zweck != null && zweck.length() > 0) || (zweck2 != null && zweck2.length() > 0))
            return;
          
          DBIterator list = getTransfer().getList();
          list.addFilter("empfaenger_konto = ?", gegenkonto.getKontonummer());
          list.addFilter("empfaenger_blz = ?", gegenkonto.getBlz());
          list.setOrder("order by id desc");
          if (list.hasNext())
          {
            HibiscusTransfer t = (HibiscusTransfer) list.next();
            getZweck().setValue(t.getZweck());
            getZweck2().setText(t.getZweck2()); // "setText()" ist wichtig, weil das ein DialogInput ist
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


/**********************************************************************
 * $Log: AbstractTransferControl.java,v $
 * Revision 1.62  2012/05/03 20:51:37  willuhn
 * @N CRC-Pruefung des Empfaengerkontos bei Auftraegen direkt nach Eingabe machen und als Kommentar anzeigen
 *
 * Revision 1.61  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 * Revision 1.60  2011-08-03 15:34:29  willuhn
 * @B Fehlertext entfernen, wenn erfolgreich gespeichert
 *
 * Revision 1.59  2011-05-11 16:23:57  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.58  2011-04-07 17:52:07  willuhn
 * @N BUGZILLA 1014
 *
 * Revision 1.57  2010-08-17 11:32:10  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.56  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.55  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.54  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.53  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.52  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 **********************************************************************/