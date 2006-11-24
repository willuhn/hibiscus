/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/PassportDetail.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/11/24 00:07:08 $
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

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    Passport p = null;
  	if (context != null && (context instanceof Passport))
      p = (Passport) context;
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
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden des Sicherheitsmediums"));
        return;
      }
    }
    
    if (p == null)
      return;

		try {
			GUI.startView(p.getConfigDialog(),p);
		}
		catch (RemoteException e)
		{
			Logger.error("error while opening passport",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden des Sicherheitsmediums"));
		}

  }

}


/**********************************************************************
 * $Log: PassportDetail.java,v $
 * Revision 1.4  2006/11/24 00:07:08  willuhn
 * @C Konfiguration der Umsatz-Kategorien in View Einstellungen verschoben
 * @N Redesign View Einstellungen
 *
 * Revision 1.3  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/