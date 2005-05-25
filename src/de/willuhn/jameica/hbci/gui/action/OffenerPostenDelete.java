/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/OffenerPostenDelete.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines offenen Posten.
 */
public class OffenerPostenDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>OffenerPosten</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Kein offener Posten ausgewählt"));

    if (!(context instanceof OffenerPosten) && !(context instanceof OffenerPosten[]))
			throw new ApplicationException(i18n.tr("Keine offenen Posten ausgewählt"));

    boolean array = (context instanceof OffenerPosten[]);
    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Offene Posten löschen"));
      d.setText(i18n.tr("Wollen Sie diese {0} offenen Posten wirklich löschen?",""+((OffenerPosten[])context).length));
    }
    else
    {
      d.setTitle(i18n.tr("Offenen Posten löschen"));
      d.setText(i18n.tr("Wollen Sie diesen offenen Posten wirklich löschen?"));
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting op entries",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der offenen Posten"));
      return;
    }

    OffenerPosten[] list = null;
    if (array)
      list = (OffenerPosten[]) context;
    else
      list = new OffenerPosten[]{(OffenerPosten)context}; // Array mit einem Element

		try {

      for (int i=0;i<list.length;++i)
      {
        if (list[i].isNewObject())
          continue; // muss nicht geloescht werden

        // ok, wir loeschen das Objekt
        list[i].delete();
      }
      if (array)
        GUI.getStatusBar().setSuccessText(i18n.tr("{0} offene Posten gelöscht.",""+list.length));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("Offener Posten gelöscht."));

		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des offenen Posten."));
			Logger.error("unable to delete op entries",e);
		}
  }

}


/**********************************************************************
 * $Log: OffenerPostenDelete.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/