/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AbstractSammelTransferExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/07 14:31:59 $
 * $Author: willuhn $
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
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Action, ueber die Sammel-Auftraege exportiert werden koennen.
 */
public abstract class AbstractSammelTransferExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>SammelTransfer</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof SammelTransfer))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Samml-Auftrag aus"));

    SammelTransferBuchung[] u = null;
		try {
      DBIterator list = ((SammelTransfer) context).getBuchungen();
      u = new SammelTransferBuchung[list.size()];
      int i = 0;
      while (list.hasNext())
      {
        u[i++] = (SammelTransferBuchung) list.next();
      }

      ExportDialog d = new ExportDialog(u, getExportClass());
      d.open();
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting sammeltransfer",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Buchungen"));
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
 * $Log: AbstractSammelTransferExport.java,v $
 * Revision 1.1  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/