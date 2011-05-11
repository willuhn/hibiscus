/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungImport.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/11 10:20:28 $
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
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Sammelueberweisungen importiert werden koennen.
 * Der Context-Parameter wird ignoriert. 
 */
public class SammelUeberweisungImport implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
      ImportDialog d = new ImportDialog(null, SammelUeberweisung.class);
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
			Logger.error("error while importing transfers",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Importieren der Sammelüberweisungen"));
		}
  }

}


/**********************************************************************
 * $Log: SammelUeberweisungImport.java,v $
 * Revision 1.2  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/