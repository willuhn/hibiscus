/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIFactory.java,v $
 * $Revision: 1.55 $
 * $Date: 2007/12/05 22:42:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig. <b>Hinweis:</b>:
 * Die Factory speichert grundsaetzlich keine Objekte in der Datenbank. Das ist
 * Sache des Aufrufers. Hier werden lediglich die HBCI-Jobs ausgefuehrt.
 */
public class HBCIFactory {


  private static boolean inProgress = false;
  
  
  private static I18N i18n;
  private static HBCIFactory factory;
  	private Vector jobs = new Vector();
    private Worker worker = null;
    private Listener listener = null;

  /**
   * ct.
   */
  private HBCIFactory() {
  	i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Erzeugt eine neue Instanz der HBCIFactory oder liefert die existierende
   * zurueck.
   * @return Instanz der Job-Factory.
   */
  public static synchronized HBCIFactory getInstance()
  {
  	if (factory != null)
  	  return factory;
  
  	factory = new HBCIFactory();
  	return factory;			
  }

  /**
   * Fuegt einen weiteren Job zur Queue hinzu.
   * @param job auszufuehrender Job.
   * @throws ApplicationException
   */
  public synchronized void addJob(AbstractHBCIJob job) throws ApplicationException
  {
  	if (inProgress)
    {
      Logger.write(Level.DEBUG,"hbci factory in progress - informative stacktrace",new Exception());
      throw new ApplicationException(i18n.tr("Es läuft bereits eine andere HBCI-Abfrage."));
    }
  
  	jobs.add(job);
  }

  /**
   * Fuehrt alle Jobs aus, die bis dato geadded wurden.
   * 
   * @param konto Konto, ueber das die Jobs abgewickelt werden sollen.
   * @param l ein optionaler Listener, der ausgefuehrt werden soll, wenn die
   * HBCI-Factory fertig ist.
   * @throws ApplicationException Bei Benutzer-Fehlern (zB kein HBCI-Medium konfiguriert).
   * @throws OperationCanceledException Wenn der User den Vorgang abbricht.
   */
  public synchronized void executeJobs(final Konto konto, Listener l) throws
  	ApplicationException,
  	OperationCanceledException
  {
  
    if (konto == null)
      throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));
  
    this.listener = l;
    this.worker = new Worker(konto);
    Application.getController().start(this.worker);
  }
	
  /**
   * Prueft, ob gerade HBCI-Auftraege verarbeitet werden.
   * @return true, wenn gerade Auftraege verarbeitet werden.
   */
  public boolean inProgress()
  {
    return inProgress;
  }
  
  /**
   * Gibt Informationen ueber den Job im Log aus.
   * @param job Job.
   */
  private void dumpJob(HBCIJob job)
  {
  	Logger.debug("Job restrictions for " + job.getName());
  	Properties p = job.getJobRestrictions();
  	Enumeration en = p.keys();
  	while (en.hasMoreElements())
  	{
      String key = (String) en.nextElement();
      Logger.debug("  " + key + ": " + p.getProperty(key));
  	}
  }
	
  /**
   * Liefert den Progress-Monitor, der Informationen ueber den aktuellen
   * HBCI-Verarbeitungszustand erhaelt.
   * @return Progress-Monitor.
   */
  public ProgressMonitor getProgressMonitor()
  {
    if (this.worker == null)
      return new ProgressMonitor() {
        public void setPercentComplete(int arg0) {}
        public void addPercentComplete(int arg0) {}
        public int getPercentComplete() {return 0;}
        public void setStatus(int arg0) {}
        public void setStatusText(String arg0) {}
        public void log(String arg0) {}
      };
    return this.worker.getMonitor();
  }
  
  /**
   * Liefert eine Liste aller bankspezifischen Restriktionen fuer den
   * angegebenen Geschaeftsvorfall auf diesem Passport. Sie werden intern
   * weiterverarbeitet, um zum Beispiel die Auswahlmoeglichkeiten in der
   * Benutzeroberflaeche auf die tatsaechlichen Moeglichkeiten der Bank zu
   * beschraenken.
   * @param job zu testender Job.
   * @param h der Passport, ueber den der Job getestet werden soll.
   * @return Liste der Restriktionen.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public synchronized Properties getJobRestrictions(AbstractHBCIJob job, PassportHandle h)
    throws ApplicationException, RemoteException
  {
    if (job == null)
      throw new ApplicationException(i18n.tr("Kein Job ausgewählt"));
    
    if (h == null)
      throw new ApplicationException(i18n.tr("Kein Sicherheitsmedium ausgewählt"));

    Logger.info("checking job restrictions");
    try {
      HBCIHandler handler = h.open();
      HBCIJob j = handler.newJob(job.getIdentifier());
      return j.getJobRestrictions();
    }
    finally
    {
      try {
        h.close();
      }
      catch (Throwable t) {
        Logger.error("error while closing hbci handler",t);
      }
      Logger.info("job restrictions checked");
    }
  }

  /**
   * Schliesst den aktuellen Job. Muss von jeder Funktion in diese Factory
   * aufgerufen werden, wenn Sie mit ihrer Taetigkeit fertig ist (daher
   * sinnvollerweise im finally()) um die Factory fuer die naechsten Jobs
   * freizugeben.
   * @param status 
   */
  private synchronized void stop(final int status)
  {
    Logger.info("stopping hbci factory");
    inProgress = false;
    this.worker = null;
    this.jobs.clear();

    if (this.listener != null)
    {
      Runnable r = new Runnable()
      {
        public void run()
        {
          Event e = new Event();
          e.type = status;
          Logger.info("executing listener");
          listener.handleEvent(e);
        }
      };
      if (Application.inServerMode()) r.run();
      else GUI.getDisplay().asyncExec(r);
    }
    Logger.info("finished");
  }
	
  /**
   * Setzt die Factory auf den Status &quot;inProgress&quot; oder wirft eine
   * ApplicationException, wenn gerade ein anderer Job laeuft. Diese Funktion
   * muss von jeder Funktion der Factory ausgefuehrt werden, bevor sie mit ihrer
   * Taetigkeit beginnt. Somit ist sichergestellt, dass nie zwei Jobs
   * gleichzeitig laufen.
   * @throws ApplicationException
   */
  private synchronized void start() throws ApplicationException
  {
  	if (inProgress)
      throw new ApplicationException(i18n.tr("Es läuft bereits eine andere HBCI-Abfrage."));
  
  	inProgress = true;
  }
	
  /**
   * Teilt der HBCIFactory mit, dass die gerade laufende Aktion vom Benutzer
   * abgebrochen wurde. Wird aus dem HBCICallBack heraus aufgerufen.
   */
  public synchronized void markCancelled()
  {
    if (!inProgress)
      return; // hier gibts gar nichts abzubrechen ;)
  
    if (this.worker != null)
  	  this.worker.interrupt();
  }

  /**
   * Liefert das aktuell verwendete Konto. Es wird nur dann ein Konto geliefert,
   * wenn sich die HBCIFactory gerade in der Ausfuehrung von Jobs befindet
   * (executeJobs()). Ansonsten liefert die Funktion immer null.
   * @return das aktuelle Konto.
   */
  public Konto getCurrentKonto()
  {
    if (this.worker == null)
      return null;
    return this.worker.getKonto();
  }
  
  /**
   * Wir haben den Code zur Ausfuehrung in einen eigenen Thread verlagert damit
   * die GUI waehrenddessen nicht blockiert.
   */
  private class Worker implements BackgroundTask
  {
    private Konto konto             = null;

    private ProgressMonitor monitor = null;
    private Passport passport       = null;
    private PassportHandle handle   = null;
    private HBCIHandler handler     = null;

    private boolean error           = false;
    private boolean interrupted     = false;
    
    private Worker(Konto konto)
    {
      this.konto = konto;
    }

    private Konto getKonto()
    {
      return this.konto;
    }
    
    private ProgressMonitor getMonitor()
    {
      return this.monitor;
    }
    
    /**
     * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
     */
    public synchronized void run(final ProgressMonitor monitor) throws ApplicationException
    {
      this.monitor = monitor;
      int status = ProgressMonitor.STATUS_RUNNING;

      try
      {
        StringBuffer sb = new StringBuffer();
        sb.append(konto.getBezeichnung());
        String blz = HBCIUtils.getNameForBLZ(konto.getBLZ());
        if (blz != null && blz.length() > 0)
          sb.append(" [" + blz + "]");
        final String kn = sb.toString();
        
        if (interrupted) return;
        
        // //////////////////////////////////////////////////////////////////////
        // Jobs checken
        if (jobs.size() == 0)
        {
          Logger.warn("no hbci jobs defined");
          monitor.setStatusText(i18n.tr("{0}: Keine auszuführenden HBCI-Aufträge angegeben",kn));
          return;
        }
        //
        // //////////////////////////////////////////////////////////////////////

        HBCIFactory.this.start();
        if (!Application.inServerMode())
          GUI.getStatusBar().startProgress();
        
        // //////////////////////////////////////////////////////////////////////
        // Passport erzeugen
        monitor.setStatusText(i18n.tr("{0}: Lade HBCI-Sicherheitsmedium",kn));
        monitor.addPercentComplete(2);
        
        Runnable r = new Runnable() {
          public void run()
          {
            try
            {
              passport = PassportRegistry.findByClass(konto.getPassportClass());
              // BUGZILLA #7 http://www.willuhn.de/bugzilla/show_bug.cgi?id=7
              monitor.setStatusText(i18n.tr("{0}: Initialisiere HBCI-Sicherheitsmedium",kn));

              if (passport == null)
                throw new ApplicationException(i18n.tr("Kein HBCI-Sicherheitsmedium für das Konto gefunden"));
              passport.init(konto);
            }
            catch (ApplicationException ae)
            {
              monitor.setStatusText(ae.getMessage());
              error = true;
            }
            catch (Exception e)
            {
              Throwable t = getCause(e);
              Logger.error("unable to init passport",e);
              monitor.setStatusText(i18n.tr("{0}: Fehler beim Initialisieren des Sicherheitsmediums",kn));
              monitor.log(t.getMessage());
              error = true;
            }
          }
        };
        
        if (Application.inServerMode()) r.run();
        else GUI.getDisplay().syncExec(r);
        
        if (error) return;
        if (interrupted) return;

        if (passport == null)
        {
          Logger.error("no passport available");
          monitor.setStatusText(i18n.tr("{0}: Kein Sicherheitsmedium angegeben",kn));
          error = true;
          return;
        }
        //
        // //////////////////////////////////////////////////////////////////////
        

        // //////////////////////////////////////////////////////////////////////
        // PassportHandle erzeugen
        monitor.setStatusText(i18n.tr("{0}: Erzeuge HBCI-Handle",kn));
        monitor.addPercentComplete(2);

        r = new Runnable() {
          public void run()
          {
            try
            {
              handle = passport.getHandle();
            }
            catch (RemoteException e1)
            {
              Logger.error("unable to create HBCI handle",e1);
              monitor.setStatusText(i18n.tr("{0}: HBCI-Medium kann nicht initialisiert werden",kn));
              error = true;
            }
          }
        };
        
        if (Application.inServerMode()) r.run();
        else GUI.getDisplay().syncExec(r);
        
        if (error) return;
        if (interrupted) return;

        if (handle == null)
        {
          Logger.error("unable to create HBCI handle");
          monitor.setStatusText(i18n.tr("{0}: HBCI-Medium kann nicht initialisiert werden",kn));
          error = true;
          return;
        }
        //
        // //////////////////////////////////////////////////////////////////////
        

        // //////////////////////////////////////////////////////////////////////
        // HBCI-Verbindung aufbauen
        monitor.setStatusText(i18n.tr("{0}: Öffne HBCI-Verbindung",kn));
        monitor.addPercentComplete(2);

        r = new Runnable() {
          public void run()
          {
            try
            {
              handler = handle.open();
            }
            catch (OperationCanceledException oce)
            {
              Logger.info("operation cancelled");
              monitor.setStatusText(i18n.tr("Vorgang abgebrochen"));
              error = true;
            }
            catch (ApplicationException ae)
            {
              monitor.setStatusText(ae.getMessage());
              error = true;
            }
            catch (Exception e)
            {
              Throwable t = getCause(e);
              Logger.error("unable to open handle",e);
              monitor.setStatusText(i18n.tr("{0}: Fehler beim Öffnen der HBCI-Verbindung",kn));
              monitor.log(t.getMessage());
              error = true;
            }
          }
        };
        if (Application.inServerMode()) r.run();
        else GUI.getDisplay().syncExec(r);

        if (error) return;
        if (interrupted) return;
        //
        // //////////////////////////////////////////////////////////////////////

        // //////////////////////////////////////////////////////////////////////
        // Jobs erzeugen
        Logger.info("processing jobs");

        for (int i=0;i<jobs.size();++i)
        {
          if (interrupted) return;
          final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
          
          monitor.setStatusText(i18n.tr("{0}: Aktiviere HBCI-Job: \"{1}\"",new String[]{kn,job.getName()}));
          monitor.addPercentComplete(2);

          Logger.info("adding job " + job.getIdentifier() + " to queue");
          HBCIJob j = handler.newJob(job.getIdentifier());
          dumpJob(j);
          job.setJob(j);
          handler.addJob(j);
          if (job.isExclusive())
          {
            Logger.info("job will be executed in seperate hbci message");
            handler.newMsg();
          }
        }
        //
        // //////////////////////////////////////////////////////////////////////

        
        if (interrupted) return;
        
        // BUGZILLA 327
        try
        {
          // //////////////////////////////////////////////////////////////////////
          // Jobs ausfuehren
          Logger.info("executing jobs");
          monitor.setStatusText(i18n.tr("{0}: Führe HBCI-Jobs aus",kn));
          monitor.addPercentComplete(4);
          handler.execute();
          monitor.setStatusText(i18n.tr("{0}: HBCI-Jobs ausgeführt",kn));
          monitor.addPercentComplete(4);
          //
          // //////////////////////////////////////////////////////////////////////
        }
        finally
        {
          String name = null;

          // //////////////////////////////////////////////////////////////////////
          // Job-Ergebnisse auswerten
          for (int i=0;i<jobs.size();++i)
          {
            try
            {
              final AbstractHBCIJob job = (AbstractHBCIJob) jobs.get(i);
              name = job.getName();
              monitor.setStatusText(i18n.tr("{0}: Werte Ergebnis von HBCI-Job \"{1}\" aus",new String[]{kn,name}));
              monitor.addPercentComplete(2);
              Logger.info("executing check for job " + job.getIdentifier());
              job.handleResult();
            }
            catch (ApplicationException ae)
            {
              if (!interrupted)
              {
                monitor.setStatusText(ae.getMessage());
                error = true;
              }
            }
            catch (Throwable t)
            {
              if (!interrupted)
              {
                monitor.setStatusText(i18n.tr("Fehler beim Auswerten des HBCI-Auftrages {0}", name));
                Logger.error("error while processing job result",t);
                monitor.log(t.getMessage());
                error = true;
              }
            }
          }
          //
          // //////////////////////////////////////////////////////////////////////
        }
      }
      catch (OperationCanceledException e3)
      {
        monitor.setStatusText(i18n.tr("HBCI-Übertragung abgebrochen"));
        monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
      }
      catch (ApplicationException ae)
      {
        monitor.setStatusText(ae.getMessage());
        error = true;
      }
      catch (Throwable t)
      {
        Throwable t2 = getCause(t);
        Logger.error("error while executing hbci jobs",t);
        monitor.setStatusText(i18n.tr("Fehler beim Ausführen der HBCI-Aufträge {0}", t.toString()));
        monitor.log(t2.getMessage());
        error = true;
      }
      finally
      {
        try
        {
          monitor.setStatusText(i18n.tr("Beende HBCI-Übertragung"));
          monitor.addPercentComplete(2);
          jobs.clear(); // Jobqueue leer machen.
          try {
            if (handle != null)
              handle.close();
          }
          catch (Throwable t) {/* useless */}

          String msg = null;

          if (!interrupted && !error)
          {
            status = ProgressMonitor.STATUS_DONE;
            msg = "HBCI-Übertragung erfolgreich beendet";
          }
          if (interrupted)
          {
            status = ProgressMonitor.STATUS_CANCEL;
            msg = "HBCI-Übertragung abgebrochen";
          }
          if (error)
          {
            status = ProgressMonitor.STATUS_ERROR;
            msg = "HBCI-Übertragung mit Fehlern beendet";
            DialogFactory.clearPINCache();
          }
          monitor.setStatus(status);
          monitor.setStatusText(i18n.tr(msg));
          monitor.setPercentComplete(100);
        }
        finally
        {
          if (!Application.inServerMode())
            GUI.getStatusBar().stopProgress();
          HBCIFactory.this.stop(status);
        }
      }
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
     */
    public void interrupt()
    {
      monitor.setStatusText(i18n.tr("Breche HBCI-Übertragung ab"));
      Logger.warn("mark hbci session as interrupted");
      this.interrupted = true;
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
   * Laeuft den Stack der Exceptions bis zur urspruenglichen hoch und liefert sie zurueck.
   * HBCI4Java verpackt Exceptions oft tief ineinander. Sie werden gefangen, in eine
   * neue gepackt und wieder geworfen. Um nun die eigentliche Fehlermeldung zu kriegen,
   * suchen wir hier nach der ersten. 
   * BUGZILLA 249
   * @param t die Exception.
   * @return die urspruengliche.
   */
  public static Throwable getCause(Throwable t)
  {
    Throwable cause = t;
    
    for (int i=0;i<20;++i) // maximal 20 Schritte nach oben
    {
      Throwable current = cause.getCause();

      if (current == null)
        break; // Ende, hier kommt nichts mehr
      
      if (current == cause) // Wir wiederholen uns
        break;
      
      cause = current;
    }
    
    return cause;
  }
}


/*******************************************************************************
 * $Log: HBCIFactory.java,v $
 * Revision 1.55  2007/12/05 22:42:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.54  2007/12/04 11:24:38  willuhn
 * @B Bug 509
 *
 * Revision 1.53  2007/12/03 13:17:54  willuhn
 * @N Debugging-Infos
 *
 * Revision 1.52  2007/05/20 23:45:10  willuhn
 * @N HBCI-Jobausfuehrung Servertauglich gemacht
 *
 * Revision 1.51  2007/05/16 13:59:53  willuhn
 * @N Bug 227 HBCI-Synchronisierung auch im Fehlerfall fortsetzen
 * @C Synchronizer ueberarbeitet
 * @B HBCIFactory hat globalen Status auch bei Abbruch auf Error gesetzt
 *
 * Revision 1.50  2007/03/14 12:01:33  willuhn
 * @N made getCause public
 *
 * Revision 1.49  2007/02/21 12:10:36  willuhn
 * Bug 349
 *
 * Revision 1.48  2007/02/21 10:20:08  willuhn
 * @N Log-Ausgabe, wenn HBCI-Session abgebrochen wurde
 *
 * Revision 1.47  2007/02/21 10:02:27  willuhn
 * @C Code zum Ausfuehren exklusiver Jobs redesigned
 *
 * Revision 1.46  2006/11/15 00:13:07  willuhn
 * @B Bug 327
 *
 * Revision 1.45  2006/08/21 12:29:48  willuhn
 * @N HBCICallbackSWT.setCurrentHandle
 *
 * Revision 1.44  2006/08/03 15:32:34  willuhn
 * @N Bug 62
 *
 * Revision 1.43  2006/07/13 22:10:23  willuhn
 * @B bug 249
 *
 * Revision 1.42  2006/03/16 18:23:36  willuhn
 * @N first code for new synchronize system
 *
 * Revision 1.41  2006/01/18 18:40:35  willuhn
 * @N Redesign des Background-Task-Handlings
 *
 * Revision 1.40  2005/11/14 12:46:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.39  2005/11/14 11:36:58  willuhn
 * @B bug 148
 *
 * Revision 1.38  2005/08/05 16:33:41  willuhn
 * @B bug 108
 * @B bug 110
 *
 * Revision 1.37  2005/08/02 20:33:12  web0
 * *** empty log message ***
 *
 * Revision 1.36  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 * Revision 1.35  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.34  2005/08/01 20:35:31  web0
 * *** empty log message ***
 * Revision 1.33 2005/07/26 23:57:18 web0
 * 
 * @N Restliche HBCI-Jobs umgestellt
 * 
 * Revision 1.32 2005/07/26 23:00:03 web0
 * @N Multithreading-Support fuer HBCI-Jobs
 * 
 * Revision 1.31 2005/06/21 20:11:10 web0
 * @C cvs merge
 * 
 * Revision 1.30 2005/06/15 16:10:48 web0
 * @B javadoc fixes
 * 
 * Revision 1.29 2005/05/19 23:31:07 web0
 * @B RMI over SSL support
 * @N added handbook
 * 
 * Revision 1.28 2005/05/10 22:26:15 web0
 * @B bug 71
 * 
 * Revision 1.27 2005/05/06 14:05:04 web0 *** empty log message ***
 * 
 * Revision 1.26 2005/03/09 01:07:02 web0
 * @D javadoc fixes
 * 
 * Revision 1.25 2005/03/06 16:33:57 web0
 * @B huu, job results of exclusive jobs were not executed
 * 
 * Revision 1.24 2005/03/05 19:11:25 web0
 * @N SammelLastschrift-Code complete
 * 
 * Revision 1.23 2005/02/28 23:59:57 web0
 * @B http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
 * 
 * Revision 1.22 2005/02/01 17:15:37 willuhn *** empty log message ***
 * 
 * Revision 1.21 2004/11/13 17:02:04 willuhn
 * @N Bearbeiten des Zahlungsturnus
 * 
 * Revision 1.20 2004/11/12 18:25:08 willuhn *** empty log message ***
 * 
 * Revision 1.19 2004/11/04 22:30:33 willuhn *** empty log message ***
 * 
 * Revision 1.18 2004/11/02 18:48:32 willuhn *** empty log message ***
 * 
 * Revision 1.17 2004/10/29 00:32:32 willuhn
 * @N HBCI job restrictions
 * 
 * Revision 1.16 2004/10/26 23:47:08 willuhn *** empty log message ***
 * 
 * Revision 1.15 2004/10/25 22:39:14 willuhn *** empty log message ***
 * 
 * Revision 1.14 2004/10/25 17:58:56 willuhn
 * @N Haufen Dauerauftrags-Code
 * 
 * Revision 1.13 2004/10/24 17:19:02 willuhn *** empty log message ***
 * 
 * Revision 1.12 2004/10/19 23:33:31 willuhn *** empty log message ***
 * 
 * Revision 1.11 2004/10/18 23:38:17 willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 * 
 * Revision 1.10 2004/07/25 17:15:06 willuhn
 * @C PluginLoader is no longer static
 * 
 * Revision 1.9 2004/07/21 23:54:31 willuhn *** empty log message ***
 * 
 * Revision 1.8 2004/06/30 20:58:29 willuhn *** empty log message ***
 * 
 * Revision 1.7 2004/06/10 20:56:33 willuhn
 * @D javadoc comments fixed
 * 
 * Revision 1.6 2004/05/05 22:14:47 willuhn *** empty log message ***
 * 
 * Revision 1.5 2004/05/04 23:07:23 willuhn
 * @C refactored Passport stuff
 * 
 * Revision 1.4 2004/04/27 22:23:56 willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports
 *    verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch
 *    die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 * 
 * Revision 1.3 2004/04/24 19:04:51 willuhn
 * @N Ueberweisung.execute works!! ;)
 * 
 * Revision 1.2 2004/04/22 23:46:50 willuhn
 * @N UeberweisungJob
 * 
 * Revision 1.1 2004/04/19 22:05:51 willuhn
 * @C HBCIJobs refactored
 * 
 * Revision 1.1 2004/04/14 23:53:46 willuhn *** empty log message ***
 * 
 * Revision 1.10 2004/04/05 23:28:46 willuhn *** empty log message ***
 * 
 * Revision 1.9 2004/04/04 18:30:23 willuhn *** empty log message ***
 * 
 * Revision 1.8 2004/03/11 08:55:42 willuhn
 * @N UmsatzDetails
 * 
 * Revision 1.7 2004/03/06 18:25:10 willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 * 
 * Revision 1.6 2004/03/05 00:19:23 willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 * 
 * Revision 1.5 2004/03/05 00:04:10 willuhn
 * @N added code for umsatzlist
 * 
 * Revision 1.4 2004/02/21 19:49:04 willuhn
 * @N PINDialog
 * 
 * Revision 1.3 2004/02/20 01:25:25 willuhn *** empty log message ***
 * 
 * Revision 1.2 2004/02/17 01:01:38 willuhn *** empty log message ***
 * 
 * Revision 1.1 2004/02/17 00:53:22 willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 * 
 ******************************************************************************/