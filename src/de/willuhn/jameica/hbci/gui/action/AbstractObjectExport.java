/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AbstractObjectExport.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/11 10:20:29 $
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
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Action, ueber die Auftraege exportiert werden koennen.
 */
public abstract class AbstractObjectExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Transfer</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    Object[] objects = null;
    
    if (context instanceof Object[])
      objects = (Object[]) context;
    else
      objects = new Object[]{context};
    
		try
    {
		  ExportDialog d = new ExportDialog(objects, getExportClass());
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
			Logger.error("error while exporting transfers",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Aufträge"));
		}
  }

  /**
   * Diese Funktion muss in den abgeleiteten Klassen ueberschrieben werden
   * und das Interface der zu exportierenden Klasse liefern, damit das korrekte
   * Template geladen werden kann.
   * @return Interface fuer das Template.
   */
  abstract Class getExportClass();
}


/**********************************************************************
 * $Log: AbstractObjectExport.java,v $
 * Revision 1.2  2011/05/11 10:20:29  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2009/07/09 17:08:02  willuhn
 * @N BUGZILLA #740
 *
 * Revision 1.1  2006/10/16 14:46:30  willuhn
 * @N CSV-Export von Ueberweisungen und Lastschriften
 *
 **********************************************************************/