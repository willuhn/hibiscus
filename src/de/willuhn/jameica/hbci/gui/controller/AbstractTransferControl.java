/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractTransferControl.java,v $
 * $Revision: 1.23 $
 * $Date: 2005/03/02 00:22:05 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Transfer;
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
	private Adresse empfaenger 							= null;
	private Konto konto											= null;
	
	// Eingabe-Felder
	private DialogInput kontoAuswahl			= null;
	private Input betrag										= null;
	private TextInput zweck									= null;
	private TextInput zweck2								= null;

	private DialogInput empfkto 						= null;
	private Input empfName 									= null;
	private Input empfblz 									= null;

	private CheckboxInput storeEmpfaenger 	= null;

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
  public abstract Transfer getTransfer() throws RemoteException;

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

		ListDialog d = new ListDialog(Settings.getDBService().createList(Adresse.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Gegenkontos"));
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
		empfName = new TextInput(getTransfer().getEmpfaengerName(),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
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
		zweck = new TextInput(getTransfer().getZweck(),27);
		zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
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
		// BUGZILLA #10 http://www.willuhn.de/bugzilla/show_bug.cgi?id=10
		zweck2 = new TextInput(getTransfer().getZweck2(),27);
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
				DBIterator list = Settings.getDBService().createList(Adresse.class);
				list.addFilter("kontonummer = '" + kto + "'");
				list.addFilter("blz = '" + blz + "'");
				if (list.hasNext())
				{
					YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
					d.setTitle(i18n.tr("Empfänger existiert"));
					d.setText(i18n.tr("Ein Empfänger mit dieser Kontonummer und BLZ existiert bereits. " +
							"Möchten Sie den Empfänger dennoch zum Adressbuch hinzufügen?"));
					if (!((Boolean) d.open()).booleanValue()) return false;
				}
				Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
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
			empfaenger = (Adresse) event.data;
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