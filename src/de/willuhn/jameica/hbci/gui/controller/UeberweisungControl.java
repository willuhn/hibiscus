/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UeberweisungControl.java,v $
 * $Revision: 1.18 $
 * $Date: 2004/06/03 00:23:42 $
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
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.UeberweisungDialog;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Ueberweisungen.
 */
public class UeberweisungControl extends AbstractControl {

	// Fach-Objekte
	private Ueberweisung ueberweisung = null;
	private Empfaenger empfaenger 		= null;
	private Konto konto								= null;
	
	// Eingabe-Felder
	private AbstractInput kontoAuswahl				= null;
	private AbstractInput betrag							= null;
	private AbstractInput zweck								= null;
	private AbstractInput zweck2							= null;
	private AbstractInput termin							= null;

	private AbstractInput empfName 						= null;
	private AbstractInput empfkto 						= null;
	private AbstractInput empfblz 						= null;
	private AbstractInput comment							= null;

	private CheckboxInput storeEmpfaenger 		= null;

	private I18N i18n;

	private boolean stored								= false;

  /**
   * ct.
   * @param view
   */
  public UeberweisungControl(AbstractView view) {
    super(view);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die Ueberweisung oder erzeugt bei Bedarf eine neue.
   * @return die Ueberweisung.
   * @throws RemoteException
   */
  public Ueberweisung getUeberweisung() throws RemoteException
	{
		if (ueberweisung != null)
			return ueberweisung;
		
		ueberweisung = (Ueberweisung) getCurrentObject();
		if (ueberweisung != null)
			return ueberweisung;
		
		ueberweisung = (Ueberweisung) Settings.getDatabase().createObject(Ueberweisung.class,null);
		return ueberweisung;
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

		konto = getUeberweisung().getKonto();
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public TablePart getUeberweisungListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);

		TablePart table = new TablePart(list,this);
		table.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
      	Ueberweisung u = (Ueberweisung) item.getData();
      	if (u == null)
      		return;

				try {
					if (u.getTermin().before(new Date()) && !u.ausgefuehrt())
					{
						item.setBackground(Settings.getUeberfaelligBackground());
						item.setForeground(Settings.getUeberfaelligForeground());
					}
				}
				catch (RemoteException e) { /*ignore */}
      }
    });
    table.addMenu(i18n.tr("Jetzt ausführen"), new Listener() {
      public void handleEvent(Event event) {
				try {
					Ueberweisung u = (Ueberweisung) event.data;
					if (u == null)
						return;
					if (u.ausgefuehrt())
					{
						GUI.getView().setErrorText(i18n.tr("Die Überweisung wurde bereits ausgeführt."));
						return;
					}
					u.execute();
				}
				catch (ApplicationException e)
				{
					GUI.getStatusBar().setErrorText(e.getMessage());
				}
				catch (RemoteException e2)
				{
					Application.getLog().error("error while executing ueberweisung",e2);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Überweisung"));
				}
      }
    });
		table.addMenu(i18n.tr("Duplizieren"), new Listener() {
      public void handleEvent(Event event) {
      	Ueberweisung u = (Ueberweisung) event.data;
      	if (u == null)
      		return;
      	try {
					GUI.startView(UeberweisungNeu.class.getName(),u.duplicate());
      	}
      	catch (RemoteException e)
      	{
					Application.getLog().error("error while duplicating ueberweisung",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren der Überweisung"));
      	}
      }
    });
		table.addColumn(i18n.tr("Konto"),"konto_id");
		table.addColumn(i18n.tr("Kto. des Empfängers"),"empfaenger_konto");
		table.addColumn(i18n.tr("BLZ des Empfängers"),"empfaenger_blz");
		table.addColumn(i18n.tr("Name des Empfängers"),"empfaenger_name");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
		table.addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
				try {
					int i = ((Integer) o).intValue();
					return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
				}
				catch (Exception e) {}
				return ""+o;
      }
    });
		return table;
	}

	/**
	 * Liefert ein Kommentar-Feld zu dieser Ueberweisung.
   * @return Kommentarfeld.
   * @throws RemoteException
   */
  public AbstractInput getComment() throws RemoteException
	{
		if (comment != null)
			return comment;
		comment = new LabelInput("");
		Ueberweisung u = getUeberweisung();
		if (u.ausgefuehrt())
		{
			comment.setValue(i18n.tr("Die Überweisung wurde bereits ausgeführt"));
		}
		else if (u.ueberfaellig())
		{
			comment.setValue(i18n.tr("Die Überweisung ist überfällig"));
		}
		return comment;
	}

	/**
	 * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public AbstractInput getKontoAuswahl() throws RemoteException
	{
		if (kontoAuswahl != null)
			return kontoAuswahl;

		ListDialog d = new ListDialog(Settings.getDatabase().createList(Konto.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Kontos"));
		d.addCloseListener(new KontoListener());

		Konto k = getKonto();
		kontoAuswahl = new DialogInput(k == null ? "" : k.getKontonummer(),d);
		kontoAuswahl.setComment(k == null ? "" : k.getBezeichnung());

		if (getUeberweisung().ausgefuehrt())
		{
			kontoAuswahl.disable();
			GUI.getView().setSuccessText(i18n.tr("Die Überweisung wurde bereits ausgeführt und kann daher nicht mehr geändert werden."));
		}
		return kontoAuswahl;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEmpfaengerKonto() throws RemoteException
	{
		if (empfkto != null)
			return empfkto;

		ListDialog d = new ListDialog(Settings.getDatabase().createList(Empfaenger.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Empfängers"));
		d.addCloseListener(new EmpfaengerListener());

		empfkto = new DialogInput(getUeberweisung().getEmpfaengerKonto(),d);

		if (getUeberweisung().ausgefuehrt())
			empfkto.disable();
		return empfkto;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEmpfaengerBlz() throws RemoteException
	{
		if (empfblz != null)
			return empfblz;
		empfblz = new TextInput(getUeberweisung().getEmpfaengerBlz());

		empfblz.setComment("");
		empfblz.addListener(new BLZListener());
		if (getUeberweisung().ausgefuehrt())
			empfblz.disable();
		return empfblz;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getEmpfaengerName() throws RemoteException
	{
		if (empfName != null)
			return empfName;
		empfName = new TextInput(getUeberweisung().getEmpfaengerName());

		if (getUeberweisung().ausgefuehrt())
			empfName.disable();

		return empfName;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getZweck() throws RemoteException
	{
		if (zweck != null)
			return zweck;
		zweck = new TextInput(getUeberweisung().getZweck());

		if (getUeberweisung().ausgefuehrt())
			zweck.disable();

		return zweck;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den "weiteren" Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getZweck2() throws RemoteException
	{
		if (zweck2 != null)
			return zweck2;
		zweck2 = new TextInput(getUeberweisung().getZweck2());

		if (getUeberweisung().ausgefuehrt())
			zweck2.disable();

		return zweck2;
	}


	/**
	 * Liefert das Eingabe-Feld fuer den Betrag.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public AbstractInput getBetrag() throws RemoteException
	{
		if (betrag != null)
			return betrag;
		betrag = new DecimalInput(getUeberweisung().getBetrag(),HBCI.DECIMALFORMAT);

		// wir loesen den KontoListener aus, um die Waehrung sofort anzuzeigen
		
		betrag.setComment(getKonto() == null ? "" : getKonto().getWaehrung());
		new KontoListener().handleEvent(null);

		if (getUeberweisung().ausgefuehrt())
			betrag.disable();

		return betrag;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getTermin() throws RemoteException
	{
		if (termin != null)
			return termin;
		CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		cd.setTitle(i18n.tr("Termin"));
		cd.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Date choosen = (Date) event.data;
				termin.setValue(HBCI.DATEFORMAT.format(choosen));

				try {
					// Wenn das neue Datum spaeter als das aktuelle ist,
					// nehmen wir den Kommentar weg
					if (getUeberweisung().ueberfaellig() && choosen.after(new Date()));
						getComment().setValue("");
					if (choosen.before(new Date()))
						getComment().setValue(i18n.tr("Die Überweisung ist überfällig."));
				}
				catch (RemoteException e) {/*ignore*/}
			}
		});

		Date d = getUeberweisung().getTermin();
		if (d == null)
			d = new Date();
		cd.setDate(d);
		termin = new DialogInput(HBCI.DATEFORMAT.format(d),cd);

		if (getUeberweisung().ausgefuehrt())
			termin.disable();

		return termin;
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

		// Nur bei neuen Ueberweisungen aktivieren
		storeEmpfaenger = new CheckboxInput(getUeberweisung().isNewObject());

		if (getUeberweisung().ausgefuehrt())
			storeEmpfaenger.disable();

		return storeEmpfaenger;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
		try {
			if (getUeberweisung() == null || getUeberweisung().isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Sicher?"));
			d.setText(i18n.tr("Wollen Sie die Überweisung wirklich löschen?"));
			if (!((Boolean) d.open()).booleanValue())
				return;
			getUeberweisung().delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Überweisung gelöscht."));
		}
		catch (Exception e)
		{
			Application.getLog().error("error while deleting ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Überweisung."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		// GUI.startView(UeberweisungListe.class.getName(),null);
		GUI.startPreviousView();
  }

	/**
   * Deaktiviert alle Eingabe-Felder.
   */
  private void disableAll()
	{
		try {
			if (!getUeberweisung().ausgefuehrt())
				return;
			getBetrag().disable();
			getEmpfaengerBlz().disable();
			getEmpfaengerKonto().disable();
			getEmpfaengerName().disable();
			getKontoAuswahl().disable();
			getTermin().disable();
			getZweck().disable();
			getZweck2().disable();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while disabling fields",e);
		}
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public synchronized void handleStore()
  {
		stored = false;
  	try {
  		
  		// erstmal die evtl. Fehler-Zeilen leer machen
  		GUI.getView().setSuccessText("");
  		GUI.getStatusBar().setSuccessText("");

			getUeberweisung().transactionBegin();

  		getUeberweisung().setBetrag(((Double)getBetrag().getValue()).doubleValue());
  		getUeberweisung().setKonto((Konto)getKontoAuswahl().getValue());
  		getUeberweisung().setZweck((String)getZweck().getValue());
			getUeberweisung().setZweck2((String)getZweck2().getValue());
			getUeberweisung().setTermin((Date)getTermin().getValue());

			String kto  = ((DialogInput) getEmpfaengerKonto()).getText();
			String blz  = (String)getEmpfaengerBlz().getValue();
			String name = (String)getEmpfaengerName().getValue();

			getUeberweisung().setEmpfaengerKonto(kto);
			getUeberweisung().setEmpfaengerBlz(blz);
			getUeberweisung().setEmpfaengerName(name);
			getUeberweisung().store();

			Boolean store = (Boolean) getStoreEmpfaenger().getValue();
			if (store.booleanValue())
			{

				// wir checken erstmal, ob wir den schon haben.
				DBIterator list = Settings.getDatabase().createList(Empfaenger.class);
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
				Empfaenger e = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
				e.setBLZ(blz);
				e.setKontonummer(kto);
				e.setName(name);
				e.store();
				GUI.getStatusBar().setSuccessText(i18n.tr("Überweisung und Adresse gespeichert"));
			}
			else {
				GUI.getStatusBar().setSuccessText(i18n.tr("Überweisung gespeichert"));
			}
			getUeberweisung().transactionCommit();
			stored = true;
  	}
  	catch (ApplicationException e)
  	{
			try {
				getUeberweisung().transactionRollback();
			}
			catch (RemoteException re)
			{
				Application.getLog().error("rollback failed",re);
			}
  		GUI.getView().setErrorText(i18n.tr(e.getMessage()));
  	}
  	catch (Exception e2)
  	{
			try {
				getUeberweisung().transactionRollback();
			}
			catch (RemoteException re)
			{
				Application.getLog().error("rollback failed",re);
			}
  		Application.getLog().error("error while storing ueberweisung",e2);
  		GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Überweisung"));
  	}
  }

	/**
   * Speichert die Ueberweisung und fuehrt sie sofort aus.
   */
  public synchronized void handleExecute() // TODO Das synchronized muesste eigentlich in alle Controller
	{

		try {
			if (getUeberweisung().ausgefuehrt())
			{
				GUI.getView().setErrorText(i18n.tr("Die Überweisung wurde bereits ausgeführt."));
				return;
			}

			handleStore();

			if (!stored)
				return;

			UeberweisungDialog d = new UeberweisungDialog(getUeberweisung(),UeberweisungDialog.POSITION_CENTER);
			if (!((Boolean)d.open()).booleanValue())
				return;

		}
		catch (Exception e)
		{
			Application.getLog().error("error while checking ueberweisung",e);
			GUI.getView().setErrorText(i18n.tr("Fehler beim Prüfen der Überweisung"));
		}


		GUI.getStatusBar().startProgress();
		GUI.getStatusBar().setSuccessText(i18n.tr("Führe Überweisung aus..."));

		final StringBuffer errorText = new StringBuffer();
    try {
      GUI.startSync(new Runnable() {
      	public void run() {
      		try {
      			getUeberweisung().execute();
						GUI.getStatusBar().setSuccessText(i18n.tr("...Überweisung erfolgreich ausgeführt."));
      			disableAll();
      		}
      		catch (ApplicationException e)
      		{
      			errorText.append(e.getMessage());
      			throw new RuntimeException(); // wir wollen nicht warten, bis sich die GUI ausgekaest hat, raus hier!
      		}
      		catch (Throwable t)
      		{
      			Application.getLog().error("error while executing ueberweisung",t);
      			errorText.append("Fehler beim Ausführen der Überweisung.");
      			throw new RuntimeException(); // wir wollen nicht warten, bis sich die GUI ausgekaest hat, raus hier!
      		}
      	}
      });
    }
    catch (Throwable t)
    {
			GUI.getStatusBar().setErrorText(i18n.tr(errorText.toString()));
    }

		GUI.getStatusBar().stopProgress();
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(UeberweisungNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(UeberweisungNeu.class.getName(),o);
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
				getKontoAuswahl().setValue(konto.getKontonummer());
				getKontoAuswahl().setComment(b == null ? "" : b);
				getBetrag().setComment(konto.getWaehrung());
			}
			catch (RemoteException er)
			{
				Application.getLog().error("error while updating currency",er);
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
				getEmpfaengerKonto().setValue(empfaenger.getKontonummer());
				getEmpfaengerBlz().setValue(empfaenger.getBLZ());
				getEmpfaengerName().setValue(empfaenger.getName());
				// Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreEmpfaenger().setValue(Boolean.FALSE);
				
				// und jetzt noch das Geld-Institut dranpappen
				new BLZListener().handleEvent(null);
			}
			catch (RemoteException er)
			{
				Application.getLog().error("error while choosing empfaenger",er);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Empfängers"));
    	}
    }
	}
}


/**********************************************************************
 * $Log: UeberweisungControl.java,v $
 * Revision 1.18  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.16  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.14  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.13  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/04/19 22:53:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.10  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.6  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.5  2004/03/04 00:35:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.3  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.2  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/