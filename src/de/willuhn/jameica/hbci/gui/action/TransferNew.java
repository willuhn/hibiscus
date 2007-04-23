/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/TransferNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/23 18:07:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Generische Action zum Oeffnen eines Transfers.
 */
public class TransferNew implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    if (context == null || !(context instanceof Transfer))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Auftrag aus"));
    
    if (context instanceof Umsatz)
      new UmsatzDetail().handleAction(context);
    else if (context instanceof Ueberweisung)
      new UeberweisungNew().handleAction(context);
    else if (context instanceof Lastschrift)
      new LastschriftNew().handleAction(context);
    else if (context instanceof SammelLastBuchung)
      new SammelLastBuchungNew().handleAction(context);
    else if (context instanceof SammelUeberweisungBuchung)
      new SammelUeberweisungBuchungNew().handleAction(context);
  }

}


/*********************************************************************
 * $Log: TransferNew.java,v $
 * Revision 1.1  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 **********************************************************************/