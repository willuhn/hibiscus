/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UeberweisungControl.java,v $
 * $Revision: 1.29 $
 * $Date: 2004/10/08 13:37:47 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.UeberweisungDialog;
import de.willuhn.jameica.hbci.gui.menus.UeberweisungList;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Logger;

/**
 * Controller fuer die Ueberweisungen.
 */
public class UeberweisungControl extends AbstractTransferControl
{

	// Eingabe-Felder
	private DialogInput termin = null;
	private Input comment			 = null;

  /**
   * ct.
   * @param view
   */
  public UeberweisungControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Ueberschrieben, damit wir bei Bedarf eine neue Ueberweisung erzeugen koennen.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
   */
  public Transfer getTransfer() throws RemoteException
	{
		if (super.getTransfer() != null)
			return (Ueberweisung) super.getTransfer();
		
		transfer = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
		return (Ueberweisung) transfer;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public Part getUeberweisungListe() throws RemoteException
	{
		DBIterator list = Settings.getDBService().createList(Ueberweisung.class);

		TablePart table = new TablePart(list,this);
		table.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
      	Ueberweisung u = (Ueberweisung) item.getData();
      	if (u == null)
      		return;

				try {
					if (u.getTermin().before(new Date()) && !u.ausgefuehrt())
					{
						item.setForeground(Settings.getUeberfaelligForeground());
					}
				}
				catch (RemoteException e) { /*ignore */}
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

		table.setContextMenu(new UeberweisungList());
		return table;
	}

	/**
	 * Liefert ein Kommentar-Feld zu dieser Ueberweisung.
   * @return Kommentarfeld.
   * @throws RemoteException
   */
  public Input getComment() throws RemoteException
	{
		if (comment != null)
			return comment;
		comment = new LabelInput("");
		Ueberweisung u = (Ueberweisung) getTransfer();
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
	 * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DialogInput getTermin() throws RemoteException
	{
		final Ueberweisung u = (Ueberweisung) getTransfer();

		if (termin != null)
			return termin;
		CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		cd.setTitle(i18n.tr("Termin"));
		cd.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Date choosen = (Date) event.data;
				termin.setText(HBCI.DATEFORMAT.format(choosen));

				try {
					// Wenn das neue Datum spaeter als das aktuelle ist,
					// nehmen wir den Kommentar weg
					if (u.ueberfaellig() && choosen.after(new Date()));
						getComment().setValue("");
					if (choosen.before(new Date()))
						getComment().setValue(i18n.tr("Die Überweisung ist überfällig."));
				}
				catch (RemoteException e) {/*ignore*/}
			}
		});

		Date d = u.getTermin();
		if (d == null)
			d = new Date();
		cd.setDate(d);
		termin = new DialogInput(HBCI.DATEFORMAT.format(d),cd);
		termin.disableClientControl();
		termin.setValue(d);

		if (u.ausgefuehrt())
			termin.disable();

		return termin;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public synchronized void handleDelete() {

		try {

			Ueberweisung u = (Ueberweisung) getTransfer();

			if (u == null || u.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Sicher?"));
			d.setText(i18n.tr("Wollen Sie die Überweisung wirklich löschen?"));
			if (!((Boolean) d.open()).booleanValue())
				return;
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Überweisung gelöscht."));
		}
		catch (Exception e)
		{
			Logger.error("error while deleting ueberweisung",e);
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
			Ueberweisung u = (Ueberweisung) getTransfer();

			if (!u.ausgefuehrt())
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
			Logger.error("error while disabling fields",e);
		}
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public synchronized void handleStore()
  {
		try
		{
			Ueberweisung u = (Ueberweisung) getTransfer();
			Date termin = (Date) getTermin().getValue();
			if (termin == null)
			{
				try
				{
					termin = HBCI.DATEFORMAT.parse(getTermin().getText());
				}
				catch (Exception e)
				{
					GUI.getView().setErrorText("Bitte geben Sie einen Termin ein.");
					return;
				}
			}
			u.setTermin(termin);
		}
		catch (RemoteException re)
		{
			Logger.error("rollback failed",re);
			Logger.error("error while storing ueberweisung",re);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Überweisung"));
			return;
  	}
		super.handleStore();
  }

	/**
   * Speichert die Ueberweisung und fuehrt sie sofort aus.
   */
  public synchronized void handleExecute()
	{

		try {
			if (((Ueberweisung)getTransfer()).ausgefuehrt())
			{
				GUI.getView().setErrorText(i18n.tr("Die Überweisung wurde bereits ausgeführt."));
				return;
			}

			handleStore();

			if (!stored)
				return;

			UeberweisungDialog d = new UeberweisungDialog(((Ueberweisung)getTransfer()),UeberweisungDialog.POSITION_CENTER);
			if (!((Boolean)d.open()).booleanValue())
				return;

		}
		catch (Exception e)
		{
			Logger.error("error while checking ueberweisung",e);
			GUI.getView().setErrorText(i18n.tr("Fehler beim Prüfen der Überweisung"));
		}


		GUI.getStatusBar().startProgress();
		GUI.getStatusBar().setSuccessText(i18n.tr("Führe Überweisung aus..."));

		final StringBuffer errorText = new StringBuffer();
    try {
      GUI.startSync(new Runnable() {
      	public void run() {
      		try {
						((Ueberweisung)getTransfer()).execute();
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
      			Logger.error("error while executing ueberweisung",t);
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
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getBetrag()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getBetrag() throws RemoteException
  {
    Input i = super.getBetrag();
    if (((Ueberweisung)getTransfer()).ausgefuehrt())
    	i.disable();
    return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerBlz()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getEmpfaengerBlz() throws RemoteException
  {
		Input i = super.getEmpfaengerBlz();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerKonto()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public DialogInput getEmpfaengerKonto() throws RemoteException
  {
		DialogInput i = super.getEmpfaengerKonto();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerName()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getEmpfaengerName() throws RemoteException
  {
		Input i = super.getEmpfaengerName();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getKontoAuswahl()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public DialogInput getKontoAuswahl() throws RemoteException
  {
		DialogInput i = super.getKontoAuswahl();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getStoreEmpfaenger()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public CheckboxInput getStoreEmpfaenger() throws RemoteException
  {
		CheckboxInput i = super.getStoreEmpfaenger();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getZweck() throws RemoteException
  {
		Input i = super.getZweck();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck2()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getZweck2() throws RemoteException
  {
		Input i = super.getZweck2();
		if (((Ueberweisung)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

}


/**********************************************************************
 * $Log: UeberweisungControl.java,v $
 * Revision 1.29  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2004/10/08 00:19:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/08/01 13:08:42  willuhn
 * @B Handling von Ueberweisungsterminen
 *
 * Revision 1.26  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.25  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.24  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.23  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.22  2004/07/09 00:12:29  willuhn
 * @B minor bugs
 *
 * Revision 1.21  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.20  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
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