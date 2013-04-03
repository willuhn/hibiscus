/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeEngine.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/01/26 23:17:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Basis-Klasse fuer den Zugriff auf Synchronisierungsaufgaben bei der Bank.
 */
@Lifecycle(Type.CONTEXT)
public class SynchronizeEngine
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private List<SynchronizeBackend> backends = null;

  /**
   * Liefert eine Liste der gefundenen Backends.
   * @return Liste der Backends.
   */
  public synchronized List<SynchronizeBackend> getBackends()
  {
    if (this.backends != null)
      return this.backends;
    
    this.backends = new LinkedList<SynchronizeBackend>();
    
    try
    {
      Logger.info("loading synchronize backends");
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getClassLoader().getClassFinder().findImplementors(SynchronizeBackend.class);
      for (Class<SynchronizeBackend> c:found)
      {
        try
        {
          Logger.debug("  " + c.getName());
          this.backends.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load synchronize backend " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no synchronize backends found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching vor synchronize backends",e);
    }
    return this.backends;
  }
  
  /**
   * Liefert ein passendes Backend fuer den angegebenen Job.
   * @param type der Job-Typ.
   * @param konto das Konto, fuer das der Job erzeugt werden soll.
   * @return die Instanz des Backend.
   * @throws ApplicationException wenn kein Backend gefunden wurde.
   */
  public SynchronizeBackend getBackend(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    List<SynchronizeBackend> found = new ArrayList<SynchronizeBackend>();
    
    for (SynchronizeBackend backend:this.getBackends())
    {
      if (backend.supports(type,konto))
        found.add(backend);
    }
    
    int size = found.size();

    if (size == 0)
      throw new ApplicationException(i18n.tr("Dieser Geschäftsvorfall wird für das angegebene Konto nicht unterstützt"));
    
    if (size > 1)
      Logger.warn("found " + size + " possible backends for " + type.getSimpleName());

    // wir nehmen das erste gefundene
    return found.get(0);
  }
}
