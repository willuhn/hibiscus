/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractTransferControl.java,v $
 * $Revision: 1.41 $
 * $Date: 2008/06/02 08:06:29 $
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

import de.willuhn.datasource.GenericIterator;
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
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.VerwendungszweckDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
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
	private DialogInput kontoAuswahl			     = null;
	private Input betrag										   = null;
	private TextInput zweck									   = null;
	private DialogInput zweck2							   = null;
  private VerwendungszweckDialog zweckDialog = null;

	private DialogInput empfkto 						   = null;
	private TextInput empfName 					  	   = null;
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
  public DialogInput getKontoAuswahl() throws RemoteException
	{
		if (kontoAuswahl != null)
			return kontoAuswahl;

    KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_MOUSE);
		d.addCloseListener(new KontoListener());

		Konto k = getKonto();
		kontoAuswahl = new DialogInput(k == null ? "" : k.getKontonummer(),d);
		kontoAuswahl.setComment(k == null ? "" : k.getBezeichnung());
		kontoAuswahl.disableClientControl();
		kontoAuswahl.setValue(k);
    kontoAuswahl.setMandatory(true);

		return kontoAuswahl;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DialogInput getEmpfaengerKonto() throws RemoteException
	{
		if (empfkto != null)
			return empfkto;

    AdresseAuswahlDialog d = new AdresseAuswahlDialog(AdresseAuswahlDialog.POSITION_MOUSE);
		d.addCloseListener(new EmpfaengerListener());
		empfkto = new DialogInput(getTransfer().getGegenkontoNummer(),d);
    // BUGZILLA 280
    empfkto.setMaxLength(HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    empfkto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    empfkto.setMandatory(true);
		return empfkto;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerBlz() throws RemoteException
	{
		if (empfblz != null)
			return empfblz;
		empfblz = new BLZInput(getTransfer().getGegenkontoBLZ());
    empfblz.setMandatory(true);
		return empfblz;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getEmpfaengerName() throws RemoteException
	{
		if (empfName != null)
			return empfName;
		empfName = new TextInput(getTransfer().getGegenkontoName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    // BUGZILLA 163
    empfName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    empfName.setMandatory(true);
		return empfName;
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
    HibiscusTransfer t = getTransfer();
    String[] lines = VerwendungszweckUtil.toArray(t);
    final String buttonText = "weitere Zeilen ({0})...";
    boolean readOnly = false;
    if (t instanceof Terminable)
      readOnly = ((Terminable)t).ausgefuehrt();
    this.zweckDialog = new VerwendungszweckDialog(lines,readOnly,VerwendungszweckDialog.POSITION_MOUSE);
    this.zweckDialog.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          String[] newLines = (String[]) zweckDialog.getData();
          if (newLines != null) // andernfalls wurde "Abbrechen" gedrueckt
            zweck2.setButtonText(i18n.tr(buttonText,Integer.toString(newLines.length)));
        }
        catch (Exception e)
        {
          Logger.error("unable to update line count",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Zeilen-Anzahl"), StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
		zweck2 = new DialogInput(getTransfer().getZweck2(),this.zweckDialog);
    zweck2.setButtonText(i18n.tr(buttonText,Integer.toString(lines.length)));
    zweck2.setMaxLength(HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    zweck2.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    zweck2.disableButton(); // TODO EVZ - Freischalten, wenn alles implementiert ist
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
		betrag = new DecimalInput(getTransfer().getBetrag(),HBCI.DECIMALFORMAT);

		// wir loesen den KontoListener aus, um die Waehrung sofort anzuzeigen
		
		betrag.setComment(getKonto() == null ? "" : getKonto().getWaehrung());
    betrag.setMandatory(true);
		new KontoListener().handleEvent(null);

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
		storeEmpfaenger = new CheckboxInput(t.isNewObject() && t.getGegenkontoNummer() == null);

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

			getTransfer().setBetrag(((Double)getBetrag().getValue()).doubleValue());
			getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
			getTransfer().setZweck((String)getZweck().getValue());
			getTransfer().setZweck2(getZweck2().getText());  // "getText()" ist wichtig, weil das ein DialogInput ist

			String kto  = ((DialogInput) getEmpfaengerKonto()).getText();
			String blz  = (String)getEmpfaengerBlz().getValue();
			String name = (String)getEmpfaengerName().getValue();

			getTransfer().setGegenkontoNummer(kto);
			getTransfer().setGegenkontoBLZ(blz);
			getTransfer().setGegenkontoName(name);

      // Erst den Auftrag selbst speichern, damit er eine ID hat.
      // Die zusaetzlichen Verwendungszwecke kommen danach
      getTransfer().store();

      // Geaenderte Verwendungszwecke uebernehmen. Allerdings nur, wenn
      // der Dialog tatsaechlich geoffnet und auf "Uebernehmen" geklickt wurde
      String[] lines = (String[]) this.zweckDialog.getData();
      if (lines != null)
      {
        // Wir loeschen die urspruenglichen weg
        GenericIterator orig = getTransfer().getWeitereVerwendungszwecke();
        if (orig != null)
        {
          while (orig.hasNext())
          {
            Verwendungszweck z = (Verwendungszweck) orig.next();
            z.delete();
          }
        }
        
        // und schreiben sie dann komplett neu
        for (int i=0;i<lines.length;++i)
        {
          // leere Zeilen ueberspringen
          if (lines[i] == null)
            continue;
          String text = lines[i].trim();
          if (text.length() == 0)
            continue;
          
          VerwendungszweckUtil.create(getTransfer(),text);
        }
      }
      
			Boolean store = (Boolean) getStoreEmpfaenger().getValue();
			if (store.booleanValue())
			{
				HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
				e.setBLZ(blz);
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
	 * Listener, der die Auswahl des Kontos ueberwacht und die Waehrungsbezeichnung
	 * hinter dem Betrag abhaengig vom ausgewaehlten Konto anpasst.
   */
  private class KontoListener implements Listener
	{
		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event == null || event.data == null)
				return;
			konto = (Konto) event.data;

			try {
				String b = konto.getBezeichnung();
				getKontoAuswahl().setText(konto.getKontonummer());
				getKontoAuswahl().setComment(b == null ? "" : b);
				getBetrag().setComment(konto.getWaehrung());
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
				getEmpfaengerKonto().setText(gegenkonto.getKontonummer());
				getEmpfaengerBlz().setValue(gegenkonto.getBLZ());
				getEmpfaengerName().setValue(gegenkonto.getName());
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
          list.addFilter("empfaenger_blz = ?",  new Object[]{gegenkonto.getBLZ()});
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
 * Revision 1.41  2008/06/02 08:06:29  willuhn
 * @C Button fuer weitere Verwendungszwecke vorerst gesperrt
 *
 * Revision 1.40  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.39  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.38  2008/02/22 00:52:36  willuhn
 * @N Erste Dialoge fuer erweiterte Verwendungszwecke (noch auskommentiert)
 *
 * Revision 1.37  2007/11/01 21:56:28  willuhn
 * @N Bugzilla 408
 *
 * Revision 1.36  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.35  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.34  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 * Revision 1.33  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.32  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.31  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.30  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.29  2006/02/06 16:03:50  willuhn
 * @B bug 163
 *
 * Revision 1.28  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.27  2005/06/23 23:03:20  web0
 * @N much better KontoAuswahlDialog
 *
 * Revision 1.26  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.25  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.24  2005/03/02 17:59:31  web0
 * @N some refactoring
 *
 * Revision 1.23  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.22  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.21  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.20  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.19  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.18  2005/01/19 00:33:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.16  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 * Revision 1.13  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.12  2004/10/21 14:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.9  2004/10/15 20:09:43  willuhn
 * @B Laengen-Pruefung bei Empfaengername
 *
 * Revision 1.8  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/08 00:19:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.5  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.2  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/