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
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Forecast-Providers fuer SEPA-Dauerauftraege.
 */
public class ForecastProviderSepaDauerauftrag extends AbstractForecastProvider<SepaDauerauftrag>
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(ForecastProvider.class);
  
  /**
   * @see de.willuhn.jameica.hbci.forecast.AbstractForecastProvider#getData(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date)
   */
  @Override
  public List<Value> getData(Konto k, Date to) throws Exception
  {
    if (!settings.getBoolean("forecast.dauerauftrag",true))
      return null;
    
    return super.getData(k, to);
  }

  @Override
  Value createValue(Schedule<SepaDauerauftrag> schedule) throws RemoteException
  {
    // Negativ-Betrag verwenden
    return new Value(schedule.getDate(),-schedule.getContext().getBetrag());
  }
}
