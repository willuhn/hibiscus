/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/NachrichtControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/09 17:26:56 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.gui.parts.NachrichtList;

/**
 * Controller fuer die System-Nachrichten.
 */
public class NachrichtControl extends AbstractControl {

  private Part list         = null;

  /**
   * @param view
   */
  public NachrichtControl(AbstractView view) {
    super(view);
  }

  /**
   * Liefert eine Liste aller vorhandenen Nachrichten
   * @return
   * @throws RemoteException
   */
  public Part getListe() throws RemoteException
	{
    if (list != null)
      return list;
    list = new NachrichtList(null);
    return list;
	}
}


/**********************************************************************
 * $Log: NachrichtControl.java,v $
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/