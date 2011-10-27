/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/forecast/ForecastProviderDauerauftrag.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/27 17:10:02 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.DauerauftragUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Forecast-Providers fuer Dauerauftraege.
 */
public class ForecastProviderDauerauftrag implements ForecastProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Daueraufträge");
  }

  /**
   * @see de.willuhn.jameica.hbci.forecast.ForecastProvider#getData(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date)
   */
  public List<Value> getData(Konto k, Date from, Date to) throws Exception
  {
    DBIterator list = Settings.getDBService().createList(Dauerauftrag.class);
    if (k != null) list.addFilter("konto_id = " + k.getID());

    List<Value> result = new ArrayList<Value>();
    while (list.hasNext())
    {
      Dauerauftrag d = (Dauerauftrag) list.next();
      List<Date> dates = DauerauftragUtil.getTermine(d,from,to);
      double betrag = -d.getBetrag();
      for (Date date:dates)
      {
        result.add(new Value(date,betrag));
      }
    }
    return result;
  }
}



/**********************************************************************
 * $Log: ForecastProviderDauerauftrag.java,v $
 * Revision 1.1  2011/10/27 17:10:02  willuhn
 * @N Erster Code fuer die Forecast-API - Konto-Prognose
 *
 **********************************************************************/