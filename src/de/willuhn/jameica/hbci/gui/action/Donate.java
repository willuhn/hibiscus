/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/Donate.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/27 13:36:53 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.DonateDialog;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Aktion, die fuer Spenden zustaendig ist.
 */
public class Donate implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    Ueberweisung u = null;
    if (context != null && (context instanceof Ueberweisung))
    {
      u = (Ueberweisung) context;
    }
    else
    {
      DonateDialog d = new DonateDialog(DonateDialog.POSITION_CENTER);
      try
      {
        u = (Ueberweisung) d.open();
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (OperationCanceledException oce)
      {
        return;
      }
      catch (Exception e)
      {
        Logger.error("error while opening donate dialog",e);
        throw new ApplicationException(i18n.tr("Überweisung konnte nicht erzeugt werden. Bitte legen Sie sie manuell an."));
      }
    }
    new UeberweisungNew().handleAction(u);
  }

}


/*********************************************************************
 * $Log: Donate.java,v $
 * Revision 1.1  2005/06/27 13:36:53  web0
 * @N added donate button
 *
 **********************************************************************/