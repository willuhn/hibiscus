/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoResetAuszugsdatum.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/11/24 00:07:08 $
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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Reset des Kontoauszugsdatums
 */
public class KontoResetAuszugsdatum implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * 
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    if (context == null || !(context instanceof Konto))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

    try
    {
      Konto k = (Konto) context;
      if (k.isNewObject())
        return;

      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle(i18n.tr("Kontoauszugsdatum zurücksetzen"));
      d.setText(i18n.tr("Soll das Kontoauszugsdatum wirklich zurückgesetzt werden?\n"
                      + "Bei der nächsten Synchronisierung werden alle bei der Bank \n"
                      + "verfügbaren Umsätze abgeholt."));

      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;

      k.resetSaldoDatum();
      k.store();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Kontoauszugsdatum zurückgesetzt."), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while resetting saldo_date", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zurücksetzen des Datums"), StatusBarMessage.TYPE_ERROR));
      return;
    }
  }

}

/*******************************************************************************
 * $Log: KontoResetAuszugsdatum.java,v $
 * Revision 1.2  2006/11/24 00:07:08  willuhn
 * @C Konfiguration der Umsatz-Kategorien in View Einstellungen verschoben
 * @N Redesign View Einstellungen
 *
 * Revision 1.1  2006/10/09 16:55:51  jost
 * Bug #284
 *
 ******************************************************************************/
