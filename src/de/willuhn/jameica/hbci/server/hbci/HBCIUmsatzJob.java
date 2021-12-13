/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.comm.Comm;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.KontoUtil;
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
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

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
				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;
			
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        konto.setWaehrung(HBCIProperties.CURRENCY_DEFAULT_DE);
			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
      
      this.saldoDatum = konto.getSaldoDatum();
      if (this.saldoDatum != null)
      {
        // Mal schauen, ob wir ein konfiguriertes Offset haben
        int offset = res.getSettings().getInt("umsatz.startdate.offset", 0);
        if (offset != 0)
        {
          Logger.info("using custom offset for startdate: " + offset);
          Calendar cal = Calendar.getInstance();
          cal.setTime(this.saldoDatum);
          cal.add(Calendar.DATE, offset);
          this.saldoDatum = cal.getTime();
        }

        // BUGZILLA 917 - checken, ob das Datum vielleicht in der Zukunft liegt. Das ist nicht zulaessig
        Date now = new Date();
        if (saldoDatum.after(now))
        {
          Logger.warn("future start date " + saldoDatum + " given. this is not allowed, changing to current date " + now);
          this.saldoDatum = now;
        }
        else
        {
          // andernfalls pruefen, ob das Datum innerhalb der von der Bank erlaubten Zeitspanne liegt
          int timeRange = KontoUtil.getUmsaetzeTimeRange(konto, true);
          if (timeRange > 0)
          {
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.DATE, -timeRange);
            Date earliestDate = cal.getTime();
            if (saldoDatum.before(earliestDate))
            {
              Logger.warn("start date " + saldoDatum + " is more than " + timeRange + " days ago. this is not allowed, changing to earliest date " + earliestDate);
              this.saldoDatum = earliestDate;
            }
          }
        }
        
        this.saldoDatum = DateUtil.startOfDay(this.saldoDatum);
        Logger.info("startdate: " + HBCI.LONGDATEFORMAT.format(this.saldoDatum));
        setJobParam("startdate", this.saldoDatum);
      }
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

  @Override
  protected HibiscusDBObject getContext()
  {
    return this.konto;
  }

  @Override
  public String getIdentifier() {
    return KontoUtil.useCamt(this.konto,true) ? "KUmsAllCamt" : "KUmsAll";
  }

  @Override
  public String getName() throws RemoteException
  {
    return i18n.tr("Umsatzabruf {0}",konto.getLongName());
  }
  
  @Override
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    konto.addToProtokoll(i18n.tr("Ums�tze abgerufen"),Protokoll.TYP_SUCCESS);

    // In HBCI gibts fuer Umsaetze ja keine eindeutigen IDs. Daher muessen
    // wir anhand der Eigenschaften vergleichen, ob wir den Umsatz schon
    // haben oder nicht. Mehrere Umsaetze mit gleichen Eigenschaften werden
    // von Hibiscus daher als "der selbe" erkannt und nicht erneut in der Datenbank
    // angelegt. In 99% der Faelle ist das auch korrekt. Unter Umstaenden kann
    // eine Buchung jedoch tatsaechlich identisch aussehen und trotzdem nicht
    // die selbe sein. Da den Banken diese Problematik ebenfalls bekannt ist,
    // verweigern die meisten das Einreichen von mehreren identischen Auftraegen
    // innerhalb eines Tages. Allerdings machen das nicht alle Banken. Und manche
    // tolerieren es auch, wenn man den Auftrag nach Erhalt des Doppel-Einreichungs-
    // Fehlers nochmal einreicht. Wir haben hierzu eine Map, die fuer mehrfach
    // vorhandene Umsaetze zaehlt, wie oft sie bereits lokal in der DB vorliegen.
    // Schickt die Bank mehr, als wir in der DB haben, muessen wir die verbleibenden
    // noch anlegen.
    Map<Umsatz,Integer> duplicates = new HashMap<Umsatz,Integer>();
    
    boolean fetchUnbooked  = settings.getBoolean("umsatz.fetchnotbooked",true);

    GVRKUms result         = (GVRKUms) getJobResult();
    List<UmsLine> booked   = result.getFlatData();
    List<UmsLine> unbooked = fetchUnbooked ? result.getFlatDataUnbooked() : null;
    Date d                 = this.getMergeWindow(booked,unbooked);

    // zu mergende Umsaetze ermitteln
    DBIterator existing = konto.getUmsaetze(d,null);
    
    ////////////////////////////////////////////////////////////////////////////
    // Gebuchte Umsaetze
    if (booked != null && booked.size() > 0)
    {
      this.dumpCamt(result.camtBooked,true);
      
      int created = 0;
      int skipped = 0;
      Logger.info("applying booked entries");
      
      UmsatzRewriter rewriter = RewriterRegistry.getRewriter(konto.getBLZ(),konto.getKontonummer());
      
      for (int i=0;i<booked.size();++i)
      {
        final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)booked.get(i));
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

        Umsatz fromDB = null;
        // Anzahl der vorhandenen Umsaetze in der DB zaehlen
        int counter = 0;
        existing.begin();
        for (int j = 0; j<existing.size(); j++)
        {
          GenericObject dbObject = existing.next();
          if (dbObject.equals(umsatz)) {
            counter++;
            fromDB = (Umsatz) dbObject; //wir merken uns immer den letzten Umsatz
          }
        }
        
        if (fromDB != null)
        {
          // Wir duerfen den Umsatz nur dann ueberspringen, wenn er bereits 
          // OFT GENUG in der Datenbank ist. Andernfalls ist er tatsaechlich 
          // neu. Dazu zaehlen wir mit, wie oft wir gerade einen "gleichen" 
          // Umsatz empfangen haben. 
          Integer countInCurrentJobResult = duplicates.get(fromDB);
          if (countInCurrentJobResult == null) {
            duplicates.put(fromDB, 1);
            skipped++;
            continue;
          }
          else if (countInCurrentJobResult <= counter)  
          {
            // In der Datenbank sind mehr als bislang abgerufen -> Ueberspringen
            duplicates.put(fromDB, countInCurrentJobResult+1);
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
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Nicht alle empfangenen Ums�tze konnten gespeichert werden. Bitte pr�fen Sie das System-Protokoll"),StatusBarMessage.TYPE_ERROR));
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
		if (fetchUnbooked)
		{
		  // Den Abgleich gegen die Vormerkbuchungen machen wir gegen alle
		  // vorhandenen Vormerkbuchungen, nicht nur gegen die aus dem Zeitraum
		  // der aktuellen Lieferung. Denn hier wollen wir nicht nur Doppler
		  // vermeiden sondern ausserdem auch die loeschen, die von der Bank nicht
		  // mehr geliefert werden. Die sind zwischenzeitlich valutiert worden
		  // und muessen in Hibiscus geloescht werden.
	    DBIterator existingUnbooked = konto.getUmsaetze(null,null);
	    existingUnbooked.addFilter("flags = " + Umsatz.FLAG_NOTBOOKED);

      if (unbooked != null && unbooked.size() > 0)
      {
        this.dumpCamt(result.camtNotBooked,false);

        List<Umsatz> fetched = new ArrayList<Umsatz>();
        
        int created = 0;
        int skipped = 0;
        Logger.info("applying not-booked (vorgemerkte) entries");
        for (int i=0;i<unbooked.size();++i)
        {
          final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)unbooked.get(i));
          umsatz.setFlags(Umsatz.FLAG_NOTBOOKED);
          umsatz.setSaldo(0d); // Muss gemacht werden, weil der Saldo beim naechsten Mal anders lauten koennte
          umsatz.setKonto(konto);
          fetched.add(umsatz);

          Umsatz fromDB = null;
          // Anzahl der vorhandenen Umsaetze in der DB zaehlen
          int counter = 0;
          existingUnbooked.begin();
          for (int j = 0; j<existingUnbooked.size(); j++)
          {
            GenericObject dbObject = existingUnbooked.next();
            if (dbObject.equals(umsatz))
            {
              counter++;
              fromDB = (Umsatz) dbObject; //wir merken uns immer den letzten Umsatz
            }
          }
          
          if (fromDB != null)
          {
            // Wir duerfen den Umsatz nur dann ueberspringen, wenn er bereits 
            // OFT GENUG in der Datenbank ist. Andernfalls ist er tatsaechlich 
            // neu. Dazu zaehlen wir mit, wie oft wir gerade einen "gleichen" 
            // Umsatz empfangen haben. 
            Integer countInCurrentJobResult = duplicates.get(fromDB);
            if (countInCurrentJobResult == null)
            {
              duplicates.put(fromDB, 1);
              skipped++;
              continue;
            }
            else if (countInCurrentJobResult <= counter)  
            {
              // In der Datenbank sind mehr als bislang abgerufen -> Ueberspringen
              duplicates.put(fromDB, countInCurrentJobResult+1);
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
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Nicht alle empfangenen Ums�tze konnten gespeichert werden. Bitte pr�fen Sie das System-Protokoll"),StatusBarMessage.TYPE_ERROR));
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
        existingUnbooked.begin();
        while (existingUnbooked.hasNext())
        {
          Umsatz u = (Umsatz) existingUnbooked.next();
          
          if (!u.hasFlag(Umsatz.FLAG_NOTBOOKED))
            continue; // nur zur Sicherheit, dass wir nicht versehentlich welche loeschen, die keine Vormerkbuchungen sind

          // Mal schauen, ob der im aktuellen Durchlauf enthalten war:
          if (newList.contains(u) == null)
          {
            // Wurde nicht mehr von der Bank uebertragen, kann daher raus
            u.delete();
            deleted++;
          }
        }
        Logger.info("removed entries: " + deleted);
        Logger.info("done. new entries: " + created + ", skipped entries (already in database): " + skipped);
      }
      else
      {
        Logger.info("got no new not-booked (vorgemerkte) entries");
        
        // Keine neuen vorgemerkten Umsaetze
        // Dann loeschen wir pauschal alle, die in der Vergangenheit liegen
        // (mindestens gestern).
        Logger.info("clean obsolete not-booked entries");
        Date current = DateUtil.startOfDay(new Date());
        int count = 0;
        existingUnbooked.begin();
        while (existingUnbooked.hasNext())
        {
          Umsatz u = (Umsatz) existingUnbooked.next();
          
          if (!u.hasFlag(Umsatz.FLAG_NOTBOOKED))
            continue; // nur zur Sicherheit, dass wir nicht versehentlich welche loeschen, die keine Vormerkbuchungen sind

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
   * Schreibt die rohen CAMT-Daten in einen Ordner. 
   * @param camt die CAMT-Daten.
   * @param booked true, wenn es sich um gebuchte Umsaetze handelt.
   */
  private void dumpCamt(List<String> camt, boolean booked)
  {
    if (camt == null || camt.size() == 0)
      return;
    
    try
    {
      String storeCamt = StringUtils.trimToNull(settings.getString("umsatz.camt.path",null));
      if (storeCamt == null)
        return;
      
      Logger.info("dump CAMT data into " + storeCamt);
      
      File dir = new File(storeCamt);
      if (!dir.exists() && !dir.mkdirs())
      {
        Logger.warn("unable to create folder " + dir);
        return;
      }

      if (!dir.canWrite())
      {
        Logger.warn("unable to write into folder " + dir);
        return;
      }
      
      final Date date = new Date();
      for (String data:camt)
      {
        File f = this.createFile(dir,date,booked);
        Logger.info("dump CAMT data into " + f);
        
        OutputStream os = null;
        try
        {
          os = new BufferedOutputStream(new FileOutputStream(f));
          os.write(data.getBytes(Comm.ENCODING));
        }
        finally
        {
          IOUtil.close(os);
        }
      }
      
    }
    catch (Exception e)
    {
      Logger.error("unable to dump raw CAMt data",e);
    }
  }
  
  /**
   * Erzeugt einen freien Dateinamen.
   * @param dir der Ordner.
   * @param date das Datum.
   * @param booked true, wenn es gebuchte Umsaetze sind.
   * @return die Datei.
   * @throws IOException
   */
  private File createFile(File dir, Date date, boolean booked) throws IOException
  {
    // Wir nehmen den aktuellen Zeistempel als Dateiname
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSS");
    
    File f = null;
    for (int i=0;i<1000;++i)
    {
      f = new File(dir,df.format(date) + "-" + (booked ? "booked" : "notbooked") + (i > 0 ? ("-" + i) : "") + ".xml");
      if (!f.exists())
        return f;
    }
    
    throw new IOException("cannot create file " + f + ", already exists");
  }
  
  @Override
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Umsatzabruf fehlgeschlagen: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
  
  /**
   * Liefert das Beginn-Datum des Merge-Window.
   * @param booked Liste der abgerufenen Buchungen. Wenn da welche enthalten
   * sind, wird das Datum der darin befindlichen aeltesten Buchung verwendet.
   * Andernfalls wird das Start-Datum des Abrufs (minus 30 Tage) verwendet.
   * @param unbooked die optionale Liste der Vormerkbuchungen. Insofern die
   * Bank welche geliefert hat und das Abrufen der Vormerkbuchungen aktiviert ist.
   * Andernfalls ist der Parameter NULL.
   * @return das Beginn-Datgum des Merge-Window.
   */
  private Date getMergeWindow(List<UmsLine> booked, List<UmsLine> unbooked)
  {
    Date d = null;
    
    String basedOn = null;
    
    if (booked != null && booked.size() > 0)
    {
      for (UmsLine line:booked)
      {
        if (line.bdate == null)
          continue;
        
        if (d == null || line.bdate.before(d))
        {
          d = line.bdate;
          basedOn = "fetched booked entries";
        }
      }
    }
    
    if (unbooked != null && unbooked.size() > 0)
    {
      for (UmsLine line:unbooked)
      {
        if (line.bdate == null)
          continue;
        
        if (d == null || line.bdate.before(d))
        {
          d = line.bdate;
          basedOn = "fetched not-booked entries";
        }
      }
    }

    
    if (d == null && this.saldoDatum != null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(this.saldoDatum);
      cal.add(Calendar.DATE,settings.getInt("umsatz.mergewindow.offset",-30));
      d = cal.getTime();
      basedOn = "last sync";
    }
    
    if (d == null)
      Logger.info("merge window: not set");
    else
      Logger.info("merge window: " + d + " - now (based on " + basedOn + ")");
    
    return d;
  }
}
