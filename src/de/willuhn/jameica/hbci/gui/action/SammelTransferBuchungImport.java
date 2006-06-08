/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/SammelTransferBuchungImport.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/08 22:29:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ImportDialog;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Buchungen einer Sammellastschriften und Sammel-Ueberweisungen importiert werden koennen.
 */
public class SammelTransferBuchungImport implements Action
{

  /**
   * Als Context wird der zugehoerige Sammel-Transfer verlangt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    if (context == null || !(context instanceof SammelTransfer))
      throw new ApplicationException(i18n.tr("Bitte geben Sie den zugehörigen Sammel-Auftrag an"));
    try
    {
      // Nicht sehr schoen generisch. Aber ich erwaerte nicht,
      // dass weitere Objekte hinzukommen, die von SammelTransfer abgeleitet sind ;)
      boolean isUeb = (context instanceof SammelUeberweisung);
      
      ImportDialog d = new ImportDialog((GenericObject)context, isUeb ? SammelUeberweisungBuchung.class : SammelLastBuchung.class);
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
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Importieren der Buchungen"));
		}
  }

}


/**********************************************************************
 * $Log: SammelTransferBuchungImport.java,v $
 * Revision 1.1  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 **********************************************************************/