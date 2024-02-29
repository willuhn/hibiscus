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
   * Liefert die voraussichtlichen Zahlungen beginnend mit heute und endend mit dem angegeben Datum.
   * @param k das Konto. Wenn es fehlt, sollte der Provider die Zahlungen
   * aller Konten liefern.
   * @param to Ende des Zeitraumes (inclusive). Das Datum ist
   * immer angegeben. Die Implementierung muss hier also nicht auf NULL pruefen.
   * @return Liste der voraussichtlichen Zahlungen.
   * @throws Exception
   */
  public List<Value> getData(Konto k, Date to) throws Exception;
  
  /**
   * Liefert true, wenn der Provider per Default aktiv sein soll.
   * @return true, wenn der Provider per Default aktiv sein soll.
   */
  public boolean isDefaultEnabled();
}
