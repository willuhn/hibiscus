/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/KontoControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/02/17 00:53:22 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.LabelInput;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.parts.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.KontoListe;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.gui.views.PassportDetails;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller der fuer den Dialog "Bankverbindungen" zustaendig ist.
 */
public class KontoControl extends AbstractControl {

	// Fachobjekte
	private Konto konto = null;
	
	// Eingabe-Felder
	private Input kontonummer  = null;
	private Input blz          = null;
	private Input name				 = null;
	private Input passport     = null;
  private Input waehrung     = null;
  private Input kundennummer = null;
  
  private Input saldo				 = null;
  private Input saldoDatum   = null;

	private boolean stored = false;

  /**
   * ct.
   * @param view
   */
  public KontoControl(AbstractView view) {
    super(view);
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
		
		konto = (Konto) getCurrentObject();
		if (konto != null)
			return konto;
		
		konto = (Konto) Settings.getDatabase().createObject(Konto.class,null);
		return konto;
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
		blz.addComment("",new BLZListener());
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
	 * Lifert das Auswahl-Feld fuer das Sicherheitsmedium.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPassport() throws RemoteException
	{
		if (passport != null)
			return passport;
		
		Passport p = getKonto().getPassport();
		if (p == null)
			p = (Passport) Settings.getDatabase().createObject(Passport.class,null);
		passport = new SelectInput(p);
		return passport;
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
  public Table getKontoListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Konto.class);

		Table table = new Table(list,this);
		table.addColumn(I18N.tr("Kontonummer"),"kontonummer");
		table.addColumn(I18N.tr("Bankleitzahl"),"blz");
		table.addColumn(I18N.tr("Kontoinhaber"),"name");
		table.addColumn(I18N.tr("Kundennummer"),"kundennummer");
		table.addColumn(I18N.tr("Sicherheitsmedium"),"passport_id");
		return table;
	}

	/**
   * Initialisiert den Dialog und loest die EventHandler aus.
   */
  public void init()
	{
		new BLZListener().handleEvent(null);
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
		try {

			MessageBox box = new MessageBox(GUI.getShell(),SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText(I18N.tr("Bankverbindung wirklich löschen?"));
			box.setMessage(I18N.tr("Wollen Sie diese Bankverbindung wirklich löschen?"));
			if (box.open() != SWT.YES)
				return;

			// ok, wir loeschen das Objekt
			getKonto().delete();
			GUI.setActionText(I18N.tr("Bankverbindung gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.setActionText(I18N.tr("Fehler beim Löschen der Bankverbindung."));
			Application.getLog().error("unable to delete konto");
		}
		catch (ApplicationException ae)
		{
			GUI.setActionText(ae.getLocalizedMessage());
		}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		GUI.startView(KontoListe.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
		try {

			//////////////////////////////////////////////////////////////////////////
			// Passport checken
      
			Passport p = (Passport) Settings.getDatabase().createObject(Passport.class,
																																  getPassport().getValue());
			if (p.isNewObject())
			{
				GUI.setActionText(I18N.tr("Bitte wählen Sie ein Sicherheitsmedium aus."));
				return;
			}
			getKonto().setPassport(p);
			//
			//////////////////////////////////////////////////////////////////////////

			getKonto().setKontonummer(getKontonummer().getValue());
			getKonto().setBLZ(getBlz().getValue());
			getKonto().setName(getName().getValue());
      getKonto().setWaehrung(getWaehrung().getValue());
      getKonto().setKundennummer(getKundennummer().getValue());
      
			// und jetzt speichern wir.
			getKonto().store();
			GUI.setActionText(I18N.tr("Bankverbindung gespeichert."));
			stored = true;
		}
		catch (ApplicationException e1)
		{
			GUI.setActionText(e1.getLocalizedMessage());
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to store konto",e);
			GUI.setActionText("Fehler beim Speichern der Bankverbindung.");
		}

  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(KontoNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleLoad(java.lang.String)
   */
  public void handleLoad(String id) {
		try {
			Konto k = (Konto) Settings.getDatabase().createObject(Konto.class,id);
			GUI.startView(KontoNeu.class.getName(),k);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to load konto with id " + id);
			GUI.setActionText(I18N.tr("Bankverbindung wurde nicht gefunden."));
		}
  }

	/**
   * Wird aufgerufen, wenn der Passport konfiguriert werden soll.
   */
  public void handleConfigurePassport()
	{
		try {
			handleStore();
			if (stored)
				GUI.startView(PassportDetails.class.getName(),getKonto());

		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while configuring passport",e);
			GUI.setActionText(I18N.tr("Fehler beim Öffnen der Konfiguration"));
		}
	}

	/**
   * Aktualisiert die aktuellen Eingaben mit denen des Sicherheitsmediums.
   */
  public void handleReadFromPassport()
	{
		try {
			if ("".equals(getKontonummer().getValue()))
			{
				GUI.setActionText(I18N.tr("Bitte geben Sie mindestens die Kontonummer ein"));
				return;
			}
			GUI.setActionText(I18N.tr("Chipkarte wird ausgelesen..."));
			getKonto().readFromPassport();
			getKundennummer().setValue(getKonto().getKundennummer());
			getName().setValue(getKonto().getName());
			getWaehrung().setValue(getKonto().getWaehrung());
			GUI.setActionText(I18N.tr("Daten erfolgreich gelesen"));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while reading data from passport",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Konto-Daten"));
		}
	}

	/**
   * Aktualisiert den angezeigten Saldo.
   */
  public void handleRefreshSaldo()
	{
		try {
			if ("".equals(getKontonummer().getValue()))
			{
				GUI.setActionText(I18N.tr("Bitte geben Sie mindestens die Kontonummer ein"));
				return;
			}
		}
		catch (Exception e)
		{
			Application.getLog().error("error while reading kontonummer",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Kontonummer"));
		}

		GUI.getDisplay().asyncExec(new Runnable() {
      public void run() {
      	try {
      		GUI.setActionText(I18N.tr("Saldo des Kontos wird ermittelt..."));
					getKonto().refreshSaldo();
					getSaldo().setValue(HBCI.DECIMALFORMAT.format(getKonto().getSaldo()));
					getSaldoDatum().setValue(HBCI.LONGDATEFORMAT.format(getKonto().getSaldoDatum()));
					GUI.setActionText(I18N.tr("Saldo des Kontos erfolgreich übertragen..."));
      	}
      	catch (RemoteException e)
      	{
					Application.getLog().error("error while reading saldo",e);
					GUI.setActionText(I18N.tr("Fehler beim Lesen des Saldos"));
      	}
      	catch (ApplicationException e2)
      	{
      		GUI.setActionText(e2.getLocalizedMessage());
      	}
      }
    });
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
				String name = HBCIUtils.getNameForBLZ(getBlz().getValue());
				getBlz().updateComment(name);
			}
			catch (RemoteException e)
			{
				Application.getLog().error("error while updating blz comment",e);
			}
    }
	}
}


/**********************************************************************
 * $Log: KontoControl.java,v $
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