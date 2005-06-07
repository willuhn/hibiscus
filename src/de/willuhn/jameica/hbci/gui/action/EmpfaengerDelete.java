/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/EmpfaengerDelete.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/06/07 22:41:09 $
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
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen von Adressen.
 */
public class EmpfaengerDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Adresse</code> oder <code>Adresse[]</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Keine Adresse ausgewählt"));

    if (!(context instanceof Adresse) && !(context instanceof Adresse[]))
			throw new ApplicationException(i18n.tr("Keine Adresse ausgewählt"));

    boolean array = (context instanceof Adresse[]);
    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Adressen löschen"));
      d.setText(i18n.tr("Wollen Sie diese {0} Adressen wirklich löschen?",""+((Adresse[])context).length));
    }
    else
    {
      d.setTitle(i18n.tr("Adresse löschen"));
      d.setText(i18n.tr("Wollen Sie diese Adresse wirklich löschen?"));
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting address",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Adresse"));
      return;
    }

    Adresse[] list = null;
    if (array)
      list = (Adresse[]) context;
    else
      list = new Adresse[]{(Adresse)context}; // Array mit einem Element

		try {

      for (int i=0;i<list.length;++i)
      {
        if (list[i].isNewObject())
          continue; // muss nicht geloescht werden

        // ok, wir loeschen das Objekt
        list[i].delete();
      }
      if (array)
        GUI.getStatusBar().setSuccessText(i18n.tr("{0} Adressen gelöscht.",""+list.length));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("Adresse gelöscht."));

		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Adressen."));
			Logger.error("unable to delete address",e);
		}
  }

}


/**********************************************************************
 * $Log: EmpfaengerDelete.java,v $
 * Revision 1.6  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.5  2005/05/09 15:02:11  web0
 * @N mehrere Adressen gleichzeitig loeschen
 *
 * Revision 1.4  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.3  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/