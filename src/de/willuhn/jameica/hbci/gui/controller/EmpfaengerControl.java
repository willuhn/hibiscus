/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/EmpfaengerControl.java,v $
 * $Revision: 1.16 $
 * $Date: 2004/07/21 23:54:30 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.menus.EmpfaengerList;
import de.willuhn.jameica.hbci.gui.views.EmpfaengerNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

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

	private I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
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
  public Part getEmpfaengerListe() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Empfaenger.class);

		TablePart table = new TablePart(list,this);
		table.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		table.addColumn(i18n.tr("Bankleitzahl"),"blz");
		table.addColumn(i18n.tr("Name"),"name");
		table.setContextMenu(new EmpfaengerList());
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
		blz.setComment("");
		blz.addListener(new BLZListener());
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
  public synchronized void handleDelete() {
		try {

			if (getEmpfaenger() == null || getEmpfaenger().isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Empfängeradresse löschen"));
			d.setText(i18n.tr("Wollen Sie diese Empfängeradresse wirklich löschen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error(e.getLocalizedMessage(),e);
				return;
			}

			// ok, wir loeschen das Objekt
			getEmpfaenger().delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Empfängeradresse gelöscht."));
			GUI.startPreviousView();
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Empfängeradresse."));
			Logger.error("unable to delete empfaenger");
		}
		catch (ApplicationException ae)
		{
			GUI.getView().setErrorText(ae.getLocalizedMessage());
		}

  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
		// GUI.startView(EmpfaengerListe.class.getName(),null);
		GUI.startPreviousView();
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public synchronized void handleStore() {
  	try {
  		getEmpfaenger().setKontonummer((String)getKontonummer().getValue());
  		getEmpfaenger().setBLZ((String)getBlz().getValue());
  		getEmpfaenger().setName((String)getName().getValue());
  		getEmpfaenger().store();
  		GUI.getStatusBar().setSuccessText(i18n.tr("Empfängeradresse gespeichert"));
  	}
  	catch (RemoteException e)
  	{
  		Logger.error("error while storing empfaenger",e);
  		GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Adresse"));
  	}
  	catch (ApplicationException e2)
  	{
  		GUI.getView().setErrorText(e2.getLocalizedMessage());
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(EmpfaengerNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(EmpfaengerNeu.class.getName(),o);
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
 * $Log: EmpfaengerControl.java,v $
 * Revision 1.16  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.9  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.5  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.4  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/17 01:09:45  willuhn
 * *** empty log message ***
 *
 **********************************************************************/