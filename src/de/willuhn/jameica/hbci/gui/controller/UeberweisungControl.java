/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UeberweisungControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/22 20:04:53 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.CheckboxInput;
import de.willuhn.jameica.gui.parts.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.SearchInput;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.parts.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.EmpfaengerSearchDialog;
import de.willuhn.jameica.hbci.gui.views.UeberweisungListe;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Ueberweisungen.
 */
public class UeberweisungControl extends AbstractControl {

	// Fach-Objekte
	private Ueberweisung ueberweisung = null;
	private Empfaenger empfaenger = null;
	
	// Eingabe-Felder
	private Input konto 			= null;
	private Input betrag			= null;
	private Input zweck				= null;
	private Input zweck2			= null;
	private Input termin			= null;

	private Input empfName 	= null;
	private Input empfkto 	= null;
	private Input empfblz 	= null;
	
	private CheckboxInput storeEmpfaenger = null;

  /**
   * ct.
   * @param view
   */
  public UeberweisungControl(AbstractView view) {
    super(view);
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
	 * Liefert den Empfaenger der Ueberweisung.
   * @return Empfaenger der Ueberweisung.
   * @throws RemoteException
   */
  private Empfaenger getEmpfaenger() throws RemoteException
	{
		if (empfaenger != null)
			return empfaenger;
		empfaenger = getUeberweisung().getEmpfaenger();
		if (empfaenger != null)
			return empfaenger;
		
		empfaenger = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
		return empfaenger;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public Table getUeberweisungListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);

		Table table = new Table(list,this);
		table.addColumn(I18N.tr("Konto"),"konto_id");
		table.addColumn(I18N.tr("Empfänger"),"empfaenger_id");
		table.addColumn(I18N.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		return table;
	}

	/**
	 * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public Input getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;

		Konto k = getUeberweisung().getKonto();
		if (k == null)
			k = (Konto) Settings.getDatabase().createObject(Konto.class,null);
		konto = new SelectInput(k);
		return konto;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerKonto() throws RemoteException
	{
		if (empfkto != null)
			return empfkto;
		empfkto = new SearchInput(getEmpfaenger().getKontonummer(), new EmpfaengerSearchDialog());
		empfkto.addComment("",new EmpfaengerListener());
		// TODO: Listener wird nicht rechtzeitig ausgeloest.
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
		empfblz = new TextInput(getEmpfaenger().getBLZ());
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
		empfName = new TextInput(getEmpfaenger().getName());
		return empfName;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
    // TODO Auto-generated method stub
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		GUI.startView(UeberweisungListe.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(UeberweisungNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleLoad(java.lang.String)
   */
  public void handleLoad(String id) {
		try {
			Ueberweisung u = (Ueberweisung) Settings.getDatabase().createObject(Ueberweisung.class,id);
			GUI.startView(UeberweisungNeu.class.getName(),u);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to load ueberweisung with id " + id);
			GUI.setActionText(I18N.tr("Überweisung wurde nicht gefunden."));
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
    	String kto = empfkto.getValue();
    	if (kto != null && kto.length() > 0)
    	{
				try {
					DBIterator list = Settings.getDatabase().createList(Empfaenger.class);
					list.addFilter("kontonummer = '" + kto + "'");
					if (!list.hasNext())
						return;
					Empfaenger e = (Empfaenger) list.next();
					empfblz.setValue(e.getBLZ());
					empfName.setValue(e.getName());
				}
				catch (RemoteException er)
				{
					Application.getLog().error("error while choosing empfaenger",er);
					GUI.setActionText(I18N.tr("Fehler bei der Auswahl des Empfängers"));
				}
    		
    	}
    }
	}
}


/**********************************************************************
 * $Log: UeberweisungControl.java,v $
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/