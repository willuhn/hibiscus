/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/forecast/ForecastProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/27 17:10:02 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Interface fuer einen Provider, der Prognose-Daten fuer kuenftige
 * Zahlungen liefert. 
 */
public interface ForecastProvider
{
  /**
   * Liefert einen sprechenden Namen fuer den Provider.
   * @return sprechender Name.
   */
  public String getName();
  
  /**
   * Liefert die voraussichtlichen Zahlungen fuer den angegebenen Zeitraum.
   * @param k das Konto. Wenn es fehlt, sollte der Provider die Zahlungen
   * aller Konten liefern.
   * @param from Beginn des Zeitraumes (inclusive). Das Datum ist
   * immer angegeben. Die Implementierung muss hier also nicht auf NULL pruefen.
   * @param to Ende des Zeitraumes (inclusive). Das Datum ist
   * immer angegeben. Die Implementierung muss hier also nicht auf NULL pruefen.
   * @return Liste der voraussichtlichen Zahlungen.
   * @throws Exception
   */
  public List<Value> getData(Konto k, Date from, Date to) throws Exception;
}



/**********************************************************************
 * $Log: ForecastProvider.java,v $
 * Revision 1.1  2011/10/27 17:10:02  willuhn
 * @N Erster Code fuer die Forecast-API - Konto-Prognose
 *
 **********************************************************************/