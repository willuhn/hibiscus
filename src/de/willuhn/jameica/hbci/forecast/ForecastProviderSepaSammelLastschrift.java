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

import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer SEPA-Sammellastschriften.
 */
public class ForecastProviderSepaSammelLastschrift extends AbstractForecastProvider<SepaSammelLastschrift>
{
  @Override
  Value createValue(Schedule<SepaSammelLastschrift> schedule) throws RemoteException
  {
    // Positiv-Betrag verwenden, weil wir das ja gutgeschrieben kriegen
    return new Value(schedule.getContext().getTargetDate(),schedule.getContext().getSumme().doubleValue());
  }
}
