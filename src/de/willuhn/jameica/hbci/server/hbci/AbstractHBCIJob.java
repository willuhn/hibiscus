/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/AbstractHBCIJob.java,v $
 * $Revision: 1.41 $
 * $Date: 2012/03/01 22:19:15 $
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.status.HBCIRetVal;
import org.kapott.hbci.status.HBCIStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Basis-Klasse fuer die HBCI-Jobs.
 * Ein HBCI-Job muss quasi atomar sein. Das heisst, in dessen <code>handleResult</code>
 * nimmt er auch gleich ggf. notwendige Aenderungen <b>und</b> Speicherungen
 * an den betroffenen Fachobjekten vor. Grund: Es darf nicht sein, dass zB.
 * eine Ueberweisung ausgefuehrt wird, ihr Status jedoch in der DB nicht auf
 * "ausgefuehrt" gesetzt wird.
 */
public abstract class AbstractHBCIJob
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	private org.kapott.hbci.GV.HBCIJob job = null;
  private boolean exclusive              = false;
	private Hashtable params 			         = new Hashtable(); 

	/**
	 * HBCI4Java verwendet intern eindeutige Job-Namen.
	 * Diese Funktion liefert genau den Namen fuer genau den
	 * gewuenschten Job.
	 * @return Job-Identifier.
	 */
  abstract String getIdentifier();
  
  /**
   * Liefert einen sprechenden Namen fuer diesen Job.
   * @return sprechender Name.
   * @throws RemoteException
   */
  public abstract String getName() throws RemoteException;

  /**
   * Markiert den Auftrag als erledigt.
   * @throws RemoteException
   * @throws ApplicationException
   */
  abstract void markExecuted() throws RemoteException, ApplicationException;
  
  /**
   * Markiert den Auftrag als fehlerhaft.
   * @param error der Fehlertext aus der HBCI-Nachricht.
   * @return der Fehlertext, wie er weitergeworfen werden soll.
   * Hier kann der Implementierer noch weitere Informationen zum Job hinzufuegen.
   * @throws RemoteException
   * @throws ApplicationException
   */
  abstract String markFailed(String error) throws RemoteException, ApplicationException;

  /**
   * Wird aufgerufen, wenn der User den Vorgang abgebrochen hat.
   * Kann von den Jobs implementiert werden, muss aber nicht.
   * Die Funktion wird nur genau dann aufgerufen, wenn der Job noch abbrechbar war - sprich,
   * wenn er noch nicht an die Bank uebertragen wurde. Wurde er jedoch bereits an die Bank
   * gesendet, dann wird entweder markFailed() oder markExecuted() aufgerufen.
   * @throws RemoteException
   * @throws ApplicationException
   * BUGZILLA 690
   */
  void markCancelled() throws RemoteException, ApplicationException
  {
  }
  
  /**
   * Wird aufgerufen, wenn Warnungen gefunden wurden.
   * Kann von abgeleiteten Klassen ueberschrieben werden.
   * @param warnings die aufgetretenen Warnungen.
   * @throws RemoteException
   * @throws ApplicationException
   */
  void hasWarnings(HBCIRetVal[] warnings) throws RemoteException, ApplicationException
  {
  }

	/**
	 * Diese Funktion wird von der HBCIFactory intern aufgerufen.
	 * Sie uebergibt hier den erzeugten HBCI-Job der Abfrage.
	 * @param job der erzeugte Job.
	 */
  final void setJob(org.kapott.hbci.GV.HBCIJob job)
  {
  	this.job = job;
  	Enumeration e = params.keys();
  	while (e.hasMoreElements())
  	{
  		String name = (String) e.nextElement();
  		Object value = params.get(name);

  		if (value instanceof Konto)
	  		job.setParam(name,(Konto)value);

			else if (value instanceof Date)
				job.setParam(name,(Date)value);

			else if (value instanceof Value)
				job.setParam(name,(Value)value);

	  	else
				job.setParam(name,value.toString());
  	}
  }

  /**
   * Liefert das Job-Resultat.
   * @return Job-Resultat.
   */
  final HBCIJobResult getJobResult()
	{
		return job.getJobResult();
	}
  
  /**
   * Diese Funktion wird von der HBCIFactory nach Beendigung der Kommunikation mit der Bank ausgefuehrt.
   * Sie prueft globalen Status und Job-Status und ruft entsprechend markExecuted() oder markFailed(String) auf. 
   * @throws RemoteException
   * @throws ApplicationException
   */
  final void handleResult() throws ApplicationException, RemoteException
  {
    HBCIJobResult result    = getJobResult();
    HBCIStatus status       = result.getJobStatus();

    // BUGZILLA 964 - nur dann als abgebrochen markieren, wenn wir fuer den Job noch keinen richtigen
    // Status haben. Denn wenn der vorliegt, ist es fuer den Abbruch - zumindest fuer diesen Auftrag - zu spaet.
    // BUGZILLA 1109 - Wenn im ChipTAN-Dialog abgebrochen wird, haben wir hier ein HBCIStatus.STATUS_OK. Das
    // ist ziemlich daemlich. Wegen 964 koennen wir aber nicht pauschal abbrechen, weil wir sonst Jobs
    // als abgebrochen markieren, die schon ausgefuehrt wurden. In dem Status steht OK, drin, weil die Bank da
    // mit SUCCESS-Statuscode sowas hier geschickt hat: "0030 Auftrag entgegengenommen. Bitte TAN eingeben"
    // Rein via Status-Codes sieht alles OK aus. Gemaess "FinTS_3.0_Rueckmeldungscodes_2010-10-27_final_version.pdf"
    // steht "0030" fuer "Auftrag empfangen - Sicherheitsfreigabe erforderlich". Wir machen hier also einen
    // Sonderfall fuer diesen einen Code.
    
    // TODO Das koennte man vermutlich auch direkt in HBCI4Java implementieren
    boolean tanNeeded = false;
    HBCIRetVal[] values = status.getSuccess();
    if (values != null && values.length > 0)
    {
      for (HBCIRetVal val:values)
      {
        if (val.code != null && val.code.equals("0030"))
        {
          tanNeeded = true;
          break;
        }
      }
    }

    if ((tanNeeded || status.getStatusCode() == HBCIStatus.STATUS_UNKNOWN) && HBCIFactory.getInstance().isCancelled()) // BUGZILLA 690
    {
      Logger.warn("hbci session cancelled by user, mark job as cancelled");
      markCancelled();
      return;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Warnungen ausgeben, falls vorhanden - BUGZILLA 899
    HBCIRetVal[] warnings = status.getWarnings();
    if (warnings != null && warnings.length > 0)
    {
      // Loggen
      ProgressMonitor monitor = HBCIFactory.getInstance().getProgressMonitor();
      monitor.log(" ");
      for (HBCIRetVal val:warnings)
        monitor.log("  " + val.code + ": " + val.text);
      monitor.log(" ");
      
      // Auftrag informieren
      hasWarnings(warnings);
    }
    ////////////////////////////////////////////////////////////////////////////
    
    if (result.isOK())
    {
      // Globaler Status ist OK - Job wurde zweifelsfrei erfolgreich ausgefuehrt
      // Wir markieren die Ueberweisung als "ausgefuehrt"
      markExecuted();
      return;
    }

    // Globaler Status ist nicht OK. Mal schauen, was der Job-Status sagt
    String statusText = getStatusText();
    if (status.getStatusCode() == HBCIStatus.STATUS_OK)
    {
      // Wir haben zwar global einen Fehler. Aber zumindest der Auftrag
      // scheint in Ordnung zu sein. Wir markieren ihn sicherheitshalber
      // als ausgefuehrt (damit er nicht mehrfach ausgefuhert wird), melden
      // den globalen Fehler aber trotzdem weiter
      try
      {
        markExecuted();
      }
      catch (Exception e)
      {
        // Das ist ein Folge-Fehler. Den loggen wir. Wir werfen aber die originale
        // Fehlermeldung weiter
        Logger.error("unable to mark job as executed",e);
      }
      throw new ApplicationException(statusText);
    }

    // Nichts hat geklappt. Weder der globale Status ist in Ordnung
    // noch der Job-Status. Wir geben dem Job die Moeglichkeit, ihn
    // als fehlerhaft zu markieren.
    String error = null;
    try
    {
      error = markFailed(statusText);
    }
    catch (Exception e)
    {
      // Folge-Fehler. Loggen. Aber originale Meldung weiterwerfen
      Logger.error("unable to mark job as failed",e);
    }
    throw new ApplicationException(error != null && error.length() > 0 ? error : statusText);
  }
  
	/**
	 * Liefert den Status-Text, der vom HBCI-Kernel nach Ausfuehrung des Jobs zurueckgeliefert wurde.
   * @return Status-Text oder <code>Unbekannter Fehler</code> wenn dieser nicht ermittelbar ist.
   */
  final String getStatusText()
	{
    String sr = "";
		try
		{
      String sGlob = getJobResult().getGlobStatus().getErrorString();
      Logger.info("global status: " + sGlob);

      String sJob = getJobResult().getJobStatus().getErrorString();
      Logger.info("job status: " + sJob);
      
      HBCIRetVal[] retValues = getJobResult().getJobStatus().getRetVals();
      StringBuffer sb = new StringBuffer();
      for (int i=0;i<retValues.length;++i)
      {
        Logger.info("retval[ " + i + "]: " + retValues[i].text);
        sb.append(retValues[i].code + " - " + retValues[i].text);
        if (i < (retValues.length - 1))
          sb.append(", ");
      }
      String sDetail = sb.toString();
      if (sDetail != null && sDetail.length() > 0)
        sr += System.getProperty("line.separator","\n") + sDetail;
      if (sJob != null && sJob.length() > 0)
        sr += System.getProperty("line.separator","\n") + sJob;
      if (sGlob != null && sGlob.length() > 0)
        sr += System.getProperty("line.separator","\n") + sGlob;
		}
		catch (ArrayIndexOutOfBoundsException aio)
		{
			// skip
		}
		catch (Exception e2)
		{
			Logger.error("error while reading status text",e2);
		}
    
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    if (sr != null && sr.length() > 0)
      return i18n.tr("Fehlermeldung der Bank: {0}",sr);
    return i18n.tr("Unbekannter Fehler");
	}

	/**
	 * Ueber diese Funktion koennen die konkreten Implementierungen
	 * ihre zusaetzlichen Job-Parameter setzen.
   * @param name Name des Parameters.
   * @param value Wert des Parameters.
   */
  final void setJobParam(String name, String value)
	{
		if (name == null || value == null)
		{
			Logger.warn("[job parameter] no name or value given");
			return;
		}
		params.put(name,value);
	}

	/**
	 * Speichern eines komplexes Objektes 
	 * @param name Name des Parameters.
	 * @param konto das Konto.
	 */
	final void setJobParam(String name, org.kapott.hbci.structures.Konto konto)
	{
		if (name == null || konto == null)
		{
			Logger.warn("[job parameter] no name or value given");
			return;
		}
		params.put(name,konto);
	}

  /**
	 * Speichern eines Int-Wertes.
	 * Bitte diese Funktion verwenden, damit sichergestellt ist, dass
	 * der Kernel die Werte typsicher erhaelt und Formatierungsfehler
	 * aufgrund verschiedener Locales fehlschlagen.
   * @param name Name des Parameters.
   * @param i Wert.
   */
  final void setJobParam(String name, int i)
	{
		if (name == null)
		{
			Logger.warn("[job parameter] no name given");
			return;
		}
		params.put(name,new Integer(i));
	}

  /**
	 * Speichern eines Geld-Betrages
	 * Bitte diese Funktion fuer Betraege verwenden, damit sichergestellt ist,
	 * dass der Kernel die Werte typsicher erhaelt und Formatierungsfehler
	 * aufgrund verschiedener Locales fehlschlagen.
	 * @param name Name des Parameters.
   * @param value Geldbetrag.
   * @param currency Waehrung.
	 */
	final void setJobParam(String name, double value, String currency)
	{
		if (name == null)
		{
			Logger.warn("[job parameter] no name given");
			return;
		}
		params.put(name,new Value(String.valueOf(value),currency));
	}

	/**
	 * Speichern eines Datums.
	 * Bitte diese Funktion verwenden, damit sichergestellt ist, dass
	 * der Kernel die Werte typsicher erhaelt und Formatierungsfehler
	 * aufgrund verschiedener Locales fehlschlagen.
	 * @param name Name des Parameters.
	 * @param date Datum.
	 */
	final void setJobParam(String name, Date date)
	{
		if (name == null || date == null)
		{
			Logger.warn("[job parameter] no name given or value given");
			return;
		}
		params.put(name,date);
	}
	
	/**
	 * Setzt die Job-Parameter fuer die Verwendungszweck-Zeilen.
	 * Sie werden auf die Job-Parameter usage, usage_2, usage_3,...
	 * verteilt. Wenn zwischendrin welche fehlen, werden die hinteren
	 * nach vorn geschoben.
	 * @param t der Auftrag.
	 * @throws RemoteException
	 */
	void setJobParamUsage(Transfer t) throws RemoteException
	{
	  if (t == null)
	    return;
	  
	  String[] lines = VerwendungszweckUtil.toArray(t);
	  for (int i=0;i<lines.length;++i)
	  {
	    setJobParam(HBCIUtilsInternal.withCounter("usage",i),lines[i]);
	  }
	}
  
  /**
   * Legt fest, ob der HBCI-Job exclusive (also in einer einzelnen HBCI-Nachricht) gesendet werden soll.
   * Standardmaessig ist ein Job nicht exclusiv.
   * @return true, wenn er exclusiv gesendet werden soll.
   */
  public boolean isExclusive()
  {
    return this.exclusive;
  }
  
  /**
   * Legt fest, ob der HBCI-Job exclusive (also in einer einzelnen HBCI-Nachricht) gesendet werden soll.
   * Standardmaessig ist ein Job nicht exclusiv.
   * @param exclusive
   */
  public void setExclusive(boolean exclusive)
  {
    this.exclusive = exclusive;
  }
}


/**********************************************************************
 * $Log: AbstractHBCIJob.java,v $
 * Revision 1.41  2012/03/01 22:19:15  willuhn
 * @N i18n statisch und expliziten Super-Konstruktor entfernt - unnoetig
 *
 * Revision 1.40  2011-07-28 09:01:07  willuhn
 * @B BUGZILLA 1109
 *
 * Revision 1.39  2011-07-28 08:45:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.38  2011-06-07 10:07:51  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.37  2011-05-11 16:23:57  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.36  2011-05-10 12:18:11  willuhn
 * @C Code zum Setzen der usage-Parameter in gemeinsamer Basisklasse AbstractHBCIJob - der Code war 3x identisch vorhanden
 *
 * Revision 1.35  2010-12-27 23:03:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2010-12-27 22:51:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2010-12-27 22:47:52  willuhn
 * @N BUGZILLA 964
 *
 * Revision 1.32  2010-09-02 12:25:13  willuhn
 * @N BUGZILLA 900
 *
 * Revision 1.31  2010-09-02 10:21:06  willuhn
 * @N BUGZILLA 899
 *
 * Revision 1.30  2009/01/16 22:50:00  willuhn
 * @N bugzilla token
 *
 * Revision 1.29  2009/01/16 22:44:22  willuhn
 * @B Wenn eine HBCI-Session vom User abgebrochen wurde, liefert das JobResult#isOK() u.U. trotzdem true, was dazu fuehrt, dass eine Ueberweisung versehentlich als ausgefuehrt markiert wurde. Neue Funktion "markCancelled()" eingefuehrt.
 *
 * Revision 1.28  2008/09/23 11:28:30  willuhn
 * @N Statuscode auch bei Erfolg mit loggen
 *
 * Revision 1.27  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.26  2007/12/06 23:53:56  willuhn
 * @B Bug 490
 *
 * Revision 1.25  2007/02/21 10:02:27  willuhn
 * @C Code zum Ausfuehren exklusiver Jobs redesigned
 *
 * Revision 1.24  2006/11/15 00:13:07  willuhn
 * @B Bug 327
 *
 * Revision 1.23  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 * Revision 1.22  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.21  2006/03/15 17:34:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2006/03/15 17:28:41  willuhn
 * @C Refactoring der Anzeige der HBCI-Fehlermeldungen
 *
 * Revision 1.19  2006/01/23 12:16:57  willuhn
 * @N Update auf HBCI4Java 2.5.0-rc5
 *
 * Revision 1.18  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.17  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.16  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.15  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.14  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.12  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.8  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.6  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.3  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.2  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/