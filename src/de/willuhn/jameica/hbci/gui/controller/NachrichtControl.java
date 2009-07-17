/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/NachrichtControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/07/17 08:42:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.gui.action.NachrichtOpen;
import de.willuhn.jameica.hbci.gui.parts.NachrichtList;
import de.willuhn.jameica.hbci.rmi.Nachricht;

/**
 * Controller fuer die System-Nachrichten.
 */
public class NachrichtControl extends AbstractControl {

  private Part list = null;

  /**
   * @param view
   */
  public NachrichtControl(AbstractView view) {
    super(view);
  }
  
  /**
   * Liefert die aktuelle Nachricht.
   * @return die aktuelle Nachricht.
   * @throws RemoteException
   */
  public Nachricht getNachricht() throws RemoteException
  {
    return (Nachricht) this.getCurrentObject();
  }

  /**
   * Liefert eine Liste aller vorhandenen Nachrichten
   * @return liefert eine Liste der vorhandenen Nachrichten.
   * @throws RemoteException
   */
  public Part getListe() throws RemoteException
	{
    if (list != null)
      return list;
    list = new NachrichtList(new NachrichtOpen());
    return list;
	}
}


/**********************************************************************
 * $Log: NachrichtControl.java,v $
 * Revision 1.3  2009/07/17 08:42:57  willuhn
 * @N Detail-Ansicht fuer Systemnachrichten der Bank
 * @N Systemnachrichten in Zwischenablage kopieren
 *
 * Revision 1.2  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/