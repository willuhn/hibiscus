/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/JobFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/02/21 19:49:04 $
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
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.status.HBCIDialogStatus;
import org.kapott.hbci.status.HBCIExecStatus;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Diese Klasse ist fuer die Ausfuehrung der HBCI-Jobs zustaendig.
 */
public class JobFactory {

	private static boolean inProgress = false;
	private static Passport passport = null;

  /**
   * ct.
   */
  private JobFactory() {
  }

	/**
	 * Fuehrt eine Salden-Abfrage fuer das Konto durch.
   * @param konto das Konto.
   * @return der Saldo.
   * @throws RemoteException wenn ein interner Fehler aufgetreten ist.
   * @throws ApplicationException wenn ein Benutzerfehler aufgetreten ist.
   * Der Text dieser Exception muss dem Benutzer angezeigt werden.
   */
  public static synchronized double getSaldo(Konto konto) throws ApplicationException, RemoteException
	{

		if (inProgress)
		{
			throw new ApplicationException("Es läuft bereits eine andere HBCI-Abfrage.");
		}

		inProgress = true;

		GUI.startProgress();

		if (konto == null)
			throw new RemoteException("Konto is null");

		HBCIDialogStatus statusMsg = null;
		String statusText = null;
		try {
			passport = konto.getPassport();
			HBCIHandler handler = passport.open();

			Application.getLog().info("creating new job SaldoReq");
			HBCIJob job = handler.newJob("SaldoReq");
			job.setParam("my",((KontoImpl)konto).getHBCIKonto());

			Application.getLog().info("adding job SaldoReq to queue");
			handler.addJob(konto.getKundennummer(),job);

			Application.getLog().info("submitting job to bank");
			HBCIExecStatus status = handler.execute();
			statusMsg = status.getDialogStatus(konto.getKundennummer());

			try {
				statusText = statusMsg.initStatus.segStatus.getRetVals()[0].text;
			}
			catch (Exception e) {/*useless*/}

			Application.getLog().info("retrieving job result");
			GVRSaldoReq result=(GVRSaldoReq) job.getJobResult();
			if (!result.isOK())
			{
				Application.getLog().error("got a dirty result: " + statusMsg.toString());
				throw new RemoteException(statusText != null ? ("Fehlermeldung der Bank: " + statusText) : "Fehler bei der Ermittlung des Saldos");
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
			close();
		}
	}
	
	/**
	 * Fuehrt die angegebene Ueberweisung sofort aus.
   * @param u die Ueberweisung.
   * @throws RemoteException
   */
  public static synchronized void execute(Ueberweisung u) throws RemoteException
	{
	}

	/**
	 * Schliesst den aktuellen Job bzw. bricht ihn ab, falls noch einer laeuft.
   */
  public static synchronized void close()
	{
		inProgress = false;
		try {
			passport.close();
		}
		catch (Exception e)
		{
			Application.getLog().error("error while closing job",e);
		}
		GUI.stopProgress();
	}
}


/**********************************************************************
 * $Log: JobFactory.java,v $
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