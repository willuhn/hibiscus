/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoList.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/13 17:02:03 $
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
 * Action fuer die Liste der Konten.
 */
public class KontoList implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		GUI.startView(de.willuhn.jameica.hbci.gui.views.KontoListe.class.getName(),null);
  }

}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.1  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/