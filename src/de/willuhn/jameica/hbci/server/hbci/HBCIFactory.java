/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIFactory.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/07/25 17:15:06 $
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
import java.util.ArrayList;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig.
 * <b>Hinweis:</b>: Die Factory speichert grundsaetzlich keine Objekte
 * in der Datenbank. Das ist Sache des Aufrufers. Hier werden lediglich
 * die HBCI-Jobs ausgefuehrt.
 */
public class HBCIFactory {


	private static boolean inProgress = false;

	private static I18N i18n;
	private static HBCIFactory factory;
  	private ArrayList jobs = new ArrayList(); 

  /**
   * ct.
   */
  private HBCIFactory() {
  	i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Erzeugt eine neue Instanz der HBCIFactory oder liefert die
	 * existierende zurueck.
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
  public synchronized void addJob(de.willuhn.jameica.hbci.rmi.hbci.HBCIJob job) throws ApplicationException
	{
		if (inProgress)
			throw new ApplicationException("Es läuft bereits eine andere HBCI-Abfrage.");

		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	/**
	 * Fuehrt alle Jobs aus, die bis dato geadded wurden.
	 * @param handle der Passport, ueber den die Jobs ausgefuehrt werden sollen.
	 * @throws ApplicationException
	 * @throws RemoteException
	 */
	public synchronized void executeJobs(PassportHandle handle) throws ApplicationException, RemoteException
	{

		if (handle == null)
			throw new ApplicationException(i18n.tr("Kein HBCI-Medium ausgewählt"));

		synchronized(jobs)
		{

			if (jobs.size() == 0)
			{
				Logger.warn("no jobs defined");
				return;
			}

			start();

			try {
				HBCIHandler handler = handle.open();

				Logger.info("creating jobs");

				for (int i=0;i<jobs.size();++i)
				{
					de.willuhn.jameica.hbci.rmi.hbci.HBCIJob job = (de.willuhn.jameica.hbci.rmi.hbci.HBCIJob) jobs.get(i);
					
					Konto konto = job.getKonto();
					if (konto == null)
					{
						Logger.warn("no konto defined in job " + job.getIdentifier() + ", skipping");
						continue;
					}

					Logger.info("adding job " + job.getIdentifier() + " to queue");
					HBCIJob j = handler.newJob(job.getIdentifier());
					job.setJob(j);
					handler.addJob(j);
				}

				Logger.info("executing jobs");
				handler.execute();
			}
			catch (RemoteException e)
			{
				throw e;
			}
			finally
			{
				stop();
				jobs = new ArrayList(); // Jobqueue leer machen.
				try {
					handle.close();
				}
				catch (Throwable t) {/* useless*/}
			}


		}
	}

	/**
	 * Schliesst den aktuellen Job.
	 * Muss von jeder Funktion in diese Factory aufgerufen werden, wenn Sie mit
	 * ihrer Taetigkeit fertig ist (daher sinnvollerweise im finally()) um
	 * die Factory fuer die naechsten Jobs freizugeben.
   */
  private synchronized void stop()
	{
		inProgress = false;
	}
	
	/**
	 * Setzt die Factory auf den Status &quot;inProgress&quot; oder wirft
	 * eine ApplicationException, wenn gerade ein anderer Job laeuft.
	 * Diese Funktion muss von jeder Funktion der Factory ausgefuehrt werden,
	 * bevor sie mit ihrer Taetigkeit beginnt. Somit ist sichergestellt,
	 * dass nie zwei Jobs gleichzeitig laufen.
   * @throws ApplicationException
   */
  private synchronized void start() throws ApplicationException
	{
		if (inProgress)
			throw new ApplicationException("Es läuft bereits eine andere HBCI-Abfrage.");

		inProgress = true;
	}
}


/**********************************************************************
 * $Log: HBCIFactory.java,v $
 * Revision 1.10  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.9  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.6  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.4  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
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
 * Revision 1.1  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.7  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.6  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.5  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.4  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.3  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/