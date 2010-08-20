/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DonateView.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/08/20 12:42:02 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Starten der Donate-View.
 */
public class DonateView implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.DonateView.class,null);
  }

}



/**********************************************************************
 * $Log: DonateView.java,v $
 * Revision 1.1  2010/08/20 12:42:02  willuhn
 * @N Neuer Spenden-Aufruf. Ich bin gespannt, ob das klappt ;)
 *
 **********************************************************************/