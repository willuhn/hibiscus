/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzTypNew.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/03/05 15:24:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.views.UmsatzTypDetail;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Erstellen einer neuen Umsatz-Kategorie.
 * @author willuhn
 */
public class UmsatzTypNew implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Object o = context;
    if (context != null)
    {
      try
      {
        if (context instanceof UmsatzTreeNode)
          o = ((UmsatzTreeNode)context).getUmsatzTyp();
        else if (context instanceof Umsatz)
          o = ((Umsatz)context).getUmsatzTyp();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine umsatztyp",re);
      }
    }
    GUI.startView(UmsatzTypDetail.class,o);
  }

}


/*********************************************************************
 * $Log: UmsatzTypNew.java,v $
 * Revision 1.3  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 * Revision 1.2  2007/12/04 23:59:00  willuhn
 * @N Bug 512
 *
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 *********************************************************************/