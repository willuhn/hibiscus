/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/search/UmsatzSearchProvider.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/12/14 23:18:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.search;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Umsätze");
  }

  /**
   * @see de.willuhn.jameica.search.SearchProvider#search(java.lang.String)
   */
  public List search(String search) throws RemoteException,
      ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;

    String text = "%" + search.toLowerCase() + "%";
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(zweck2) LIKE ? OR " +
                   "LOWER(zweck3) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "empfaenger_konto LIKE ? OR " +
                   "empfaenger_blz LIKE ? OR " +
                   "LOWER(primanota) LIKE ? OR " +
                   "LOWER(art) LIKE ? OR " +
                   "LOWER(customerref) LIKE ? OR " +
                   "LOWER(kommentar) LIKE ?",
                   new String[]{text,text,text,text,text,text,text,text,text,text});

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

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new UmsatzDetail().handleAction(this.umsatz);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

        Konto konto           = umsatz.getKonto();
        Date valuta           = umsatz.getValuta();
        double betrag         = umsatz.getBetrag();
        String rel            = i18n.tr(betrag > 0 ? "von" : "an");
        String zweck          = umsatz.getZweck();
        String gegenkontoName = umsatz.getGegenkontoName();

        betrag = Math.abs(betrag);
        if (gegenkontoName == null || gegenkontoName.length() == 0)
        {
          return i18n.tr("{0}: {1} {2} - {5}", new String[]{HBCI.DATEFORMAT.format(valuta),
                                                            HBCI.DECIMALFORMAT.format(betrag), 
                                                            konto.getWaehrung(),
                                                            zweck});
        }
        return i18n.tr("{0}: {1} {2} {3} {4} - {5}", new String[]{HBCI.DATEFORMAT.format(valuta),
                                                                  HBCI.DECIMALFORMAT.format(betrag), 
                                                                  konto.getWaehrung(),
                                                                  rel,
                                                                  gegenkontoName,
                                                                  zweck});
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
    
  }

}


/**********************************************************************
 * $Log: UmsatzSearchProvider.java,v $
 * Revision 1.3  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.2  2008/09/03 11:13:51  willuhn
 * @N Mehr Suchprovider
 *
 * Revision 1.1  2008/09/03 00:12:06  willuhn
 * @N Erster Code fuer Searchprovider in Hibiscus
 *
 **********************************************************************/
