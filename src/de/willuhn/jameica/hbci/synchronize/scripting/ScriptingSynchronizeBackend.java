/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.scripting;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Backend, welches Konten via Scripting anbindet.
 */
@Lifecycle(Type.CONTEXT)
public class ScriptingSynchronizeBackend extends AbstractSynchronizeBackend<ScriptingSynchronizeJobProvider>
{
  /**
   * Der Context-Name fuer den Javascript-Funktionsnamen.
   */
  private final static String CTX_JS_FUNCTION = "ctx.js.function";
  
  @Resource
  private SynchronizeEngine engine = null;

  @Override
  public String getName()
  {
    return "Scripting";
  }

  @Override
  protected Class<ScriptingSynchronizeJobProvider> getJobProviderInterface()
  {
    return ScriptingSynchronizeJobProvider.class;
  }

  @Override
  protected de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.JobGroup createJobGroup(Konto k)
  {
    return new ScriptingJobGroup(k);
  }

  @Override
  public List<Konto> getSynchronizeKonten(Konto k)
  {
    List<Konto> list = super.getSynchronizeKonten(k);
    List<Konto> result = new ArrayList<Konto>();
    
    // Wir wollen nur die Offline-Konten und jene, bei denen Scripting explizit konfiguriert ist
    for (Konto konto:list)
    {
      if (this.supports(konto))
        result.add(konto);
    }
    
    return result;
  }
  
  @Override
  public <T> T create(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    // 1. Checken, ob wir ueberhaupt ein Script haben, welches diesen Job beherrscht
    String function = this.getFunction(type,konto);
    if (function == null)
    {
      Logger.warn("job type " + type.getSimpleName() + " not supported by " + this.getName());
      throw new ApplicationException(i18n.tr("Der Gesch�ftsvorfall wird nicht via {0} unterst�tzt",this.getName()));
    }

    SynchronizeJob instance = super.create(type,konto);
    instance.setContext(CTX_JS_FUNCTION,function); // Funktion noch drin speichern
    return (T) instance;
  }

  @Override
  public boolean supports(Class<? extends SynchronizeJob> type, Konto konto)
  {
    // 1. Haben wir ein Script, welches diesen Typ unterstuetzt?
    if (this.getFunction(type,konto) == null)
      return false;

    return super.supports(type,konto);
  }
  
  @Override
  public synchronized SynchronizeSession execute(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException
  {
    try
    {
      // Wir checken extra noch, ob es wirklich alles Offline-Konten sind oder ob bei denen das Scripting ausgewaehlt wurde
      for (SynchronizeJob job:jobs)
      {
        Konto konto = job.getKonto();
        if (!this.supports(konto))
          throw new ApplicationException(i18n.tr("Das Konto ist kein Offline-Konto oder das Zugangsverfahren {0} wurde nicht ausgew�hlt: {1}",this.getName(),konto.getLongName()));
      }
    }
    catch (RemoteException re)
    {
      Logger.error("error while performing synchronization",re);
      throw new ApplicationException(i18n.tr("Synchronisierung fehlgeschlagen: {0}",re.getMessage()));
    }

    return super.execute(jobs);
  }
  
  /**
   * Prueft, ob das Konto prinzipiell unterstuetzt wird.
   * @param konto das Konto.
   * @return true, wenn es prinzipiell unterstuetzt wird.
   */
  boolean supports(Konto konto)
  {
    if (konto == null)
      return false;
    
    try
    {
      SynchronizeBackend backend = engine.getBackend(konto);
      return konto.hasFlag(Konto.FLAG_OFFLINE) || (backend != null && backend.equals(this));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine synchronization support for konto",re);
    }
    return false;
  }
  
  @Override
  public List<String> getPropertyNames(Konto konto)
  {
    try
    {
      // Nur Offline-Konten.
      if (konto == null || !this.supports(konto) || konto.hasFlag(Konto.FLAG_DISABLED))
        return null;
      
      QueryMessage msg = new QueryMessage("hibiscus.sync.options",konto);
      Application.getMessagingFactory().getMessagingQueue("jameica.scripting").sendSyncMessage(msg);
      Object data = msg.getData();
      if (data == null)
      {
        Logger.debug("no property names found");
        return null;
      }
      
      List<String> result = new ArrayList<String>();
      List list = (data instanceof List) ? (List) data : Arrays.asList(data);
      for (Object o:list)
      {
        if (o instanceof Exception) // eines der Scripte hat eine Exception geworfen
        {
          // brauchen wir nicht loggen. Hat der JS-Invoker schon gemacht
          continue;
        }
        
        if (o instanceof String)
        {
          result.add((String)o);
        }
      }
      
      return result;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine property-names",re);
      return null;
    }
  }

  /**
   * Liefert den auszufuehrenden Javascript-Funktionsnamen fuer den angegebenen Job.
   * @param type der Job-Typ.
   * @param konto das Konto.
   * @return der Javascript-Funktionsname oder NULL, wenn kein Script diesen Job unterstuetzt.
   */
  private String getFunction(Class<? extends SynchronizeJob> type, Konto konto)
  {
    try
    {
      // 1. Nur Offline-Konten und jene, bei denen explizite Scripting ausgewaehl ist.
      if (konto == null || !this.supports(konto) || konto.hasFlag(Konto.FLAG_DISABLED))
        return null;

      Logger.debug("searching javascript function for job type " + type.getSimpleName());
      
      // 2. Checken, ob wir ein passendes Script haben
      // Das Javascript liefert den Namen der auszufuehrenden JS-Funktion zurueck
      QueryMessage msg = new QueryMessage("hibiscus.sync.function",new Object[]{konto,type});
      Application.getMessagingFactory().getMessagingQueue("jameica.scripting").sendSyncMessage(msg);
      Object data = msg.getData();
      if (data != null)
      {
        List result = (data instanceof List) ? (List) data : Arrays.asList(data);
        for (Object o:result)
        {
          if (o instanceof Exception) // eines der Scripte hat eine Exception geworfen
          {
            // brauchen wir nicht loggen. Hat der JS-Invoker schon gemacht
            continue;
          }
          
          if (o instanceof String)
          {
            Logger.debug("found " + o);
            return (String) o; // wir haben einen passenden Funktionsnamen
          }
        }
      }
      Logger.debug("no javascript function found");
      return null;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine support for job type " + type,re);
      return null;
    }
  }
  
  /**
   * Unsere Scripting-basierte Implementierung.
   */
  protected class ScriptingJobGroup extends JobGroup
  {
    /**
     * ct.
     * @param k das Konto.
     */
    protected ScriptingJobGroup(Konto k)
    {
      super(k);
    }

    @Override
    protected void sync() throws Exception
    {
      ////////////////////////////////////////////////////////////////////
      // lokale Variablen
      ProgressMonitor monitor = worker.getMonitor();
      String kn               = this.getKonto().getLongName();
      
      int step = 100 / worker.getSynchronization().size();
      ////////////////////////////////////////////////////////////////////
      
      // boolean haveError = false;

      try
      {
        this.checkInterrupted();

        monitor.log(" ");
        monitor.log(i18n.tr("Synchronisiere Konto: {0}",kn));

        Logger.info("processing jobs");
        for (SynchronizeJob job:this.jobs)
        {
          this.checkInterrupted();

          String function = (String) job.getContext(CTX_JS_FUNCTION);
          if (StringUtils.isEmpty(function))
            throw new ApplicationException(i18n.tr("Kein g�ltiger Scripting-Auftrag: {0}",job.getName()));
          
          Logger.info("executing javascript function " + function);
          QueryMessage msg = new QueryMessage("function." + function,new Object[]{job,getCurrentSession()}); // Direkt-Aufruf - ohne Event-Mapping
          Application.getMessagingFactory().getMessagingQueue("jameica.scripting").sendSyncMessage(msg);
          monitor.addPercentComplete(step);
          
          // Checken, ob der Rueckgabewert eine Exception ist
          Object data = msg.getData();
          List list = (data instanceof List) ? ((List)data) : Arrays.asList(data);
          for (Object o:list)
          {
            if (o instanceof Exception)
              throw (Exception) o;
          }
        }
      }
      catch (Exception e)
      {
        // haveError = true;
        throw e;
      }
      finally
      {
        // TODO: PIN-Cache leeren geht hier noch nicht, weil der nur mit HBCIHandlern umgehen kann
        // Das sollte ohnehin besser ueber den ApplicationCallback gehen - damit ist das auch
        // gleich Server-tauglich
//        if (haveError || ScriptingSynchronizeBackend.this.worker.isInterrupted())
//        {
//          Logger.warn("found errors or synchronization cancelled, clear PIN cache");
//          DialogFactory.clearPINCache();
//        }
      }
    }
  }
}


