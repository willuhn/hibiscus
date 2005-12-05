/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/FilterEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/05 17:20:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.kapott.hbci.GV_Result.GVRKUms;

import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.filter.UmsatzFilter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Zentrale Filter-Engine fuer Umsaetze.
 * Beim Abrufen der Konto-Umsaetze werden alle neu hinzugekommenen
 * Umsaetze durch diese Engine geschleust. Hibiscus-Module koennen
 * diese Filter-Engine nutzen, wenn sie ueber den Empfaeng von
 * Umsaetzen informiert werden wollen, die den gewuenschten Kriterien
 * entsprechen. Dies ist unter anderem die Offene-Posten-Verwaltung
 * und die automatische Kategorisierung von Umsaetzen. Um als Umsatz-
 * Filter von der Engine erkannt zu werden genuegt es, wenn das
 * Interface <code>de.willuhn.jameica.hibiscus.rmi.filter.FilterTarget</code>
 * implementiert wird. Beim Initialisieren laedt die Engine automatisch
 * alle Implementationen und uebergibt ihnen die Umsaetze, welche
 * den jeweils gewuenschten Kriterien entsprechen.
 * Wichtig: Die Implementierungen muessen einen parameterlosen
 * oeffentlichen Konstruktor haben, um instanziiert werden zu koennen.
 */
public class FilterEngine
{
  private static FilterEngine engine = null;
    private ArrayList filters        = null;
    private boolean enabled          = true;

  private FilterEngine()
  {
    Logger.info("init umsatz filter engine");
    
    Logger.info("searching for filter targets");
    ClassFinder finder = Application.getClassLoader().getClassFinder();
    try
    {
      Class[] classes = finder.findImplementors(UmsatzFilter.class);
      if (classes == null || classes.length == 0)
      {
        Logger.warn("no filter targets found");
        return;
      }
      this.filters = new ArrayList();
      for (int i=0;i<classes.length;++i)
      {
        try
        {
          this.filters.add((UmsatzFilter) classes[i].newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load filter target " + classes[i].getName() + ", skipping",e);
        }
      }
      Logger.info("loaded " + this.filters.size() + " filter targets");
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no filter targets found, filter engine disabled");
      this.enabled = false;
    }
  }

  /**
   * Liefert die Instanz der Filter-Engine.
   * @return Instanz.
   */
  public synchronized static FilterEngine getInstance()
  {
    if (engine == null)
      engine = new FilterEngine();

    return engine;
  }

  /**
   * Filter einen einzelnen Umsatz durch die registrierten Filter.
   * @param u
   * @param rawData die HBCI4Java-Rohdaten.
   * @throws RemoteException
   */
  public void filter(Umsatz u, GVRKUms.UmsLine rawData) throws RemoteException
  {
    if (!this.enabled)
      return;

    long start = System.currentTimeMillis();
    Logger.debug("filtering " + u.getAttribute(u.getPrimaryAttribute()));
    for (int i=0;i<this.filters.size();++i)
    {
      UmsatzFilter filter = (UmsatzFilter) this.filters.get(i);
      try
      {
        filter.filter(u,rawData);
      }
      catch (Throwable t)
      {
        Logger.error("error while applying umsatz filter " + filter.getClass().getName() + " - skipping");
      }
    }
    Logger.info("used time: " + (System.currentTimeMillis() - start) + " millis");
  }
  
}


/**********************************************************************
 * $Log: FilterEngine.java,v $
 * Revision 1.1  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.5  2005/06/27 22:27:53  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 * Revision 1.3  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.2  2005/05/09 23:54:41  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 **********************************************************************/