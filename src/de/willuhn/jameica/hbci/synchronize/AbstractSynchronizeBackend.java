/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.KontoType;
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
 * Abstrakte Basis-Implementierung fuer ein Synchronize-Backend.
 * @param <T> der konkrete Typ des JobProviders.
 */
public abstract class AbstractSynchronizeBackend<T extends SynchronizeJobProvider> implements SynchronizeBackend
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private List<T> providers = null;
  private SynchronizeSession session = null;
  protected Worker worker = null;
  
  /**
   * Liefert eine Liste der Konten, fuer die die Synchronisierung ausgefuehrt
   * werden.
   * Die Funktion macht nichts anderes, als:
   *  - alle zur Synchronisierung aktiven zurueckzuliefern, wenn k=null ist
   *  - eine Liste mit nur dem angegebenen Konto zurueckzuliefern, wenn k!=null ist.
   *  
   * Die Liste enthaelt jedoch generell nur Konten, die nicht deaktiviert sind.
   * Kann ueberschrieben werden, um die Liste weiter einzuschraenken.
   * @param k das Konto.
   * @return die Liste der Konten.
   */
  public List<Konto> getSynchronizeKonten(Konto k)
  {
    List<Konto> list = k == null ? SynchronizeOptions.getSynchronizeKonten() : Arrays.asList(k);
    List<Konto> result = new ArrayList<Konto>();
    
    for (Konto konto:list)
    {
      try
      {
        if (konto.hasFlag(Konto.FLAG_DISABLED))
          continue;
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
   * Liefert das Marker-Interface der Job-Provider des Backends.
   * @return das Marker-Interface der Job-Provider des Backends.
   */
  protected abstract Class<T> getJobProviderInterface();
  
  /**
   * Muss ueberschrieben werden, um dort eine Instanz der JobGroup zurueckzuliefern,
   * in der die sync()-Funktion implementiert ist.
   * @param k das Konto der Gruppe.
   * @return die Instanz der Gruppe.
   */
  protected abstract JobGroup createJobGroup(Konto k);

  /**
   * Liefert unsere Job-Provider.
   * @return unsere Job-Provider.
   */
  protected synchronized List<T> getJobProviders()
  {
    if (this.providers != null)
      return this.providers;
    
    this.providers = new ArrayList<T>();

    try
    {
      Logger.info("loading synchronize providers for backend " + getName());
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader().getClassFinder().findImplementors(this.getJobProviderInterface());
      for (Class<T> c:found)
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
      Logger.info("  found " + this.providers.size() + " provider(s)");
      Logger.debug("provider order before sorting:");
      for (T p:this.providers)
      {
        Logger.debug("  " + p.getClass().getSimpleName());
      }
      Collections.sort(this.providers);
      Logger.debug("provider order after sorting:");
      for (T p:this.providers)
      {
        Logger.debug("  " + p.getClass().getSimpleName());
      }
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
   * @param konto das Konto, fuer das der Job gesucht wird.
   * @return die passende Implementierung oder null, wenn keine Implementierung gefunden wurde.
   */
  protected Class<? extends SynchronizeJob> getImplementor(final Class<? extends SynchronizeJob> type, final Konto konto)
  {
    KontoType kt = null;
    String id = null;
    try
    {
      kt = KontoType.find(konto != null ? konto.getAccountType() : null);
      id = konto != null ? konto.getID() : null;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine id/account-type for konto",re);
    }

    Logger.debug("searching for implementation for synchronize job " + type.getSimpleName() + " for backend " + getName() + " [account-type " + kt + ", konto ID: " + id + "]");
    for (T p:this.getJobProviders())
    {
      List<Class<? extends SynchronizeJob>> classes = p.getJobTypes();
      for (Class<? extends SynchronizeJob> c:classes)
      {
        if (type.isAssignableFrom(c) && p.supports(c,konto))
        {
          Logger.debug("    found " + c.getSimpleName());
          return c;
        }
      }
    }
    Logger.debug("no implementation found");
    return null;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#create(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   * Kann ueberschrieben werden, um weitere Checks durchzufuehren oder weitere Context-Properties im Job zu setzen.
   */
  public <R> R create(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    try
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_DISABLED))
        throw new ApplicationException(i18n.tr("Das Konto ist deaktiviert"));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to check konto flags",re);
      throw new ApplicationException(i18n.tr("Der Geschäftsvorfall konnte nicht erstellt werden: {0}",re.getMessage()));
    }

    Class<? extends SynchronizeJob> job = this.getImplementor(type, konto);
    if (job == null)
      throw new ApplicationException(i18n.tr("Der Geschäftsvorfall \"{0}\" wird für {1} nicht unterstützt",type.getSimpleName(),this.getName()));
    
    // Instanz erzeugen
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeJob instance = service.get(job);
    instance.setKonto(konto);
    
    return (R) instance;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#supports(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   * Kann ueberschrieben werden, um weitere Checks durchzufuehren.
   */
  public boolean supports(Class<? extends SynchronizeJob> type, Konto konto)
  {
    try
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_DISABLED))
        return false;
      
      return this.getImplementor(type, konto) != null;
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
  public final List<SynchronizeJob> getSynchronizeJobs(Konto k)
  {
    // Wenn kein Konto angegeben ist, ermitteln wir selbst die Liste
    // der zu synchronisierenden Konten und lassen das nicht
    // die Job-Provider tun. Denn wir wollen die Jobs in diesem
    // Fall nach Konten gruppiert haben und nicht nach Auftragsart.
    List<SynchronizeJob> jobs = new LinkedList<SynchronizeJob>();
    for (Konto konto:this.getSynchronizeKonten(k))
    {
      for (T provider:this.getJobProviders())
      {
        try
        {
          List<SynchronizeJob> list = provider.getSynchronizeJobs(konto);
          if (list == null || list.size() == 0)
            continue;
          jobs.addAll(list);
        }
        catch (Throwable t)
        {
          Logger.error("unable to determine synchronize jobs for provider " + provider.getClass().getName(),t);
        }
      }
    }

    return jobs;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getPropertyNames(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<String> getPropertyNames(Konto k)
  {
    return null;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#execute(java.util.List)
   * Kann ueberschrieben werden, um weitere Checks durchzufuehren.
   */
  public synchronized SynchronizeSession execute(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException
  {
    if (this.session != null)
      throw new ApplicationException(i18n.tr("Synchronisierung via {0} läuft bereits",this.getName()));
    
    Logger.info("starting " + this.getName() + " synchronization");
    this.worker = new Worker(jobs);
    this.session = new SynchronizeSession(this.worker);
    Application.getController().start(worker);
    return this.session;
  }
  
   /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getCurrentSession()
   */
  public final SynchronizeSession getCurrentSession()
  {
    return this.session;
  }
  
  /**
   * Implementierung des eigentlichen Worker-Threads.
   */
  protected class Worker implements BackgroundTask
  {
    private ProgressMonitor monitor  = null;
    private JobGroup currentJobGroup = null;
    private Synchronization sync     = null;
    private boolean interrupted      = false;
    
    /**
     * ct.
     * @param jobs die Liste der auszufuehrenden Jobs.
     * @throws ApplicationException
     */
    private Worker(List<SynchronizeJob> jobs) throws ApplicationException
    {
      if (jobs == null || jobs.size() == 0)
        throw new ApplicationException(i18n.tr("Keine auszuführenden Aufträge ausgewählt"));
      
      try
      {
        // Auftraege nach Konten gruppieren - dabei aber deren Reihenfolge
        // innerhalb der Konten beibehalten. Wir gehen bei der Ausfuehrung Konto
        // fuer Konto durch und fuehren auf diesem die Auftraege aus.
        this.sync = new Synchronization();
        
        for (SynchronizeJob job:jobs)
        {
          Konto konto = job.getKonto();

          // wir brechen hier komplett ab
          if (konto.hasFlag(Konto.FLAG_DISABLED))
            throw new ApplicationException(i18n.tr("Das Konto ist deaktiviert: {0}",konto.getLongName()));

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
     * Liefert den ProgressMonitor.
     * @return der ProgressMonitor.
     */
    public final ProgressMonitor getMonitor()
    {
      return this.monitor;
    }
    
    /**
     * Liefert die gerade in Arbeit befindliche Job-Gruppe.
     * @return die gerade in Arbeit befindliche Job-Gruppe.
     */
    public final JobGroup getCurrentJobGroup()
    {
      return this.currentJobGroup;
    }
    
    /**
     * Liefert die gesamte Synchronisierung.
     * @return die gesamte Synchronisierung.
     */
    public Synchronization getSynchronization()
    {
      return this.sync;
    }
    
    /**
     * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
     */
    public final void run(ProgressMonitor monitor) throws ApplicationException
    {
      this.monitor = monitor;
      
      try
      {
        this.updateStatus(ProgressMonitor.STATUS_RUNNING,i18n.tr("Synchronisierung via {0} läuft",getName()));
        
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
            Logger.warn("operation cancelled");
            Logger.write(Level.DEBUG,"stacktrace for debugging purpose",oce);
            this.updateStatus(ProgressMonitor.STATUS_CANCEL,i18n.tr("Synchronisierung via {0} abgebrochen",getName()));
            break; // expliziter User-Wunsch - egal, ob getCancelSyncOnError true ist oder nicht
          }
          catch (Exception e)
          {
            if (e instanceof ApplicationException)
            {
              Logger.write(Level.INFO,e.getMessage(),e);
            }
            else
            {
              Logger.error("error while synchronizing",e);
              
              // Wir holen uns noch die eigentliche Ursache aus den Causes um eine plausible Fehlermeldung zu kriegen
              Throwable t = HBCIProperties.getCause(e);
              if (t instanceof Exception)
                e = (Exception) t;
            }
            
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
              this.monitor.log(i18n.tr("Fehler: {0}",e.getMessage()));
              this.monitor.log(i18n.tr("Synchronisierung via {0} wird nach Fehler fortgesetzt",getName()));
            }
            else
            {
              if (e instanceof ApplicationException)
                this.updateStatus(ProgressMonitor.STATUS_ERROR,e.getMessage());
              else
                this.updateStatus(ProgressMonitor.STATUS_ERROR,i18n.tr("Fehler: {0}",e.getMessage()));
              break;
            }
          }
        }
        
        if (session.getStatus() == ProgressMonitor.STATUS_RUNNING) // Nur, wenn kein Fehler und nicht abgebrochen
          this.updateStatus(ProgressMonitor.STATUS_DONE,i18n.tr("Synchronisierung via {0} erfolgreich beendet",getName()));
      }
      finally
      {
        Logger.info("stopping synchronization");
        worker = null;
        session = null;
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
      Logger.info("updating synchronization status to: " + ProgressMonitor.STATUS_MAP.get(status));
      session.setStatus(status);
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
    public final void interrupt()
    {
      this.monitor.setStatusText(i18n.tr("Breche Synchronisierung via {0} ab",getName()));
      Logger.warn("interrupting synchronization");
      this.interrupted = true;
      
      // wir muessen den Status hier schonmal manuell setzen, da der HBCICallback
      // diesen Status u.a. in "log" prueft
      session.setStatus(ProgressMonitor.STATUS_CANCEL);
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
     */
    public final boolean isInterrupted()
    {
      return this.interrupted;
    }
  }
  
  /**
   * Abstrakte Basis-Klasse, die die Jobs nach Konten gruppiert und ausfuehrt. 
   */
  protected abstract class JobGroup
  {
    private Konto konto = null;
    protected List<SynchronizeJob> jobs = new ArrayList<SynchronizeJob>();
    
    /**
     * ct.
     * @param k das Konto der Job-Gruppe.
     */
    protected JobGroup(Konto k)
    {
      this.konto = k;
    }
    
    /**
     * Liefert das Konto der Job-Gruppe.
     * @return das Konto der Job-Gruppe.
     */
    public Konto getKonto()
    {
      return this.konto;
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
    protected abstract void sync() throws Exception;

    /**
     * Prueft, ob die Synchronisierung abgebrochen wurde und wirft in dem Fall eine OperationCancelledException.
     * @throws OperationCanceledException
     */
    protected final void checkInterrupted() throws OperationCanceledException
    {
      if (worker.isInterrupted())
        throw new OperationCanceledException(i18n.tr("Synchronisierung durch Benutzer abgebrochen"));
    }
  }
  
  /**
   * Container fuer alle auszufuehrenden Jobs gruppiert nach Konto.
   */
  protected class Synchronization
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
      JobGroup group = createJobGroup(k);
      this.groups.add(group);
      return group;
    }
    
    /**
     * Liefert die Gesamt-Anzahl der Jobs.
     * @return die Gesamt-Anzahl der Jobs.
     */
    public final int size()
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


