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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.schedule.ScheduleProvider;
import de.willuhn.jameica.hbci.schedule.ScheduleProviderFactory;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung eines Forecast-Providers.
 * @param <T> der konkrete Typ des Providers.
 */
public abstract class AbstractForecastProvider<T extends HibiscusDBObject> implements ForecastProvider
{
  private ScheduleProvider provider = null;

  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getName()
   */
  public String getName()
  {
    ScheduleProvider provider = this.getScheduleProvider();
    return provider != null ? provider.getName() : "<unknown provider>";
  }

  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getData(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date)
   */
  public List<Value> getData(Konto k, Date from, Date to) throws Exception
  {
    List<Value> result = new LinkedList<Value>();

    ScheduleProvider provider = this.getScheduleProvider();
    if (provider == null)
    {
      Logger.warn("unable to determine schedule provider for " + this.getClass().getSimpleName());
      return result;
    }
    
    List<Schedule<T>> list = provider.getSchedules(k,from,to);

    // In Values kopieren
    for (Schedule<T> schedule:list)
    {
      result.add(this.createValue(schedule));
    }
    return result;
  }

  /**
   * Erzeugt das Value-Objekt.
   * @param schedule das Schedule.
   * @return das Value-Objekt.
   * @throws RemoteException
   */
  abstract Value createValue(Schedule<T> schedule) throws RemoteException;

  /**
   * Liefert den passenden Schedule-Provider.
   * @return der Schedule-Provider.
   */
  private ScheduleProvider getScheduleProvider()
  {
    if (this.provider == null)
      this.provider = ScheduleProviderFactory.getScheduleProvider(BeanUtil.getType(this.getClass()));
    return this.provider;
  }

}



/**********************************************************************
 * $Log: AbstractForecastProvider.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/