/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/listener/Attic/UeberweisungCreate.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/09 00:04:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.listener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;

/**
 * Listener, der eine neue Ueberweisung erzeugt und im Detail-Dialog anzeigt.
 */
public class UeberweisungCreate implements Listener
{

  /**
   * ct.
   */
  public UeberweisungCreate()
  {
    super();
  }

  /**
   * Das uebergebene Event kann <code>null</code> sein.
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(Event event)
  {
		GUI.startView(UeberweisungNeu.class.getName(),null);
  }

}


/**********************************************************************
 * $Log: UeberweisungCreate.java,v $
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/