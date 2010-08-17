/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractTransferControl.java,v $
 * $Revision: 1.57 $
 * $Date: 2010/08/17 11:32:10 $
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
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
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
	private Input kontoAuswahl			           = null;
	private Input betrag										   = null;
	private TextInput zweck									   = null;
	private DialogInput zweck2							   = null;
  private VerwendungszweckDialog zweckDialog = null;

  private AddressInput empfName              = null;
	private TextInput empfkto 						     = null;
	private TextInput empfblz								   = null;

	private CheckboxInput storeEmpfaenger 	   = null;
	
	I18N i18n;

  /**
   * ct.
   * @param view
   */
  public AbstractTransferControl(AbstractView view) {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
  public Input getKontoAuswahl() throws RemoteException
	{
		if (this.kontoAuswahl != null)
		  return this.kontoAuswahl;
		
    Konto k = getKonto();
    KontoListener kl = new KontoListener();
		this.kontoAuswahl = new KontoInput(k,KontoFilter.ACTIVE);
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

		empfkto = new TextInput(getTransfer().getGegenkontoNummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_SOFT);
		empfkto.setName(i18n.tr("Kontonummer"));
    empfkto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS + " ");
    empfkto.setMandatory(true);
    empfkto.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String) empfkto.getValue();
        if (s == null || s.length() == 0 || s.indexOf(" ") == -1)
          return;
        empfkto.setValue(s.replaceAll(" ",""));
      }
    });
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
    empfblz.setMandatory(true);
		return empfblz;
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
    
    // Forciert das korrekte Formatieren des Betrages nach Focus-Wechsel
    betrag.addListener(new Listener() {
      public void handleEvent(Event event) {
        try
        {
          Double value = (Double) betrag.getValue();
          if (value == null)
            return;
          betrag.setValue(value);
        }
        catch (Exception e)
        {
          Logger.error("unable to autoformat value",e);
        }
      }
    
    });
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

		// Nur bei neuen Transfers aktivieren
    HibiscusTransfer t = getTransfer();
    // Checkbox nur setzen, wenn es eine neue Ueberweisung ist und
    // noch kein Gegenkonto definiert ist.
    boolean enabled = t.isNewObject() && t.getGegenkontoNummer() == null;
    
    // Per Hidden-Parameter kann die Checkbox komplett ausgeschaltet werden
    de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    enabled &= settings.getBoolean("transfer.addressbook.autoadd",true);
		storeEmpfaenger = new CheckboxInput(enabled);

		return storeEmpfaenger;
	}

  /**
   * Speichert den Geld-Transfer.
   * @return true, wenn das Speichern erfolgreich war.
   */
	public synchronized boolean handleStore()
	{
		try {
  		
			getTransfer().transactionBegin();

			Double d = (Double) getBetrag().getValue();
      getTransfer().setBetrag(d == null ? Double.NaN : d.doubleValue());
			
			getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
			getTransfer().setZweck((String)getZweck().getValue());
			getTransfer().setZweck2(getZweck2().getText());  // "getText()" ist wichtig, weil das ein DialogInput ist

			String kto  = (String)getEmpfaengerKonto().getValue();
			String blz  = (String)getEmpfaengerBlz().getValue();
			String name = getEmpfaengerName().getText();

			getTransfer().setGegenkontoNummer(kto);
			getTransfer().setGegenkontoBLZ(blz);
			getTransfer().setGegenkontoName(name);


      // Geaenderte Verwendungszwecke uebernehmen. Allerdings nur, wenn
      // der Dialog tatsaechlich geoffnet und auf "Uebernehmen" geklickt wurde
      String[] lines = (String[]) this.zweckDialog.getData();
      if (lines != null)
        getTransfer().setWeitereVerwendungszwecke(lines);
        
      getTransfer().store();
      
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
  		GUI.getStatusBar().setSuccessText(i18n.tr("Auftrag gespeichert"));
			getTransfer().transactionCommit();

      if (getTransfer().getBetrag() > Settings.getUeberweisungLimit())
        GUI.getView().setErrorText(i18n.tr("Warnung: Auftragslimit überschritten: {0} ", 
          HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + getKonto().getWaehrung()));
      
      return true;
		}
		catch (ApplicationException e)
		{
			try {
				getTransfer().transactionRollback();
			}
			catch (RemoteException re)
			{
				Logger.error("rollback failed",re);
			}
			GUI.getView().setErrorText(i18n.tr(e.getMessage()));
		}
		catch (Exception e2)
		{
			try {
				getTransfer().transactionRollback();
			}
			catch (RemoteException re)
			{
				Logger.error("rollback failed",re);
			}
			Logger.error("error while storing transfer",e2);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Auftrags"));
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
			gegenkonto = (Address) event.data;
			if (gegenkonto == null)
				return;
			try {
        getEmpfaengerName().setText(gegenkonto.getName());
				getEmpfaengerKonto().setValue(gegenkonto.getKontonummer());
				getEmpfaengerBlz().setValue(gegenkonto.getBlz());

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
          list.addFilter("empfaenger_konto = ?",new Object[]{gegenkonto.getKontonummer()});
          list.addFilter("empfaenger_blz = ?",  new Object[]{gegenkonto.getBlz()});
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
 * Revision 1.57  2010/08/17 11:32:10  willuhn
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