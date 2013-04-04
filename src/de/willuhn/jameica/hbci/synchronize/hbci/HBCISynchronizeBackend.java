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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Synchronize-Backend fuer HBCI.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeBackend implements SynchronizeBackend
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Map<Class<? extends SynchronizeJob>,Class<? extends HBCISynchronizeJob>> jobs = null;
  private List<HBCISynchronizeJobProvider> providers = null;
  private HBCISynchronizeSession session = null;
  private Worker worker = null;
  
  private final static Map<Integer,String> statusMap = new HashMap<Integer,String>()
  {{
    put(ProgressMonitor.STATUS_CANCEL, "CANCEL");
    put(ProgressMonitor.STATUS_DONE,   "DONE");
    put(ProgressMonitor.STATUS_ERROR,  "ERROR");
    put(ProgressMonitor.STATUS_NONE,   "NONE");
    put(ProgressMonitor.STATUS_RUNNING,"RUNNING");
  }};

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getName()
   */
  public String getName()
  {
    return "HBCI";
  }
  
  /**
   * Liefert unsere Job-Provider.
   * @return unsere Job-Provider.
   */
  private synchronized List<HBCISynchronizeJobProvider> getJobProviders()
  {
    if (this.providers != null)
      return this.providers;
    
    this.providers = new ArrayList<HBCISynchronizeJobProvider>();

    try
    {
      Logger.info("loading hbci synchronize providers");
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getClassLoader().getClassFinder().findImplementors(HBCISynchronizeJobProvider.class);
      for (Class<HBCISynchronizeJobProvider> c:found)
      {
        try
        {
          Logger.debug("  " + c.getSimpleName());
          this.providers.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load synchronize provider " + c.getName() + ", skipping",e);
        }
      }
      
      // Sortieren der Jobs
      Collections.sort(this.providers);
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no synchronize providers found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching vor synchronize providers",e);
    }
    
    return this.providers;
  }
  
  /**
   * Liefert die passende Implementierung fuer den angegebenen Job.
   * @param type der Typ des Jobs.
   * @return die passende Implementierung oder null, wenn keine Implementierung gefunden wurde.
   */
  private Class<? extends HBCISynchronizeJob> getImplementor(Class<? extends SynchronizeJob> type)
  {
    if (this.jobs != null)
      return this.jobs.get(type);
    
    // Map der unterstuetzten Jobs aufbauen
    this.jobs = new HashMap<Class<? extends SynchronizeJob>,Class<? extends HBCISynchronizeJob>>();
    try
    {
      Logger.info("loading supported hbci synchronize jobs");
      Class[] found = Application.getClassLoader().getClassFinder().findImplementors(HBCISynchronizeJob.class);
      for (Class<HBCISynchronizeJob> c:found)
      {
        try
        {
          Logger.debug("  " + c.getSimpleName());
          Class[] interfaces = c.getInterfaces();
          for (Class i:interfaces)
          {
            // Checken, ob das Interface das SynchronizeJob Interface erweitert
            if (i.isAssignableFrom(SynchronizeJob.class))
              continue;
            Logger.debug("    implements " + i.getSimpleName());
            this.jobs.put(i,c);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to load synchronize provider " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no supported synchronize jobs found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching vor synchronize jobs",e);
    }

    return this.jobs.get(type);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#create(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public <T> T create(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    Class<? extends HBCISynchronizeJob> job = this.getImplementor(type);
    if (job == null)
      throw new ApplicationException(i18n.tr("Der Geschäftsvorfall \"{0}\" wird für HBCI nicht unterstützt",type.getSimpleName()));
    
    // Instanz erzeugen
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeJob instance = service.get(job);
    instance.setKonto(konto);
    return (T) instance;
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
      
      return this.getImplementor(type) != null;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine support for job type " + type,re);
      return false;
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<SynchronizeJob> getSynchronizeJobs(Konto k)
  {
    // Wenn kein Konto angegeben ist, ermitteln wir selbst die Liste
    // der zu synchronisierenden Konten und lassen das nicht
    // die Job-Provider tun. Denn wir wollen die Jobs in diesem
    // Fall nach Konten gruppiert haben und nicht nach Auftragsart.
    
    List<Konto> konten = null;
    if (k != null)
      konten = Arrays.asList(k);
    else
      konten = SynchronizeOptions.getSynchronizeKonten();
    
    List<SynchronizeJob> jobs = new LinkedList<SynchronizeJob>();
    for (Konto konto:konten)
    {
      for (HBCISynchronizeJobProvider provider:this.getJobProviders())
      {
        List<SynchronizeJob> list = provider.getSynchronizeJobs(konto);
        if (list == null || list.size() == 0)
          continue;
        jobs.addAll(list);
      }
    }

    return jobs;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#execute(java.util.List)
   */
  public synchronized SynchronizeSession execute(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException
  {
    if (this.session != null)
      throw new ApplicationException(i18n.tr("HBCI-Synchronisierung läuft bereits"));
    
    Logger.info("starting HBCI synchronization");
    this.worker = new Worker(jobs);
    this.session = new HBCISynchronizeSession(this.worker);
    Application.getController().start(worker);
    return this.session;
  }
  
   /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getCurrentSession()
   */
  public SynchronizeSession getCurrentSession()
  {
    return this.session;
  }

  
  
  /**
   * Implementierung des eigentlichen Worker-Threads.
   */
  class Worker implements BackgroundTask
  {
    ProgressMonitor monitor      = null;
    JobGroup currentJobGroup     = null;
    private Synchronization sync = null;
    private boolean interrupted  = false;
    
    /**
     * ct.
     * @param jobs die Liste der auszufuehrenden Jobs.
     * @throws ApplicationException
     */
    private Worker(List<SynchronizeJob> jobs) throws ApplicationException
    {
      if (jobs == null || jobs.size() == 0)
        throw new ApplicationException(i18n.tr("Keine auszuführenden HBCI-Aufträge ausgewählt"));
      
      try
      {
        // Auftraege nach Konten gruppieren - dabei aber deren Reihenfolge
        // innerhalb der Konten beibehalten. Wir gehen bei der Ausfuehrung Konto
        // fuer Konto durch und fuehren auf diesem die Auftraege aus.
        this.sync = new Synchronization();
        
        for (SynchronizeJob job:jobs)
        {
          // Wir checken, ob das auch wirklich HBCI-Jobs sind
          if (!(job instanceof HBCISynchronizeJob))
            throw new ApplicationException(i18n.tr("Kein gültiger HBCI-Auftrag: {0}",job.getName()));

          Konto konto = job.getKonto();

          // wir brechen hier komplett ab
          if (konto.hasFlag(Konto.FLAG_DISABLED))
            throw new ApplicationException(i18n.tr("Das Konto ist deaktiviert: {0}",konto.getLongName()));

          if (konto.hasFlag(Konto.FLAG_OFFLINE))
            throw new ApplicationException(i18n.tr("Das Konto ist ein Offline-Konto: {0}",konto.getLongName()));
          
          JobGroup group = sync.get(job.getKonto());
          group.add(job);
        }
        
        Logger.info("accounts to synchronize: " + sync.groups.size() + ", jobs: " + sync.size());
      }
      catch (RemoteException re)
      {
        Logger.error("error while performing synchronization",re);
        throw new ApplicationException(i18n.tr("Synchronisierung fehlgeschlagen: {0}",re.getMessage()));
      }
    }
    
    /**
     * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
     */
    public void run(ProgressMonitor monitor) throws ApplicationException
    {
      this.monitor = monitor;
      
      try
      {
        this.updateStatus(ProgressMonitor.STATUS_RUNNING,i18n.tr("HBCI-Synchronisierung läuft"));
        
        // Wir iterieren ueber jede Gruppe der Synchronisierung und verarbeiten deren Jobs.
        for (int i=0;i<this.sync.groups.size();++i)
        {
          try
          {
            // Wenn wir abgebrochen wurden, fangen wir gar nicht erst die naechste Gruppe an,
            // die wuerde in "checkInterrupted" eh gleich abbrechen
            if (!this.isInterrupted())
            {
              Logger.info("BEGIN synchronization of account " + (i+1) + "/" + this.sync.groups.size());
              this.currentJobGroup = this.sync.groups.get(i);
              this.currentJobGroup.sync();
              Logger.info("END synchronization of account " + (i+1) + "/" + this.sync.groups.size());
            }
          }
          catch (OperationCanceledException oce)
          {
            this.updateStatus(ProgressMonitor.STATUS_CANCEL,i18n.tr("HBCI-Synchronisierung abgebrochen"));
            break; // expliziter User-Wunsch - egal, ob getCancelSyncOnError true ist oder nicht
          }
          catch (Exception e)
          {
            this.monitor.log(e.getMessage());
            
            // Wir muessen den User nur fragen, wenn auch wirklich noch weitere Job-Gruppen vorhanden sind
            boolean resume = false;
            if (i+1 < this.sync.groups.size())
            {
              QueryMessage msg = new QueryMessage(e);
              Application.getMessagingFactory().getMessagingQueue(QUEUE_ERROR).sendSyncMessage(msg);
              Object response = msg.getData();
              resume = ((response instanceof Boolean) && ((Boolean)response).booleanValue());
            }
            if (resume)
            {
              Logger.warn("continue synchronization after error");
              this.monitor.log(i18n.tr("HBCI-Synchronisierung wird nach Fehler fortgesetzt"));
            }
            else
            {
              this.updateStatus(ProgressMonitor.STATUS_ERROR,i18n.tr("HBCI-Synchronisierung mit Fehlern beendet"));
              break;
            }
          }
        }
        
        if (session.getStatus() == ProgressMonitor.STATUS_RUNNING) // Nur, wenn kein Fehler und nicht abgebrochen
          this.updateStatus(ProgressMonitor.STATUS_DONE,i18n.tr("HBCI-Synchronisierung erfolgreich beendet"));
      }
      finally
      {
        Logger.info("stopping HBCI synchronization");
        HBCISynchronizeBackend.this.worker = null;
        HBCISynchronizeBackend.this.session = null;
        this.monitor.setPercentComplete(100);
        Logger.info("finished");
      }
    }
    
    /**
     * Aktualisiert den Status des Progress-Monitors und versendet ihn via Messaging.
     * @param status der neue Status.
     * @param text der Status-Text.
     */
    private void updateStatus(int status, String text)
    {
      Logger.info("updating synchronization status to: " + statusMap.get(status));
      HBCISynchronizeBackend.this.session.setStatus(status);
      this.monitor.setStatus(status);
      this.monitor.setStatusText(text);
      
      // Message-Consumer ueber neuen Status benachrichtigen.
      Application.getMessagingFactory().getMessagingQueue(QUEUE_STATUS).sendMessage(new QueryMessage(status));
      
      // Statusbar-Message schicken
      int type = (status == ProgressMonitor.STATUS_ERROR || status == ProgressMonitor.STATUS_CANCEL) ? StatusBarMessage.TYPE_ERROR : StatusBarMessage.TYPE_SUCCESS;
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,type));
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
     */
    public void interrupt()
    {
      this.monitor.setStatusText(i18n.tr("Breche HBCI-Synchronisierung ab"));
      Logger.warn("interrupting hbci synchronization");
      this.interrupted = true;
      
      // wir muessen den Status hier schonmal manuell setzen, da der HBCICallback
      // diesen Status u.a. in "log" prueft
      HBCISynchronizeBackend.this.session.setStatus(ProgressMonitor.STATUS_CANCEL);
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
     */
    public boolean isInterrupted()
    {
      return this.interrupted;
    }
  }
  
  /**
   * Hilfsklasse, um die Jobs nach Konten zu gruppieren. 
   */
  class JobGroup implements Closeable
  {
    Konto konto = null;
    private List<SynchronizeJob> jobs = new ArrayList<SynchronizeJob>();
    private List<AbstractHBCIJob> hbciJobs = new ArrayList<AbstractHBCIJob>();
    private PassportHandle handle = null;
    private HBCIHandler handler   = null;
    
    /**
     * ct.
     * @param k das Konto der Job-Gruppe.
     */
    private JobGroup(Konto k)
    {
      this.konto = k;
    }
    
    /**
     * Fuegt einen neuen Job hinzu.
     * @param job der neue Job.
     */
    private void add(SynchronizeJob job)
    {
      this.jobs.add(job);
    }
    
    /**
     * Fuehrt die Synchronisierung fuer die Job-Gruppe aus.
     * @throws Exception
     */
    private void sync() throws Exception
    {
      ////////////////////////////////////////////////////////////////////
      // lokale Variablen
      ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.monitor;
      
      // die fehlenden 6% sind fuer die Initialisierung.
      // Das +1 weil wir beim letzten ja nicht schon zu Beginn bei 100% sein wollen
      int step                = 94 / (HBCISynchronizeBackend.this.worker.sync.size() + 1);
      String kn               = this.konto.getLongName();
      ////////////////////////////////////////////////////////////////////

      try
      {
        this.checkInterrupted();

        monitor.log(" ");
        monitor.log(i18n.tr("Synchronisiere Konto: {0}",kn));

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
            
            monitor.setStatusText(i18n.tr("{0}: Aktiviere HBCI-Job: \"{1}\"",kn,job.getName()));
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
        monitor.setStatusText(i18n.tr("{0}: Führe HBCI-Jobs aus",kn));
        this.handler.execute();
        monitor.setStatusText(i18n.tr("{0}: HBCI-Jobs ausgeführt",kn));
        //
        ////////////////////////////////////////////////////////////////////////
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
          boolean haveError = false;
          for (AbstractHBCIJob hbciJob:this.hbciJobs)
          {
            try
            {
              name = hbciJob.getName();
              monitor.setStatusText(i18n.tr("{0}: Werte Ergebnis von HBCI-Job \"{1}\" aus",new String[]{kn,name}));
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
          
          if (haveError || HBCISynchronizeBackend.this.worker.isInterrupted())
          {
            Logger.warn("found errors or synchronization cancelled, clear PIN cache");
            DialogFactory.clearPINCache(this.handler != null ? this.handler.getPassport() : null);
          }
            
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
     * Prueft, ob die Synchronisierung abgebrochen wurde und wirft in dem Fall eine OperationCancelledException.
     * @throws OperationCanceledException
     */
    private void checkInterrupted() throws OperationCanceledException
    {
      if (HBCISynchronizeBackend.this.worker.isInterrupted())
        throw new OperationCanceledException(i18n.tr("Synchronisierung durch Benutzer abgebrochen"));
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
       * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.JobGroup.AbstractTaskWrapper#internalExecute()
       */
      public Passport internalExecute() throws Throwable
      {
        checkInterrupted();
        
        ////////////////////////////////////////////////////////////////////
        // lokale Variablen
        ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.monitor;
        String kn               = konto.getLongName();
        ////////////////////////////////////////////////////////////////////

        try
        {
          Passport passport = PassportRegistry.findByClass(konto.getPassportClass());
          if (passport == null)
            throw new ApplicationException(i18n.tr("Kein HBCI-Sicherheitsmedium für das Konto gefunden"));
          
          monitor.setStatusText(i18n.tr("{0}: Initialisiere HBCI-Sicherheitsmedium",kn));
          passport.init(konto);
          monitor.addPercentComplete(2);
          
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
       * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.JobGroup.AbstractTaskWrapper#internalExecute()
       */
      public PassportHandle internalExecute() throws Throwable
      {
        checkInterrupted();
        
        ////////////////////////////////////////////////////////////////////
        // lokale Variablen
        ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.monitor;
        String kn               = konto.getLongName();
        ////////////////////////////////////////////////////////////////////

        monitor.setStatusText(i18n.tr("{0}: Erzeuge HBCI-Handle",kn));
        PassportHandle handle = this.passport.getHandle();
        
        if (handle == null)
          throw new ApplicationException(i18n.tr("Fehler beim Erzeugen der HBCI-Verbindung"));
        
        monitor.addPercentComplete(2);
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
       * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.JobGroup.AbstractTaskWrapper#internalExecute()
       */
      public HBCIHandler internalExecute() throws Throwable
      {
        checkInterrupted();
        
        ////////////////////////////////////////////////////////////////////
        // lokale Variablen
        ProgressMonitor monitor = HBCISynchronizeBackend.this.worker.monitor;
        String kn               = konto.getLongName();
        ////////////////////////////////////////////////////////////////////

        try
        {
          monitor.setStatusText(i18n.tr("{0}: Öffne HBCI-Verbindung",kn));
          HBCIHandler handler = this.handle.open();
          
          if (handler == null)
            throw new ApplicationException(i18n.tr("Fehler beim Öffnen der HBCI-Verbindung"));
          
          monitor.addPercentComplete(2);
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
  

  
  /**
   * Container fuer alle auszufuehrenden Jobs gruppiert nach Konto.
   */
  private class Synchronization
  {
    List<JobGroup> groups = new ArrayList<JobGroup>();
    
    /**
     * Liefert die JobGroup fuer das angegebene Konto.
     * Die Funktion liefert nie NULL sondern erstellt in dem
     * Fall on-the-fly eine neue Gruppe fuer dieses Konto.
     * @param k das Konto.
     * @return die JobGroup.
     * @throws RemoteException
     */
    private JobGroup get(Konto k) throws RemoteException
    {
      for (JobGroup group:groups)
      {
        if (BeanUtil.equals(group.konto,k))
          return group;
      }
      
      // Neue Gruppe erstellen
      JobGroup group = new JobGroup(k);
      this.groups.add(group);
      return group;
    }
    
    /**
     * Liefert die Gesamt-Anzahl der Jobs.
     * @return die Gesamt-Anzahl der Jobs.
     */
    private int size()
    {
      int i = 0;
      for (JobGroup group:this.groups)
      {
        i += group.jobs.size();
      }
      return i;
    }
  }
}


