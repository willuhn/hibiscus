/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/KontoControl.java,v $
 * $Revision: 1.82 $
 * $Date: 2009/05/07 13:36:57 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoFetchFromPassport;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeOptionsDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.PassportInput;
import de.willuhn.jameica.hbci.gui.parts.ProtokollList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzChart;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller der fuer den Dialog "Bankverbindungen" zustaendig ist.
 */
public class KontoControl extends AbstractControl {

	// Fachobjekte
	private Konto konto 			 		= null;
	
	// Eingabe-Felder
	private TextInput kontonummer  		= null;
  private TextInput unterkonto      = null;
	private TextInput blz          		= null;
	private Input name				 		    = null;
	private Input bezeichnung	 		    = null;
	private Input passportAuswahl     = null;
  private Input kundennummer 		    = null;
  private Input kommentar           = null;
  
  private LabelInput saldo			        = null;
  private SaldoMessageConsumer consumer = null;
  
  private Button synchronizeOptions = null;

	private TablePart kontoList						= null;
	private TablePart protokoll						= null;
  private UmsatzList umsatzList         = null;
  private UmsatzChart umsatzChart       = null;

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

		protokoll = new ProtokollList(getKonto(),null);
		return protokoll;
	}

  /**
   * Liefert eine Tabelle mit den Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzList() throws RemoteException
  {
    if (umsatzList != null)
      return umsatzList;

    umsatzList = new UmsatzList(getKonto(),HBCIProperties.UMSATZ_DEFAULT_DAYS,new UmsatzDetail());
    umsatzList.setFilterVisible(false);
    return umsatzList;
  }

  /**
   * Liefert einen Chart mit den Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzChart() throws RemoteException
  {
    if (umsatzChart != null)
      return umsatzChart;

    umsatzChart = new UmsatzChart(getKonto());
    return umsatzChart;
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
		kontonummer = new TextInput(getKonto().getKontonummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    // BUGZILLA 280
    kontonummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    kontonummer.setMandatory(true);
		return kontonummer;
	}

  /**
   * Liefert das Eingabe-Feld fuer die Unterkontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getUnterkonto() throws RemoteException
  {
    if (unterkonto != null)
      return unterkonto;
    unterkonto = new TextInput(getKonto().getUnterkonto(),HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    unterkonto.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    unterkonto.setComment(i18n.tr("Kann meist frei gelassen werden"));
    return unterkonto;
  }

  /**
   * Liefert einen Button, ueber den die Synchronisierungsdetails konfiguriert
   * werden.
   * @return Button.
   * @throws RemoteException
   */
  public Button getSynchronizeOptions() throws RemoteException
  {
    if (this.synchronizeOptions != null)
      return this.synchronizeOptions;

    this.synchronizeOptions = new Button(i18n.tr("Synchronisierungsoptionen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          SynchronizeOptionsDialog d = new SynchronizeOptionsDialog(getKonto(),SynchronizeOptionsDialog.POSITION_CENTER);
          d.open();
        }
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to configure synchronize options");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Konfigurieren der Synchronisierungsoptionen"),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    },getKonto(),false,"document-properties.png");
    return this.synchronizeOptions;
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
		blz = new BLZInput(getKonto().getBLZ());
    blz.setMandatory(true);
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
		name = new TextInput(getKonto().getName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    name.setMandatory(true);
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
    kundennummer.setMandatory(true);
		return kundennummer;
	}

	/**
	 * Liefert das Auswahl-Feld fuer das Sicherheitsmedium.
   * @return Eingabe-Feld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getPassportAuswahl() throws RemoteException, ApplicationException
	{
		if (passportAuswahl != null)
			return passportAuswahl;

		passportAuswahl = new PassportInput(getKonto());
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
			
		saldo = new LabelInput("");
    saldo.setComment(""); // Platz fuer Kommentar reservieren
    // Einmal ausloesen, damit das Feld mit Inhalt gefuellt wird.
    this.consumer = new SaldoMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
    Application.getMessagingFactory().sendMessage(new SaldoMessage(getKonto()));
    return saldo;
	}
  
  /**
   * Liefert ein Eingabe-Feld fuer einen Kommentar.
   * @return Kommentar.
   * @throws RemoteException
   */
  public Input getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    this.kommentar = new TextAreaInput(getKonto().getKommentar());
    return this.kommentar;
  }

  /**
   * Liefert einen Saldo-MessageConsumer.
   * @return Consumer.
   */
  public MessageConsumer getSaldoMessageConsumer()
  {
    return this.consumer;
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

    kontoList = new de.willuhn.jameica.hbci.gui.parts.KontoList(new KontoNew());
    // BUGZILLA 108 http://www.willuhn.de/bugzilla/show_bug.cgi?id=108
    kontoList.addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
    // BUGZILLA 81 http://www.willuhn.de/bugzilla/show_bug.cgi?id=81
    kontoList.addColumn(i18n.tr("Umsätze"),"numumsaetze");
		return kontoList;
	}

  /**
   * Speichert das Konto.
   */
  public synchronized void handleStore() {
		try {

			Passport p = (Passport) getPassportAuswahl().getValue();

			if (p == null)
			{
			  Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie ein Sicherheitsmedium aus"), StatusBarMessage.TYPE_ERROR));
				return;
			}
			
			getKonto().setPassportClass(p.getClass().getName());

			getKonto().setKontonummer((String)getKontonummer().getValue());
      getKonto().setUnterkonto((String)getUnterkonto().getValue());
			getKonto().setBLZ((String)getBlz().getValue());
			getKonto().setName((String)getName().getValue());
			getKonto().setBezeichnung((String)getBezeichnung().getValue());
      getKonto().setKundennummer((String)getKundennummer().getValue());
      getKonto().setKommentar((String) getKommentar().getValue());
      
      // und jetzt speichern wir.
			getKonto().store();
			GUI.getStatusBar().setSuccessText(i18n.tr("Bankverbindung gespeichert."));
      GUI.getView().setSuccessText("");
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
   * Liest alle ueber das Sicherheitsmedium verfuegbaren Konten
   * aus und speichert sie (insofern Konten mit identischer kto-Nummer/BLZ nicht schon existieren).
   */
  public synchronized void handleReadFromPassport()
	{

		try 
		{
      PassportAuswahlDialog d = new PassportAuswahlDialog(PassportAuswahlDialog.POSITION_CENTER);
      Passport p = (Passport) d.open();

      new KontoFetchFromPassport().handleAction(p);
		}
    catch (OperationCanceledException oce)
    {
      // ignore
    }
		catch (ApplicationException ae)
		{
			GUI.getStatusBar().setErrorText(ae.getMessage());
		}
		catch (Exception e)
		{
			Logger.error("error while reading passport from select box",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Auslesen der Konto-Informationen"));
		}
	}

  /**
   * Laedt die Tabelle mit den Umsaetzen neu.
   */
  public void handleReload()
  {
    GUI.startSync(new Runnable() {
      public void run()
      {
        try
        {
          UmsatzList list = ((UmsatzList)getUmsatzList());
          list.removeAll();
          Konto k = getKonto();
          DBIterator i = k.getUmsaetze(HBCIProperties.UMSATZ_DEFAULT_DAYS);
          while (i.hasNext())
            list.addItem(i.next());
          list.sort();
          Application.getMessagingFactory().sendMessage(new SaldoMessage(getKonto()));
        }
        catch (IllegalArgumentException iae)
        {
          // Fliegt, wenn der Dialog zwischenzeitlich verlassen
          // wurde und die Tabelle disposed ist.
          // Dann brechen wir ab und ignorieren den Fehler.
          Logger.warn("umsatz table has be disposed in the meantime, skip reload");
          return;
        }
        catch (RemoteException e)
        {
          Logger.error("error while reloading umsatz list",e);
        }
      }
    });
  }

  /**
   * Wird beim Eintreffen neuer Salden benachrichtigt und aktualisiert ggf die Anzeige.
   */
  private class SaldoMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SaldoMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable() {
        
        public void run()
        {
          try
          {
            if (saldo == null)
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory().unRegisterMessageConsumer(SaldoMessageConsumer.this);
              return;
            }
            
            SaldoMessage msg = (SaldoMessage) message;
            Konto k = (Konto)msg.getObject();
            if (k == null || !k.equals(getKonto()))
              return; // Kein Konto oder nicht unseres

            double s = k.getSaldo();
            Date   d = k.getSaldoDatum();
            if (d == null)
            {
              // Kein Datum, kein Saldo
              saldo.setValue("");
              saldo.setComment(i18n.tr("noch kein Saldo abgerufen"));
            }
            else
            {
              saldo.setValue(HBCI.DECIMALFORMAT.format(s) + " " + getKonto().getWaehrung());
              if (s < 0)
                saldo.setColor(Color.ERROR);
              else if (s > 0)
                saldo.setColor(Color.SUCCESS);
              else
                saldo.setColor(Color.WIDGET_FG);

              saldo.setComment(i18n.tr("vom {0}",HBCI.LONGDATEFORMAT.format(d)));
            }
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("unable to refresh saldo",e);
            Application.getMessagingFactory().unRegisterMessageConsumer(SaldoMessageConsumer.this);
          }
        }
      
      });
    }
    
  }


}


/**********************************************************************
 * $Log: KontoControl.java,v $
 * Revision 1.82  2009/05/07 13:36:57  willuhn
 * @R Hilfsobjekt "PassportObject" entfernt
 * @C Cleanup in PassportInput (insb. der weisse Hintergrund hinter dem "Konfigurieren..."-Button hat gestoert
 *
 * Revision 1.81  2009/01/26 23:17:46  willuhn
 * @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
 *
 * Revision 1.80  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.79  2009/01/04 17:43:30  willuhn
 * @N BUGZILLA 532
 *
 * Revision 1.78  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.77  2007/12/11 12:23:26  willuhn
 * @N Bug 355
 *
 * Revision 1.76  2007/08/29 10:04:42  willuhn
 * @N Bug 476
 *
 * Revision 1.75  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 * Revision 1.74  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 * Revision 1.73  2007/02/22 23:37:11  willuhn
 * @B dont reload umsatz list when table has been disposed
 *
 * Revision 1.72  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.71  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 * Revision 1.70  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.69  2006/08/17 21:46:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.68  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.67  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
 * Revision 1.66  2006/03/30 22:22:32  willuhn
 * @B bug 217
 *
 * Revision 1.65  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 * Revision 1.64  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 * Revision 1.63  2006/01/23 11:11:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.62  2005/08/08 16:10:26  willuhn
 * @B bug 108
 *
 * Revision 1.61  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 * Revision 1.60  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.59  2005/07/04 21:57:08  web0
 * @B bug 80
 *
 * Revision 1.58  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 * Revision 1.57  2005/06/03 17:14:20  web0
 * @B NPE
 *
 * Revision 1.56  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.55  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.54  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.53  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.52  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.51  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.50  2005/02/01 18:27:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.49  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.48  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
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