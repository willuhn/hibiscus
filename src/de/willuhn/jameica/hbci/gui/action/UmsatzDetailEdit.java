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
import java.util.Date;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Bearbeiten eines Umsatzes.
 */
public class UmsatzDetailEdit implements Action
{
  private boolean createReverse = false;
  private Konto toBookTo = null;

  /**
   * setzt ein flag, dass die Gegenbuchung des Kontext-Objects erstellt werden soll
   * @param toBookTo Offline-Konto, auf das die Gegenbuchung erstellt werden soll
   * @return das (modifizierte) Objekt selbst
   * */
  public UmsatzDetailEdit asReverse(Konto toBookTo)
  {
    this.createReverse = true;
    this.toBookTo = toBookTo;
    return this;
  }

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> im Context.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    // Wenn der Context ein Konto ist und dieses ein Offline-Konto ist,
    // legen wir automatisch einen neuen Umsatz fuer dieses Konto an.
    if (context instanceof Konto)
    {
      try
      {
        Konto k = (Konto) context;
        if (!k.hasFlag(Konto.FLAG_OFFLINE))
          return; // ist kein Offline-Konto
        
        Umsatz u = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);
        u.setKonto(k);
        Date d = new Date();
        u.setDatum(d);
        context = u;
      }
      catch (RemoteException re)
      {
        Logger.error("unable to create umsatz",re);
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        throw new ApplicationException(i18n.tr("Fehler beim Anlegen des Umsatzes: {0}",re.getMessage()));
      }
    }
    else if (!(context instanceof Umsatz))
    {
      return;
    }
    else if (createReverse)
    {
      try
      {
        Umsatz orig=(Umsatz)context;
        Umsatz u = orig.duplicate();
        u.setKonto(toBookTo);
        u.setBetrag(-orig.getBetrag());
        Konto konto = orig.getKonto();
        u.setGegenkontoBLZ(konto.getBLZ());
        u.setGegenkontoName(konto.getName());
        u.setGegenkontoNummer(konto.getKontonummer());
        u.setUmsatzTyp(null);
        context = u;
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      } 
    }
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzDetailEdit.class,context);
  }
}
