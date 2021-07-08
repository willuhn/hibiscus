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
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einen Search-Provider fuer die Suche in Lastschriften.
 */
public class LastschriftSearchProvider implements SearchProvider
{
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Lastschriften");
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
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator list = service.createList(Lastschrift.class);
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(zweck2) LIKE ? OR " +
                   "LOWER(zweck3) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "empfaenger_konto LIKE ? OR " +
                   "empfaenger_blz LIKE ?",
                   text,text,text,text,text,text);
    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");

    ArrayList results = new ArrayList();
    while (list.hasNext())
    {
      results.add(new MyResult((Lastschrift)list.next()));
    }
    return results;
  }

  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Lastschrift l = null;

    /**
     * ct.
     * @param l
     */
    private MyResult(Lastschrift l)
    {
      this.l = l;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new LastschriftNew().handleAction(this.l);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        Konto k = l.getKonto();
        String[] params = new String[] {
            HBCI.DATEFORMAT.format(l.getTermin()),
            HBCI.DECIMALFORMAT.format(l.getBetrag()),
            k.getWaehrung(),
            l.getGegenkontoName(),
            l.getZweck(),
            k.getLongName(),
        };
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        return i18n.tr("{0}: {1} {2} von {3} - {4} (via {5})",params);
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
 * $Log: LastschriftSearchProvider.java,v $
 * Revision 1.5  2011/08/05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.4  2010-08-17 11:51:08  willuhn
 * @N Datum in Lastschriften und SEPA-Ueberweisungen mit anzeigen
 *
 * Revision 1.3  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.2  2008/09/04 23:42:33  willuhn
 * @N Searchprovider fuer Sammel- und Dauerauftraege
 * @N Sortierung von Ueberweisungen und Lastschriften in Suchergebnissen
 * @C "getNaechsteZahlung" von DauerauftragUtil nach TurnusHelper verschoben
 *
 * Revision 1.1  2008/09/03 11:13:51  willuhn
 * @N Mehr Suchprovider
 *
 * Revision 1.1  2008/09/03 00:12:06  willuhn
 * @N Erster Code fuer Searchprovider in Hibiscus
 *
 **********************************************************************/
