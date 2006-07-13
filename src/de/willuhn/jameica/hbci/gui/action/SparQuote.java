/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SparQuote.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/07/13 00:21:15 $
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
 * Action zum Oeffnen der Sparquote eines Kontos.
 */
public class SparQuote implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.SparQuote.class,context);
  }

}


/*********************************************************************
 * $Log: SparQuote.java,v $
 * Revision 1.1  2006/07/13 00:21:15  willuhn
 * @N Neue Auswertung "Sparquote"
 *
 **********************************************************************/