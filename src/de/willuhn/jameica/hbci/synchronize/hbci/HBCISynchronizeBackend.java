/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider;
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
public class HBCISynchronizeBackend extends AbstractSynchronizeBackend
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getName()
   */
  public String getName()
  {
    return "HBCI";
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
  protected Class<? extends SynchronizeJobProvider> getJobProviderInterface()
  {
    return HBCISynchronizeJobProvider.class;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getSynchronizeKonten(de.willuhn.jameica.hbci.rmi.Konto)
   */
  protected List<Konto> getSynchronizeKonten(Konto k)
  {
    List<Konto> list = super.getSynchronizeKonten(k);
    List<Konto> result = new ArrayList<Konto>();
    
    // Wir wollen nur die Online-Konten haben
    for (Konto konto:list)
    {
      try
      {
        if (!konto.hasFlag(Konto.FLAG_OFFLINE))
          result.add(konto);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine flags of konto",re);
      }
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
    try
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_OFFLINE) || konto.hasFlag(Konto.FLAG_DISABLED))
        return false;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine support for job type " + type,re);
      return false;
    }
    
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
        if (konto.hasFlag(Konto.FLAG_OFFLINE))
          throw new ApplicationException(i18n.tr("Das Konto ist ein Offline-Konto: {0}",konto.getLongName()));
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
   * Hilfsklasse, um die Jobs nach Konten zu gruppieren. 
   */
  protected class HBCIJobGroup extends JobGroup implements Closeable
  {
    private List<AbstractHBCIJob> hbciJobs = new ArrayList<AbstractHBCIJob>();
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
      
      // Wir ermitteln anhand der Gesamt-Anzahl von Jobs, wieviel Fortschritt
      // pro Job gemacht wird, addieren das fuer unsere Gruppe, ziehen noch
      // einen Teil fuer Passport-Initialisierung ab (3%) sowie 3% fuer die Job-Auswertung
      // und geben den Rest den Jobs in unserer Gruppe. Wir rechnen am Anfang erstmal mit Double,
      // um die Rundungsdifferenzen etwas kleiner zu halten
      double chunk = 100d / ((double) HBCISynchronizeBackend.this.worker.getSynchronization().size()) * ((double)this.jobs.size());
      int step     = (int) ((chunk - 6) / this.jobs.size());
      ////////////////////////////////////////////////////////////////////

      boolean haveError = false;
      boolean inCatch   = false;

      try
      {
        this.checkInterrupted();

        monitor.log(" ");
        monitor.log(i18n.tr("Synchronisiere Konto: {0}",this.getKonto().getLongName()));

        Passport passport     = new TaskPassportInit().execute();
        this.handle           = new TaskHandleInit(passport).execute();
        this.handler          = new TaskHandleOpen(handle).execute();
  
        Logger.info("processing jobs");
        for (SynchronizeJob job:this.jobs)
        {
          this.checkInterrupted();
          AbstractHBCIJob[] list = ((HBCISynchronizeJob)job).createHBCIJobs();
          for (AbstractHBCIJob hbciJob:list)
          {
            this.checkInterrupted();
            
            monitor.setStatusText(i18n.tr("Aktiviere HBCI-Job: \"{0}\"",job.getName()));
            Logger.info("adding job " + hbciJob.getIdentifier() + " to queue");
            
            HBCIJob j = handler.newJob(hbciJob.getIdentifier());
            this.dumpJob(j);
            hbciJob.setJob(j);
            j.addToQueue();
            this.hbciJobs.add(hbciJob);
            if (hbciJob.isExclusive())
            {
              Logger.info("job will be executed in seperate hbci message");
              handler.newMsg();
            }
          }
          monitor.addPercentComplete(step);
        }

        ////////////////////////////////////////////////////////////////////////
        // Jobs ausfuehren
        Logger.info("executing jobs");
        monitor.setStatusText(i18n.tr("Führe HBCI-Jobs aus"));
        this.handler.execute();
        monitor.setStatusText(i18n.tr("HBCI-Jobs ausgeführt"));
        //
        ////////////////////////////////////////////////////////////////////////
      }
      catch (Exception e)
      {
        haveError = true;
        inCatch = true;
        throw e;
      }
      finally
      {
        try
        {
          String name = null;
          
          // Waehrend der Ergebnis-Auswertung findet KEIN "checkInterrupted" Check statt,
          // da sonst Job-Ergebnisse verloren gehen wuerden.

          // //////////////////////////////////////////////////////////////////////
          // Job-Ergebnisse auswerten.
          // checkInterrupted wird hier nicht aufgerufen, um sicherzustellen, dass
          // dieser Vorgang nicht abgebrochen wird.
          for (AbstractHBCIJob hbciJob:this.hbciJobs)
          {
            try
            {
              name = hbciJob.getName();
              monitor.setStatusText(i18n.tr("Werte Ergebnis von HBCI-Job \"{0}\" aus",name));
              Logger.info("executing check for job " + hbciJob.getIdentifier());
              hbciJob.handleResult();
            }
            catch (Throwable t)
            {
              haveError = true;
              
              // Nur loggen, wenn wir nicht abgebrochen wurden. Waeren sonst nur Folgefehler
              if (!HBCISynchronizeBackend.this.worker.isInterrupted())
              {
                if (t instanceof ApplicationException)
                {
                  monitor.setStatusText(t.getMessage());
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
          
          monitor.addPercentComplete(3);

          if (haveError || HBCISynchronizeBackend.this.worker.isInterrupted())
          {
            Logger.warn("found errors or synchronization cancelled, clear PIN cache");
            DialogFactory.clearPINCache(this.handler != null ? this.handler.getPassport() : null);
          }
          
          if (!inCatch)
          {
            // Fehler nur werfen, wenn wir nicht abgebrochen wurden - in dem Fall
            // werfen die handleResult-Funktionen naemlich ohnehin Fehler. Die
            // interessieren beim Abbruch aber nicht.
            // Der Abbruch-Check kommt unten drunter
            if (haveError && !HBCISynchronizeBackend.this.worker.isInterrupted())
              throw new ApplicationException(i18n.tr("Fehler beim Auswerten eines HBCI-Auftrages"));
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
     * Gibt Informationen ueber den Job im Log aus.
     * @param job Job.
     */
    private void dumpJob(HBCIJob job)
    {
      Logger.debug("Job restrictions for " + job.getName());
      Properties p = job.getJobRestrictions();
      Iterator it = p.keySet().iterator();
      while (it.hasNext())
      {
        String key = (String) it.next();
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
          
          monitor.setStatusText(i18n.tr("Initialisiere HBCI-Sicherheitsmedium"));
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

        monitor.setStatusText(i18n.tr("Erzeuge HBCI-Handle"));
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
          monitor.setStatusText(i18n.tr("Öffne HBCI-Verbindung"));
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


