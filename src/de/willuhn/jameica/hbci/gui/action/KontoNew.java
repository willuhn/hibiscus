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
import de.willuhn.util.ApplicationException;

/**
 * Action fuer die Detail-Ansicht eines Kontos.
 */
public class KontoNew implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Konto k = null;
    if (context instanceof Konto)
    {
      k = (Konto) context;
    }
    
		GUI.startView(de.willuhn.jameica.hbci.gui.views.KontoNew.class,k);
  }

}


/**********************************************************************
 * $Log: KontoNew.java,v $
 * Revision 1.5  2005/11/07 18:51:28  willuhn
 * *** empty log message ***
 *
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