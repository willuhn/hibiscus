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
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.Konto;
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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public String getName()
  {
    return i18n.tr("SEPA-Sammellastschriften");
  }

  @Override
  public List search(String search) throws RemoteException, ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;
    
    final List<MyResult> result = new ArrayList<>();
    
    String text = "%" + search.toLowerCase() + "%";
    
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
      result.add(new MyResult(ueb,buchung));
    }
    
    // Schritt 2: Sammel-Auftraege selbst
    list = Settings.getDBService().createList(SepaSammelLastschrift.class);
    list.addFilter("LOWER(bezeichnung) LIKE ?",text);

    while (list.hasNext())
    {
      SepaSammelLastschrift ueb = (SepaSammelLastschrift) list.next();
      result.add(new MyResult(ueb,null));
    }

    return result;
  }
  
  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private SepaSammelLastschrift u = null;
    private SepaSammelLastBuchung buchung = null;
    
    /**
     * ct.
     * @param u
     * @param buchung optionale Angabe der Buchung.
     */
    private MyResult(SepaSammelLastschrift u, SepaSammelLastBuchung buchung)
    {
      this.u = u;
      this.buchung = buchung;
    }

    @Override
    public void execute() throws RemoteException, ApplicationException
    {
      if (this.buchung != null)
        new SepaSammelLastBuchungNew().handleAction(this.buchung);
      else
        new SepaSammelLastschriftNew().handleAction(this.u);
    }

    @Override
    public String getName()
    {
      try
      {
        if (buchung != null)
        {
          Konto k = u.getKonto();
          String[] params = new String[] {
              HBCI.DATEFORMAT.format(u.getTermin()),
              HBCI.DECIMALFORMAT.format(buchung.getBetrag()),
              k.getWaehrung(),
              buchung.getGegenkontoName(),
              buchung.getZweck(),
              k.getLongName(),
          };
          I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
          return i18n.tr("{0}: {1} {2} von {3} einziehen - {4} (via {5})",params);
        }
        else
        {
          return i18n.tr("{0}: {1}", HBCI.DATEFORMAT.format(u.getTermin()), u.getBezeichnung());
        }
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
    
  }

}
