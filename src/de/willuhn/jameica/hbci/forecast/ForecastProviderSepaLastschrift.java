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

import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer SEPA-Lastschriften.
 */
public class ForecastProviderSepaLastschrift extends AbstractForecastProvider<SepaLastschrift>
{
  @Override
  Value createValue(Schedule<SepaLastschrift> schedule) throws RemoteException
  {
    // Positiv-Betrag verwenden, weil wir das ja gutgeschrieben kriegen
    return new Value(schedule.getDate(),schedule.getContext().getBetrag());
  }
}
