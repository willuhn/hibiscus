/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer SEPA-Lastschriften.
 */
public class ForecastProviderSepaLastschrift extends AbstractForecastProvider<SepaLastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.forecast.AbstractForecastProvider#createValue(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  Value createValue(Schedule<SepaLastschrift> schedule) throws RemoteException
  {
    // Positiv-Betrag verwenden, weil wir das ja gutgeschrieben kriegen
    return new Value(schedule.getDate(),schedule.getContext().getBetrag());
  }
}
