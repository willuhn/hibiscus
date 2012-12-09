/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUmsatzJob.java,v $
 * $Revision: 1.57 $
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kapott.hbci.GV_Result.GVRKUms;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.rewriter.RewriterRegistry;
import de.willuhn.jameica.hbci.server.hbci.rewriter.UmsatzRewriter;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
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
		try
		{
      PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
      
      this.saldoDatum = konto.getSaldoDatum();
      if (this.saldoDatum != null)
      {
        // BUGZILLA 917 - checken, ob das Datum vielleicht in der Zukunft liegt. Das ist nicht zulaessig
        Date now = new Date();
        if (saldoDatum.after(now))
        {
          Logger.warn("future start date " + saldoDatum + " given. this is not allowed, changing to current date " + now);
          this.saldoDatum = now;
        }
        
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
        
        this.saldoDatum = DateUtil.startOfDay(this.saldoDatum);
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

    // In HBCI gibts fuer Umsaetze ja keine eindeutigen IDs. Daher muessen
    // wir anhand der Eigenschaften vergleichen, ob wir den Umsatz schon
    // haben oder nicht. Zwei Umsaetze mit gleichen Eigenschaften werden
    // von Hibiscus daher als "der selbe" erkannt und nicht erneut in der Datenbank
    // angelegt. In 99% der Faelle ist das auch korrekt. Unter Umstaenden kann
    // eine Buchung jedoch tatsaechlich identisch aussehen und trotzdem nicht
    // die selbe sein. Da den Banken diese Problematik ebenfalls bekannt ist,
    // verweigern die meisten das Einreichen von mehreren identischen Auftraegen
    // innerhalb eines Tages. Allerdings machen das nicht alle Banken. Und manche
    // tolerieren es auch, wenn man den Auftrag nach Erhalt des Doppel-Einreichungs-
    // Fehlers nochmal einreicht. Beim Abruf der Umsaetze ist auch das meist kein
    // Problem. Denn wir vergleichen hier nur gegen die bereits in der Datenbank
    // vorhandenen Umsaetze. Kommen zwei identisch aussehende innerhalb eines
    // Umsatz-Abrufs von der Bank, dann werden beide angelegt, weil keiner von
    // beiden bereits in der Datenbank ist. Kommen sie jedoch durch zwei getrennte
    // Abrufe, dann wuerde der zweite Umsatz nicht mehr angelegt werden, weil
    // Hibiscus der Meinung ist, diesen Umsatz bereits in der DB zu haben.
    // Genau hierfuer ist diese Map da. Wenn ein Umsatz bereits in der Datenbank
    // gefunden wurde, wird er nicht erneut angelegt, zusaetzlich jedoch in dieser
    // Map gespeichert. Kommt dann innerhalb des selben Abrufes nochmal ein
    // Umsatz, der bereits in der Datenbank ist, zusaetzlich aber auch bereits
    // in dieser Map hier steht, dann kann es nicht der selbe sein sondern muss
    // tatsaechlich ein neuer - identisch aussehender - sein.
    Map<Umsatz,Umsatz> duplicates = new HashMap<Umsatz,Umsatz>();
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Gebuchte Umsaetze
    List lines  = result.getFlatData();
    if (lines != null && lines.size() > 0)
    {
      int created = 0;
      int skipped = 0;
      Logger.info("applying booked entries");
      
      UmsatzRewriter rewriter = RewriterRegistry.getRewriter(konto.getBLZ(),konto.getKontonummer());
      
      for (int i=0;i<lines.size();++i)
      {
        final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
        umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt

        if (rewriter != null)
        {
          try
          {
            rewriter.rewrite(umsatz);
          }
          catch (Exception e) {
            Logger.error("error while rewriting umsatz",e);
          }
        }

        Umsatz fromDB = (Umsatz) existing.contains(umsatz);
        if (fromDB != null)
        {
          // Wir duerfen den Umsatz nur dann ueberspringen, wenn wir ihn noch
          // nicht in der duplicates Map haben. Andernfalls sieht er zwar anders
          // aus, ist aber wirklich ein neuer
          if (duplicates.get(fromDB) == null)
          {
            // In die duplicates-Map tun. Wenn dann noch einer kommt,
            // der genauso aussieht, wird der nicht mehr uebersprungen,
            // weil er wirklich neu ist
            duplicates.put(fromDB,umsatz);
            skipped++;
            continue;
          }
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

    duplicates.clear();

    ////////////////////////////////////////////////////////////////////////////
    // Vorgemerkte Umsaetze
    boolean fetchNotbooked = settings.getBoolean("umsatz.fetchnotbooked",true);
    lines = result.getFlatDataUnbooked();
		if (fetchNotbooked)
		{
      if (lines != null && lines.size() > 0)
      {
        List<Umsatz> fetched = new ArrayList<Umsatz>();
        
        int created = 0;
        int skipped = 0;
        Logger.info("applying not-booked (vorgemerkte) entries");
        for (int i=0;i<lines.size();++i)
        {
          final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
          umsatz.setFlags(Umsatz.FLAG_NOTBOOKED);
          umsatz.setSaldo(0d); // Muss gemacht werden, weil der Saldo beim naechsten Mal anders lauten koennte
          umsatz.setKonto(konto);
          fetched.add(umsatz);

          Umsatz fromDB = (Umsatz) existing.contains(umsatz);
          if (fromDB != null)
          {
            // Wir duerfen den Umsatz nur dann ueberspringen, wenn wir ihn noch
            // nicht in der duplicates Map haben. Andernfalls sieht er zwar anders
            // aus, ist aber wirklich ein neuer
            if (duplicates.get(fromDB) == null)
            {
              // In die duplicates-Map tun. Wenn dann noch einer kommt,
              // der genauso aussieht, wird der nicht mehr uebersprungen,
              // weil er wirklich neu ist
              duplicates.put(fromDB,umsatz);
              skipped++;
              continue;
            }
          }

          // Vormerkposten neu anlegen
          try
          {
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
        
        // Jetzt loeschen wir all die vorgemerkten Umsaetze des
        // Kontos, die noch in der Datenbank sind, aber im
        // aktuellen Durchlauf nicht mehr uebertragen wurden.
        // Das muessen dann die vom Vortag sein
        Logger.info("clean obsolete notbooked entries");
        GenericIterator newList = PseudoIterator.fromArray((Umsatz[]) fetched.toArray(new Umsatz[fetched.size()]));
        int deleted = 0;
        existing.begin();
        while (existing.hasNext())
        {
          Umsatz u = (Umsatz) existing.next();
          if ((u.getFlags() & Umsatz.FLAG_NOTBOOKED) != 0)
          {
            // Ist ein vorgemerkter Umsatz. Mal schauen, ob der im aktuellen
            // Durchlauf enthalten war:
            if (newList.contains(u) == null)
            {
              // Wurde nicht mehr von der Bank uebertragen, kann daher raus
              u.delete();
              deleted++;
            }
          }
        }
        Logger.info("removed entries: " + deleted);
        Logger.info("done. new entries: " + created + ", skipped entries (already in database): " + skipped);
      }
      else
      {
        Logger.info("got no new not-booked (vorgemerkte) entries");
        
        // Keine neuen vorgemerkten Umsaetze 
        Logger.info("clean obsolete not-booked entries");
        Date current = DateUtil.startOfDay(new Date());
        int count = 0;
        existing.begin();
        while (existing.hasNext())
        {
          Umsatz u = (Umsatz) existing.next();
          if ((u.getFlags() & Umsatz.FLAG_NOTBOOKED) == 0)
            continue;

          Date test = u.getDatum();
          if (test == null)
            test = u.getValuta();
          
          if (test == null)
          {
            Logger.warn("notbooked entry contains no date, skipping");
            continue; // Das darf eigentlich nicht passieren
          }
          
          // Wenn die Vormerkbuchung nicht von heute ist, loeschen wir sie
          if (test.before(current))
          {
            u.delete();
            count++;
          }
        }
        Logger.info("removed entries: " + count);
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
 * Revision 1.57  2012/03/01 22:19:15  willuhn
 * @N i18n statisch und expliziten Super-Konstruktor entfernt - unnoetig
 *
 * Revision 1.56  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.55  2010-10-11 21:25:42  willuhn
 * @B Da das Notbooked-Flag jetzt Bestandteil der Checksumme ist, muss das Flag vor dem contains() gemacht werden
 *
 * Revision 1.54  2010-10-07 21:02:36  willuhn
 * @B BUGZILLA 917
 *
 * Revision 1.53  2010-08-27 11:36:48  willuhn
 * @R unnoetiges if
 *
 * Revision 1.52  2010/04/25 23:09:04  willuhn
 * @N BUGZILLA 244
 *
 * Revision 1.51  2009/03/13 17:58:07  willuhn
 * @N Loeschen der Vormerkbuchungen (die aelter als heute sind) auch dann, wenn keine neuen von der Bank gekommen sind
 *
 * Revision 1.50  2009/03/12 10:56:01  willuhn
 * @B Double.NaN geht nicht
 *
 * Revision 1.49  2009/03/11 17:53:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.48  2009/03/11 17:51:14  willuhn
 * @B Saldo wurde an der falschen Stelle zurueckgesetzt
 *
 * Revision 1.47  2009/03/11 16:21:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.46  2009/03/11 11:03:38  willuhn
 * @C Nur noch jene Vormerkposten loeschen, die nicht mehr von der Bank uebertragen wurden
 *
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