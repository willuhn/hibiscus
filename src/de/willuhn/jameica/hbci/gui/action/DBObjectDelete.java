/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DBObjectDelete.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/23 18:07:14 $
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

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Generische Action fuer das Loeschen von Datensaetzen.
 */
public class DBObjectDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>DBObject</code> oder <code>DBObject[]</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Keine zu löschenden Daten ausgewählt"));

    if (!(context instanceof DBObject) && !(context instanceof DBObject[]))
    {
      Logger.warn("wrong type to delete: " + context.getClass());
      return;
    }

    boolean array = (context instanceof DBObject[]);
    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Daten löschen"));
      d.setText(i18n.tr("Wollen Sie diese {0} Datensätze wirklich löschen?",""+((DBObject[])context).length));
    }
    else
    {
      d.setTitle(i18n.tr("Daten löschen"));
      d.setText(i18n.tr("Wollen Sie diesen Datensatz wirklich löschen?"));
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting objects",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Datensatzes"));
      return;
    }

    DBObject[] list = null;
    if (array)
      list = (DBObject[]) context;
    else
      list = new DBObject[]{(DBObject)context}; // Array mit einem Element

		try {

      for (int i=0;i<list.length;++i)
      {
        if (list[i].isNewObject())
          continue; // muss nicht geloescht werden

        // ok, wir loeschen das Objekt
        list[i].delete();
      }
      if (array)
        GUI.getStatusBar().setSuccessText(i18n.tr("{0} Datensätze gelöscht.",""+list.length));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("Datensatz gelöscht."));

		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Datensätze."));
			Logger.error("unable to delete objects",e);
		}
  }

}


/**********************************************************************
 * $Log: DBObjectDelete.java,v $
 * Revision 1.2  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.1  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 **********************************************************************/