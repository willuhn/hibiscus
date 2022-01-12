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
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in SEPA-Sammelueberweisungen.
 */
public class SepaSammelUeberweisungSearchProvider implements SearchProvider
{
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("SEPA-Sammelüberweisungen");
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
    DBIterator list = Settings.getDBService().createList(SepaSammelUeberweisungBuchung.class);
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(endtoendid) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "LOWER(empfaenger_konto) LIKE ? OR " +
                   "LOWER(empfaenger_bic) LIKE ?",
                   text,text,text,text,text);

    while (list.hasNext())
    {
      SepaSammelUeberweisungBuchung buchung = (SepaSammelUeberweisungBuchung) list.next();
      SepaSammelUeberweisung ueb = (SepaSammelUeberweisung) buchung.getSammelTransfer();
      hash.put(ueb.getID(),new MyResult(ueb));
    }
    
    // Schritt 2: Sammel-Auftraege selbst
    list = Settings.getDBService().createList(SepaSammelUeberweisung.class);
    list.addFilter("LOWER(bezeichnung) LIKE ?",text);

    while (list.hasNext())
    {
      SepaSammelUeberweisung ueb = (SepaSammelUeberweisung) list.next();
      hash.put(ueb.getID(),new MyResult(ueb));
    }

    return Arrays.asList(hash.values().toArray(new MyResult[0]));
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private SepaSammelUeberweisung u = null;
    
    /**
     * ct.
     * @param u
     */
    private MyResult(SepaSammelUeberweisung u)
    {
      this.u = u;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new SepaSammelUeberweisungNew().handleAction(this.u);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
        return i18n.tr("{0}: {1}", HBCI.DATEFORMAT.format(u.getTermin()), u.getBezeichnung());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
    
  }

}
