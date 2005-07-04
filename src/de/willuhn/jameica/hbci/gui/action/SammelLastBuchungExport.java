/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/SammelLastBuchungExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/07/04 12:41:39 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die die Buchungen einer Sammellastschrift exportiert werden koennen.
 */
public class SammelLastBuchungExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>SammelLastBuchung</code>, <code>SammelLastBuchung[]</code>
   * oder <code>SammelLastschrift</code>. In letzterem Fall werden alle in dieser Lastschrift
   * enthaltenen Buchungen exportiert.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Buchung aus"));

		if (!(context instanceof SammelLastBuchung) && !(context instanceof SammelLastBuchung[]) && !(context instanceof SammelLastschrift))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Buchungen aus"));

    SammelLastBuchung[] u = null;
		try {

			if (context instanceof SammelLastBuchung)
			{
				u = new SammelLastBuchung[1];
        u[0] = (SammelLastBuchung) context;
			}
      else if (context instanceof SammelLastBuchung[])
      {
        u = (SammelLastBuchung[]) context;
      }
      else if (context instanceof SammelLastschrift)
      {
        DBIterator list = ((SammelLastschrift) context).getBuchungen();
        u = new SammelLastBuchung[list.size()];
        int i = 0;
        while (list.hasNext())
        {
          u[i++] = (SammelLastBuchung) list.next();
        }
      }

      ExportDialog d = new ExportDialog(u, SammelLastBuchung.class);
      d.open();
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting sammellastbuchung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Buchungen"));
		}
  }

}


/**********************************************************************
 * $Log: SammelLastBuchungExport.java,v $
 * Revision 1.1  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 **********************************************************************/