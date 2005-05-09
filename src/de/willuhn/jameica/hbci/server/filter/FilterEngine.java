/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/filter/Attic/FilterEngine.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/05/09 23:54:41 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.filter;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.filter.Filter;
import de.willuhn.jameica.hbci.rmi.filter.FilterTarget;
import de.willuhn.jameica.hbci.rmi.filter.Pattern;
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
    private ArrayList targets        = null;

  private FilterEngine()
  {
    Logger.info("init umsatz filter engine");
    
    Logger.info("searching for filter targets");
    ClassFinder finder = Application.getClassLoader().getClassFinder();
    try
    {
      Class[] classes = finder.findImplementors(FilterTarget.class);
      if (classes == null || classes.length == 0)
      {
        Logger.warn("no filter targets found");
        return;
      }
      this.targets = new ArrayList();
      for (int i=0;i<classes.length;++i)
      {
        try
        {
          this.targets.add((FilterTarget) classes[i].newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load filter target " + classes[i].getName() + ", skipping",e);
        }
      }
      Logger.info("loaded " + this.targets.size() + " filter targets");
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("error while loading filter targets, filter engine will not work",e);
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
   * Filtert den Umsatz und leitet ihn bei Treffern an die entsprechenden
   * Filter-Targets weiter.
   * @param u
   * @throws RemoteException
   */
  public void filter(Umsatz u) throws RemoteException
  {
    long start = System.currentTimeMillis();
    Logger.debug("filtering " + u.getAttribute(u.getPrimaryAttribute()));
    for (int i=0;i<this.targets.size();++i)
    {

      FilterTarget target = (FilterTarget) this.targets.get(i);
      Filter[] filters    = target.getFilters();

      if (filters == null || filters.length == 0)
      {
        Logger.warn("no filters defined for target " + target.getClass().getName() + ", skipping");
        continue;
      }

      for (int j=0;j<filters.length;++j)
      {
        Filter filter = filters[j];

        Pattern[] pattern = filter.getPattern();

        if (pattern == null || pattern.length == 0)
        {
          Logger.warn("no pattern defined in filter " + filter.getClass().getName() + " for target " + target.getClass().getName() +", skipping");
          continue;
        }

        boolean match = false;

        for (int k=0;k<pattern.length;++k)
        {
          Object attribute = u.getAttribute(pattern[k].getField());
          if (attribute == null) continue;

          String s = attribute.toString();
          if (s == null) continue;

          String test = pattern[k].getPattern();
          if (test == null) continue;

          if (pattern[k].getType() == Pattern.TYPE_CONTAINS)
            test = ".*" + test + ".*";
          else if (pattern[k].getType() == Pattern.TYPE_STARTSWITH)
            test = "^" + test + ".*";
          else if (pattern[k].getType() == Pattern.TYPE_ENDSWITH)
            test = ".*" + test + "$";
          else if (pattern[k].getType() == Pattern.TYPE_EQUALS)
            test = "^" + test + "$";

          // TODO Java kann via Inlined Flag (?i) auch im regulaeren Ausdruck Gross-Kleinschreibung ignorieren.
          // Allerdings kenne ich die Syntax hierfuer nicht und weiss nicht, was schneller ist
          if (pattern[k].ignoreCase())
          {
            test = test.toUpperCase();
            s = s.toUpperCase();
          }
          match &= s.matches(test);
        }
        if (match)
        {
          Logger.info("filter match for umsatz \"" + u.getAttribute(u.getPrimaryAttribute()) + "\"");
          target.match(u,filter);
        }
      }
    }
    Logger.info("used time: " + (System.currentTimeMillis() - start) + " millis");
  }
  
}


/**********************************************************************
 * $Log: FilterEngine.java,v $
 * Revision 1.2  2005/05/09 23:54:41  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 **********************************************************************/