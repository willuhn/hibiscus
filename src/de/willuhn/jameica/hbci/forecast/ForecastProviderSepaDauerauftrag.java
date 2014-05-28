/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer SEPA-Dauerauftraege.
 */
public class ForecastProviderSepaDauerauftrag extends AbstractForecastProvider<SepaDauerauftrag>
{
  /**
   * @see de.willuhn.jameica.hbci.forecast.AbstractForecastProvider#createValue(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  Value createValue(Schedule<SepaDauerauftrag> schedule) throws RemoteException
  {
    // Negativ-Betrag verwenden
    return new Value(schedule.getDate(),-schedule.getContext().getBetrag());
  }
}
