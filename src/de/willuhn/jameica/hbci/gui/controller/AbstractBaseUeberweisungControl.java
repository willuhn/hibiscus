/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractBaseUeberweisungControl.java,v $
 * $Revision: 1.19 $
 * $Date: 2011/05/20 16:22:31 $
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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.TerminInput;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Basis-Controller fuer die Ueberweisungen und Lastschriften.
 */
public abstract class AbstractBaseUeberweisungControl extends AbstractTransferControl
{

	// Eingabe-Felder
	private TerminInput termin = null;
	
  /**
   * ct.
   * @param view
   */
  public AbstractBaseUeberweisungControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TerminInput getTermin() throws RemoteException
	{
		if (this.termin == null)
		  this.termin = new TerminInput((Terminable) getTransfer());
    return this.termin;
	}
  
  /**
   * Liefert ein Auswahlfeld fuer den Textschluessel.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public abstract Input getTextSchluessel() throws RemoteException;
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
		try
		{
      BaseUeberweisung bu = (BaseUeberweisung) getTransfer();
			
			if (bu.ausgefuehrt())
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Der Auftrag wurde bereits ausgeführt und kann daher nicht geändert werden"));
				return false;
			}

			Date termin = (Date) getTermin().getValue();
			if (termin == null)
			{
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte geben Sie einen Termin ein."),StatusBarMessage.TYPE_ERROR));
			  return false;
			}
			bu.setTermin(termin);
      
      TextSchluessel s = (TextSchluessel) getTextSchluessel().getValue();
      bu.setTextSchluessel(s == null ? null : s.getCode());

			return super.handleStore();
		}
		catch (RemoteException re)
		{
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
  public TextInput getEmpfaengerBlz() throws RemoteException
  {
    TextInput i = super.getEmpfaengerBlz();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerKonto()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public TextInput getEmpfaengerKonto() throws RemoteException
  {
    TextInput i = super.getEmpfaengerKonto();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getEmpfaengerName()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public AddressInput getEmpfaengerName() throws RemoteException
  {
    AddressInput i = super.getEmpfaengerName();
		if (((Terminable)getTransfer()).ausgefuehrt())
			i.disable();
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getKontoAuswahl()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public Input getKontoAuswahl() throws RemoteException
  {
		Input i = super.getKontoAuswahl();
		i.setEnabled(!((Terminable)getTransfer()).ausgefuehrt());
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
  public TextInput getZweck() throws RemoteException
  {
    TextInput i = super.getZweck();
    i.setEnabled(!((Terminable)getTransfer()).ausgefuehrt());
		return i;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getZweck2()
   * Ueberschrieben, um das Control zu deaktivieren, wenn die Ueberweisung bereits ausgefuehrt wurde.
   */
  public DialogInput getZweck2() throws RemoteException
  {
		DialogInput i = super.getZweck2();
    if (((Terminable)getTransfer()).ausgefuehrt())
      i.disableClientControl();
		return i;
  }
}


/**********************************************************************
 * $Log: AbstractBaseUeberweisungControl.java,v $
 * Revision 1.19  2011/05/20 16:22:31  willuhn
 * @N Termin-Eingabefeld in eigene Klasse ausgelagert (verhindert duplizierten Code) - bessere Kommentare
 *
 * Revision 1.18  2011-05-11 16:23:57  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.17  2010-08-17 11:32:11  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.16  2009-03-13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.15  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.14  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 **********************************************************************/