/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.search;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in Umsaetzen.
 */
public class UmsatzSearchProvider implements SearchProvider
{
  @Override
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Umsätze");
  }

  @Override
  public List search(String search) throws RemoteException, ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;

    DBIterator list = UmsatzUtil.find(search);
    ArrayList results = new ArrayList();
    while (list.hasNext())
    {
      results.add(new MyResult((Umsatz)list.next()));
    }
    return results;
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Umsatz umsatz = null;
    
    /**
     * ct.
     * @param u
     */
    private MyResult(Umsatz u)
    {
      this.umsatz = u;
    }

    @Override
    public void execute() throws RemoteException, ApplicationException
    {
      new UmsatzDetail().handleAction(this.umsatz);
    }

    @Override
    public String getName()
    {
      try
      {
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

        Konto konto           = umsatz.getKonto();
        Date date             = umsatz.getDatum();
        double betrag         = umsatz.getBetrag();
        String rel            = i18n.tr(betrag > 0 ? "von" : "an");
        String zweck          = StringUtils.trimToEmpty(umsatz.getZweck());
        String gegenkontoName = umsatz.getGegenkontoName();

        betrag = Math.abs(betrag);
        if (gegenkontoName == null || gegenkontoName.length() == 0)
        {
          return i18n.tr("{0}: {1} {2} - {3}", HBCI.DATEFORMAT.format(date),
                                               HBCI.DECIMALFORMAT.format(betrag),
                                               konto.getWaehrung(),
                                               zweck);
        }
        return i18n.tr("{0}: {1} {2} {3} {4} - {5}", HBCI.DATEFORMAT.format(date),
                                                     HBCI.DECIMALFORMAT.format(betrag),
                                                     konto.getWaehrung(),
                                                     rel,
                                                     gegenkontoName,
                                                     zweck);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
  }
}
