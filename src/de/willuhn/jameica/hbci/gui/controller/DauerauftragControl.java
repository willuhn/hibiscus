/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/DauerauftragControl.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/10/25 23:12:02 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNeu;
import de.willuhn.jameica.hbci.gui.dialogs.TurnusAuswahlDialog;
import de.willuhn.jameica.hbci.gui.menus.DauerauftragList;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.util.Logger;

/**
 * Controller fuer Dauer-Auftraege.
 */
public class DauerauftragControl extends AbstractTransferControl {

	private Input orderID				= null;
	private DialogInput turnus	= null;
	private DialogInput ersteZahlung	= null;
	private DialogInput letzteZahlung	= null;

  /**
   * ct.
   * @param view
   */
  public DauerauftragControl(AbstractView view) {
    super(view);
  }

	/**
	 * Ueberschrieben, damit wir bei Bedarf einen neuen Dauerauftrag erzeugen koennen.
	 * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
	 */
	public Transfer getTransfer() throws RemoteException
	{
		if (super.getTransfer() != null)
			return (Dauerauftrag) super.getTransfer();
		
		transfer = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
		return (Dauerauftrag) transfer;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Dauerauftraegen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public Part getDauerauftragListe() throws RemoteException
	{
		DBIterator list = Settings.getDBService().createList(Dauerauftrag.class);

		TablePart table = new TablePart(list,new DauerauftragNeu());

		table.addColumn(i18n.tr("Konto"),"konto_id");
		table.addColumn(i18n.tr("Kto. des Empfängers"),"empfaenger_konto");
		table.addColumn(i18n.tr("BLZ des Empfängers"),"empfaenger_blz");
		table.addColumn(i18n.tr("Name des Empfängers"),"empfaenger_name");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("aktiv?"),"orderid",new Formatter()
    {
      public String format(Object o)
      {
      	if (o == null)
      		return "nein";
				String s = o.toString();
				if (s != null && s.length() > 0)
					return i18n.tr("ja");
				return i18n.tr("nein");
      }
    });
		table.addColumn(i18n.tr("Turnus"),"turnus_id");
		table.setContextMenu(new DauerauftragList());
		return table;
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

		TurnusAuswahlDialog tad = new TurnusAuswahlDialog(TurnusAuswahlDialog.POSITION_MOUSE);
		tad.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Turnus choosen = (Turnus) event.data;
				try
				{
					((Dauerauftrag)getTransfer()).setTurnus(choosen);
					getTurnus().setText(choosen.getBezeichnung());
				}
				catch (RemoteException e)
				{
					Logger.error("error while choosing turnus",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Zahlungsturnus"));
				}
			}
		});

		Turnus t = ((Dauerauftrag)getTransfer()).getTurnus();
		turnus = new DialogInput(t == null ? "" : t.getBezeichnung(),tad);
		turnus.disableClientControl();
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
		 orderID = new LabelInput(((Dauerauftrag)getTransfer()).getOrderID());
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
		CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		cd.setTitle(i18n.tr("Datum der ersten Zahlung"));
		cd.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Date choosen = (Date) event.data;
				ersteZahlung.setText(HBCI.DATEFORMAT.format(choosen));
			}
		});

		Date d = ((Dauerauftrag)getTransfer()).getErsteZahlung();
		if (d == null)
			d = new Date();
		cd.setDate(d);
		ersteZahlung = new DialogInput(HBCI.DATEFORMAT.format(d),cd);
		ersteZahlung.disableClientControl();
		ersteZahlung.setValue(d);
		return ersteZahlung;
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
		CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		cd.setTitle(i18n.tr("Datum der letzten Zahlung"));
		cd.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Date choosen = (Date) event.data;
				letzteZahlung.setText(HBCI.DATEFORMAT.format(choosen));
			}
		});

		Date d = ((Dauerauftrag)getTransfer()).getLetzteZahlung();
		if (d != null)
			cd.setDate(d);
		letzteZahlung = new DialogInput(d == null ? null : HBCI.DATEFORMAT.format(d),cd);
		letzteZahlung.disableClientControl();
		letzteZahlung.setValue(d);
		return letzteZahlung;
	}

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized void handleStore()
  {
		super.handleStore();
		// TODO: Turnus
  }

  /**
   * Ueberschreiben wir, um die Auswahl des Kontos zu verbieten, wenn der Dauerauftrag
   * schon aktiv ist.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getKontoAuswahl()
   */
  public DialogInput getKontoAuswahl() throws RemoteException
  {
    DialogInput i = super.getKontoAuswahl();
    if (((Dauerauftrag)getTransfer()).isActive())
    	i.disable();
    return i;
  }

}


/**********************************************************************
 * $Log: DauerauftragControl.java,v $
 * Revision 1.11  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.9  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.7  2004/10/18 23:38:18  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.6  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.5  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.3  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.2  2004/07/16 00:07:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/