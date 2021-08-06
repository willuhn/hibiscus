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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in Kontoauszuegen.
 */
public class KontoauszugSearchProvider implements SearchProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Elektr. Kontoauszüge");
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
    DBIterator<Kontoauszug> list = service.createList(Kontoauszug.class);
    list.addFilter("jahr LIKE ? OR " +
                   "LOWER(kommentar) LIKE ? OR " +
                   "LOWER(dateiname) LIKE ? OR " +
                   "LOWER(name1) LIKE ? OR " +
                   "LOWER(name2) LIKE ? OR " +
                   "LOWER(name3) LIKE ?",
                   text,text,text,text,text,text);
    list.setOrder("order by jahr desc, nummer desc, " + 
                  service.getSQLTimestamp("erstellungsdatum") + " desc, " + 
                  service.getSQLTimestamp("von") + " desc, " + 
                  service.getSQLTimestamp("ausgefuehrt_am") + " desc");

    ArrayList results = new ArrayList();
    while (list.hasNext())
    {
      results.add(new MyResult(list.next()));
    }
    return results;
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Kontoauszug u = null;
    
    /**
     * ct.
     * @param u
     */
    private MyResult(Kontoauszug u)
    {
      this.u = u;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new Open().handleAction(this.u);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        Konto k = u.getKonto();
        
        List<String> params = new ArrayList<String>();
        params.add(HBCI.DATEFORMAT.format(u.getAusfuehrungsdatum()));
        params.add(k.getLongName());
        
        Integer jahr  = u.getJahr();
        Integer nr    = u.getNummer();
        Date erstellt = u.getErstellungsdatum();
        if (jahr != null && nr != null)
        {
          params.add(Integer.toString(jahr));
          params.add(Integer.toString(nr));
        }
        else if (erstellt != null)
        {
          params.add(HBCI.DATEFORMAT.format(erstellt));
        }

        String[] s = params.toArray(new String[0]);
        if (s.length == 4)
          return i18n.tr("Kontoauszug {2}-{3}, abgerufen am {0} ({1})",s);
        
        if (s.length == 3)
          return i18n.tr("Kontoauszug {2}, abgerufen am {0} ({1})",s);

        return i18n.tr("Kontoauszug, abgerufen am {0} ({1})",s);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
  }
}
