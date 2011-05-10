/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/LastschriftControl.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/05/10 11:41:30 $
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
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.parts.LastschriftList;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Terminable;

/**
 * Ueberschreiben wir von UeberweisungControl, weil es fast das
 * gleiche ist.
 */
public class LastschriftControl extends AbstractBaseUeberweisungControl
{

	private LastschriftList table      = null;
  private Lastschrift transfer       = null;
  private SelectInput textschluessel = null;
	
  /**
   * ct.
   * @param view
   */
  public LastschriftControl(AbstractView view)
  {
    super(view);
  }


  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
   */
  public HibiscusTransfer getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (Lastschrift) getCurrentObject();
    if (transfer != null)
      return transfer;
      
    transfer = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
    return transfer;
  }

  /**
   * Liefert eine Liste existierender Lastschriften.
   * @return Liste der Lastschriften.
   * @throws RemoteException
   */
  public LastschriftList getLastschriftListe() throws RemoteException
  {
		if (table != null)
			return table;

    table = new de.willuhn.jameica.hbci.gui.parts.LastschriftList(new LastschriftNew());
		return table;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractBaseUeberweisungControl#getTextSchluessel()
   */
  public Input getTextSchluessel() throws RemoteException
  {
    if (textschluessel != null)
      return textschluessel;

    textschluessel = new SelectInput(TextSchluessel.get(TextSchluessel.SET_LAST),TextSchluessel.get(((BaseUeberweisung)getTransfer()).getTextSchluessel()));
    textschluessel.setName(i18n.tr("Textschlüssel"));
    textschluessel.setEnabled(!((Terminable)getTransfer()).ausgefuehrt());
    return textschluessel;
  }
}


/**********************************************************************
 * $Log: LastschriftControl.java,v $
 * Revision 1.14  2011/05/10 11:41:30  willuhn
 * @N Text-Schluessel als Konstanten definiert - Teil aus dem Patch von Thomas vom 07.12.2010
 *
 * Revision 1.13  2011-04-11 14:36:38  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.12  2010-08-17 11:32:11  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.11  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.10  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.9  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.8  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.7  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.6  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.5  2005/02/19 17:22:05  willuhn
 * @B Bug 8
 *
 * Revision 1.4  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/19 00:33:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 **********************************************************************/