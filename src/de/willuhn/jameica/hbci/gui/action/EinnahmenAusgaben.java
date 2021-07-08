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
 * Action zum Oeffnen der Einnahmen/Ausgaben-Übersicht.
 */
public class EinnahmenAusgaben implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.hbci.gui.views.EinnahmenAusgaben.class,context);
  }

}

/*********************************************************************
 * $Log: EinnahmenAusgaben.java,v $
 * Revision 1.1  2007/06/04 15:57:39  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 **********************************************************************/