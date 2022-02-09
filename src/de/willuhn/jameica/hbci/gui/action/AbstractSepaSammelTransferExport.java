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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Action, ueber die SEPA-Sammel-Auftraege exportiert werden koennen.
 * @param <T> der konkrete Typ des SEPA-Sammel-Auftrages.
 */
public abstract class AbstractSepaSammelTransferExport<T extends SepaSammelTransfer> implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>SepaSammelTransfer</code>.
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || (!(context instanceof SepaSammelTransfer) && !(context instanceof SepaSammelTransfer[])))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens einen SEPA-Sammelauftrag aus"));

		try
    {
		  SepaSammelTransfer[] list = null;
		  if (context instanceof SepaSammelTransfer)
		    list = new SepaSammelTransfer[]{(SepaSammelTransfer) context};
		  else
		    list = (SepaSammelTransfer[]) context;
		  
		  ExportDialog d = new ExportDialog(list, getExportClass());
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
			Logger.error("error while exporting sepa sammeltransfer",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der SEPA-Sammelaufträge"));
		}
  }

  /**
   * Diese Funktion muss in den abgeleiteten Klassen ueberschrieben werden
   * und das Interface der zu exportierenden Klasse liefern, damit das korrekte
   * Template geladen werden kann.
   * @return Interface fuer das Template.
   */
  abstract Class<T> getExportClass();
}
