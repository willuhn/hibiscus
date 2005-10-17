/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/Welcome.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/10/17 15:11:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.views.Start;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.FirstStart;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer Welcome-Dialog.
 */
public class Welcome implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Start start = null;
    if (context != null && context instanceof Start)
      start = (Start) context;
    
    if (start == null)
    {
      GUI.startView(de.willuhn.jameica.hbci.gui.views.Welcome.class,null);
    }
    else
    {
      boolean firstStart = false;
      try
      {
        // Wenn noch keine Konten existieren, dann Anleitung zum Einrichten anzeigen
        DBIterator konten = Settings.getDBService().createList(Konto.class);
        firstStart = konten.size() == 0;
      }
      catch (Exception e)
      {
        Logger.error("unable to read konto list",e);
      }

      AbstractView view = null;
      if (firstStart)
        view = new FirstStart();
      else
        view = new de.willuhn.jameica.hbci.gui.views.Welcome();
      view.setParent(start.getParent());
      view.setCurrentObject(start.getCurrentObject());
      try
      {
        view.bind();
      }
      catch (Exception e)
      {
        Logger.error("unable to open welcome view",e);
        GUI.startView(de.willuhn.jameica.hbci.gui.views.Welcome.class,null);
      }
    }
  }

}


/**********************************************************************
 * $Log: Welcome.java,v $
 * Revision 1.3  2005/10/17 15:11:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/