/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/schedule/ScheduleProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/02/20 17:03:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Interface fuer einen Scheduler, der geplante Zahlungen in der Zukunft errechnen/ermitteln kann.
 * @param <T> der konkrete Typ des Auftrages.
 */
public interface ScheduleProvider<T extends HibiscusDBObject>
{
  /**
   * Liefert einen sprechenden Namen fuer den Provider.
   * @return sprechender Name fuer den Provider.
   */
  public String getName();
  
  /**
   * Liefert die Termine fuer den angegebenen Zeitraum.
   * @param k optionale Angabe eines Kontos. Ist es angegeben, werden nur Zahlungen des
   * angegebenen Kontos geliefert. Andernfalls alle Konten.
   * @param from Beginn des Zeitraumes (einschliesslich dieses Tages).
   * @param to Ende des Zeitraumes (einschliesslich dieses Tages).
   * @return Liste der gefundendenen Termine.
   * Die Funktion darf NICHT NULL liefern sondern hoechstens eine leere Liste.
   */
  public List<Schedule<T>> getSchedules(Konto k, Date from, Date to);
}



/**********************************************************************
 * $Log: ScheduleProvider.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/