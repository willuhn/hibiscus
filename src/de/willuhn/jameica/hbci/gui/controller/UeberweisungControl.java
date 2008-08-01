/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UeberweisungControl.java,v $
 * $Revision: 1.44 $
 * $Date: 2008/08/01 11:05:14 $
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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.logging.Logger;

/**
 * Controller fuer die Ueberweisungen.
 */
public class UeberweisungControl extends AbstractBaseUeberweisungControl
{

	private TablePart table		         = null;
  private Ueberweisung transfer      = null;
  private CheckboxInput bankTermin   = null;
  private SelectInput textschluessel = null;

  /**
   * ct.
   * @param view
   */
  public UeberweisungControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Ueberschrieben, damit wir bei Bedarf eine neue Ueberweisung erzeugen koennen.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
   */
  public HibiscusTransfer getTransfer() throws RemoteException
	{
    if (transfer != null)
      return transfer;

    transfer = (Ueberweisung) getCurrentObject();
    if (transfer != null)
      return transfer;
      
    transfer = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
		return transfer;
	}

  /**
   * Liefert eine Checkbox, mit der der Benutzer auswaehlen kann, ob es sich
   * um eine bankseitige Termin-Ueberweisung handelt.
   * @return die Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getBankTermin() throws RemoteException
  {
    if (this.bankTermin != null)
      return this.bankTermin;
    Ueberweisung u = (Ueberweisung) getTransfer();
    this.bankTermin = new CheckboxInput(u.isTerminUeberweisung());
    if (u.ausgefuehrt())
      this.bankTermin.disable();
    return this.bankTermin;
  }
  
  /**
	 * Liefert eine Tabelle mit allen vorhandenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public TablePart getUeberweisungListe() throws RemoteException
	{
		if (table != null)
			return table;

    table = new de.willuhn.jameica.hbci.gui.parts.UeberweisungList(new UeberweisungNew());
		return table;
	}
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractBaseUeberweisungControl#getTextSchluessel()
   */
  public Input getTextSchluessel() throws RemoteException
  {
    if (textschluessel != null)
      return textschluessel;

    textschluessel = new SelectInput(TextSchluessel.get(new String[]{"51","53","54"}),TextSchluessel.get(((BaseUeberweisung)getTransfer()).getTextSchluessel()));
    textschluessel.setEnabled(!((Terminable)getTransfer()).ausgefuehrt());
    return textschluessel;
  }
  
  /**
   * Ueberschrieben, um das Flag fuer die Termin-Ueberweisung zu speichern.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
    try
    {
      Ueberweisung u = (Ueberweisung) getTransfer();
      u.setTerminUeberweisung(((Boolean)getBankTermin().getValue()).booleanValue());
      return super.handleStore();
    }
    catch (RemoteException re)
    {
      Logger.error("error while storing ueberweisung",re);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Überweisung"));
      return false;
    }
  }
}


/**********************************************************************
 * $Log: UeberweisungControl.java,v $
 * Revision 1.44  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.43  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.42  2006/06/06 22:41:25  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.41  2005/11/14 13:08:11  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.40  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.39  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.38  2005/02/04 18:27:54  willuhn
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