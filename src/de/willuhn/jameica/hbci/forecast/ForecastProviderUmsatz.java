/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.I18N;

/**
 * Forecast-Provider für eine Saldo-Prognose anhand der Umsätze der letzten 3 Monate.
 */
public class ForecastProviderUmsatz implements ForecastProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("Umsätze");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#isDefaultEnabled()
   */
  @Override
  public boolean isDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getData(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date)
   */
  @Override
  public List<Value> getData(Konto k, Date to) throws Exception
  {
    // Wir holen uns erstmal eine Liste der Umsätze aus den letzten 6 Monaten
    final Date end = DateUtil.endOfDay(new Date());
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH,-6);
    final Date start = DateUtil.startOfDay(cal.getTime());
    
    final Map<String,SumEntry> map = new HashMap<>();
    
    final DBIterator<Umsatz> it = UmsatzUtil.find(k,null,start,end,null);
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    it.setOrder("ORDER BY " + service.getSQLTimestamp("datum") + ", id");
    
    while (it.hasNext())
    {
      final Umsatz u = it.next();
      final String iban = u.getGegenkontoNummer();
      final double d = u.getBetrag();
      final Date date = u.getDatum();
      
      // Zahlungen unter 10,- EUR ignorieren wir aus Performance-Gründen
      if (Double.isNaN(d) || date == null || Math.abs(d) <= 10d)
        continue;
      
      final SumEntry entry = map.computeIfAbsent(iban,i -> new SumEntry());
      cal.setTime(date);
      entry.add(cal.get(Calendar.DATE),d);
    }
    
    // OK, jetzt haben wir eine Map mit Zahlungen pro Gegenkonto.
    // Daraus können wir eine Prognose ableiten
    final List<Value> result = new ArrayList<Value>();
    
    // Wir iterieren tagesweise von heute bis um Ende-Zeitraum und
    // schauen, ob wir Zahlungen an diesen Tagen haben
    int limit = 0;
    cal.setTime(DateUtil.startOfDay(new Date()));
    while (!cal.getTime().after(to))
    {
      // Maximal ~3 Jahre
      if (limit++ >= 1000)
        break;
      
      int day = cal.get(Calendar.DATE);
      for (SumEntry e:map.values())
      {
        // Zahlungen, die in dem ganzen Zeitraum nur einmal vorgekommen sind,
        // ignorieren wir. Hier kann nicht mit einer Wiederholung gerechnet werden
        if (e.count < 2)
          continue;
        
        if (e.getDay() == day)
        {
          // An dem Tag findet typischerweise eine Zahlung statt
          final Value v = new Value(cal.getTime(),e.getValue());
          result.add(v);
        }
      }
      
      cal.add(Calendar.DATE,1);
    }
    
    return result;
  }
  
  private class SumEntry
  {
    private int count = 0;
    private int days = 0;
    private double sum = 0.0d;
    
    /**
     * Liefert den mittleren Tag der Zahlungen.
     * @return der mittlere Tag der Zahlungen.
     */
    private int getDay()
    {
      return this.days / this.count;
    }
    
    /**
     * Liefert den Durchschnittsbetrag.
     * @return der Durchschnittsbetrag.
     */
    private double getValue()
    {
      return this.sum / this.count;
   }
    
    /**
     * Fügt den Wert an einem Tag hinzu.
     * @param day der Tag.
     * @param value der Wert.
     */
    private void add(int day, double value)
    {
      this.count++;
      this.days += day;
      this.sum += value;
    }
  }
}


