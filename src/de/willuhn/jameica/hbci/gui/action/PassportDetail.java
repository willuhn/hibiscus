/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/PassportDetail.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/04/29 11:38:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die ein Passport konfiguriert werden kann.
 */
public class PassportDetail implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ {@link Passport} oder {@link Configuration}.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (context instanceof Configuration)
      {
        GUI.startView(((Configuration) context).getConfigDialog(),context);
        return;
      }
      
      Passport p = null;
      if (context instanceof Passport)
      {
        p = (Passport) context;
      }
      else
      {
        try
        {
          p = (Passport) new PassportAuswahlDialog(PassportAuswahlDialog.POSITION_CENTER).open();
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (OperationCanceledException oce)
        {
          Logger.info("operation cancelled");
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while opening passport",e);
          throw new ApplicationException(i18n.tr("Fehler beim Laden: {0}",e.getMessage()));
        }
      }
      
      if (p == null)
        return;

      GUI.startView(p.getConfigDialog(),p);
    }
    catch (RemoteException e)
    {
      Logger.error("error while opening passport",e);
      throw new ApplicationException(i18n.tr("Fehler beim Laden: {0}",e.getMessage()));
    }
  }
}


/**********************************************************************
 * $Log: PassportDetail.java,v $
 * Revision 1.6  2011/04/29 11:38:57  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.5  2011-04-28 07:32:58  willuhn
 * @C Code-Cleanup
 *
 **********************************************************************/