/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungImport.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/17 23:44:15 $
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
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Auslandsueberweisungen importiert werden koennen.
 * Der Context-Parameter wird ignoriert. 
 */
public class AuslandsUeberweisungImport implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
      ImportDialog d = new ImportDialog(null, AuslandsUeberweisung.class);
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
			Logger.error("error while importing transfers",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Importieren der Auslandsüberweisungen"));
		}
  }

}


/**********************************************************************
 * $Log: AuslandsUeberweisungImport.java,v $
 * Revision 1.1  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 **********************************************************************/