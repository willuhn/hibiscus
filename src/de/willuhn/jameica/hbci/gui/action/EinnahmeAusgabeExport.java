/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/EinnahmeAusgabeExport.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/05/11 10:20:28 $
 * $Author: willuhn $
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
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action für die Ausgabe der Einnahmen/Ausgaben
 */
public class EinnahmeAusgabeExport implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Array mit Objekten des Typs <code>Einnahmeausgabe</code>
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof EinnahmeAusgabe[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    try
    {
      ExportDialog d = new ExportDialog((EinnahmeAusgabe[]) context,EinnahmeAusgabe.class);
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
      Logger.error("error while writing report", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Erstellung der Liste"),StatusBarMessage.TYPE_ERROR));
    }
  }

}

/*******************************************************************************
 * $Log: EinnahmeAusgabeExport.java,v $
 * Revision 1.5  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.4  2010-08-24 17:38:04  willuhn
 * @N BUGZILLA 896
 *
 * Revision 1.3  2009/04/05 21:16:22  willuhn
 * @B BUGZILLA 716
 ******************************************************************************/
