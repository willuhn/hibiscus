/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/forecast/ForecastCreator.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/27 17:10:02 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.hbci.util.SaldoFinder;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Diese Klasse erzeugt eine Saldo-Prognose basierend auf den konfigurierten
 * Forecast-Providern.
 */
public class ForecastCreator
{
  private final static Settings settings = new Settings(ForecastCreator.class);
  private static List<ForecastProvider> providers = null;
  
  /**
   * Liefert die Liste aller Forecast-Provider - unabhaengig davon, ob sie
   * gerade aktiv sind oder nicht.
   * @return die Liste aller Forecast-Provider.
   */
  public synchronized List<ForecastProvider> getProviders()
  {
    if (providers != null)
      return providers;

    providers = new ArrayList<ForecastProvider>();
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      MultipleClassLoader loader = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader();
      Class<ForecastProvider>[] classes = loader.getClassFinder().findImplementors(ForecastProvider.class);
      for (Class<ForecastProvider> c:classes)
      {
        try
        {
          providers.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load " + c,e);
        }
      }
    }
    catch (ClassNotFoundException cne)
    {
      Logger.error("no forecast providers found",cne);
    }
    return providers;
  }
  
  /**
   * Erzeugt eine Liste von Salden fuer das angegebene Konto im angegebenen Zeitraum.
   * Die Liste enthaelt hierbei fuer jeden Tag einen Wert (auch wenn an diesem Tag
   * keine Zahlungsvorgaenge stattfanden - in dem Fall besitzt der Wert den Saldo des Vortages),
   * kann daher also 1:1 auf eine Chart-Grafik gemappt werden.
   * @param k das Konto. Optional. Ist keines angegeben, wird eine Prognose ueber
   * alle Konten erstellt.
   * @param from Beginn des Zeitraumes. Ist keiner angegeben, beginnt die
   * Auswertung beim heutigen Tag.
   * @param to Ende des Zeitraumes. Ist keines angegeben, endet die Auswertung 1 Jahr nach Beginn
   * des Zeitraumes.
   * @return die Liste der Salden.
   * @throws RemoteException
   */
  public List<Value> create(Konto k, Date from, Date to) throws RemoteException
  {
    ////////////////////////////////////////////////////////////////////////////
    // Start- und End-Datum vorbereiten
    if (from == null)
      from = new Date();
    
    if (to == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.YEAR,1);
      to = cal.getTime();
    }
    
    from = DateUtil.startOfDay(from);
    to   = DateUtil.endOfDay(to);
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Schritt 1: Die Daten aller Provider in einer Liste zusammenfassen.
    // Das sind erstmal noch keine Salden sondern nur die Geldbewegungen
    TreeMap<Date,Value> dates = new TreeMap<Date,Value>();
    List<ForecastProvider> providers = this.getProviders();
    for (ForecastProvider p:providers)
    {
      if (!this.isEnabled(p))
        continue;

      try
      {
        List<Value> values = p.getData(k,from,to);
        for (Value v:values)
        {
          // Haben wir den Tag schon?
          Value existing = dates.get(v.getDate());
          if (existing != null) // haben wir schon. Dann dazu addieren
          {
            existing.setValue(existing.getValue() + v.getValue());
            continue;
          }
          
          // haben wir noch nicht. Also neu anlegen
          dates.put(v.getDate(),v);
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to calculate data from forecast provider \"" + p.getName() + "\", skipping",e);
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Schritt 2: Start-Saldo ermitteln
    double startSaldo = 0.0d;
    if (k != null)
      startSaldo = k.getNumUmsaetze() > 0 ? KontoUtil.getAnfangsSaldo(k,from) : k.getSaldo();
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Schritt 3: Salden draus machen - hierzu addieren wir die Werte jeweils auf
    List<Value> salden = new LinkedList<Value>();
    double prev = startSaldo;
    for (Value v:dates.values())
    {
      Value newValue = new Value(v.getDate(),v.getValue() + prev);
      salden.add(newValue);
      prev = newValue.getValue();
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Schritt 4: Homogenisieren, sodass wir fuer jeden Tag einen Wert haben.
    List<Value> result = new LinkedList<Value>();
    SaldoFinder finder = new SaldoFinder(salden,startSaldo);
    
    // Iterieren ueber den Zeitraum.
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    
    while (!from.after(to))
    {
      Value v = new Value(from,finder.get(from));
      result.add(v);
      
      // Und weiter zum naechsten Tag
      cal.add(Calendar.DATE,1);
      from = cal.getTime();
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    return result;
  }
  
  /**
   * Liefert true, wenn der Provider aktiv ist.
   * @param provider der zu pruefende Provider.
   * @return true, wenn er aktiv ist.
   */
  public boolean isEnabled(ForecastProvider provider)
  {
    return settings.getBoolean(provider.getClass().getName() + ".enabled",true);
  }
  
  /**
   * Legt fest, ob der Provider verwendet werden soll.
   * @param provider der Provider.
   * @param enabled true, wenn der Provider verwendet werden soll.
   */
  public void setEnabled(ForecastProvider provider, boolean enabled)
  {
    settings.setAttribute(provider.getClass().getName() + ".enabled",enabled);
  }

}



/**********************************************************************
 * $Log: ForecastCreator.java,v $
 * Revision 1.1  2011/10/27 17:10:02  willuhn
 * @N Erster Code fuer die Forecast-API - Konto-Prognose
 *
 **********************************************************************/