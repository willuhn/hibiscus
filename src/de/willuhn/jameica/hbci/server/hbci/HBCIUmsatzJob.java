/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUmsatzJob.java,v $
 * $Revision: 1.33 $
 * $Date: 2008/12/15 10:57:44 $
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
import java.util.Calendar;
import java.util.Date;

import org.kapott.hbci.GV_Result.GVRKUms;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Umsatz-Abfrage".
 */
public class HBCIUmsatzJob extends AbstractHBCIJob
{

	private Konto konto     = null;
	private Date saldoDatum = null;

  /**
	 * ct.
   * @param konto Konto, fuer das die Umsaetze abgerufen werden sollen.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIUmsatzJob(Konto konto) throws ApplicationException, RemoteException
	{
    super();
		try
		{
      PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
			i18n = res.getI18N();

			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
      
      this.saldoDatum = konto.getSaldoDatum();
      if (saldoDatum != null)
      {
        // Mal schauen, ob wir ein konfiguriertes Offset haben
        int offset = res.getSettings().getInt("umsatz.startdate.offset",0);
        if (offset != 0)
        {
          Logger.info("using custom offset for startdate: " + offset);
          Calendar cal = Calendar.getInstance();
          cal.setTime(this.saldoDatum);
          cal.add(Calendar.DATE,offset);
          this.saldoDatum = cal.getTime();
        }
        
        this.saldoDatum = HBCIProperties.startOfDay(this.saldoDatum);
        Logger.info("startdate: " + HBCI.LONGDATEFORMAT.format(this.saldoDatum));
        setJobParam("startdate", this.saldoDatum);
      }

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;
      setJobParam("my.curr",curr);
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
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Umsatzabruf {0}",konto.getLongName());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
		GVRKUms result = (GVRKUms) getJobResult();
		konto.addToProtokoll(i18n.tr("Umsätze abgerufen"),Protokoll.TYP_SUCCESS);

		// So, jetzt kopieren wir das ResultSet noch in unsere
		// eigenen Datenstrukturen.

		// Wir vergleichen noch mit den Umsaetzen, die wir schon haben und
		// speichern nur die neuen.
		Date d = null;
		if (this.saldoDatum != null)
		{
      PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
      Calendar cal = Calendar.getInstance();
      cal.setTime(this.saldoDatum);
      cal.add(Calendar.DATE,res.getSettings().getInt("umsatz.mergewindow.offset",-30));
      d = cal.getTime();
		}
    Logger.info("merge window: " + d + " - " + new Date());
		DBIterator existing = konto.getUmsaetze(d,null);

		GVRKUms.UmsLine[] lines = result.getFlatData();
		for (int i=0;i<lines.length;++i)
		{
			final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz(lines[i]);
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
          Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
				}
				catch (Exception e2)
				{
				  Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Nicht alle empfangenen Umsätze konnten gespeichert werden. Bitte prüfen Sie das System-Protokoll"),StatusBarMessage.TYPE_ERROR));
					Logger.error("error while adding umsatz, skipping this one",e2);
				}
			}
		}

		Logger.info("umsatz list fetched successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Abrufen der Umsätze: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}


/**********************************************************************
 * $Log: HBCIUmsatzJob.java,v $
 * Revision 1.33  2008/12/15 10:57:44  willuhn
 * @N Beim Synchronisieren mit den vorhandenen Umsaetzen nicht mehr mit allen Umsaetzen des Kontos vergleichen sondern nur noch mite den relevanten Daten aus dem "Merge-Window". Das umfasst den Bereich ab ${startdatum} - 30 Tage
 *
 * Revision 1.32  2008/11/25 00:52:38  willuhn
 * @N Wenn Auslandsueberweisungen nicht gespeichert werden konnten, dann wenigstens einen Hinweis melden
 *
 * Revision 1.31  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.30  2008/02/18 15:17:28  willuhn
 * @N offset in startdate bei Umsatzabruf konfigurierbar (via "umsatz.startdate.offset")
 *
 * Revision 1.29  2007/12/11 13:46:48  willuhn
 * @N Waehrung auch bei Saldo-Abfrage - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=43618#43618
 *
 * Revision 1.28  2007/12/11 13:17:26  willuhn
 * @N Waehrung bei Umsatzabfrage - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=43618#43618
 *
 * Revision 1.27  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 **********************************************************************/