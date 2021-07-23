/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
   * Das Primaer-Backend.
   */
  private final static Class<? extends SynchronizeBackend> PRIMARY = HBCISynchronizeBackend.class;

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
      Class[] found = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder().findImplementors(SynchronizeBackend.class);
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
      
      // Wir sortieren die Backends so, dass das Primaer-Backend immer Vorrang hat
      Collections.sort(this.backends, new Comparator<SynchronizeBackend>()
      {
        public int compare(SynchronizeBackend o1, SynchronizeBackend o2)
        {
          
          if (PRIMARY.isInstance(o1))
            return -1;
          if (PRIMARY.isInstance(o2))
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
   * Liefert das im Konto hinterlegte Backend.
   * @param konto das Konto.
   * @return das angegebene Backend oder NULL, wenn keines angegeben ist oder
   * das angegebene nicht gefunden wurde.
   */
  public SynchronizeBackend getBackend(Konto konto)
  {
    try
    {
      String s = konto != null ? StringUtils.trimToNull(konto.getBackendClass()) : null;
      for (SynchronizeBackend b:this.getBackends())
      {
        if (s != null && s.equals(b.getClass().getName()))
          return b;
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine backend for konto",re);
    }
    
    return null;
  }
  
  /**
   * Liefert das Primaer-Backend.
   * @return das Primaer-Backend.
   */
  public SynchronizeBackend getPrimary()
  {
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    return service.get(PRIMARY);
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
    // Wenn im Konto ein Backend hinterlegt ist, dan dieses verwenden
    SynchronizeBackend bk = this.getBackend(konto);
    if (bk != null)
    {
      
      // Checken, ob das Backend den Geschaeftsvorfall ueberhaupt unterstuetzt
      // TODO: Ich bin mir nicht sicher, ob auch bei einem explizit gewaehlten Backend geprueft
      // werden sollte, ob das Backend den Geschaeftsvorfall unterstuetzt.
//      if (!bk.supports(type,konto))
//        throw new ApplicationException(i18n.tr("Das Zugangsverfahren des Kontos unterstützt diesen Geschäftsvorfall nicht"));
      return bk;
    }

    // Ansonsten das erste nehmen, welches den Geschaeftsvorfall fuer das Konto unterstuetzt
    for (SynchronizeBackend backend:this.getBackends())
    {
      if (backend.supports(type,konto))
        return backend;
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
