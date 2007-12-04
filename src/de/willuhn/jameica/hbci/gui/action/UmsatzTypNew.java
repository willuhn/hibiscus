/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzTypNew.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/12/04 23:59:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.views.UmsatzTypDetail;
import de.willuhn.jameica.hbci.server.UmsatzGroup;
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
    if (context != null && (context instanceof UmsatzGroup))
      o = ((UmsatzGroup)context).getUmsatzTyp();
    GUI.startView(UmsatzTypDetail.class,o);
  }

}


/*********************************************************************
 * $Log: UmsatzTypNew.java,v $
 * Revision 1.2  2007/12/04 23:59:00  willuhn
 * @N Bug 512
 *
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 *********************************************************************/