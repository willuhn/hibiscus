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
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung eines Forecast-Providers.
 * @param <T> der konkrete Typ des Providers.
 */
public abstract class AbstractForecastProvider<T extends HibiscusDBObject> implements ForecastProvider
{
  private ScheduleProvider provider = null;

  @Override
  public String getName()
  {
    ScheduleProvider provider = this.getScheduleProvider();
    return provider != null ? provider.getName() : "<unknown provider>";
  }

  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getData(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date)
   */
  @Override
  public List<Value> getData(Konto k, Date to) throws Exception
  {
    List<Value> result = new LinkedList<Value>();

    ScheduleProvider provider = this.getScheduleProvider();
    if (provider == null)
    {
      Logger.warn("unable to determine schedule provider for " + this.getClass().getSimpleName());
      return result;
    }
    
    List<Schedule<T>> list = provider.getSchedules(k,DateUtil.startOfDay(new Date()),to);

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
  
  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#isDefaultEnabled()
   */
  @Override
  public boolean isDefaultEnabled()
  {
    return true;
  }
}
