/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/EinnahmeAusgabeExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/04 15:57:25 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.io.EinnahmeAusgabe;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action für die Ausgabe der Einnahmen/Ausgaben
 */
public class EinnahmeAusgabeExport implements Action
{
  /**
   * Erwartet ein Objekt vom Typ <code>GenericIterator</code>
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    if (context == null)
      throw new ApplicationException(i18n
          .tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    if (!(context instanceof EinnahmeAusgabe[]))
      throw new ApplicationException(i18n
          .tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    try
    {
      ExportDialog d = new ExportDialog((EinnahmeAusgabe[]) context,
          EinnahmeAusgabe.class);
      d.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while writing report", e);
      Application.getMessagingFactory().sendMessage(
          new StatusBarMessage(i18n.tr("Fehler bei der Erstellung der Liste"),
              StatusBarMessage.TYPE_ERROR));
    }
  }

}

/*******************************************************************************
 * $Log: EinnahmeAusgabeExport.java,v $
 * Revision 1.1  2007/06/04 15:57:25  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
