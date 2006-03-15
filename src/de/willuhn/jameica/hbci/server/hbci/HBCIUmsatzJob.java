/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUmsatzJob.java,v $
 * $Revision: 1.19 $
 * $Date: 2006/03/15 17:28:41 $
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

import org.kapott.hbci.GV_Result.GVRKUms;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.FilterEngine;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Umsatz-Abfrage".
 */
public class HBCIUmsatzJob extends AbstractHBCIJob {

	private Konto konto = null;
	private I18N i18n = null;

  /**
	 * ct.
   * @param konto Konto, fuer das die Umsaetze abgerufen werden sollen.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIUmsatzJob(Konto konto) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
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
    return "KUmsAll";
  }

  /**
   * Prueft, ob das Abrufen der Umsaetze erfolgreich war und speichert die
   * neu hinzugekommenen.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws RemoteException, ApplicationException
  {
		GVRKUms result = (GVRKUms) getJobResult();

		if (!result.isOK())
		{
			String msg = getStatusText();

			konto.addToProtokoll(i18n.tr("Fehler beim Abrufen der Umsätze: {0}",msg),Protokoll.TYP_ERROR);
			throw new ApplicationException(msg);
		}

		konto.addToProtokoll(i18n.tr("Umsätze abgerufen"),Protokoll.TYP_SUCCESS);

		// So, jetzt kopieren wir das ResultSet noch in unsere
		// eigenen Datenstrukturen.

		// Wir vergleichen noch mit den Umsaetzen, die wir schon haben und
		// speichern nur die neuen.
		DBIterator existing = konto.getUmsaetze();

		GVRKUms.UmsLine[] lines = result.getFlatData();
		Umsatz umsatz;
		for (int i=0;i<lines.length;++i)
		{
			umsatz = Converter.HBCIUmsatz2HibiscusUmsatz(lines[i]);
			umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt
      
      // Wenn keine geparsten Verwendungszwecke da sind, machen wir
      // den Umsatz editierbar.
      if(lines[i].usage == null || lines[i].usage.length == 0)
        umsatz.setChangedByUser();
      
			if (existing.contains(umsatz) == null)
			{
				try
				{
					umsatz.store(); // den Umsatz haben wir noch nicht, speichern!
          try
          {
            FilterEngine.getInstance().filter(umsatz,lines[i]);
          }
          catch (Exception e)
          {
            Logger.error("error while filtering umsatz",e);
          }
				}
				catch (Exception e2)
				{
					Logger.error("error while adding umsatz, skipping this one",e2);
				}
			}
		}

		Logger.info("umsatz list fetched successfully");

  }
}


/**********************************************************************
 * $Log: HBCIUmsatzJob.java,v $
 * Revision 1.19  2006/03/15 17:28:41  willuhn
 * @C Refactoring der Anzeige der HBCI-Fehlermeldungen
 *
 * Revision 1.18  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.17  2005/11/22 17:31:31  willuhn
 * @B NPE
 *
 * Revision 1.16  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 * Revision 1.15  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.14  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.11  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.9  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.8  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.6  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.2  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/