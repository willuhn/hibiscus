/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/EmpfaengerControl.java,v $
 * $Revision: 1.2 $
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.parts.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.EmpfaengerListe;
import de.willuhn.jameica.hbci.gui.views.EmpfaengerNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Empfaenger-Adressen.
 */
public class EmpfaengerControl extends AbstractControl {

	// Fach-Objekte
	private Empfaenger empfaenger = null;
	// Eingabe-Felder
	private Input kontonummer = null;
	private Input blz					= null;
	private Input name				= null;

  /**
   * @param view
   */
  public EmpfaengerControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert den Empfaenger.
	 * Existiert er nicht, wird ein neuer erzeugt.
   * @return der Empfaenger.
   * @throws RemoteException
   */
  public Empfaenger getEmpfaenger() throws RemoteException
	{
		if (empfaenger != null)
			return empfaenger;
		
		empfaenger = (Empfaenger) getCurrentObject();
		if (empfaenger != null)
			return empfaenger;

		empfaenger = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
		return empfaenger;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Empfaengern.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Table getEmpfaengerListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Empfaenger.class);

		Table table = new Table(list,this);
		table.addColumn(I18N.tr("Kontonummer"),"kontonummer");
		table.addColumn(I18N.tr("Bankleitzahl"),"blz");
		table.addColumn(I18N.tr("Name"),"name");
		return table;
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
		kontonummer = new TextInput(getEmpfaenger().getKontonummer());
		return kontonummer;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new TextInput(getEmpfaenger().getBLZ());
		blz.addComment("", new BLZListener());
		return blz;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Namen.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getEmpfaenger().getName());
		return name;
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

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(I18N.tr("Empfängeradresse löschen"));
			d.setText(I18N.tr("Wollen Sie diese Empfängeradresse wirklich löschen?"));

			try {
				if (!d.getChoice())
					return;
			}
			catch (Exception e)
			{
				Application.getLog().error(e.getLocalizedMessage(),e);
				return;
			}

			// ok, wir loeschen das Objekt
			getEmpfaenger().delete();
			GUI.setActionText(I18N.tr("Empfängeradresse gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.setActionText(I18N.tr("Fehler beim Löschen der Empfängeradresse."));
			Application.getLog().error("unable to delete empfaenger");
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
		GUI.startView(EmpfaengerListe.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  	try {
  		getEmpfaenger().setKontonummer(getKontonummer().getValue());
  		getEmpfaenger().setBLZ(getBlz().getValue());
  		getEmpfaenger().setName(getName().getValue());
  		getEmpfaenger().store();
  		GUI.setActionText(I18N.tr("Empfängeradresse gespeichert"));
  	}
  	catch (RemoteException e)
  	{
  		Application.getLog().error("error while storing empfaenger",e);
  		GUI.setActionText(I18N.tr("Fehler beim Speichern der Adresse"));
  	}
  	catch (ApplicationException e2)
  	{
  		GUI.setActionText(e2.getLocalizedMessage());
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(EmpfaengerNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleLoad(java.lang.String)
   */
  public void handleLoad(String id) {
		try {
			Empfaenger e = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,id);
			GUI.startView(EmpfaengerNeu.class.getName(),e);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to load empfaenger with id " + id);
			GUI.setActionText(I18N.tr("Empfängeradresse wurde nicht gefunden."));
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
 * $Log: EmpfaengerControl.java,v $
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/17 01:09:45  willuhn
 * *** empty log message ***
 *
 **********************************************************************/