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
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung einen Search-Provider fuer die Suche in SEPA-Sammellastschriften.
 */
public class SepaSammelLastschriftSearchProvider implements SearchProvider
{
  @Override
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("SEPA-Sammellastschriften");
  }

  @Override
  public List search(String search) throws RemoteException,
      ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;
    
    String text = "%" + search.toLowerCase() + "%";
    
    // Wir speichern die Daten erstmal in einem Hash, damit wir Duplikate rausfischen koennen
    Hashtable hash = new Hashtable();
    
    // Schritt 1: Die Buchungen von Sammel-Auftraegen
    DBIterator list = Settings.getDBService().createList(SepaSammelLastBuchung.class);
    list.addFilter("LOWER(zweck) LIKE ? OR " +
                   "LOWER(endtoendid) LIKE ? OR " +
                   "LOWER(creditorid) LIKE ? OR " +
                   "LOWER(mandateid) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "LOWER(empfaenger_konto) LIKE ? OR " +
                   "LOWER(empfaenger_bic) LIKE ?",
                   text,text,text,text,text,text,text);

    while (list.hasNext())
    {
      SepaSammelLastBuchung buchung = (SepaSammelLastBuchung) list.next();
      SepaSammelLastschrift ueb = (SepaSammelLastschrift) buchung.getSammelTransfer();
      hash.put(ueb.getID(),new MyResult(ueb));
    }
    
    // Schritt 2: Sammel-Auftraege selbst
    list = Settings.getDBService().createList(SepaSammelLastschrift.class);
    list.addFilter("LOWER(bezeichnung) LIKE ?",text);

    while (list.hasNext())
    {
      SepaSammelLastschrift ueb = (SepaSammelLastschrift) list.next();
      hash.put(ueb.getID(),new MyResult(ueb));
    }

    return Arrays.asList(hash.values().toArray(new MyResult[hash.size()]));
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private SepaSammelLastschrift u = null;
    
    /**
     * ct.
     * @param u
     */
    private MyResult(SepaSammelLastschrift u)
    {
      this.u = u;
    }

    @Override
    public void execute() throws RemoteException, ApplicationException
    {
      new SepaSammelLastschriftNew().handleAction(this.u);
    }

    @Override
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
