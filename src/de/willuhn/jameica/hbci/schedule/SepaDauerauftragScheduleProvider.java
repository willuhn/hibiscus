/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.server.DauerauftragUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Schedule-Providers fuer anstehende SEPA-Dauerauftraege.
 */
@Lifecycle(Type.REQUEST)
public class SepaDauerauftragScheduleProvider implements ScheduleProvider<SepaDauerauftrag>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public List<Schedule<SepaDauerauftrag>> getSchedules(Konto k, Date from, Date to)
  {
    List<Schedule<SepaDauerauftrag>> result = new LinkedList<Schedule<SepaDauerauftrag>>();
    
    try
    {
      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(SepaDauerauftrag.class);
      if (k != null)
        list.addFilter("konto_id = " + k.getID());

      while (list.hasNext())
      {
        // Wir checken, ob einer der Dauerauftraege am genannten Tag
        // ausgefuehrt wird oder wurde
        SepaDauerauftrag t = (SepaDauerauftrag) list.next();
        List<Date> termine = DauerauftragUtil.getTermine(t,from,to);
        if (termine == null || termine.size() == 0)
          continue; // Keine Zahlung in dem Zeitraum

        for (Date termin:termine)
          result.add(new Schedule(termin,t,false));
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return result;
  }

  @Override
  public String getName()
  {
    return i18n.tr("SEPA-Daueraufträge");
  }
}
