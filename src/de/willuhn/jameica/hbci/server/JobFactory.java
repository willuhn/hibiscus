/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/JobFactory.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/04/04 18:30:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.status.HBCIDialogStatus;
import org.kapott.hbci.status.HBCIExecStatus;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig.
 * <br>
 * Sie kriegt i.d.R. Fach-Objekte uebergeben und entnimmt von dort
 * die notwendigen Informationen fuer die HBCI-Jobs.<br>
 * <b>Hinweis:</b>: Die Factory speichert grundsaetzlich keine Objekte
 * in der Datenbank. Das ist Sache des Aufrufers. Hier werden lediglich
 * die HBCI-Jobs ausgefuehrt.
 * TODO: Die Jobs muessten mal noch in extra Klassen aufgeteilt werden.
 */
public class JobFactory {


	/**
	 * HBCI4Java-interner Name fuer &quot;Saldo abrufen&quot.
	 */
	public final static String JOB_KONTO_SALDO 					= "SaldoReq";
	
	/**
	 * HBCI4Java-interner Name fuer &quot;Ueberweisung ausfuehren&quot.
	 */
	public final static String JOB_UEBERWEISUNG_EXECUTE = "TODO";

	private static I18N i18n;
	private static JobFactory factory;
  private static boolean inProgress = false;

  /**
   * ct.
   */
  private JobFactory() {
  	i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Erzeugt eine neue Instanz der JobFactory oder liefert die
	 * existierende zurueck.
   * @return Instanz der Job-Factory.
   */
  public static synchronized JobFactory getInstance()
	{
		if (factory != null)
			return factory;

		factory = new JobFactory();
		return factory;			
	}

	/**
	 * Fuehrt eine Salden-Abfrage fuer das Konto durch.
   * @param konto das Konto.
   * @return der Saldo.
   * @throws RemoteException wenn ein interner Fehler aufgetreten ist.
   * @throws ApplicationException wenn ein Benutzerfehler aufgetreten ist.
   * Der Text dieser Exception muss dem Benutzer angezeigt werden.
   */
  public synchronized double getSaldo(Konto konto) throws ApplicationException, RemoteException
	{

		if (konto == null)
			throw new ApplicationException(i18n.tr("Bitte geben Sie ein Konto an, für welches Sie den Saldo ermitteln wollen"));

		HBCIDialogStatus statusMsg = null;
		String statusText = null;
		Passport passport = null;

		start();

		try {
			passport = konto.getPassport();
			HBCIHandler handler = passport.open();

			Application.getLog().info("creating new job " + JOB_KONTO_SALDO);
			HBCIJob job = handler.newJob(JOB_KONTO_SALDO);
			job.setParam("my",Converter.convert(konto));

			Application.getLog().info("adding job to queue");
			handler.addJob(konto.getKundennummer(),job);

			Application.getLog().info("submitting job to bank");
			HBCIExecStatus status = handler.execute();
			statusMsg = status.getDialogStatus(konto.getKundennummer());

			try {
				statusText = statusMsg.initStatus.segStatus.getRetVals()[0].text;
			}
			catch (Exception e) {/*useless*/}

			Application.getLog().info("retrieving job result");
			GVRSaldoReq result = (GVRSaldoReq) job.getJobResult();
			if (!result.isOK())
			{
				Application.getLog().error("got a dirty result: " + statusMsg.toString());
				throw new ApplicationException(
					statusText != null ?
						i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
						i18n.tr("Fehler bei der Ermittlung des Saldos"));
			}
			Application.getLog().info("job result is ok, returning saldo");
			return result.getEntries()[0].ready.value.value;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		finally
		{
			stop();
			try {
				passport.close();
			}
			catch (Throwable t) {/* useless*/}
		}
	}
	
	/**
	 * Fuehrt die angegebene Ueberweisung zum angegebenen Termin aus.
	 * Ein boolscher Rueckgabe-Wert ist nicht noetig. Entweder hat
	 * die Ueberweisung geklappt oder es wird eine Exception geworfen. 
   * @param u die Ueberweisung.
   * @throws RemoteException wenn ein interner Fehler aufgetreten ist.
   * @throws ApplicationException wenn ein Benutzerfehler aufgetreten ist.
   * Der Text dieser Exception muss dem Benutzer angezeigt werden.
   */
  public synchronized void execute(Ueberweisung u) throws ApplicationException, RemoteException
	{
		if (u == null)
			throw new ApplicationException(i18n.tr("Bitte geben Sie die Überweisungan, " +				"welche Sie ausführen möchten"));

		if (u.ausgefuehrt())
			throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt."));

//		HBCIDialogStatus statusMsg = null;
//		String statusText = null;
//		Passport passport = null;
//
//		start();
//
//		try {
//
//			Konto konto = u.getKonto();
//			
//			passport = konto.getPassport();
//			HBCIHandler handler = passport.open();
//
//			Application.getLog().info("creating new job " + JOB_UEBERWEISUNG_EXECUTE);
//			HBCIJob job = handler.newJob(JOB_UEBERWEISUNG_EXECUTE);
//			job.setParam("my",((KontoImpl)konto).getHBCIKonto());
//
//			Application.getLog().info("adding job to queue");
//			handler.addJob(konto.getKundennummer(),job);
//
//			Application.getLog().info("submitting job to bank");
//			HBCIExecStatus status = handler.execute();
//			statusMsg = status.getDialogStatus(konto.getKundennummer());
//
//			try {
//				statusText = statusMsg.initStatus.segStatus.getRetVals()[0].text;
//			}
//			catch (Exception e) {/*useless*/}
//
//			Application.getLog().info("retrieving job result");
//			GVRSaldoReq result=(GVRSaldoReq) job.getJobResult();
//			if (!result.isOK())
//			{
//				Application.getLog().error("got a dirty result: " + statusMsg.toString());
//				throw new ApplicationException(
//					statusText != null ?
//						i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
//						i18n.tr("Fehler bei der Ermittlung des Saldos"));
//			}
//			Application.getLog().info("job result is ok, returning saldo");
//			return result.getEntries()[0].ready.value.value;
//		}
//		catch (RemoteException e)
//		{
//			throw e;
//		}
//		finally
//		{
//			stop();
//			try {
//				passport.close();
//			}
//			catch (Throwable t) {/* useless*/}
//		}
	}

  /**
	 * Holt <b>alle verfuegbaren</b> Umsaetze vom uebergebenen Konto ab und liefert sie als
	 * Array von Objekten des Typs <code>Umsatz</code> zurueck.
	 * <br>
	 * Eine zusaetzliche zeitliche Einschraenkung ist vorerst nicht geplant.
	 * @param konto das Konto, fuer welches die Umsaetze abgeholt werden.
	 * @return die Umsaetze.
	 * @throws ApplicationException
	 * @throws RemoteException
	 */
	public synchronized Umsatz[] getUmsaetze(Konto konto) throws ApplicationException, RemoteException
	{
		if (konto == null)
			throw new ApplicationException(i18n.tr("Bitte geben Sie ein Konto an, für welches Sie die Umsätze ermitteln wollen"));

		HBCIDialogStatus statusMsg = null;
		String statusText = null;
		Passport passport = null;

		start();

		try {

			passport = konto.getPassport();
			HBCIHandler handler = passport.open();

			Application.getLog().info("creating new job KUmsAll");
			HBCIJob job = handler.newJob("KUmsAll"); // KUmsNew wird von meiner SPK nicht unterstuetzt.
			job.setParam("my",Converter.convert(konto));

			Application.getLog().info("adding job to queue");
			handler.addJob(konto.getKundennummer(),job);

			Application.getLog().info("submitting job to bank");
			HBCIExecStatus status = handler.execute();

			statusMsg = status.getDialogStatus(konto.getKundennummer());

			try {
				statusText = statusMsg.initStatus.segStatus.getRetVals()[0].text;
			}
			catch (Exception e) {/*useless*/}

			Application.getLog().info("retrieving job result");

			GVRKUms result = (GVRKUms) job.getJobResult();

			if (!result.isOK())
			{
				Application.getLog().error("got a dirty result: " + statusMsg.toString());
				throw new ApplicationException(
					statusText != null ?
						i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
						i18n.tr("Fehler beim Abrufen der Umsätze"));
			}
			Application.getLog().info("job result is ok, returning saldo");

			// So, jetzt kopieren wir das ResultSet noch in unsere
			// eigenen Datenstrukturen. ;)
			GVRKUms.UmsLine[] lines = result.getFlatData();
			Umsatz[] umsaetze = new Umsatz[lines.length];
			for (int i=0;i<lines.length;++i)
			{
				umsaetze[i] = Converter.convert(lines[i]);
				umsaetze[i].setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt
			}
			return umsaetze;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		finally
		{
			stop();
			try {
				passport.close();
			}
			catch (Throwable t) {/* useless*/}
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
 * $Log: JobFactory.java,v $
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