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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zur Neubestimmung der Buchungssalden eines Offline-Kontos
 */
public class KontoRecalculateOfflineSaldo implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * 
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {

    if (context == null || !(context instanceof Konto))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

    try
    {
      Konto k = (Konto) context;
      if (k.isNewObject() || !k.hasFlag(Konto.FLAG_OFFLINE))
        return;

      String q = i18n.tr("Die Umsatzsalden werden ab dem letzten geprüften Umsatz neu berechnet.\nSollte es keinen geben, werden alle Salden neu berechnet, wobei der Kontoanfangssaldo 0,00 angenommen wird.");

      if (!Application.getCallback().askUser(q))
        return;

      Umsatz newer = null;
      List<Umsatz> umsaetze = new ArrayList<Umsatz>();
      DBIterator<Umsatz> it = k.getUmsaetze();
      double currentSaldo = 0d;
      
      while (it.hasNext())
      {
        Umsatz um = it.next();
        checkOrder(um,newer);
        if(um.hasFlag(Umsatz.FLAG_CHECKED))
        {
          currentSaldo = um.getSaldo();
          break;
        }
        umsaetze.add(um);
        newer = um;
      }

      Collections.reverse(umsaetze);

      for (Umsatz umsatz:umsaetze)
      {
        currentSaldo += umsatz.getBetrag();
        umsatz.setSaldo(currentSaldo);
        umsatz.store();
      }
      k.setSaldo(currentSaldo);
      k.store();
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Salden neu berechnet."), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while recalculating balances", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Berechnen der Salden."), StatusBarMessage.TYPE_ERROR));
      return;
    }
  }

  /**
   * Prueft die Reihefolge von zwei Buchungen.
   * Das Buchungsdatum von newer darf nicht aelter als das von current sein.
   * Treffen die Bedingungen nicht zu, wird eine ApplicationException mit sprechendem Text geworfen.
   * @param current die aktuelle Buchung.
   * @param newer die neuere Buchung.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private void checkOrder(Umsatz current, Umsatz newer) throws RemoteException, ApplicationException
  {
    if (newer == null)
      return;

    Date dc = (Date) current.getAttribute("datum_pseudo");
    Date dn = (Date) newer.getAttribute("datum_pseudo");
    
    if (dn.before(dc))
      throw new ApplicationException(i18n.tr("Reihenfolge der Buchungsdaten falsch. Buchung Nr. {0} befindet sich vor Buchung Nr. {1}",newer.getID(),current.getID()));
  }

}