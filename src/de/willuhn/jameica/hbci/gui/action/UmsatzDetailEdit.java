/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzDetailEdit.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/02 09:44:32 $
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
import java.util.Date;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Bearbeiten eines Umsatzes.
 */
public class UmsatzDetailEdit implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    // Wenn der Context ein Konto ist und dieses ein Offline-Konto ist,
    // legen wir automatisch einen neuen Umsatz fuer dieses Konto an.
    if (context instanceof Konto)
    {
      try
      {
        Konto k = (Konto) context;
        if (!k.hasFlag(Konto.FLAG_OFFLINE))
          return; // ist kein Offline-Konto
        
        Umsatz u = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);
        u.setKonto(k);
        Date d = new Date();
        u.setValuta(d);
        u.setDatum(d);
        context = u;
      }
      catch (RemoteException re)
      {
        Logger.error("unable to create umsatz",re);
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        throw new ApplicationException(i18n.tr("Fehler beim Anlegen des Umsatzes: {0}",re.getMessage()));
      }
    }
    else if (!(context instanceof Umsatz))
      return;
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzDetailEdit.class,context);
  }
}
