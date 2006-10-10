/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/DauerauftragControl.java,v $
 * $Revision: 1.23 $
 * $Date: 2006/10/10 22:55:10 $
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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.dialogs.TurnusDialog;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.logging.Logger;

/**
 * Controller fuer Dauer-Auftraege.
 */
public class DauerauftragControl extends AbstractTransferControl {

	private Input orderID				      = null;
	private DialogInput turnus	      = null;
	private DateInput ersteZahlung	  = null;
	private DateInput letzteZahlung	  = null;

  private Dauerauftrag transfer     = null;

  private Part list                 = null;

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
    if (transfer != null)
      return transfer;

    transfer = (Dauerauftrag) getCurrentObject();
    if (transfer != null)
      return transfer;
      
    transfer = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
    return transfer;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Dauerauftraegen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public Part getDauerauftragListe() throws RemoteException
	{
    if (list != null)
      return list;
    list = new de.willuhn.jameica.hbci.gui.parts.DauerauftragList(new DauerauftragNew());
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
		turnus = new DialogInput(t == null ? "" : t.getBezeichnung(),td);
		turnus.setValue(t);
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
    
    Date d = ((Dauerauftrag)getTransfer()).getErsteZahlung();
    if (d == null)
      d = new Date();

    ersteZahlung = new DateInput(d,HBCI.DATEFORMAT);
    ersteZahlung.setComment("");
		ersteZahlung.setTitle(i18n.tr("Datum der ersten Zahlung"));
    ersteZahlung.setText(i18n.tr("Bitte geben Sie das Datum der ersten Zahlung ein"));
    ersteZahlung.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Nur, um den Parser zu triggern
        ersteZahlung.getValue();
      }
    
    });
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

    Date d = ((Dauerauftrag)getTransfer()).getLetzteZahlung();

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
    return letzteZahlung;
	}

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
  	try
  	{
			Dauerauftrag d = (Dauerauftrag) getTransfer();
			d.setErsteZahlung((Date)getErsteZahlung().getValue());
			d.setLetzteZahlung((Date)getLetzteZahlung().getValue());
			d.setTurnus((Turnus)getTurnus().getValue());
			return super.handleStore();
  	}
  	catch (RemoteException e)
  	{
  		Logger.error("error while saving dauerauftrag",e);
  		GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Dauerauftrages"));
  	}
  	return false;
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
 * Revision 1.23  2006/10/10 22:55:10  willuhn
 * @N Alle Datumseingabe-Felder auf DateInput umgestellt
 *
 * Revision 1.22  2006/10/09 23:56:13  willuhn
 * @N TODO-Tags
 *
 * Revision 1.21  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.20  2005/03/04 00:50:16  web0
 * @N Eingrauen abgelaufener Dauerauftraege
 * @N automatisches Loeschen von Dauerauftraegen, die lokal zwar
 * noch als aktiv markiert sind, bei der Bank jedoch nicht mehr existieren
 *
 * Revision 1.19  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.18  2004/11/26 00:04:08  willuhn
 * @N TurnusDetail
 *
 * Revision 1.17  2004/11/18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.15  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 * Revision 1.13  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/25 23:22:39  willuhn
 * *** empty log message ***
 *
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