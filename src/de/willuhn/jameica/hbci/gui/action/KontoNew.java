/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoNew.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/03/31 23:05:46 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import org.eclipse.ui.forms.events.HyperlinkEvent;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer die Detail-Ansicht eines Kontos.
 */
public class KontoNew implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Konto k = null;
    if (context instanceof Konto)
    {
      k = (Konto) context;
    }
    
    else if (context instanceof HyperlinkEvent && context != null)
    {
      try
      {
        String s = ((HyperlinkEvent)context).getLabel();
        if (s != null && s.length() > 0)
        {
          Logger.info("trying to load account by number");
          DBIterator i = Settings.getDBService().createList(Konto.class);
          i.addFilter("kontonummer = '" + s + "'");
          if (i.hasNext())
            k = (Konto) i.next();
        }
      }
      catch (Throwable t)
      {
        Logger.error("error while loading account by number, creating a new one",t);
      }
      
    }
		GUI.startView(de.willuhn.jameica.hbci.gui.views.KontoNew.class,k);
  }

}


/**********************************************************************
 * $Log: KontoNew.java,v $
 * Revision 1.4  2005/03/31 23:05:46  web0
 * @N geaenderte Startseite
 * @N klickbare Links
 *
 * Revision 1.3  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.2  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 **********************************************************************/