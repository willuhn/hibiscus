/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Converter.java,v $
 * $Revision: 1.38 $
 * $Date: 2007/04/19 17:47:27 $
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

import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci.swift.DTAUS;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.parser.UmsatzParser;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Hilfeklasse, welche Objekte aus HBCI4Java in unsere Datenstrukturen konvertiert
 * und umgekehrt.
 */
public class Converter {


	/**
	 * Konvertiert einen einzelnen Umsatz von HBCI4Java nach Hibiscus.
	 * <br>
	 * <b>WICHTIG:</b><br>
	 * <ul>
	 * 	<li>
	 * 		Da das <code>UmsLine</code> zwar die Kundennummer
	 *    enthaelt, nicht aber das konkrete Konto, auf das sich der Umsatz
	 * 		bezieht, wird das Feld leer gelassen. Es ist daher Sache des Aufrufers,
	 * 		noch die Funktion <code>umsatz.setKonto(Konto)</code> aufzurufen, damit
	 * 		das Objekt in der Datenbank gespeichert werden kann.
	 *  </li>
	 *  <li>
	 * 		Eine Buchung enthaelt typischerweise einen Empfaenger ;)
	 *    Bei Haben-Buchungen ist man selbst dieser. Von daher bleibt
	 *    der Empfaenger bei eben jenen leer. Bei Soll-Buchungen wird die
	 *    Bankverbindung des Gegenkontos ermittelt, damit ein <code>Empfaenger</code>
	 *    erzeugt und dieser im Umsatz-Objekt gesetzt. Es wird jedoch nocht nicht
	 *    in der Datenbank gespeichert. Vorm Speichern des Umsatzes muss also
	 *    noch ein <code>umsatz.getEmpfaenger().store()</code> gemacht werden.<br>
	 *    Hinweis: Laut JavaDoc von HBCI4Java ist das Gegenkonto optional. Es
	 *    kann also auch bei Soll-Buchungen fehlen.
	 *  </li>
	 * </ul>
	 * @param u der zu convertierende Umsatz.
   * @return das neu erzeugte Umsatz-Objekt.
	 * @throws RemoteException
   */
  public static Umsatz HBCIUmsatz2HibiscusUmsatz(GVRKUms.UmsLine u) throws RemoteException
	{
		Umsatz umsatz = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);

		umsatz.setArt(u.text);
		umsatz.setCustomerRef(u.customerref);
		umsatz.setPrimanota(u.primanota);

    double kurs = 1.95583;

    //BUGZILLA 67 http://www.willuhn.de/bugzilla/show_bug.cgi?id=67
    Saldo s = u.saldo;
    if (s != null)
    {
      Value v = s.value;
      if (v != null)
      {
        // BUGZILLA 318
        double saldo = v.getDoubleValue();
        String curr  = v.getCurr();
        if (curr != null && "DEM".equals(curr))
          saldo /= kurs;
        umsatz.setSaldo(saldo);
      }
    }
    
    Value v = u.value;
    double betrag = v.getDoubleValue();
    String curr = v.getCurr();
    
    // BUGZILLA 318
    if (curr != null && "DEM".equals(curr))
      betrag /= kurs;

    umsatz.setBetrag(betrag);
		umsatz.setDatum(u.bdate);
		umsatz.setValuta(u.valuta);

    // BUGZILLA 146
    // Aus einer Mail von Stefan Palme
    //    Es geht noch besser. Wenn in "umsline.gvcode" nicht der Wert "999"
    //    drinsteht, sind die Variablen "text", "primanota", "usage", "other"
    //    und "addkey" irgendwie sinnvoll gefüllt.  Steht in "gvcode" der Wert
    //    "999" drin, dann sind diese Variablen alle null, und der ungeparste 
    //    Inhalt des Feldes :86: steht komplett in "additional".
    

    // Selberparsen kann ich wohl vergessen, wenn 999 drin steht. Wenn selbst
    // Stefan das nicht macht, lass ich lieber gleich die Finger davon ;)
    if (u.usage == null || u.usage.length == 0)
		{
      String usage = u.additional;
      if (usage == null || usage.length() == 0)
      {
        // Wir haben ueberhaupt nichts.
        umsatz.setZweck("-");
      }
      else
      {
        // Java's Regex-Implementierung ist sowas von daemlich.
        // String.split() macht nur Rotz, wenn man mit Quantifierern
        // arbeitet. Also ersetzten wir erst mal alles gegen nen
        // eigenen String und verwenden den dann zum Splitten.
        usage = usage.replaceAll("(.{27})","$1--##--##");
        String[] lines = usage.split("--##--##");
        
        // Jetzt schauen wir noch, ob wir einen Spezialparser registriert haben
        boolean success = false;
        String parser = HBCIProperties.HBCI_TRANSFER_SPECIAL_PARSER;
        if (parser != null && parser.length() > 0)
        {
          try
          {
            // OK, wir haben einen. Mal schauen, ob wir den instanziieren koennen.
            Class c = Application.getClassLoader().load(parser);
            UmsatzParser up = (UmsatzParser) c.newInstance();
            Logger.info("applying special parser: " + parser);
            up.parse(lines,umsatz);
            success = true;
          }
          catch (Exception e)
          {
            Logger.error("error while loading special parser " + parser,e);
          }
          catch (NoClassDefFoundError ncd)
          {
            Logger.error("special parser not found: " + parser,ncd);
          }
        }

        // Special-Parser wollte nicht. Also dann die regulaere Methode
        if (!success)
        {
          if (lines.length >= 1) umsatz.setZweck(lines[0]);
          if (lines.length >= 2) umsatz.setZweck2(lines[1]);
          if (lines.length >= 3)
          {
            // Wenn noch mehr da ist, pappen wir den Rest zusammen in
            // den Kommentar
            StringBuffer sb = new StringBuffer();
            for (int i=2;i<lines.length;++i)
            {
              sb.append(lines[i]);
            }
            umsatz.setKommentar(sb.toString());
          }
        }
      }
		}
		else {
      // erste Zeile in den ersten Verwendungszweck
			umsatz.setZweck(u.usage[0]);
      
      // Noch eine Zeile?
      // Die kommt in den zweiten Verwendungszweck
      if (u.usage.length > 1)
        umsatz.setZweck2(u.usage[1]);
      
      // Noch mehr Zeilen?
      // Die kommen in den Kommentar
      if (u.usage.length > 2)
      {
        StringBuffer sb = new StringBuffer();
        for (int i=2;i<u.usage.length;++i)
        {
          sb.append(u.usage[i]);
        }
        umsatz.setKommentar(sb.toString());
      }
		}


		// und jetzt noch der Empfaenger (wenn er existiert)
		if (u.other != null) 
		{
		  umsatz.setEmpfaenger(HBCIKonto2HibiscusAdresse(u.other));
		}
		return umsatz;
	}

  /**
	 * Konvertiert eine Zeile aus der Liste der abgerufenen Dauerauftraege.
   * @param d der Dauerauftrag aus HBCI4Java.
   * @return Unser Dauerauftrag.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Dauerauftrag HBCIDauer2HibiscusDauerauftrag(GVRDauerList.Dauer d)
  	throws RemoteException, ApplicationException
	{
		DauerauftragImpl auftrag = (DauerauftragImpl) Settings.getDBService().createObject(Dauerauftrag.class,null);
		auftrag.setErsteZahlung(d.firstdate);
		auftrag.setLetzteZahlung(d.lastdate);
    
		// TODO: Das ist nicht eindeutig. Da der Converter schaut, ob er ein solches
    // Konto schon hat und bei Bedarf das existierende verwendet. Es kann aber
    // sein, dass ein User ein und das selbe Konto mit verschiedenen Sicherheitsmedien
    // bedient. In diesem Fall wird der Dauerauftrag evtl. beim falschen Konto
    // einsortiert.
    auftrag.setKonto(HBCIKonto2HibiscusKonto(d.my));

    auftrag.setBetrag(d.value.getDoubleValue());
		auftrag.setOrderID(d.orderid);

		// Jetzt noch der Empfaenger
		auftrag.setGegenkonto(HBCIKonto2HibiscusAdresse(d.other));

		// Verwendungszweck
		if (d.usage.length == 0)
		{
			auftrag.setZweck("-");
		}
		else {
			auftrag.setZweck(d.usage[0]);
		}

		// Wir haben nur zwei Felder fuer den Zweck. Wenn also mehr als
		// 2 vorhanden sind (kann das ueberhaupt sein?), muessen wir die
		// restlichen leider ignorieren um nicht ueber die 27-Zeichen Maximum
		// pro Zweck zu kommen.
		if (d.usage.length > 1)
			auftrag.setZweck2(d.usage[1]);

		auftrag.setTurnus(TurnusHelper.createByDauerAuftrag(d));
		return auftrag;
	}

	/**
	 * Konvertiert ein Hibiscus-Konto in ein HBCI4Java Konto.
   * @param konto unser Konto.
   * @return das HBCI4Java Konto.
   * @throws RemoteException
   */
  public static Konto HibiscusKonto2HBCIKonto(de.willuhn.jameica.hbci.rmi.Konto konto) throws RemoteException
	{
		org.kapott.hbci.structures.Konto k =
			new org.kapott.hbci.structures.Konto(konto.getBLZ(),konto.getKontonummer());
		k.country = "DE";
		k.curr = konto.getWaehrung();
		k.customerid = konto.getKundennummer();
    k.type = konto.getBezeichnung(); // BUGZILLA 338
		k.name = konto.getName();
		return k;  	
	}

	/**
	 * Konvertiert ein HBCI4Java-Konto in ein Hibiscus Konto.
	 * Existiert ein Konto mit dieser Kontonummer und BLZ bereits in Hibiscus,
	 * wird jenes stattdessen zurueckgeliefert.
	 * @param konto das HBCI4Java Konto.
	 * @param passportClass optionale Angabe einer Passport-Klasse. Ist er angegeben wird, nur dann ein existierendes Konto
   * verwendet, wenn neben Kontonummer und BLZ auch die Klasse des Passportuebereinstimmt.
	 * @return unser Konto.
	 * @throws RemoteException
	 */
	public static de.willuhn.jameica.hbci.rmi.Konto HBCIKonto2HibiscusKonto(Konto konto, Class passportClass) throws RemoteException
	{
		DBIterator list = Settings.getDBService().createList(de.willuhn.jameica.hbci.rmi.Konto.class);
		list.addFilter("kontonummer = ?", new Object[]{konto.number});
		list.addFilter("blz = ?",         new Object[]{konto.blz});
    if (passportClass != null)
      list.addFilter("passport_class = ?", new Object[]{passportClass.getName()});
    
    // BUGZILLA 338: Wenn das Konto eine Bezeichnung hat, muss sie uebereinstimmen
    if (konto.type != null && konto.type.length() > 0)
      list.addFilter("bezeichnung = ?", new Object[]{konto.type});

    if (list.hasNext())
			return (de.willuhn.jameica.hbci.rmi.Konto) list.next(); // Konto gibts schon

		// Ne, wir erstellen ein neues
		de.willuhn.jameica.hbci.rmi.Konto k =
			(de.willuhn.jameica.hbci.rmi.Konto) Settings.getDBService().createObject(de.willuhn.jameica.hbci.rmi.Konto.class,null);
		k.setBLZ(konto.blz);
		k.setKontonummer(konto.number);
		k.setKundennummer(konto.customerid);
		k.setName(konto.name);
		k.setBezeichnung(konto.type);
		k.setWaehrung(konto.curr);
		return k;  	
	}

  /**
   * Konvertiert ein HBCI4Java-Konto in ein Hibiscus Konto.
   * Existiert ein Konto mit dieser Kontonummer und BLZ bereits in Hibiscus,
   * wird jenes stattdessen zurueckgeliefert.
   * @param konto das HBCI4Java Konto.
   * @return unser Konto.
   * @throws RemoteException
   */
  public static de.willuhn.jameica.hbci.rmi.Konto HBCIKonto2HibiscusKonto(Konto konto) throws RemoteException
  {
    return HBCIKonto2HibiscusKonto(konto,null);
  }

  /**
	 * Konvertiert einen Hibiscus-Adresse in ein HBCI4Java Konto.
	 * @param adresse unsere Adresse.
	 * @return das HBCI4Java Konto.
	 * @throws RemoteException
	 */
	public static Konto HibiscusAdresse2HBCIKonto(Adresse adresse) throws RemoteException
	{
		org.kapott.hbci.structures.Konto k =
			new org.kapott.hbci.structures.Konto("DE",adresse.getBLZ(),adresse.getKontonummer());
		k.name = adresse.getName();
		return k;
	}

	/**
	 * Konvertiert ein HBCI4Java Konto in eine Hibiscus-Adresse.
	 * @param konto das HBCI-Konto.
	 * @return unsere Adresse.
	 * @throws RemoteException
	 */
	public static Adresse HBCIKonto2HibiscusAdresse(Konto konto) throws RemoteException
	{
		Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
		e.setBLZ(konto.blz);
		e.setKontonummer(konto.number);
		String name = konto.name;
		if (konto.name2 != null)
			name += (" " + konto.name2);
		e.setName(name);
		return e;  	
	}
	
  /**
   * Konvertiert eine Sammel-Ueberweisung in DTAUS-Format.
   * @param su Sammel-Ueberweisung.
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  public static DTAUS HibiscusSammelUeberweisung2DTAUS(SammelUeberweisung su) throws RemoteException
  {
    // TYPE_CREDIT = Sammelüberweisung
    // TYPE_DEBIT = Sammellastschrift
    return HibiscusSammelTransfer2DTAUS(su, DTAUS.TYPE_CREDIT);
  }

  /**
   * Konvertiert eine Sammel-Lastschrift in DTAUS-Format.
   * @param sl Sammel-Lastschrift.
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  public static DTAUS HibiscusSammelLastschrift2DTAUS(SammelLastschrift sl) throws RemoteException
  {
    // TYPE_CREDIT = Sammelüberweisung
    // TYPE_DEBIT = Sammellastschrift
    return HibiscusSammelTransfer2DTAUS(sl, DTAUS.TYPE_DEBIT);
  }

  /**
   * Hilfsfunktion. Ist private, damit niemand aus Versehen den falschen Type angibt.
   * @param s Sammel-Transfer.
   * @param type Art des Transfers.
   * @see DTAUS#TYPE_CREDIT
   * @see DTAUS#TYPE_DEBIT
   * @return DTAUS-Repraesentation.
   * @throws RemoteException
   */
  private static DTAUS HibiscusSammelTransfer2DTAUS(SammelTransfer s, int type) throws RemoteException
	{

    DTAUS dtaus = new DTAUS(HibiscusKonto2HBCIKonto(s.getKonto()),type);
		DBIterator buchungen = s.getBuchungen();
		SammelTransferBuchung b = null;
		while (buchungen.hasNext())
		{
			b = (SammelTransferBuchung) buchungen.next();
			final DTAUS.Transaction tr = dtaus.new Transaction();
			tr.otherAccount = HibiscusAdresse2HBCIKonto(b.getGegenkonto());
			tr.value = new Value(String.valueOf(b.getBetrag()));
			tr.addUsage(b.getZweck());
			String z2 = b.getZweck2();
			if (z2 != null && z2.length() > 0)
				tr.addUsage(z2);
			dtaus.addEntry(tr);
		}
		return dtaus;
	}
  
}


/**********************************************************************
 * $Log: Converter.java,v $
 * Revision 1.38  2007/04/19 17:47:27  willuhn
 * @B Zeile 2 des Verwendungszwecks konnte ggf. zu lang werden
 *
 * Revision 1.37  2007/04/15 22:23:16  willuhn
 * @B Bug 338
 *
 * Revision 1.36  2007/02/26 12:48:23  willuhn
 * @N Spezial-PSD-Parser von Michael Lambers
 *
 * Revision 1.35  2006/11/06 22:39:30  willuhn
 * @B bug 318
 *
 * Revision 1.34  2006/11/03 10:28:24  willuhn
 * @B bug 318 (Saldo noch umrechnen)
 *
 * Revision 1.33  2006/11/03 01:12:56  willuhn
 * @B Bug 318
 *
 * Revision 1.32  2006/08/23 09:45:13  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.31  2006/01/23 12:16:57  willuhn
 * @N Update auf HBCI4Java 2.5.0-rc5
 *
 * Revision 1.30  2005/11/22 17:31:31  willuhn
 * @B NPE
 *
 * Revision 1.29  2005/11/20 22:04:19  willuhn
 * @N umsatz changable by user if usage not parsable
 *
 * Revision 1.28  2005/11/18 00:19:11  willuhn
 * @B bug 146
 *
 * Revision 1.27  2005/11/02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.26  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.25  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.24  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.23  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.22  2005/03/06 18:04:17  web0
 * @B Converter hat beim Konvertieren eines HBCI4Java-Kontos in eine Adresse ggf. eine lokal vorhandene geliefert
 *
 * Revision 1.21  2005/03/06 14:04:26  web0
 * @N SammelLastschrift seems to work now
 *
 * Revision 1.20  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.19  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.18  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.17  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.14  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.13  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.12  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.11  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.10  2004/07/04 17:07:58  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
 * Revision 1.9  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.8  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/27 23:50:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.5  2004/04/25 17:41:05  willuhn
 * @D javadoc
 *
 * Revision 1.4  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.3  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.2  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.1  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/