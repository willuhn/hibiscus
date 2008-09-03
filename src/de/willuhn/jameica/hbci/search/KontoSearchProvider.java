/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/search/KontoSearchProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/09/03 11:13:51 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Implementierung einen Search-Provider fuer die Suche nach Konten.
 */
public class KontoSearchProvider implements SearchProvider
{
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Konten");
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
    DBIterator list = Settings.getDBService().createList(Konto.class);
    list.addFilter("LOWER(name) LIKE ? OR " +
                   "LOWER(bezeichnung) LIKE ? OR " +
                   "kontonummer LIKE ? OR " +
                   "blz LIKE ? OR " +
                   "kundennummer LIKE ?",
                   new String[]{text,text,text,text,text});

    ArrayList results = new ArrayList();
    while (list.hasNext())
    {
      results.add(new MyResult((Konto)list.next()));
    }
    return results;
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Konto konto = null;
    
    /**
     * ct.
     * @param k
     */
    private MyResult(Konto k)
    {
      this.konto = k;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new KontoNew().handleAction(this.konto);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        String bank = HBCIUtils.getNameForBLZ(this.konto.getBLZ());
        String bez = this.konto.getBezeichnung();
        
        if (bank != null && bank.length() > 0)
          return bez + ", " + bank;
        return bez;
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
 * $Log: KontoSearchProvider.java,v $
 * Revision 1.1  2008/09/03 11:13:51  willuhn
 * @N Mehr Suchprovider
 *
 **********************************************************************/
