/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/EmpfaengerImport.java,v $
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
import de.willuhn.jameica.hbci.gui.dialogs.ImportDialog;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Adressen importiert werden koennen.
 * Es wird kein Parameter erwartet.
 */
public class EmpfaengerImport implements Action
{

  /**
   * Erwartet keinen Parameter.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
      ImportDialog d = new ImportDialog(null, Adresse.class);
      d.open();
		}
    catch (OperationCanceledException oce)
    {
      // ignore
    }
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while importing addresses",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Importieren der Adressen"));
		}
  }

}


/**********************************************************************
 * $Log: EmpfaengerImport.java,v $
 * Revision 1.1  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 **********************************************************************/