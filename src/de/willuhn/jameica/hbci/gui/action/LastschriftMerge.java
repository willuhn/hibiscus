/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftMerge.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/10/25 15:47:21 $
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

import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Erzeugt eine Sammel-Lastschrift aus einem Buendel Einzel-Lastschriften.
 */
public class LastschriftMerge extends AbstractTransferMerge
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractTransferMerge#getBuchungClass()
   */
  Class getBuchungClass() throws RemoteException
  {
    return SammelLastBuchung.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractTransferMerge#getTransferClass()
   */
  Class getTransferClass() throws RemoteException
  {
    return SammelLastschrift.class;
  }

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      new SammelLastschriftNew().handleAction(create(context));
    }
    catch (OperationCanceledException e)
    {
       Logger.info("operation cancelled");
    }
  }

}


/*********************************************************************
 * $Log: LastschriftMerge.java,v $
 * Revision 1.1  2007/10/25 15:47:21  willuhn
 * @N Einzelauftraege zu Sammel-Auftraegen zusammenfassen (BUGZILLA 402)
 *
 **********************************************************************/