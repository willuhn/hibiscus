/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Converter.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/04/19 22:05:51 $
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

import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.structures.Konto;

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Hilfeklasse, welche Objekte aus HBCI4Java in unsere Datenstrukturen konvertiert
 * und umgekehrt.
 */
public class Converter {


	/**
	 * Konvertiert einen einzelnen Umsatz von HBCI4Java nach Jameica.
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
   * @return das neu erzeugte Umsatz-Objekt.
   */
  public static Umsatz convert(GVRKUms.UmsLine u) throws RemoteException
	{
		Umsatz umsatz = (Umsatz) Settings.getDatabase().createObject(Umsatz.class,null);

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
		  Empfaenger e = convert(u.other);
		  umsatz.setEmpfaengerBLZ(e.getBLZ());
		  umsatz.setEmpfaengerKonto(e.getKontonummer());
		  umsatz.setEmpfaengerName(e.getName());
		}
		return umsatz;
	}

	/**
	 * Konvertiert ein Jameica-Konto in ein HBCI4Java Konto.
   * @param konto unser Konto.
   * @return das HBCI4Java Konto.
   * @throws RemoteException
   */
  public static Konto convert(de.willuhn.jameica.hbci.rmi.Konto konto) throws RemoteException
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
	 * Konvertiert ein HBCI4Java Konto in einen Jameica-Empfaenger.
	 * @param konto das HBCI-Konto.
	 * @return unser Empfaenger.
	 * @throws RemoteException
	 */
	public static Empfaenger convert(Konto konto) throws RemoteException
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