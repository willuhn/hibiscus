/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractTransferControl.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/10/20 12:08:18 $
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Abstrakter Basis-Controler fuer die Zahlungen.
 */
public abstract class AbstractTransferControl extends AbstractControl
{

	// Fach-Objekte
	Transfer transfer         						= null;
	private Empfaenger empfaenger 				= null;
	private Konto konto										= null;
	
	// Eingabe-Felder
	private DialogInput kontoAuswahl			= null;
	private Input betrag									= null;
	private Input zweck										= null;
	private Input zweck2									= null;

	private DialogInput empfkto 					= null;
	private Input empfName 								= null;
	private Input empfblz 								= null;

	private CheckboxInput storeEmpfaenger = null;

	boolean stored												= false;

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
	 * Hinweis: Die Funktion kann <b>nicht</b> bei Bedarf einen
	 * neuen Transfer erzeugen, weil sie ja nicht weiss, um welche
	 * Art von Transfer es sich handelt (Ueberweisung oder Dauerauftrag).
	 * Die implementierende Klasse muss diese Funktion also ueberschreiben,
	 * <code>super.getTransfer</code> aufrufen, wenn neues Objekt erzeugen,
	 * wenn <code>null</code> geliefert wird und ihn im Member <code>transfer</code>
	 * speichern.
   * @return der Transfer oder <code>null</code> wenn keiner existiert.
   * @throws RemoteException
   */
  public Transfer getTransfer() throws RemoteException
	{
		if (transfer != null)
			return transfer;
		
		transfer = (Transfer) getCurrentObject();
		return transfer;
	}

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

		ListDialog d = new ListDialog(Settings.getDBService().createList(Konto.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Kontos"));
		d.addCloseListener(new KontoListener());

		Konto k = getKonto();
		kontoAuswahl = new DialogInput(k == null ? "" : k.getKontonummer(),d);
		kontoAuswahl.setComment(k == null ? "" : k.getBezeichnung());
		kontoAuswahl.disableClientControl();
		kontoAuswahl.setValue(k);

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

		ListDialog d = new ListDialog(Settings.getDBService().createList(Empfaenger.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Empfängers"));
		d.addCloseListener(new EmpfaengerListener());

		empfkto = new DialogInput(getTransfer().getEmpfaengerKonto(),d);

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
		empfblz = new TextInput(getTransfer().getEmpfaengerBLZ());

		empfblz.setComment("");
		empfblz.addListener(new BLZListener());
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
		empfName = new TextInput(getTransfer().getEmpfaengerName(),27);
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
		zweck = new TextInput(getTransfer().getZweck());
		return zweck;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den "weiteren" Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getZweck2() throws RemoteException
	{
		if (zweck2 != null)
			return zweck2;
		zweck2 = new TextInput(getTransfer().getZweck2());
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
		storeEmpfaenger = new CheckboxInput(getTransfer().isNewObject());

		return storeEmpfaenger;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public synchronized void handleDelete() {
		try {
			if (getTransfer() == null || getTransfer().isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Sicher?"));
			d.setText(i18n.tr("Wollen Sie diesen Auftrag wirklich löschen?"));
			if (!((Boolean) d.open()).booleanValue())
				return;
			getTransfer().delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Auftrag gelöscht."));
		}
		catch (Exception e)
		{
			Logger.error("error while deleting transfer",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Auftrags."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		GUI.startPreviousView();
  }

	/**
   * Speichert den Geld-Transfer.
   */
	public synchronized void handleStore()
	{
		stored = false;
		try {
  		
			getTransfer().transactionBegin();

			getTransfer().setBetrag(((Double)getBetrag().getValue()).doubleValue());
			getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
			getTransfer().setZweck((String)getZweck().getValue());
			getTransfer().setZweck2((String)getZweck2().getValue());

			String kto  = ((DialogInput) getEmpfaengerKonto()).getText();
			String blz  = (String)getEmpfaengerBlz().getValue();
			String name = (String)getEmpfaengerName().getValue();

			getTransfer().setEmpfaengerKonto(kto);
			getTransfer().setEmpfaengerBLZ(blz);
			getTransfer().setEmpfaengerName(name);
			getTransfer().store();

			Boolean store = (Boolean) getStoreEmpfaenger().getValue();
			if (store.booleanValue())
			{

				// wir checken erstmal, ob wir den schon haben.
				DBIterator list = Settings.getDBService().createList(Empfaenger.class);
				list.addFilter("kontonummer = '" + kto + "'");
				list.addFilter("blz = '" + blz + "'");
				if (list.hasNext())
				{
					YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
					d.setTitle(i18n.tr("Empfänger existiert"));
					d.setText(i18n.tr("Ein Empfänger mit dieser Kontonummer und BLZ existiert bereits. " +
							"Möchten Sie den Empfänger dennoch zum Adressbuch hinzufügen?"));
					if (!((Boolean) d.open()).booleanValue()) return;
				}
				Empfaenger e = (Empfaenger) Settings.getDBService().createObject(Empfaenger.class,null);
				e.setBLZ(blz);
				e.setKontonummer(kto);
				e.setName(name);
				e.store();
				GUI.getStatusBar().setSuccessText(i18n.tr("Auftrag und Adresse gespeichert"));
			}
			else {
				GUI.getStatusBar().setSuccessText(i18n.tr("Auftrag gespeichert"));
			}
			getTransfer().transactionCommit();
			stored = true;
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
			if (event == null)
				return;
			konto = (Konto) event.data;
			if (konto == null)
				return;
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
	 * Listener, der den Namen des Geldinstitutes bei BLZ-Auswahl dranschreibt.
   */
  private class BLZListener implements Listener
	{
		/**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
			String name = HBCIUtils.getNameForBLZ((String)empfblz.getValue());
			empfblz.setComment(name);
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
			empfaenger = (Empfaenger) event.data;
			if (empfaenger == null)
				return;
			try {
				getEmpfaengerKonto().setText(empfaenger.getKontonummer());
				getEmpfaengerBlz().setValue(empfaenger.getBLZ());
				getEmpfaengerName().setValue(empfaenger.getName());
				// Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreEmpfaenger().setValue(Boolean.FALSE);
				
				// und jetzt noch das Geld-Institut dranpappen
				new BLZListener().handleEvent(null);
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