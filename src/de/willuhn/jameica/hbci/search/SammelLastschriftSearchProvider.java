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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in Sammel-Lastschriften.
 */
public class SammelLastschriftSearchProvider implements SearchProvider
{
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Sammel-Lastschriften");
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
    
    // Wir speichern die Daten erstmal in einem Hash, damit wir Duplikate rausfischen koennen
    Hashtable hash = new Hashtable();
    
    // Schritt 1: Die Buchungen von Sammel-Auftraegen
    DBIterator list = Settings.getDBService().createList(SammelLastBuchung.class);
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(zweck2) LIKE ? OR " +
                   "LOWER(zweck3) LIKE ? OR " +
                   "LOWER(gegenkonto_name) LIKE ? OR " +
                   "gegenkonto_nr LIKE ? OR " +
                   "gegenkonto_blz LIKE ?",
                   text,text,text,text,text,text);

    while (list.hasNext())
    {
      SammelLastBuchung buchung = (SammelLastBuchung) list.next();
      SammelLastschrift ueb = (SammelLastschrift) buchung.getSammelTransfer();
      hash.put(ueb.getID(),new MyResult(ueb));
    }
    
    // Schritt 2: Sammel-Auftraege selbst
    list = Settings.getDBService().createList(SammelLastschrift.class);
    list.addFilter("LOWER(bezeichnung) LIKE ?",text);

    while (list.hasNext())
    {
      SammelLastschrift ueb = (SammelLastschrift) list.next();
      hash.put(ueb.getID(),new MyResult(ueb));
    }

    return Arrays.asList(hash.values().toArray(new MyResult[hash.size()]));
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private SammelLastschrift u = null;
    
    /**
     * ct.
     * @param u
     */
    private MyResult(SammelLastschrift u)
    {
      this.u = u;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new SammelLastschriftNew().handleAction(this.u);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        return i18n.tr("{0}: {1}",new String[] {HBCI.DATEFORMAT.format(u.getTermin()),u.getBezeichnung()});
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
 * $Log: SammelLastschriftSearchProvider.java,v $
 * Revision 1.3  2011/08/05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.2  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.1  2008/09/04 23:42:33  willuhn
 * @N Searchprovider fuer Sammel- und Dauerauftraege
 * @N Sortierung von Ueberweisungen und Lastschriften in Suchergebnissen
 * @C "getNaechsteZahlung" von DauerauftragUtil nach TurnusHelper verschoben
 *
 **********************************************************************/
