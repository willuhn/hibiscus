/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/13 00:25:12 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Erstellen einer neuen Auslandsueberweisung.
 */
public class AuslandsUeberweisungNew implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof AuslandsUeberweisung))
      context = null;

    GUI.startView(de.willuhn.jameica.hbci.gui.views.AuslandsUeberweisungNew.class,context);
  }

}


/**********************************************************************
 * $Log: AuslandsUeberweisungNew.java,v $
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/
