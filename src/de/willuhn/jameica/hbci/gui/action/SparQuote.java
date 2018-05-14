/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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