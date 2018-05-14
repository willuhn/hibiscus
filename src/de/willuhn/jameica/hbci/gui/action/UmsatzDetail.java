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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer die Detail-Ansicht eines Umsatzes.
 */
public class UmsatzDetail implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    // Falls die Aktion aus einem Tree heraus aufgerufen wurde
    // koennte es sich um einen "UmsatzTyp" handeln. Den ignorieren
    // wir.
    if (!(context instanceof Umsatz))
      return;
    
    // Automatisch in die Edit-View wechseln, falls es ein Offline-Konto ist
    // Siehe BUGZILLA 989
    try
    {
      Umsatz u = (Umsatz) context;
      Konto k = u.getKonto();
      if (k != null && (k.getFlags() & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
      {
        new UmsatzDetailEdit().handleAction(context);
        return;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to switch to edit view, opening read only view",e);
    }
    
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzDetail.class,context);
  }

}


/**********************************************************************
 * $Log: UmsatzDetail.java,v $
 * Revision 1.4  2011/02/10 16:43:56  willuhn
 * @N automatisch in die Edit-View wechseln, wenn der Umsatz in einem Offline-Konto geoeffnet wird - siehe BUGZILLA 989
 *
 * Revision 1.3  2007/03/22 22:36:42  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.2  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 **********************************************************************/