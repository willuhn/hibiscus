/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/UmsatzVerteilung.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/05 22:00:51 $
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
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen der Umsatzanalyse.
 */
public class UmsatzVerteilung implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzVerteilung.class,context);
  }

}


/*********************************************************************
 * $Log: UmsatzVerteilung.java,v $
 * Revision 1.1  2006/08/05 22:00:51  willuhn
 * *** empty log message ***
 *
 **********************************************************************/