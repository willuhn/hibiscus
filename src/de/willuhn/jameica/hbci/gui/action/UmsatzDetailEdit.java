/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzDetailEdit.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/04/22 16:40:57 $
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
        if ((k.getFlags() & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE)
          return; // ist kein Offline-Konto
        
        Umsatz u = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);
        u.setKonto(k);
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


/**********************************************************************
 * $Log: UmsatzDetailEdit.java,v $
 * Revision 1.2  2010/04/22 16:40:57  willuhn
 * @N Manuelles Anlegen neuer Umsaetze fuer Offline-Konten moeglich
 *
 * Revision 1.1  2009/01/04 14:47:53  willuhn
 * @N Bearbeiten der Umsaetze nochmal ueberarbeitet - Codecleanup
 *
 * Revision 1.1  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 **********************************************************************/