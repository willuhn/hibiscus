/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.messaging.SaldoLimitsMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.hbci.util.SaldoFinder;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Diese Klasse erzeugt eine Saldo-Prognose basierend auf den konfigurierten
 * Forecast-Providern.
 */
public class ForecastCreator
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = new Settings(ForecastCreator.class);
  private static List<Class<ForecastProvider>> providers = null;

  private static List<SaldoLimit> limits = new ArrayList<>();

  
  /**
   * Liefert die Liste aller Forecast-Provider - unabhaengig davon, ob sie
   * gerade aktiv sind oder nicht.
   * @return die Liste aller Forecast-Provider.
   */
  public static synchronized List<ForecastProvider> getProviders()
  {
    // load providers
    if (providers == null)
    {
      providers = new LinkedList<Class<ForecastProvider>>();
      try
      {
        MultipleClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
        Class<ForecastProvider>[] classes = loader.getClassFinder().findImplementors(ForecastProvider.class);
        for (Class<ForecastProvider> c:classes)
        {
          providers.add(c);
        }
      }
      catch (ClassNotFoundException cne)
      {
        Logger.error("no forecast providers found",cne);
      }
    }
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    
    List<ForecastProvider> result = new LinkedList<ForecastProvider>();
    for (Class<ForecastProvider> p:providers)
    {
      try
      {
        result.add(service.get(p));
      }
      catch (Exception e)
      {
        Logger.error("unable to load " + p + " - skipping",e);
      }
    }
    return result;
  }

  /**
   * Liefert das Limit für das Konto, falls vorhanden.
   * @param k das Konto.
   * @param type die Art des Limits.
   * @return das Limit. Nie NULL sondern hoechtens ein deaktiviertes Limit ohne Werte.
   */
  public static SaldoLimit getLimit(Konto k, SaldoLimit.Type type)
  {
    if (k == null || type == null)
      return null;

    final SaldoLimit limit = new SaldoLimit(k,type);
    
    try
    {
      final String prefix = "limit." + type.name().toLowerCase();
      
      final String saldo    = StringUtils.trimToNull(k.getMeta(prefix + ".saldo",null));
      final String days     = StringUtils.trimToNull(k.getMeta(prefix + ".days",null));
      final boolean enabled = Boolean.valueOf(k.getMeta(prefix + ".enabled","false"));
      final boolean notify  = Boolean.valueOf(k.getMeta(prefix + ".notify","false"));
      
      limit.setEnabled(enabled);
      limit.setNotify(notify);
      
      if (saldo == null || days == null)
        return limit;
      
      final double value = HBCI.DECIMALFORMAT.parse(saldo).doubleValue();
      final int d = Integer.parseInt(days);
      
      if (d < 0)
      {
        Logger.warn("invalid days: " + d);
        return limit;
      }
      
      limit.setDays(d);
      limit.setValue(value);
    }
    catch (Exception re)
    {
      Logger.error("unable to load " + type + " limit for account",re);
    }
    return limit;
  }

  /**
   * Speichert das Limit für das Konto.
   * @param limit das Limit.
   */
  public static void setLimit(SaldoLimit limit)
  {
    try
    {
      final Konto k = limit.getKonto();
      final String prefix = "limit." + limit.getType().name().toLowerCase();
      k.setMeta(prefix + ".saldo",HBCI.DECIMALFORMAT.format(limit.getValue()));
      k.setMeta(prefix + ".days",Integer.toString(limit.getDays()));
      k.setMeta(prefix + ".enabled",Boolean.toString(limit.isEnabled()));
      k.setMeta(prefix + ".notify",Boolean.toString(limit.isNotify()));
      Application.getMessagingFactory().sendMessage(new SaldoLimitsMessage());
    }
    catch (Exception re)
    {
      Logger.error("unable to save limit for account",re);
    }
  }

  /**
   * Prüft, ob das Limit des Kontos in der angegebenen Anzahl Tage erreicht wird.
   * @param k das Konto.
   * @param type die Art des Limits.
   * @return das Saldo-Limit oder NULL, wenn es nicht erreicht wird. Das Saldo-Limit
   * enthält den Tag, an dem das Limit erreicht wurde.
   */
  private static SaldoLimit checkLimit(Konto k, SaldoLimit.Type type)
  {
    // Checken, ob wir ueberhaupt ein Limit haben
    final SaldoLimit limit = getLimit(k,type);
    if (!limit.isEnabled())
      return null;
    
    // Saldo-Verlauf für die angegebene Zeit ermitteln
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE,limit.getDays());
    try
    {
      final Date today = DateUtil.startOfDay(new Date());
      final List<Value> values = create(k,DateUtil.endOfDay(cal.getTime()));
      
      // Wir haben keine einzige Buchung. Dann entscheiden wir basierend auf dem aktuellen Saldo
      if (values == null || values.isEmpty())
      {
        if (type.reached(k.getSaldo(),limit.getValue()))
        {
          // Das ist dann heute bereits der Fall
          limit.setDate(today);
          return limit;
        }
      }
      
      for (Value v:values)
      {
        if (type.reached(v.getValue(),limit.getValue()))
        {
          // Wir sind unter den Saldo gefallen.
          limit.setDate(v.getDate());
          return limit;
        }
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to check saldo limit",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Prüfen des Saldolimits fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
    }
    return null;
  }
  
  /**
   * Liefert die aktuellen Saldo-Limits.
   * @return die aktuellen Saldo-Limits.
   */
  public static List<SaldoLimit> getLimits()
  {
    return limits;
  }
  
  /**
   * Berechnet die Saldo-Limits neu.
   */
  public static synchronized void updateLimits()
  {
    limits.clear();

    try
    {
      final long started = System.currentTimeMillis();
      
      for (Konto k:KontoUtil.getKonten(KontoFilter.ACTIVE))
      {
        final SaldoLimit l = ForecastCreator.checkLimit(k,SaldoLimit.Type.LOWER);
        if (l != null)
          limits.add(l);
        
        final SaldoLimit u = ForecastCreator.checkLimit(k,SaldoLimit.Type.UPPER);
        if (u != null)
          limits.add(u);
      }
      
      final long used = System.currentTimeMillis() - started;
      final String msg = "recalculated saldo limits, found " + limits.size() + " limits, took " + used + " millis";
      if (used > 800)
        Logger.info(msg);
      else
        Logger.debug(msg);
    }
    catch (Exception e)
    {
      Logger.error("unable to check for saldo limits",e);
    }
  }

  /**
   * Erzeugt eine Liste von Salden fuer das angegebene Konto von heute bis zum angegebenen Zieldatum.
   * Die Liste enthaelt hierbei fuer jeden Tag einen Wert (auch wenn an diesem Tag
   * keine Zahlungsvorgaenge stattfanden - in dem Fall besitzt der Wert den Saldo des Vortages),
   * kann daher also 1:1 auf eine Chart-Grafik gemappt werden.
   * @param k das Konto. Optional. Ist keines angegeben, wird eine Prognose ueber
   * alle Konten erstellt.
   * @param to Ende des Zeitraumes. Ist keines angegeben, endet die Auswertung 1 Jahr nach Beginn
   * des Zeitraumes.
   * @return die Liste der Salden.
   * @throws RemoteException
   */
  public static List<Value> create(Konto k, Date to) throws RemoteException
  {
    ////////////////////////////////////////////////////////////////////////////
    // Start- und End-Datum vorbereiten
    Date from = DateUtil.startOfDay(new Date());
    
    if (to == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.YEAR,1);
      to = cal.getTime();
    }
    
    to = DateUtil.endOfDay(to);
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Schritt 1: Die Daten aller Provider in einer Liste zusammenfassen.
    // Das sind erstmal noch keine Salden sondern nur die Geldbewegungen
    TreeMap<Date,Value> dates = new TreeMap<Date,Value>();
    List<ForecastProvider> providers = getProviders();
    for (ForecastProvider p:providers)
    {
      if (!isEnabled(p))
        continue;

      try
      {
        List<Value> values = p.getData(k,to);
        if (values == null || values.isEmpty())
          continue;
        
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
      startSaldo = k.getNumUmsaetze() > 0 ? KontoUtil.getEndSaldo(k,from) : k.getSaldo();
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
  public static boolean isEnabled(ForecastProvider provider)
  {
    return settings.getBoolean(provider.getClass().getName() + ".enabled",provider.isDefaultEnabled());
  }
  
  /**
   * Legt fest, ob der Provider verwendet werden soll.
   * @param provider der Provider.
   * @param enabled true, wenn der Provider verwendet werden soll.
   */
  public static void setEnabled(ForecastProvider provider, boolean enabled)
  {
    settings.setAttribute(provider.getClass().getName() + ".enabled",enabled);
  }

}
