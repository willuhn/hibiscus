/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/SammelLastBuchungList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/03/01 18:51:04 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer die Liste der Buchungen in einer Sammellastschrift anzeigen.
 */
public class SammelLastBuchungList implements Action
{

  /**
   * Erwartet zwingend ein Objekt vom Typ <code>SammelLastschrift</code> im Context.
   * Es werden dann die Buchungen genau dieser Lastschrift angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    if (context == null || !(context instanceof SammelLastschrift))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Sammellastschrift aus."));

    SammelLastschrift s = (SammelLastschrift) context;
    
    try
    {
      if (s.isNewObject())
        s.store(); // Wir speichern eigenmaechtig ab.
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing sammellastschrift",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Sammellastschrift"));
    }
    GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelLastBuchungList.class,s);
  }

}


/**********************************************************************
 * $Log: SammelLastBuchungList.java,v $
 * Revision 1.1  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 **********************************************************************/