/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Converter.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/07/14 23:48:31 $
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

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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
		Umsatz umsatz = (Umsatz) Settings.getDatabase().createObject(Umsatz.class,null);

		umsatz.setArt(u.text);
		umsatz.setCustomerRef(u.customerref);
		umsatz.setPrimanota(u.primanota);

		try {
			umsatz.setSaldo(u.saldo.value.value);
		}
		catch (NullPointerException e)
		{
			// Falls u.saldo null liefert
			/* ignore */
		}

		// C(redit) = HABEN
		// D(ebit)  = SOLL
		if (u.cd.endsWith("C"))
			umsatz.setBetrag(u.value.value); // Haben-Buchung
		else
		{
			umsatz.setBetrag(-u.value.value); // Soll-Buchung
		}

		umsatz.setDatum(u.bdate);
		umsatz.setValuta(u.valuta);
		if (u.usage.length == 0)
		{
			// Baeh, eine Buchung ohne Text. Dann schreiben wir selbst 'nen Dummy rein
			umsatz.setZweck("-");
		}
		else {
			umsatz.setZweck(u.usage[0]);
		}

		// Wenn wir noch mehr Zeilen haben, dann schreiben wir alle restlichen
		// in zweck2
		if (u.usage.length > 1)
		{
			String merged = "";
			for (int i=1;i<u.usage.length;++i)
			{
				merged += (u.usage[i] + " - ");
			}
			umsatz.setZweck2(merged);
		}

		// und jetzt noch der Empfaenger (wenn er existiert)
		if (u.other != null) 
		{
		  Empfaenger e = HBCIKonto2HibiscusEmpfaenger(u.other);
		  umsatz.setEmpfaengerBLZ(e.getBLZ());
		  umsatz.setEmpfaengerKonto(e.getKontonummer());
		  umsatz.setEmpfaengerName(e.getName());
		}
		return umsatz;
	}

  /**
	 * Konvertiert eine Zeile aus der Liste der abgerufenen Dauerumsaetze.
   * @param d der Dauerauftrag aus HBCI4Java.
   * @return Unser Dauerauftrag.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Dauerauftrag HBCIDauer2HibiscusDauerauftrag(GVRDauerList.Dauer d)
  	throws RemoteException, ApplicationException
	{
		Dauerauftrag auftrag = (Dauerauftrag) Settings.getDatabase().createObject(Dauerauftrag.class,null);
		auftrag.setErsteZahlung(d.firstdate);
		auftrag.setLetzteZahlung(d.lastdate);
		auftrag.setKonto(HBCIKonto2HibiscusKonto(d.my));
		auftrag.setBetrag(d.value.value);

		// Jetzt noch der Empfaenger
		Empfaenger e = HBCIKonto2HibiscusEmpfaenger(d.other);
		auftrag.setEmpfaengerBLZ(e.getBLZ());
		auftrag.setEmpfaengerKonto(e.getKontonummer());
		auftrag.setEmpfaengerName(e.getName());

		// Verwendungszweck
		if (d.usage.length == 0)
		{
			auftrag.setZweck("-");
		}
		else {
			auftrag.setZweck(d.usage[0]);
		}
		// Wenn wir noch mehr Zeilen haben, dann schreiben wir alle restlichen
		// in zweck2
		if (d.usage.length > 1)
		{
			String merged = "";
			for (int i=1;i<d.usage.length;++i)
			{
				merged += (d.usage[i] + " - ");
			}
			auftrag.setZweck2(merged);
		}

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
		k.name = konto.getName();
		return k;  	
	}

	/**
	 * Konvertiert ein HBCI4Java-Konto in ein Hibiscus Konto.
	 * @param konto das HBCI4Java Konto.
	 * @return unser Konto.
	 * @throws RemoteException
	 */
	public static de.willuhn.jameica.hbci.rmi.Konto HBCIKonto2HibiscusKonto(Konto konto) throws RemoteException
	{
		de.willuhn.jameica.hbci.rmi.Konto k =
			(de.willuhn.jameica.hbci.rmi.Konto) Settings.getDatabase().createObject(de.willuhn.jameica.hbci.rmi.Konto.class,null);
		k.setBLZ(konto.blz);
		k.setKontonummer(konto.number);
		k.setKundennummer(konto.customerid);
		k.setName(konto.name);
		k.setBezeichnung(konto.type);
		k.setWaehrung(konto.curr);
		return k;  	
	}

	/**
	 * Konvertiert einen Hibiscus-Empfaenger in ein HBCI4Java Konto.
	 * @param empfaenger unser Empfaenger
	 * @return das HBCI4Java Konto.
	 * @throws RemoteException
	 */
	public static Konto HibiscusEmpfaenger2HBCIKonto(Empfaenger empfaenger) throws RemoteException
	{
		return new org.kapott.hbci.structures.Konto(
			"DE",
			empfaenger.getBLZ(),
			empfaenger.getKontonummer()
		);
	}

	/**
	 * Konvertiert ein HBCI4Java Konto in einen Hibiscus-Empfaenger.
	 * @param konto das HBCI-Konto.
	 * @return unser Empfaenger.
	 * @throws RemoteException
	 */
	public static Empfaenger HBCIKonto2HibiscusEmpfaenger(Konto konto) throws RemoteException
	{
		Empfaenger e = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
		e.setBLZ(konto.blz);
		e.setKontonummer(konto.number);
		String name = konto.name;
		if (konto.name2 != null)
			name += (" " + konto.name2);
		e.setName(name);
		return e;  	
	}

}


/**********************************************************************
 * $Log: Converter.java,v $
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