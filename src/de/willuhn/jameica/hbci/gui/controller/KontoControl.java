/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/KontoControl.java,v $
 * $Revision: 1.47 $
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
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoFetchFromPassport;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.menus.KontoList;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Controller der fuer den Dialog "Bankverbindungen" zustaendig ist.
 */
public class KontoControl extends AbstractControl {

	// Fachobjekte
	private Konto konto 			 		= null;
	
	// Eingabe-Felder
	private Input kontonummer  		= null;
	private Input blz          		= null;
	private Input name				 		= null;
	private Input bezeichnung	 		= null;
	private Input passportAuswahl = null;
  private Input waehrung     		= null;
  private Input kundennummer 		= null;
  
  private Input saldo				 		= null;
  private Input saldoDatum   		= null;

	private TablePart kontoList						= null;
	private TablePart protokoll						= null;

	private I18N i18n;
  /**
   * ct.
   * @param view
   */
  public KontoControl(AbstractView view) {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die aktuelle Bankverbindung.
   * @return Bankverbindung.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;
		
		try {
			konto = (Konto) getCurrentObject();
			if (konto != null)
				return konto;
		}
		catch (ClassCastException e)
		{
			// Falls wir von 'nem anderen Dialog kommen, kann es durchaus sein,
			// das getCurrentObject() was falsches liefert. Das ist aber nicht
			// weiter schlimm. Wir erstellen dann einfach ein neues.
		}
		
		// Kein Konto verfuegbar - wir bauen ein neues.
		konto = (Konto) Settings.getDBService().createObject(Konto.class,null);
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit dem Protokoll des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getProtokoll() throws RemoteException
	{
		if (protokoll != null)
			return protokoll;

		protokoll = new TablePart(getKonto().getProtokolle(),null);
		protokoll.setFormatter(new TableFormatter() {
			public void format(TableItem item) {
				Protokoll p = (Protokoll) item.getData();
				if (p == null) return;
				try {
					if (p.getTyp() == Protokoll.TYP_ERROR)
					{
						item.setForeground(Color.ERROR.getSWTColor());
					}
					else if (p.getTyp() == Protokoll.TYP_SUCCESS)
					{
						item.setForeground(Color.SUCCESS.getSWTColor());
					}
				}
				catch (RemoteException e)
				{
				}
			}
		});
		protokoll.addColumn(i18n.tr("Datum"),"datum",new DateFormatter(HBCI.LONGDATEFORMAT));
		protokoll.addColumn(i18n.tr("Kommentar"),"kommentar");
		protokoll.disableSummary();
		return protokoll;

	}

	/**
	 * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKontonummer() throws RemoteException
	{
		if (kontonummer != null)
			return kontonummer;
		kontonummer = new TextInput(getKonto().getKontonummer());
		return kontonummer;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die Bankleitzahl.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new TextInput(getKonto().getBLZ());
		blz.setComment("");
		blz.addListener(new BLZListener());
		return blz;
	}

	/**
	 * Liefert den Namen des Konto-Inhabers.
   * @return Name des Konto-Inhabers.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getKonto().getName());
		return name;
	}

	/**
	 * Liefert die Bezeichnung des Kontos.
	 * @return Bezeichnung des Kontos.
	 * @throws RemoteException
	 */
	public Input getBezeichnung() throws RemoteException
	{
		if (bezeichnung != null)
			return bezeichnung;
		bezeichnung = new TextInput(getKonto().getBezeichnung());
		return bezeichnung;
	}

	/**
	 * Liefert das Eingabefeld fuer die Kundennummer.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getKundennummer() throws RemoteException
	{
		if (kundennummer != null)
			return kundennummer;
		kundennummer = new TextInput(getKonto().getKundennummer());
		return kundennummer;
	}

  /**
   * Liefert die Waehrungsbezeichnung.
   * @return Waehrungsbezeichnung.
   * @throws RemoteException
   */
  public Input getWaehrung() throws RemoteException
  {
    if (waehrung != null)
      return waehrung;
    waehrung = new TextInput(getKonto().getWaehrung());
    return waehrung;
  }

	/**
	 * Liefert das Auswahl-Feld fuer das Sicherheitsmedium.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPassportAuswahl() throws RemoteException
	{
		if (passportAuswahl != null)
			return passportAuswahl;

		Passport[] passports = PassportRegistry.getPassports();

		GenericObject[] p = new GenericObject[passports.length];
		for (int i=0;i<passports.length;++i)
		{
			p[i] = new PassportObject(passports[i]);
		}
		passportAuswahl = new SelectInput(PseudoIterator.fromArray(p),null);
		return passportAuswahl;
	}

	/**
	 * Liefert ein Feld zur Anzeige des Saldos.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSaldo() throws RemoteException
	{
		if (saldo != null)
			return saldo;
			
		double s = getKonto().getSaldo();
		saldo = new LabelInput(
			s == 0.0 && getKonto().getSaldoDatum() == null ?
				"" :
				HBCI.DECIMALFORMAT.format(s) + " " + getKonto().getWaehrung());
		return saldo;
	}

	/**
	 * Liefert ein Feld zur Anzeige des Datums des Saldos.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSaldoDatum() throws RemoteException
	{
		if (saldoDatum != null)
			return saldoDatum;

		Date d = getKonto().getSaldoDatum();
		saldoDatum = new LabelInput(d == null ? "" : HBCI.LONGDATEFORMAT.format(d));
		return saldoDatum;
	}

  /**
	 * Liefert eine Tabelle mit allen vorhandenen Bankverbindungen.
   * @return Tabelle mit Bankverbindungen.
   * @throws RemoteException
   */
  public Part getKontoListe() throws RemoteException
	{
		if (kontoList != null)
			return kontoList;

		DBIterator list = Settings.getDBService().createList(Konto.class);

		kontoList = new TablePart(list,new de.willuhn.jameica.hbci.gui.action.KontoNeu());
		kontoList.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		kontoList.addColumn(i18n.tr("Bankleitzahl"),"blz");
		kontoList.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		kontoList.addColumn(i18n.tr("Kontoinhaber"),"name");
		kontoList.addColumn(i18n.tr("Saldo"),"saldo");
		kontoList.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
      	Konto k = (Konto) item.getData();
				try {
					item.setText(4,HBCI.DECIMALFORMAT.format(k.getSaldo()) + " " + k.getWaehrung());
				}
				catch (RemoteException e)
				{
					Logger.error("error while formatting saldo",e);
				}
      }
    });
    
		kontoList.setContextMenu(new KontoList());
		return kontoList;
	}

	/**
   * Initialisiert den Dialog und loest die EventHandler aus.
   */
  public void init()
	{
		new BLZListener().handleEvent(null);
	}

  /**
   * Speichert das Konto.
   */
  public synchronized void handleStore() {
		try {

			PassportObject po = (PassportObject) getPassportAuswahl().getValue();

			if (po == null)
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Kein Sicherheitsmedium verfügbar."));
				return;
			}
			
			getKonto().setPassport(po.getPassport());

			getKonto().setKontonummer((String)getKontonummer().getValue());
			getKonto().setBLZ((String)getBlz().getValue());
			getKonto().setName((String)getName().getValue());
			getKonto().setBezeichnung((String)getBezeichnung().getValue());
      getKonto().setWaehrung((String)getWaehrung().getValue());
      getKonto().setKundennummer((String)getKundennummer().getValue());
      
			// und jetzt speichern wir.
			getKonto().store();
			GUI.getStatusBar().setSuccessText(i18n.tr("Bankverbindung gespeichert."));
		}
		catch (ApplicationException e1)
		{
			GUI.getView().setErrorText(i18n.tr(e1.getLocalizedMessage()));
		}
		catch (RemoteException e)
		{
			Logger.error("unable to store konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Bankverbindung."));
		}

  }

	/**
   * Oeffnet den Einstellungs-Dialog des gerade ausgewaehlten Passports.
   */
  public synchronized void handleConfigurePassport()
	{
		try 
		{
			if (getPassportAuswahl().getValue() == null)
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Kein Sicherheitsmedium verfügbar"));
				return;
			}

			Passport p = ((PassportObject) getPassportAuswahl().getValue()).getPassport();
			new PassportDetail().handleAction(p);
		}
		catch (ApplicationException ae)
		{
			GUI.getStatusBar().setErrorText(ae.getMessage());
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading passport from select box",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Ermittlung des Sicherheitsmediums"));
			return;
		}
	}

	/**
   * Liest alle ueber das Sicherheitsmedium verfuegbaren Konten
   * aus und speichert sie (insofern Konten mit identischer kto-Nummer/BLZ nicht schon existieren).
   */
  public synchronized void handleReadFromPassport()
	{

		try 
		{
			if (getPassportAuswahl().getValue() == null)
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Kein Sicherheitsmedium verfügbar"));
				return;
			}

			Passport p = ((PassportObject) getPassportAuswahl().getValue()).getPassport();
			new KontoFetchFromPassport().handleAction(p);
		}
		catch (ApplicationException ae)
		{
			GUI.getStatusBar().setErrorText(ae.getMessage());
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading passport from select box",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Ermittlung des Sicherheitsmediums"));
			return;
		}

	}

	/**
	 * Sucht das Geldinstitut zur eingegebenen BLZ und zeigt es als Kommentar
	 * hinter dem BLZ-Feld an.
   */
  private class BLZListener implements Listener
	{

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

			try {
				String name = HBCIUtils.getNameForBLZ((String)getBlz().getValue());
				getBlz().setComment(name);
			}
			catch (RemoteException e)
			{
				Logger.error("error while updating blz comment",e);
			}
    }
	}
}


/**********************************************************************
 * $Log: KontoControl.java,v $
 * Revision 1.47  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.46  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.45  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.44  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.43  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.42  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.41  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.40  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.39  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.38  2004/07/04 17:07:59  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
 * Revision 1.37  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2004/06/18 19:53:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2004/06/18 19:47:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.33  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2004/06/07 22:22:33  willuhn
 * @B Spalte "Passport" in KontoListe entfernt - nicht mehr noetig
 *
 * Revision 1.31  2004/06/07 21:55:59  willuhn
 * @B ClassCastException nach dem Verlassen der Passport-Config von der KontoListe aus
 *
 * Revision 1.30  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.28  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.27  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.25  2004/05/02 17:04:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.23  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.21  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.20  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/04/01 22:06:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/03/19 01:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.15  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.14  2004/03/05 00:40:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.12  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.11  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.10  2004/02/24 22:47:05  willuhn
 * @N GUI refactoring
 *
 * Revision 1.9  2004/02/23 20:30:47  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.8  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.7  2004/02/20 01:36:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/17 01:07:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/