/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractBaseUeberweisungControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/03/02 17:59:31 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.logging.Logger;

/**
 * Basis-Controller fuer die Ueberweisungen und Lastschriften.
 */
public abstract class AbstractBaseUeberweisungControl extends AbstractTransferControl
{

	// Eingabe-Felder
	private DialogInput termin = null;
	private Input comment			 = null;
	
	private TablePart table		 = null;

  /**
   * ct.
   * @param view
   */
  public AbstractBaseUeberweisungControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert ein Kommentar-Feld zu dieser Ueberweisung.
   * @return Kommentarfeld.
   * @throws RemoteException
   */
  public Input getComment() throws RemoteException
	{
		if (comment != null)
			return comment;
		comment = new LabelInput("");
    Terminable t = (Terminable) getTransfer();
		if (t.ausgefuehrt())
		{
			comment.setValue(i18n.tr("Der Auftrag wurde bereits ausgeführt"));
		}
		else if (t.ueberfaellig())
		{
			comment.setValue(i18n.tr("Der Auftrag ist überfällig"));
		}
		return comment;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DialogInput getTermin() throws RemoteException
	{
		final Terminable bu = (Terminable) getTransfer();

		if (termin != null)
			return termin;
		CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		cd.setTitle(i18n.tr("Termin"));
		cd.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;
				Date choosen = (Date) event.data;
				termin.setText(HBCI.DATEFORMAT.format(choosen));

				try {
					// Wenn das neue Datum spaeter als das aktuelle ist,
					// nehmen wir den Kommentar weg
					if (bu.ueberfaellig() && choosen.after(new Date()));
						getComment().setValue("");
					if (choosen.before(new Date()))
						getComment().setValue(i18n.tr("Der Auftrag ist überfällig."));
				}
				catch (RemoteException e) {/*ignore*/}
			}
		});

		Date d = bu.getTermin();
		if (d == null)
			d = new Date();
		cd.setDate(d);
		termin = new DialogInput(HBCI.DATEFORMAT.format(d),cd);
		termin.disableClientControl();
		termin.setValue(d);

		if (bu.ausgefuehrt())
			termin.disable();

		return termin;
	}

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
		try
		{
			Terminable bu = (Terminable) getTransfer();
			
			if (bu.ausgefuehrt())
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Der Auftrag wurde bereits ausgeführt und kann daher nicht geändert werden"));
				return false;
			}

			Date termin = (Date) getTermin().getValue();
			if (termin == null)
			{
				try
				{
					termin = HBCI.DATEFORMAT.parse(getTermin().getText());
				}
				catch (Exception e)
				{
					GUI.getView().setErrorText("Bitte geben Sie einen Termin ein.");
					return false;
				}
			}
			bu.setTermin(termin);
			return super.handleStore();
		}
		catch (RemoteException re)
		{
			Logger.error("rollback failed",re);
			Logger.error("error while storing ueberweisung",re);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Auftrags"));
  	}
		return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getBetrag()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getBetrag() throws RemoteException
  {
    Input i = super.getBetrag();
    if (((Terminable)getTransfer()).ausgefuehrt())
    	i.disable();
    return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerBlz()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getEmpfaengerBlz() throws RemoteException
  {
		Input i = super.getEmpfaengerBlz();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerKonto()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public DialogInput getEmpfaengerKonto() throws RemoteException
  {
		DialogInput i = super.getEmpfaengerKonto();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerName()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getEmpfaengerName() throws RemoteException
  {
		Input i = super.getEmpfaengerName();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getKontoAuswahl()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public DialogInput getKontoAuswahl() throws RemoteException
  {
		DialogInput i = super.getKontoAuswahl();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getStoreEmpfaenger()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public CheckboxInput getStoreEmpfaenger() throws RemoteException
  {
		CheckboxInput i = super.getStoreEmpfaenger();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getZweck() throws RemoteException
  {
		Input i = super.getZweck();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck2()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getZweck2() throws RemoteException
  {
		Input i = super.getZweck2();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

}


/**********************************************************************
 * $Log: AbstractBaseUeberweisungControl.java,v $
 * Revision 1.4  2005/03/02 17:59:31  web0
 * @N some refactoring
 *
 * Revision 1.3  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.2  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.1  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.37  2005/02/04 00:57:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 * Revision 1.34  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.33  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.31  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.30  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2004/10/08 00:19:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/08/01 13:08:42  willuhn
 * @B Handling von Ueberweisungsterminen
 *
 * Revision 1.26  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.25  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.24  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.23  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.22  2004/07/09 00:12:29  willuhn
 * @B minor bugs
 *
 * Revision 1.21  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.20  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.16  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.14  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.13  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/04/19 22:53:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.10  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.6  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.5  2004/03/04 00:35:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
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