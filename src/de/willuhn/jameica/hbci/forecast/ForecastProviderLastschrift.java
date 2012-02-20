/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/forecast/ForecastProviderLastschrift.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/02/20 17:03:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer Lastschriften.
 */
public class ForecastProviderLastschrift extends AbstractForecastProvider<Lastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.forecast.AbstractForecastProvider#createValue(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  Value createValue(Schedule<Lastschrift> schedule) throws RemoteException
  {
    // Positiv-Betrag verwenden, weil wir das ja gutgeschrieben kriegen
    return new Value(schedule.getDate(),schedule.getContext().getBetrag());
  }
}


/**********************************************************************
 * $Log: ForecastProviderLastschrift.java,v $
 * Revision 1.2  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 * Revision 1.1  2011/10/27 17:10:02  willuhn
 * @N Erster Code fuer die Forecast-API - Konto-Prognose
 *
 **********************************************************************/