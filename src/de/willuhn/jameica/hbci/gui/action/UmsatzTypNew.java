/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzTypNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/11/23 17:25:37 $
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
    GUI.startView(UmsatzTypDetail.class,context);
  }

}


/*********************************************************************
 * $Log: UmsatzTypNew.java,v $
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 *********************************************************************/