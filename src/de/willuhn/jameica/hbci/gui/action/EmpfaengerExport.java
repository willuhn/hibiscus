/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/EmpfaengerExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/05 16:42:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Adressen exportieren werden koennen.
 * Als Parameter kann eine einzelnes Adresse-Objekt oder ein Array uebergeben werden.
 */
public class EmpfaengerExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Adresse</code> oder <code>Adresse[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Adresse aus"));

		if (!(context instanceof Adresse) && !(context instanceof Adresse[]))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Adressen aus"));

    Adresse[] u = null;
		try {

			if (context instanceof Adresse)
			{
				u = new Adresse[1];
        u[0] = (Adresse) context;
			}
      else if (context instanceof Adresse[])
      {
        u = (Adresse[]) context;
      }

      ExportDialog d = new ExportDialog(u, Adresse.class);
      d.open();
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting addresses",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Adressen"));
		}
  }

}


/**********************************************************************
 * $Log: EmpfaengerExport.java,v $
 * Revision 1.1  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 **********************************************************************/