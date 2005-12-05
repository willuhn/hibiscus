/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/UmsatzTypEdit.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/05 20:16:15 $
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
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Bearbeiten existierender Umsatz-Typen.
 */
public class UmsatzTypEdit implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    UmsatzTypDialog d = new UmsatzTypDialog(UmsatzTypDialog.POSITION_CENTER);
    try
    {
      d.open();
    }
    catch (Exception e)
    {
      Logger.error("unable to open umsatz type dialog",e);
    }
  }

}


/*********************************************************************
 * $Log: UmsatzTypEdit.java,v $
 * Revision 1.1  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 **********************************************************************/