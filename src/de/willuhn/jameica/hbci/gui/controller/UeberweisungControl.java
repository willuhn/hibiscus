/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UeberweisungControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/03/03 22:26:40 $
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
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.ListDialog;
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

	private I18N i18n;

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
		table.addColumn(i18n.tr("Konto"),"konto_id");
		table.addColumn(i18n.tr("Empfänger"),"empfaenger_id");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
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

		ListDialog d = new ListDialog(Settings.getDatabase().createList(Empfaenger.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Empfängers"));
		d.addListener(new EmpfaengerListener());

		empfkto = new SearchInput(getEmpfaenger().getKontonummer(),d);
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
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(UeberweisungNeu.class.getName(),o);
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
			empfaenger = (Empfaenger) event.data;
			try {
				empfkto.setValue(empfaenger.getKontonummer());
				empfblz.setValue(empfaenger.getBLZ());
				empfName.setValue(empfaenger.getName());
			}
			catch (RemoteException er)
			{
				Application.getLog().error("error while choosing empfaenger",er);
				GUI.setActionText(i18n.tr("Fehler bei der Auswahl des Empfängers"));
    	}
    }
	}
}


/**********************************************************************
 * $Log: UeberweisungControl.java,v $
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