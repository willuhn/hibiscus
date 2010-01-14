/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungMerge.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/01/14 23:09:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Erzeugt eine Sammel-Ueberweisung aus einem Buendel Einzelueberweisungen.
 */
public class UeberweisungMerge extends AbstractBaseUeberweisungMerge
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractBaseUeberweisungMerge#getBuchungClass()
   */
  Class getBuchungClass() throws RemoteException
  {
    return SammelUeberweisungBuchung.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractBaseUeberweisungMerge#getTransferClass()
   */
  Class getTransferClass() throws RemoteException
  {
    return SammelUeberweisung.class;
  }

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      new SammelUeberweisungNew().handleAction(create(context));
    }
    catch (OperationCanceledException e)
    {
       Logger.info("operation cancelled");
    }
  }

}


/*********************************************************************
 * $Log: UeberweisungMerge.java,v $
 * Revision 1.2  2010/01/14 23:09:14  willuhn
 * @B Beim Mergen einer Einzel-Lastschrift in eine Sammel-Lastschrift wurde der Textschluessel nicht mitkopiert (siehe Mail von Ralf vom 14.01.2010)
 *
 * Revision 1.1  2007/10/25 15:47:21  willuhn
 * @N Einzelauftraege zu Sammel-Auftraegen zusammenfassen (BUGZILLA 402)
 *
 **********************************************************************/