/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Synchronize-Backend fuer HBCI.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeBackend extends AbstractSynchronizeBackend<HBCISynchronizeJobProvider>
{
  /**
   * Queue, ueber die die rohen HBCI-Nachrichten getraced werden koennen.
   */
  public final static String HBCI_TRACE = "hibiscus.sync.hbci.trace";

  @Resource
  private SynchronizeEngine engine = null;

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getName()
   */
  public String getName()
  {
    return "FinTS/HBCI";
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#createJobGroup(de.willuhn.jameica.hbci.rmi.Konto)
   */
  protected de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.JobGroup createJobGroup(Konto k)
  {
    return new HBCIJobGroup(k);
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getJobProviderInterface()
   */
  protected Class<HBCISynchronizeJobProvider> getJobProviderInterface()
  {
    return HBCISynchronizeJobProvider.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getSynchronizeKonten(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<Konto> getSynchronizeKonten(Konto k)
  {
    List<Konto> list = super.getSynchronizeKonten(k);
    List<Konto> result = new ArrayList<Konto>();

    // Wir wollen nur die Online-Konten haben
    for (Konto konto:list)
    {
      if (this.supports(konto))
        result.add(konto);
    }

    return result;
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#create(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   */
  public <T> T create(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    try
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_OFFLINE) || konto.hasFlag(Konto.FLAG_DISABLED))
        throw new ApplicationException(i18n.tr("Das Konto ist ein Offline-Konto oder deaktiviert"));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to check konto flags",re);
      throw new ApplicationException(i18n.tr("Der Geschäftsvorfall konnte nicht erstellt werden: {0}",re.getMessage()));
    }

    // aufgrund eines Bugs im SUN compiler muessen wir hier explizit casten.
    // Siehe https://bugs.eclipse.org/bugs/show_bug.cgi?id=98379
    // Ab Java 1.6.0_25 ist das gefixt. Aber es soll ja auch in aelteren Java-Versionen compilieren
    return(T) super.create(type,konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#supports(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   */
  public boolean supports(Class<? extends SynchronizeJob> type, Konto konto)
  {
    if (!this.supports(konto))
      return false;

    return super.supports(type,konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#execute(java.util.List)
   */
  public synchronized SynchronizeSession execute(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException
  {
    try
    {
      for (SynchronizeJob job:jobs)
      {
        Konto konto = job.getKonto();
        if (!this.supports(konto))
          throw new ApplicationException(i18n.tr("Das Konto ist ein Offline-Konto oder das Zugangsverfahren {0} wurde nicht ausgewählt: {1}",this.getName(),konto.getLongName()));
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
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getPropertyNames(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public List<String> getPropertyNames(Konto konto)
  {
    try
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_DISABLED))
        return null;
      
      List<String> result = new ArrayList<String>();
      
      // Wir fragen mal die Job-Provider
      List<HBCISynchronizeJobProvider> providers = this.getJobProviders();
      for (HBCISynchronizeJobProvider p:providers)
      {
        List<String> props = p.getPropertyNames(konto);
        if (props != null && props.size() > 0)
        {
          for (String s:props)
          {
            if (!result.contains(s))
              result.add(s);
          }
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
   * Prueft, ob das Konto prinzipiell unterstuetzt wird.
   * @param konto das Konto.
   * @return true, wenn es prinzipiell unterstuetzt wird.
   */
  private boolean supports(Konto konto)
  {
    try
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_OFFLINE) || konto.hasFlag(Konto.FLAG_DISABLED))
        return false;
      
      SynchronizeBackend backend = engine.getBackend(konto);
      return (backend == null || backend.equals(this));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine synchronization support for konto",re);
    }
    return false;
  }



  /**
   * Hilfsklasse, um die Jobs nach Konten zu gruppieren.
   */
  protected class HBCIJobGroup extends JobGroup implements Closeable
  {
    private PassportHandle handle = null;
    private HBCIHandler handler   = null;

    /**
     * ct.
     * @param k das Konto der Job-Gruppe.
     */
    private HBCIJobGroup(Konto k)
    {
      super(k);
    }

    /**
     * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.JobGroup#sync()
     */
    protected void sync() throws Exception
    {
      ////////////////////////////////////////////////////////////////////
      // lokale Variablen
      ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.getMonitor();
      
      Application.getMessagingFactory().getMessagingQueue(HBCI_TRACE).sendMessage(new HBCITraceMessage(HBCITraceMessage.Type.ID,this.getKonto().getID()));
      Application.getMessagingFactory().getMessagingQueue(HBCI_TRACE).sendMessage(new HBCITraceMessage(HBCITraceMessage.Type.INFO,"\n\n" + i18n.tr("{0} Synchronisiere Konto: {1}",HBCI.LONGDATEFORMAT.format(new Date()),this.getKonto().getLongName())));

      // Wir ermitteln anhand der Gesamt-Anzahl von Jobs, wieviel Fortschritt
      // pro Jobgroup gemacht wird, addieren das fuer unsere Gruppe, ziehen noch
      // einen Teil fuer Passport-Initialisierung ab (3%) sowie 3% fuer die Job-Auswertung
      // und geben den Rest den Jobs in unserer Gruppe. Wir rechnen am Anfang erstmal mit Double,
      // um die Rundungsdifferenzen etwas kleiner zu halten
      double chunk  = 100d / ((double) HBCISynchronizeBackend.this.worker.getSynchronization().size()) * ((double)this.jobs.size());
      double window = chunk - 6d;
      getCurrentSession().setProgressWindow(window);
      ////////////////////////////////////////////////////////////////////

      boolean ok      = true;
      boolean inCatch = false;

      try
      {
        this.checkInterrupted();

        monitor.log(" ");
        monitor.log(i18n.tr("Synchronisiere Konto: {0}",this.getKonto().getLongName()));

        Passport passport     = new TaskPassportInit().execute();
        this.handle           = new TaskHandleInit(passport).execute();
        this.handler          = new TaskHandleOpen(handle).execute();

        Logger.info("processing jobs");
        
        List<AbstractHBCIJob> hbciJobs = new ArrayList<AbstractHBCIJob>();
        for (SynchronizeJob job:this.jobs)
        {
          this.checkInterrupted();
          AbstractHBCIJob[] list = ((HBCISynchronizeJob)job).createHBCIJobs();
          monitor.setStatusText(i18n.tr("Führe Geschäftsvorfall aus: \"{0}\"",job.getName()));
          for (AbstractHBCIJob hbciJob:list)
          {
            this.checkInterrupted();
            hbciJobs.add(hbciJob);
          }
        }
        
        ok = this.executeJobs(monitor,hbciJobs,ok);
      }
      catch (Exception e)
      {
        ok = false;
        inCatch = true;
        throw e;
      }
      finally
      {
        try
        {
          Application.getMessagingFactory().getMessagingQueue(HBCI_TRACE).sendMessage(new HBCITraceMessage(HBCITraceMessage.Type.CLOSE,this.getKonto().getID()));

          monitor.addPercentComplete(3);

          final boolean interrupted = HBCISynchronizeBackend.this.worker.isInterrupted();
          if (!ok || interrupted)
          {
            Logger.warn("found errors or synchronization cancelled, mark PIN cache dirty [have error: " + (!ok) + ", interrupted: " + interrupted + "]");
            DialogFactory.dirtyPINCache(this.handler != null ? this.handler.getPassport() : null);
          }

          if (!inCatch)
          {
            // Fehler nur werfen, wenn wir nicht abgebrochen wurden - in dem Fall
            // werfen die handleResult-Funktionen naemlich ohnehin Fehler. Die
            // interessieren beim Abbruch aber nicht.
            // Der Abbruch-Check kommt unten drunter
            if (!ok && !interrupted)
              throw new ApplicationException(i18n.tr("Es sind Fehler aufgetreten"));
            //
            // //////////////////////////////////////////////////////////////////////

            // Jetzt noch die OperationCancelledException werfen, falls zwischenzeitlich abgebrochen wurde
            this.checkInterrupted();
          }
        }
        finally
        {
          IOUtil.close(this);
        }
      }
    }
    
    /**
     * Fuehrt die HBCI-Jobs aus.
     * @param monitor der Monitor.
     * @param hbciJobs die Jobs.
     * @param ok der bisherige Erfolgsstatus.
     * @return true, wenn sie erfolgreich ausgefuehrt wurden.
     * throws Exception
     */
    private boolean executeJobs(ProgressMonitor monitor, List<AbstractHBCIJob> hbciJobs, boolean ok) throws Exception
    {
      try
      {
        monitor.setStatusText(i18n.tr("Führe Aufträge aus..."));

        for (AbstractHBCIJob hbciJob:hbciJobs)
        {
          this.checkInterrupted();

          Logger.info("adding job " + hbciJob.getIdentifier() + " to queue");

          HBCIJob j = handler.newJob(hbciJob.getIdentifier());
          this.dumpJob(j);
          hbciJob.setJob(j);
          j.addToQueue();
          if (hbciJob.isExclusive())
          {
            Logger.info("job will be executed in seperate hbci message");
            handler.newMsg();
          }
        }

        ////////////////////////////////////////////////////////////////////////
        // Jobs ausfuehren
        Logger.info("executing jobs");
        this.handler.execute();
        monitor.setStatusText(i18n.tr("Aufträge ausgeführt"));
        //
        ////////////////////////////////////////////////////////////////////////
      }
      finally
      {
        ////////////////////////////////////////////////////////////////////////
        // Job-Ergebnisse auswerten.
        // Waehrend der Ergebnis-Auswertung findet KEIN "checkInterrupted" Check statt,
        // da sonst Job-Ergebnisse verloren gehen wuerden.
        List<AbstractHBCIJob> followers = new ArrayList<AbstractHBCIJob>();
        for (AbstractHBCIJob hbciJob:hbciJobs)
        {
          String name = null;
          try
          {
            name = hbciJob.getName();
            Logger.info("executing check for job " + hbciJob.getIdentifier());
            hbciJob.handleResult();

            // Checken, ob wir Nachfolge-Jobs haben
            List<AbstractHBCIJob> follower = hbciJob.getFollowerJobs();
            if (follower != null && follower.size() > 0)
              followers.addAll(follower);
          }
          catch (Throwable t)
          {
            ok = false;
            final boolean interrupted = HBCISynchronizeBackend.this.worker.isInterrupted();
            
            // Nur loggen, wenn wir nicht abgebrochen wurden. Waeren sonst nur Folgefehler
            // Im Debug-Log erscheint es aber trotzdem
            Logger.write(Level.DEBUG,"error while processing job result, have error: " + (!ok) + ", interrupted: " + interrupted,t);
            if (!interrupted)
            {
              if (t instanceof ApplicationException)
              {
                monitor.setStatusText(t.getMessage());
                Logger.warn(t.getMessage());
                Logger.write(Level.DEBUG,"stacktrace for debugging purpose",t);
              }
              else
              {
                monitor.setStatusText(i18n.tr("Fehler beim Auswerten des HBCI-Auftrages {0}", name));
                Logger.error("error while processing job result",t);
                monitor.log(t.getMessage());
              }
            }
          }
        }

        if (followers != null && followers.size() > 0)
        {
          Logger.info("executing follower jobs");
          this.executeJobs(monitor, followers, ok);
        }
      }
      
      return ok;
    }

    /**
     * Gibt Informationen ueber den Job im Log aus.
     * @param job Job.
     */
    private void dumpJob(HBCIJob job)
    {
      Logger.debug("Job restrictions for " + job.getName());
      Properties p = job.getJobRestrictions();
      for (Object o : p.keySet()) {
        String key = (String) o;
        Logger.debug("  " + key + ": " + p.getProperty(key));
      }
    }

    /**
     * Schliesst die waehrend der Ausfuehrung geoeffneten Ressourcen.
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException
    {
      Logger.info("closing resources");
      if (this.handle != null)
      {
        try
        {
          this.handle.close();
        }
        catch (Throwable t)
        {
          Logger.write(Level.DEBUG,"unable to close handle",t);
        }
      }
    }

    /**
     * Task fuer die Initialisierung des Passport.
     */
    private class TaskPassportInit extends AbstractTaskWrapper<Passport>
    {
      /**
       * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.HBCIJobGroup.AbstractTaskWrapper#internalExecute()
       */
      public Passport internalExecute() throws Throwable
      {
        checkInterrupted();

        ////////////////////////////////////////////////////////////////////
        // lokale Variablen
        ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.getMonitor();
        Konto konto             = getKonto();
        ////////////////////////////////////////////////////////////////////

        try
        {
          Passport passport = PassportRegistry.findByClass(konto.getPassportClass());
          if (passport == null)
            throw new ApplicationException(i18n.tr("Kein HBCI-Sicherheitsmedium für das Konto gefunden"));

          monitor.setStatusText(i18n.tr("Initialisiere Bank-Zugang"));
          passport.init(konto);
          monitor.addPercentComplete(1);

          return passport;
        }
        catch (Exception e)
        {
          throw HBCIProperties.getCause(e);
        }
      }
    }

    /**
     * Task fuer die Initialisierung des Handle.
     */
    private class TaskHandleInit extends AbstractTaskWrapper<PassportHandle>
    {
      private Passport passport = null;

      /**
       * ct.
       * @param passport
       */
      private TaskHandleInit(Passport passport)
      {
        this.passport = passport;
      }

      /**
       * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.HBCIJobGroup.AbstractTaskWrapper#internalExecute()
       */
      public PassportHandle internalExecute() throws Throwable
      {
        checkInterrupted();

        ////////////////////////////////////////////////////////////////////
        // lokale Variablen
        ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.getMonitor();
        ////////////////////////////////////////////////////////////////////

        PassportHandle handle = this.passport.getHandle();

        if (handle == null)
          throw new ApplicationException(i18n.tr("Fehler beim Erzeugen der HBCI-Verbindung"));

        monitor.addPercentComplete(1);
        return handle;
      }
    }

    /**
     * Task fuer das Oeffnen des Handle.
     */
    private class TaskHandleOpen extends AbstractTaskWrapper<HBCIHandler>
    {
      private PassportHandle handle = null;

      /**
       * ct.
       * @param handle
       */
      private TaskHandleOpen(PassportHandle handle)
      {
        this.handle = handle;
      }

      /**
       * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.HBCIJobGroup.AbstractTaskWrapper#internalExecute()
       */
      public HBCIHandler internalExecute() throws Throwable
      {
        checkInterrupted();

        ////////////////////////////////////////////////////////////////////
        // lokale Variablen
        ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.getMonitor();
        ////////////////////////////////////////////////////////////////////

        try
        {
          HBCIHandler handler = this.handle.open();

          if (handler == null)
            throw new ApplicationException(i18n.tr("Fehler beim Öffnen der HBCI-Verbindung"));

          monitor.addPercentComplete(1);
          return handler;
        }
        catch (Exception e)
        {
          throw HBCIProperties.getCause(e);
        }
      }
    }


    /**
     * Wrappt einen Task als Runnable, damit es je nach Laufzeit-Umgebung direkt oder im GUI-Thread ausgefuehrt werden kann.
     */
    private abstract class AbstractTaskWrapper<T> implements Runnable
    {
      private Exception exception = null;
      private T result            = null;

      /**
       * Fuehrt den Task je nach Laufzeit-Umgebung passend aus.
       * @return der Rueckgabewert des Tasks.
       * @throws Exception
       */
      T execute() throws Exception
      {
        if (Application.inServerMode())
          this.run();
        else
          GUI.getDisplay().syncExec(this);

        if (this.exception != null)
          throw this.exception;

        return this.result;
      }

      /**
       * Fuehrt den Task aus.
       * @return T der Rueckgabewert des Tasks.
       * @throws Throwable
       */
      protected abstract T internalExecute() throws Throwable;

      /**
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run()
      {
        try {
          this.result = this.internalExecute();
        }
        catch (Exception e) { // wir fangen nur Exceptions, keine Errors
          this.exception = e;
        }
        catch (Throwable t) {
          if (t instanceof Error)
            throw (Error) t;
          throw new Error(t);
        }
      }
    }
  }
}


