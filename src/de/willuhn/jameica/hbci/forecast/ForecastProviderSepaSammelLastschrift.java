/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer SEPA-Sammellastschriften.
 */
public class ForecastProviderSepaSammelLastschrift extends AbstractForecastProvider<SepaSammelLastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.forecast.AbstractForecastProvider#createValue(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  Value createValue(Schedule<SepaSammelLastschrift> schedule) throws RemoteException
  {
    // Positiv-Betrag verwenden, weil wir das ja gutgeschrieben kriegen
    return new Value(schedule.getDate(),schedule.getContext().getSumme().doubleValue());
  }
}
