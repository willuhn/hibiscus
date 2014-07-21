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

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
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
  /**
   * Queue, an die der aktuelle Prozess-Status der Gesamt-Synchronisierung (RUNNING, ERROR, DONE, CANCEL) geschickt wird.
   */
  public final static String STATUS = "hibiscus.syncengine.status";

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
      
      // Wir sortieren die Backends so, dass HBCI immer Vorrang hat
      Collections.sort(this.backends,new Comparator<SynchronizeBackend>() {
        public int compare(SynchronizeBackend o1, SynchronizeBackend o2)
        {
          if (o1 instanceof HBCISynchronizeBackend)
            return -1;
          if (o2 instanceof HBCISynchronizeBackend)
            return 1;
          return 0;
        }
      });
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
   * Liefert das zu der übergebenen Klassennamen passende Backend zurück
   * @param classname Klassenname des Backends
   * @return Backend
   * @throws ApplicationException Wenn Backend mit diesem Klassennamen nicht gefunden wurde
   */
  public SynchronizeBackend getBackendByClassname(String classname) throws ApplicationException {
    for (SynchronizeBackend backend:this.getBackends())
    {
      if (backend.getClass().getName().equals(classname)) {
        return backend;
      }
    }
    throw new ApplicationException(i18n.tr("Backend {0} nicht gefunden!", classname));
    
  }
  
  /**
   * Liefert ein passendes Backend fuer den angegebenen Job.
   * Das erste gefundene wird verwendet.
   * @param type der Job-Typ.
   * @param konto das Konto, fuer das der Job erzeugt werden soll.
   * @return die Instanz des Backend.
   * @throws ApplicationException wenn kein Backend gefunden wurde.
   */
  public SynchronizeBackend getBackend(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    // Wenn im Konto eine Backendclass gesetzt ist, diese nutzen ...
    try
    {
      if (konto.getBackendClass() != null && !konto.getBackendClass().isEmpty()) {
          return getBackendByClassname(konto.getBackendClass());
      }
    } catch (RemoteException e)
    {
      throw new ApplicationException(i18n.tr("Fehler  beim Zugriff auf das Backend."), e);
    }
    // ... ansonsten nach einer Backend-Klasse suchen, die den Typ unterstützt
    for (SynchronizeBackend backend:this.getBackends())
    {
      if (backend.supports(type,konto)) {
        Logger.warn("Gewähltes Backend: " + konto.toString() + " " + backend.getClass());
        return backend;
        
      }
    }
    
    throw new ApplicationException(i18n.tr("Dieser Geschäftsvorfall wird für das angegebene Konto nicht unterstützt"));
  }
  
  /**
   * Liefert true, wenn ein Backend den angegebenen Job-Typ fuer das angegebene Konto unterstuetzt.
   * @param type der zu pruefende Job-Typ.
   * @param konto das Konto.
   * @return true, wenn es ihn unterstuetzt, sonst false.
   */
  public boolean supports(Class<? extends SynchronizeJob> type, Konto konto)
  {
    try
    {
      this.getBackend(type,konto);
      return true;
    }
    catch (ApplicationException ae)
    {
      return false;
    }
  }
}
