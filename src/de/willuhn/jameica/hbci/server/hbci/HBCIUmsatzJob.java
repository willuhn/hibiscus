/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUmsatzJob.java,v $
 * $Revision: 1.45 $
 * $Date: 2009/03/10 17:14:40 $
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
import java.util.List;

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
import de.willuhn.jameica.system.Settings;
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
    Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    konto.addToProtokoll(i18n.tr("Umsätze abgerufen"),Protokoll.TYP_SUCCESS);

    ////////////////////////////////////////////////////////////////////////////
    // Merge-Fenster ermitteln
    Date d = null;
    if (this.saldoDatum != null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(this.saldoDatum);
      cal.add(Calendar.DATE,settings.getInt("umsatz.mergewindow.offset",-30));
      d = cal.getTime();
    }
    if (d == null)
      Logger.info("merge window: not set");
    else
      Logger.info("merge window: " + d + " - now");

    // zu mergende Umsaetze ermitteln
    DBIterator existing = konto.getUmsaetze(d,null);
    
    //
    ////////////////////////////////////////////////////////////////////////////


    
    GVRKUms result = (GVRKUms) getJobResult();

    ////////////////////////////////////////////////////////////////////////////
    // Gebuchte Umsaetze
    List lines  = result.getFlatData();
    if (lines != null && lines.size() > 0)
    {
      int created = 0;
      int skipped = 0;
      Logger.info("applying booked entries");
      for (int i=0;i<lines.size();++i)
      {
        final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
        umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt

        if (existing.contains(umsatz) != null)
        {
          skipped++;
          continue; // Haben wir schon
        }

        // Umsatz neu anlegen
        try
        {
          umsatz.store(); // den Umsatz haben wir noch nicht, speichern!
          Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
          created++;
        }
        catch (Exception e2)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Nicht alle empfangenen Umsätze konnten gespeichert werden. Bitte prüfen Sie das System-Protokoll"),StatusBarMessage.TYPE_ERROR));
          Logger.error("error while adding umsatz, skipping this one",e2);
        }
      }
      Logger.info("done. new entries: " + created + ", skipped entries (already in database): " + skipped);
    }
    else
    {
      Logger.info("got no new booked entries");
    }
    //
    ////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    // Vorgemerkte Umsaetze
    boolean fetchNotbooked = settings.getBoolean("umsatz.fetchnotbooked",true);
    lines = result.getFlatDataUnbooked();
		if (fetchNotbooked)
		{
      if (lines != null && lines.size() > 0)
      {
        cleanNotBooked(d);
        int created = 0;
        int skipped = 0;
        Logger.info("applying not-booked (vorgemerkte) entries");
        if (lines != null && lines.size() > 0)
        {
          for (int i=0;i<lines.size();++i)
          {
            final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
            umsatz.setKonto(konto);
            
            if (existing.contains(umsatz) != null)
            {
              skipped++;
              continue; // Haben wir schon
            }

            // Vormerkposten neu anlegen
            try
            {
              umsatz.setFlags(Umsatz.FLAG_NOTBOOKED);
              umsatz.store();
              Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
              created++;
            }
            catch (Exception e2)
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Nicht alle empfangenen Umsätze konnten gespeichert werden. Bitte prüfen Sie das System-Protokoll"),StatusBarMessage.TYPE_ERROR));
              Logger.error("error while adding umsatz, skipping this one",e2);
            }
          }
          Logger.info("done. new entries: " + created + ", skipped entries (already in database): " + skipped);
        }
      }
      else
      {
        Logger.info("got no new not-booked (vorgemerkte) entries");
      }
		}
		else
		{
      Logger.info("fetching of not-booked (vorgemerkte) entries disabled");
		}
    //
    ////////////////////////////////////////////////////////////////////////////

    Logger.info("umsatz list fetched successfully");
  }
  
  /**
   * Loescht die vorgemerkten Buchungen weg, damit sie neu abgerufen werden koennen.
   * @param startdate Start-Datum, ab dem nach zu loeschenden vorgemerkten Umsaetze gesucht werden soll.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private void cleanNotBooked(Date startdate) throws RemoteException, ApplicationException
  {
    Logger.info("clean notbooked entries");
    DBIterator list = this.konto.getUmsaetze(startdate,null);
    int count = 0;
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      if ((u.getFlags() & Umsatz.FLAG_NOTBOOKED) != 0)
      {
        u.delete();
        count++;
      }
    }
    Logger.info("removed entries: " + count);
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
 * Revision 1.45  2009/03/10 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.44  2009/03/10 17:11:09  willuhn
 * @N Mehr Log-Ausgaben
 *
 * Revision 1.43  2009/03/10 14:45:20  willuhn
 * @B Wenn keine neuen Umsaetze vorliegen, wurden auch keine Vormerkbuchungen abgerufen
 *
 * Revision 1.42  2009/02/25 10:37:08  willuhn
 * @C notbooked umsaetze nur aus dem angegebenen Zeitraum loeschen
 *
 * Revision 1.41  2009/02/25 10:29:59  willuhn
 * @B Pruefung des Flags nicht mehr im SQL-Statement - kann H2 nicht
 *
 * Revision 1.40  2009/02/23 17:01:58  willuhn
 * @C Kein Abgleichen mehr bei vorgemerkten Buchungen sondern stattdessen vorgemerkte loeschen und neu abrufen
 *
 * Revision 1.39  2009/02/18 10:54:45  willuhn
 * @N Abruf der vorgemerkten Umsaetze konfigurierbar
 *
 * Revision 1.38  2009/02/13 09:30:35  willuhn
 * @C Erkennung von Vormerkposten nochmal leicht ueberarbeitet
 *
 * Revision 1.37  2009/02/12 18:37:18  willuhn
 * @N Erster Code fuer vorgemerkte Umsaetze
 *
 * Revision 1.36  2009/02/12 16:14:34  willuhn
 * @N HBCI4Java-Version mit Unterstuetzung fuer vorgemerkte Umsaetze
 *
 * Revision 1.35  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.34  2008/12/15 11:01:52  willuhn
 * @C Merge komplett weglassen, wenn gar keine Umsaetze empfangen wurden
 *
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