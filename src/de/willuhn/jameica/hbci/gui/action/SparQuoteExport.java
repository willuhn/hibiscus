/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.gui.parts.SparQuote.UmsatzEntry;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Exportieren der Sparquote.
 */
public class SparQuoteExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>HibiscusAddress</code> oder <code>HibiscusAddress[]</code>.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens einen Datensatz aus"));

    Object[] u = null;
		try
		{
			if (context instanceof UmsatzEntry)
			{
				u = new UmsatzEntry[1];
        u[0] = (UmsatzEntry) context;
			}
			else if (context instanceof Object[])
      {
			  // Checken, ob wirklich nur Adressen drin stehen
        u = (Object[]) context;
        for (Object o:u)
        {
          if (!(o instanceof UmsatzEntry))
          {
            u = null;
            break;
          }
        }

			  u = (Object[])context;
      }
			else if (context instanceof List)
			{
			  List l = (List) context;
			  u = l.toArray(new UmsatzEntry[l.size()]);
			}

			if (u == null || u.length == 0)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Datensätze aus"));

      ExportDialog d = new ExportDialog(u, UmsatzEntry.class);
      d.open();
		}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting data",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Daten"));
		}
  }

}
