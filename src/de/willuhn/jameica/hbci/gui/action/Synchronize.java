/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/Synchronize.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/08/01 16:10:41 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn
 */
public class Synchronize implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.Synchronize.class,context);
  }
}


/*********************************************************************
 * $Log: Synchronize.java,v $
 * Revision 1.2  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.1  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 *********************************************************************/