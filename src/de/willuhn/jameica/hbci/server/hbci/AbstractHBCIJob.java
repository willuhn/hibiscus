/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/AbstractHBCIJob.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/10/25 22:39:14 $
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
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import de.willuhn.util.ApplicationException;
import de.willuhn.util.Logger;

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

	private org.kapott.hbci.GV.HBCIJob job = null;

	private Hashtable params 			= new Hashtable(); 

	/**
	 * HBCI4Java verwendet intern eindeutige Job-Namen.
	 * Diese Funktion liefert genau den Namen fuer genau den
	 * gewuenschten Job.
	 * @return Job-Identifier.
	 */
  abstract String getIdentifier();

  /**
	 * Diese Funktion wird von der HBCIFactory nach Beendigung der Kommunikation mit der Bank ausgefuehrt.
	 * Hier kann jeder HBCI-Job (also jede abgeleitete Klasse) pruefen, ob er mit
	 * dem Ergebnis zufrieden ist. Hierfuer kann sich der Job fuer gewoehnlich mittels
	 * <code>getJobResult()</code> die Ergebnis-Informationen holen und ggf eine ApplicationException
	 * werfen, wenn damit etwas nicht in Ordnung ist. 
   * @throws RemoteException
   * @throws ApplicationException
   */
  abstract void handleResult() throws RemoteException, ApplicationException;

	/**
	 * Diese Funktion wird von der HBCIFactory intern aufgerufen.
	 * Sie uebergibt hier den erzeugten HBCI-Job der Abfrage.
	 * @param job der erzeugte Job.
	 * @throws RemoteException
	 */
  final void setJob(org.kapott.hbci.GV.HBCIJob job) throws RemoteException
  {
  	this.job = job;
  	Enumeration e = params.keys();
  	while (e.hasMoreElements())
  	{
  		String name = (String) e.nextElement();
  		Object value = params.get(name);
  		if (value instanceof Konto)
	  		job.setParam(name,(Konto)value);
			else if (value instanceof Double)
			{
				double d = ((Double) value).doubleValue();
				job.setParam(name,new Value(d));
			}
			else if (value instanceof Date)
				job.setParam(name,(Date)value);
	  	else
				job.setParam(name,(String)value);
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
	 * Liefert den Status-Text, der vom HBCI-Kernel nach Ausfuehrung des Jobs zurueckgeliefert wurde.
   * @return
   */
  final String getStatusText()
	{
		try
		{
			return getJobResult().getJobStatus().getRetVals()[0].text;
		}
		catch (Exception e2)
		{
			Logger.error("error while reading status text",e2);
			return null;
		}
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
	 * Speichern eines Dezimal-Wertes.
	 * Bitte diese Funktion verwenden, damit sichergestellt ist, dass
	 * der Kernel die Werte typsicher erhaelt und Formatierungsfehler
	 * aufgrund verschiedener Locales fehlschlagen.
   * @param name Name des Parameters.
   * @param d Wert.
   */
  final void setJobParam(String name, double d)
	{
		if (name == null)
		{
			Logger.warn("[job parameter] no name given");
			return;
		}
		params.put(name,new Double(d));
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
}


/**********************************************************************
 * $Log: AbstractHBCIJob.java,v $
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