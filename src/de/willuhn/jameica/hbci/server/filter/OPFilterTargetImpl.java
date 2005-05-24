/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/filter/Attic/OPFilterTargetImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/24 23:30:03 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.filter.Filter;
import de.willuhn.jameica.hbci.rmi.filter.FilterTarget;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Filter-Ziels fuer Offene Posten.
 */
public class OPFilterTargetImpl implements FilterTarget
{

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public OPFilterTargetImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.FilterTarget#getFilters()
   */
  public Filter[] getFilters() throws RemoteException
  {
    // Wir liefern genau die Offenen Posten, die noch nicht zugewiesen wurden
    DBIterator list = Settings.getDBService().createList(OffenerPosten.class);
    list.addFilter("umsatz_id is null");
    Filter[] f = new Filter[list.size()];
    int i = 0;
    while (list.hasNext())
    {
      f[i++] = (Filter) list.next();
    }
    return f;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.FilterTarget#match(de.willuhn.jameica.hbci.rmi.Umsatz, de.willuhn.jameica.hbci.rmi.filter.Filter)
   */
  public void match(Umsatz umsatz, Filter filter) throws RemoteException
  {
    if (!(filter instanceof OffenerPosten))
      return;

    Logger.info("found matching umsatz for OP filter");
    try
    {
      OffenerPosten p = (OffenerPosten) filter;
      p.setUmsatz(umsatz);
      p.store();
      Logger.info("assigned umsatz to OP [" + p.getAttribute(p.getPrimaryAttribute()) + "]");
    }
    catch (ApplicationException e)
    {
      Logger.error("unable to assign umsatz to OP",e);
    }
  }

}


/**********************************************************************
 * $Log: OPFilterTargetImpl.java,v $
 * Revision 1.1  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 **********************************************************************/