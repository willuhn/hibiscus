/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragDeleteJob.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/11/18 23:46:21 $
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
import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.hbci.tests.CanTermDelRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Dauerauftrag loeschen".
 */
public class HBCIDauerauftragDeleteJob extends AbstractHBCIJob
{

	private I18N i18n 								= null;
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der geloescht werden soll
   * @param date Datum, zu dem der Auftrag geloescht werden soll oder <code>null</code>
   * wenn zum naechstmoeglichen Zeitpunkt geloescht werden soll.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public HBCIDauerauftragDeleteJob(Dauerauftrag auftrag, Date date) throws RemoteException, ApplicationException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (auftrag == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Dauerauftrag aus"));

			if (!auftrag.isActive())
				throw new ApplicationException(i18n.tr("Dauerauftrag liegt nicht bei Bank vor und muss daher nicht online gelöscht werden"));

			if (auftrag.isNewObject())
				auftrag.store();

			this.dauerauftrag = auftrag;
			this.konto        = auftrag.getKonto();

			setJobParam("orderid",auftrag.getOrderID());

			if (date != null)
			{
				// Jetzt noch die Tests fuer die Job-Restriktionen
				Properties p = HBCIFactory.getInstance().getJobRestrictions(this,this.konto.getPassport().getHandle());
				Enumeration keys = p.keys();
				while (keys.hasMoreElements())
				{
					String s = (String) keys.nextElement();
					Logger.debug("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
				}

				Logger.info("target date for DauerDel: " + date.toString());
				new CanTermDelRestriction(p).test(); // Test nur, wenn Datum angegeben
				setJobParam("date",date);
			}

			// Den brauchen wir, damit das Loeschen funktioniert.
			HBCIFactory.getInstance().addJob(new HBCIDauerauftragListJob(this.konto));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (ApplicationException e2)
		{
			throw e2;
		}
		catch (Throwable t)
		{
			Logger.error("error while executing job " + getIdentifier(),t);
			throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Auftrags. Fehlermeldung: {0}",t.getMessage()),t);
		}
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "DauerDel";
  }


	/**
	 * Prueft, ob das Loeschen bei der Bank erfolgreich war und loescht den
	 * Dauerauftrag anschliessend in der Datenbank.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
	void handleResult() throws ApplicationException, RemoteException
	{
		String statusText = getStatusText();

		String empfName = i18n.tr("an") + " " + dauerauftrag.getEmpfaengerName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Löschen des Dauerauftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}

		konto.addToProtokoll(i18n.tr("Dauerauftrag gelöscht") + " " + empfName,Protokoll.TYP_SUCCESS);

		dauerauftrag.delete();

		Logger.info("dauerauftrag deleted successfully");
	}

}


/**********************************************************************
 * $Log: HBCIDauerauftragDeleteJob.java,v $
 * Revision 1.8  2004/11/18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.4  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/