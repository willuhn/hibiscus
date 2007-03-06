/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/Kategorien.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/06 20:05:50 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen der Übersicht der Umsatzkategorien.
 */
public class Kategorien implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.Kategorien.class, context);
  }

}
/*******************************************************************************
 * $Log: Kategorien.java,v $
 * Revision 1.1  2007/03/06 20:05:50  jost
 * Neu: Umsatz-Kategorien-Ãœbersicht
 *
 ******************************************************************************/
