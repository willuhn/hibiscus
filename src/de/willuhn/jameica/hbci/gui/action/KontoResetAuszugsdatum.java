/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoResetAuszugsdatum.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/04/22 16:10:43 $
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
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
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

      String q = i18n.tr("Sollen Saldo und Aktualisierungsdatum wirklich zurückgesetzt werden?");

      if ((k.getFlags() & Konto.FLAG_OFFLINE) == 0)
      {
        q += "\n\n";
        q += i18n.tr("Bei der nächsten Synchronisierung werden alle bei der Bank verfügbaren\n" +
        		         "Umsätze erneut abgerufen und Saldo sowie Datum aktualisiert.");
      }

      if (!Application.getCallback().askUser(q))
        return;

      k.reset();
      k.store();
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
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
 * Revision 1.4  2010/04/22 16:10:43  willuhn
 * @C Saldo kann bei Offline-Konten zwar nicht manuell bearbeitet werden, dafuer wird er aber beim Zuruecksetzen des Kontos (heisst jetzt "Saldo und Datum zuruecksetzen" statt "Kontoauszugsdatum zuruecksetzen") jetzt ebenfalls geloescht
 *
 * Revision 1.3  2008/12/15 10:52:16  willuhn
 * @N ObjectChangedMessage, um die Tabelle live zu aktualisieren
 *
 * Revision 1.2  2006/11/24 00:07:08  willuhn
 * @C Konfiguration der Umsatz-Kategorien in View Einstellungen verschoben
 * @N Redesign View Einstellungen
 *
 * Revision 1.1  2006/10/09 16:55:51  jost
 * Bug #284
 *
 ******************************************************************************/
