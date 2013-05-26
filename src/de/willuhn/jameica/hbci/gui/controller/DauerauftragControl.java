/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/DauerauftragControl.java,v $
 * $Revision: 1.34 $
 * $Date: 2011/08/10 10:46:50 $
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
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.dialogs.TurnusDialog;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.parts.DauerauftragList;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.TypedProperties;

/**
 * Controller fuer Dauer-Auftraege.
 */
public class DauerauftragControl extends AbstractTransferControl {

  private Listener nextDate          = new NextDateListener();
	private Input orderID				       = null;
	private DialogInput turnus	       = null;
	private DateInput ersteZahlung	   = null;
	private DateInput letzteZahlung	   = null;
	private SelectInput textschluessel = null;
	
  private Dauerauftrag transfer      = null;
  private TypedProperties bpd        = null;

  private DauerauftragList list      = null;
  
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
	public HibiscusTransfer getTransfer() throws RemoteException
	{
    if (transfer != null)
      return transfer;

    Object o = getCurrentObject();
    if (o != null && (o instanceof Dauerauftrag))
      return (Dauerauftrag) o;
      
    transfer = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
    return transfer;
	}
	
	/**
	 * Liefert die passenden BPD-Parameter fuer den Auftrag.
	 * @return die BPD.
	 * @throws RemoteException
	 */
	private TypedProperties getBPD() throws RemoteException
	{
	  if (this.bpd != null)
	    return this.bpd;
	  
	  Dauerauftrag auftrag = (Dauerauftrag) this.getTransfer();
	  if (auftrag.isActive())
      this.bpd = DBPropertyUtil.getBPD(auftrag.getKonto(),DBPropertyUtil.BPD_QUERY_DAUER_EDIT);
	  else
	    this.bpd = new TypedProperties(); // Der Auftrag ist noch nicht aktiv - dann gibt es noch keine Einschraenkungen
	  
	  return this.bpd;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Dauerauftraegen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public DauerauftragList getDauerauftragListe() throws RemoteException
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
          nextDate.handleEvent(null);
				}
				catch (RemoteException e)
				{
					Logger.error("error while choosing turnus",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Zahlungsturnus"));
				}
			}
		});

		Dauerauftrag da = (Dauerauftrag) getTransfer();
		Turnus t = da.getTurnus();
		turnus = new DialogInput(t == null ? "" : t.getBezeichnung(),td);
		turnus.setValue(t);
    turnus.setMandatory(true);
    
    if (da.isActive())
    {
      boolean changable = getBPD().getBoolean("turnuseditable",true) && getBPD().getBoolean("timeuniteditable",true);
      turnus.setEnabled(changable);
    }
    
    turnus.disableClientControl(); // Client-Control generell deaktivieren - auch wenn Aenderungen erlaubt sind
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
    
    final Dauerauftrag t = (Dauerauftrag) getTransfer();
    Date d = t.getErsteZahlung();
    if (d == null)
    {
      d = new Date();
      t.setErsteZahlung(d);
    }

    ersteZahlung = new DateInput(d);
    ersteZahlung.setComment("");
		ersteZahlung.setTitle(i18n.tr("Datum der ersten Zahlung"));
    ersteZahlung.setText(i18n.tr("Bitte geben Sie das Datum der ersten Zahlung ein"));
    ersteZahlung.setMandatory(true);
    ersteZahlung.addListener(this.nextDate);
    
    if (t.isActive())
      ersteZahlung.setEnabled(getBPD().getBoolean("firstexeceditable",true));
    
    this.nextDate.handleEvent(null); // einmal ausloesen fuer initialen Text
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

		Dauerauftrag t = (Dauerauftrag) getTransfer();
    Date d = t.getLetzteZahlung();

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

    if (t.isActive())
      letzteZahlung.setEnabled(getBPD().getBoolean("lastexeceditable",true));
    
    return letzteZahlung;
	}

  /**
   * Liefert ein Auswahlfeld fuer den Textschluessel.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public Input getTextSchluessel() throws RemoteException
  {
    if (textschluessel != null)
      return textschluessel;

    Dauerauftrag t = (Dauerauftrag) getTransfer();

    textschluessel = new SelectInput(TextSchluessel.get(TextSchluessel.SET_DAUER),TextSchluessel.get(t.getTextSchluessel()));
    
    if (t.isActive())
      textschluessel.setEnabled(getBPD().getBoolean("keyeditable",true));
    
    return textschluessel;
  }

  /**
   * Ueberschreiben wir, um die Auswahl des Kontos zu verbieten, wenn der Dauerauftrag schon aktiv ist.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getKontoAuswahl()
   */
  public KontoInput getKontoAuswahl() throws RemoteException
  {
    KontoInput i = super.getKontoAuswahl();
    i.setEnabled(!((Dauerauftrag)getTransfer()).isActive());
    return i;
  }


  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getBetrag()
   */
  public Input getBetrag() throws RemoteException
  {
    Input i = super.getBetrag();
    Dauerauftrag t = (Dauerauftrag) getTransfer();
    if (t.isActive())
      i.setEnabled(getBPD().getBoolean("valueeditable",true));
    
    return i;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerName()
   */
  public AddressInput getEmpfaengerName() throws RemoteException
  {
    AddressInput i = super.getEmpfaengerName();
    Dauerauftrag t = (Dauerauftrag) getTransfer();
    if (t.isActive())
    {
      boolean changable = getBPD().getBoolean("recnameeditable",true) && getBPD().getBoolean("recktoeditable",true);
      i.setEnabled(changable);
    }
    return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerKonto()
   */
  public TextInput getEmpfaengerKonto() throws RemoteException
  {
    TextInput i = super.getEmpfaengerKonto();
    Dauerauftrag t = (Dauerauftrag) getTransfer();
    if (t.isActive())
      i.setEnabled(getBPD().getBoolean("recktoeditable",true));
    return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerBlz()
   */
  public TextInput getEmpfaengerBlz() throws RemoteException
  {
    TextInput i = super.getEmpfaengerBlz();
    Dauerauftrag t = (Dauerauftrag) getTransfer();
    if (t.isActive())
      i.setEnabled(getBPD().getBoolean("recktoeditable",true));
    return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck()
   */
  public TextInput getZweck() throws RemoteException
  {
    TextInput i = super.getZweck();
    Dauerauftrag t = (Dauerauftrag) getTransfer();
    if (t.isActive())
      i.setEnabled(getBPD().getBoolean("usageeditable",true));
    return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck2()
   */
  public DialogInput getZweck2() throws RemoteException
  {
    DialogInput i = super.getZweck2();
    Dauerauftrag t = (Dauerauftrag) getTransfer();
    if (t.isActive())
      i.setEnabled(getBPD().getBoolean("usageeditable",true));
    return i;
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
      TextSchluessel s = (TextSchluessel) getTextSchluessel().getValue();
      d.setTextSchluessel(s == null ? null : s.getCode());
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
   * Listener, der das Datum der naechsten Zahlung aktualisiert.
   */
  private class NextDateListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try
      {
        final Dauerauftrag t = (Dauerauftrag) getTransfer();
        Date ez = (Date) ersteZahlung.getValue();
        t.setErsteZahlung(ez);
        Date next = t.getNaechsteZahlung();
        if (next != null)
          ersteZahlung.setComment(i18n.tr("Nächste: {0}", HBCI.DATEFORMAT.format(next)));
        else
          ersteZahlung.setComment("");
      }
      catch (Exception e)
      {
        Logger.error("unable to apply first payment date",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ermitteln der nächsten Zahlung"), StatusBarMessage.TYPE_ERROR));
      }
    }
  }

}


/**********************************************************************
 * $Log: DauerauftragControl.java,v $
 * Revision 1.34  2011/08/10 10:46:50  willuhn
 * @N Aenderungen nur an den DA-Eigenschaften zulassen, die gemaess BPD aenderbar sind
 * @R AccountUtil entfernt, Code nach VerwendungszweckUtil verschoben
 * @N Neue Abfrage-Funktion in DBPropertyUtil, um die BPD-Parameter zu Geschaeftsvorfaellen bequemer abfragen zu koennen
 *
 * Revision 1.33  2011-05-10 11:41:30  willuhn
 * @N Text-Schluessel als Konstanten definiert - Teil aus dem Patch von Thomas vom 07.12.2010
 *
 * Revision 1.32  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.31  2011-04-06 08:17:16  willuhn
 * @N Detail-Anzeige zweispaltig, damit sie besser auf kleinere Bildschirme passt - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=74593#74593
 *
 * Revision 1.30  2010-09-24 12:22:04  willuhn
 * @N Thomas' Patch fuer Textschluessel in Dauerauftraegen
 *
 * Revision 1.29  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 *
 * Revision 1.28  2008/08/27 14:42:24  willuhn
 * @N Naechstes Ausfuehrungsdatum in Detailansicht anzeigen
 *
 * Revision 1.27  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.26  2007/01/02 11:28:04  willuhn
 * @B ClassCastException
 *
 * Revision 1.25  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.24  2006/10/31 23:21:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2006/10/10 22:55:10  willuhn
 * @N Alle Datumseingabe-Felder auf DateInput umgestellt
 *
 * Revision 1.22  2006/10/09 23:56:13  willuhn
 * @N T O D O-Tags
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