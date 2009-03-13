/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/search/AuslandsUeberweisungSearchProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/13 00:25:12 $
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
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in Auslandsueberweisungen.
 */
public class AuslandsUeberweisungSearchProvider implements SearchProvider
{
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Auslandsüberweisungen");
  }

  /**
   * @see de.willuhn.jameica.search.SearchProvider#search(java.lang.String)
   */
  public List search(String search) throws RemoteException, ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;
    
    String text = "%" + search.toLowerCase() + "%";
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator list = service.createList(AuslandsUeberweisung.class);
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "empfaenger_konto LIKE ? OR " +
                   "empfaenger_bank LIKE ?",
                   new String[]{text,text,text,text,text,text});
    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");

    ArrayList results = new ArrayList();
    while (list.hasNext())
    {
      results.add(new MyResult((AuslandsUeberweisung)list.next()));
    }
    return results;
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private AuslandsUeberweisung u = null;
    
    /**
     * ct.
     * @param u
     */
    private MyResult(AuslandsUeberweisung u)
    {
      this.u = u;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      // TODO: ALU: Aktivieren, wenn Auslandsueberweisungen fertig sind
      // new AuslandsUeberweisungNew().handleAction(this.u);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        Konto k = u.getKonto();
        String[] params = new String[] {
            k.getLongName(),
            u.getZweck(),
            HBCI.DECIMALFORMAT.format(u.getBetrag()),
            k.getWaehrung(),
            u.getGegenkontoName()
           };
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        return i18n.tr("{0}: ({1}) {2} {3} an {4}",params);
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
 * $Log: AuslandsUeberweisungSearchProvider.java,v $
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/