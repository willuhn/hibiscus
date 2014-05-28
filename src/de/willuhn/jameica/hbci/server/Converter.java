/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Converter.java,v $
 * $Revision: 1.64 $
 * $Date: 2011/09/12 11:53:26 $
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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 * Hilfeklasse, welche Objekte aus HBCI4Java in unsere Datenstrukturen konvertiert
 * und umgekehrt.
 */
public class Converter {


	/**
	 * Konvertiert einen einzelnen Umsatz von HBCI4Java nach Hibiscus.
	 * Wichtig: Das zugeordnete Konto wird nicht gefuellt. Es ist daher Sache 
	 * des Aufrufers, noch die Funktion <code>umsatz.setKonto(Konto)</code> aufzurufen,
	 * damit das Objekt in der Datenbank gespeichert werden kann.
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

		// Wir uebernehmen den GV-Code nur, wenn was sinnvolles drin steht.
		// "999" steht hierbei fuer unstrukturiert aka unbekannt.
		if (u.gvcode != null && !u.gvcode.equals("999"))
  		umsatz.setGvCode(u.gvcode);

		if (u.addkey != null && u.addkey.length() > 0)
      umsatz.setAddKey(u.addkey);

		////////////////////////////////////////////////////////////////////////////
		// Verwendungszweck
		
    // BUGZILLA 146
    // Aus einer Mail von Stefan Palme
    //    Es geht noch besser. Wenn in "umsline.gvcode" nicht der Wert "999"
    //    drinsteht, sind die Variablen "text", "primanota", "usage", "other"
    //    und "addkey" irgendwie sinnvoll gefüllt.  Steht in "gvcode" der Wert
    //    "999" drin, dann sind diese Variablen alle null, und der ungeparste 
    //    Inhalt des Feldes :86: steht komplett in "additional".

		String[] lines = (String[]) u.usage.toArray(new String[u.usage.size()]);

		// die Bank liefert keine strukturierten Verwendungszwecke (gvcode=999).
		// Daher verwenden wir den gesamten "additional"-Block und zerlegen ihn
		// in 27-Zeichen lange Haeppchen
    if (lines.length == 0)
      lines = VerwendungszweckUtil.parse(u.additional);

    // Es gibt eine erste Bank, die 40 Zeichen lange Verwendungszwecke lieferte.
    // Siehe Mail von Frank vom 06.02.2014
    lines = VerwendungszweckUtil.rewrap(35,lines);
    VerwendungszweckUtil.apply(umsatz,lines);
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Gegenkonto
		// und jetzt noch der Empfaenger (wenn er existiert)
		if (u.other != null) 
		{
		  HibiscusAddress a = HBCIKonto2Address(u.other);
		  // Wenn keine Kontonummer/BLZ angegeben ist, versuchen wir es mit BIC/IBAN
		  if (a.getKontonummer() == null || a.getKontonummer().length() == 0)
		    a.setKontonummer(a.getIban());
		  if (a.getBlz() == null || a.getBlz().length() == 0)
		    a.setBlz(a.getBic());
		  umsatz.setGegenkonto(a);
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
    
		// Das ist nicht eindeutig. Da der Converter schaut, ob er ein solches
    // Konto schon hat und bei Bedarf das existierende verwendet. Es kann aber
    // sein, dass ein User ein und das selbe Konto mit verschiedenen Sicherheitsmedien
    // bedient. In diesem Fall wird der Dauerauftrag evtl. beim falschen Konto
    // einsortiert. Ist aber kein Problem, weil der HBCIDauerauftragListJob
		// das Konto eh nochmal gegen seines (und er kennt das richtige) ueberschreibt.
    auftrag.setKonto(HBCIKonto2HibiscusKonto(d.my));

    auftrag.setBetrag(d.value.getDoubleValue());
		auftrag.setOrderID(d.orderid);

		// Jetzt noch der Empfaenger
		auftrag.setGegenkonto(HBCIKonto2Address(d.other));

		// Textschlüssel
		auftrag.setTextSchluessel(d.key);
		
		// Verwendungszweck
		VerwendungszweckUtil.apply(auftrag,d.usage);
		
		// Es kann wohl Faelle geben, wo der Auftrag keinen Verwendungszweck hat.
		// In dem Fall tragen wir ein "-" ein.
		if (auftrag.getZweck() == null)
		  auftrag.setZweck("-");
		
		auftrag.setTurnus(TurnusHelper.createByDauerAuftrag(d));
		return auftrag;
	}

  /**
   * Konvertiert eine Zeile aus der Liste der abgerufenen SEPA-Dauerauftraege.
   * @param d der SEPA-Dauerauftrag aus HBCI4Java.
   * @return Unser Dauerauftrag.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static SepaDauerauftrag HBCIDauer2HibiscusSepaDauerauftrag(GVRDauerList.Dauer d)
    throws RemoteException, ApplicationException
  {
    SepaDauerauftragImpl auftrag = (SepaDauerauftragImpl) Settings.getDBService().createObject(SepaDauerauftrag.class,null);
    auftrag.setErsteZahlung(d.firstdate);
    auftrag.setLetzteZahlung(d.lastdate);
    
    // Das ist nicht eindeutig. Da der Converter schaut, ob er ein solches
    // Konto schon hat und bei Bedarf das existierende verwendet. Es kann aber
    // sein, dass ein User ein und das selbe Konto mit verschiedenen Sicherheitsmedien
    // bedient. In diesem Fall wird der Dauerauftrag evtl. beim falschen Konto
    // einsortiert. Ist aber kein Problem, weil der HBCIDauerauftragListJob
    // das Konto eh nochmal gegen seines (und er kennt das richtige) ueberschreibt.
    auftrag.setKonto(HBCIKonto2HibiscusKonto(d.my));

    auftrag.setBetrag(d.value.getDoubleValue());
    auftrag.setOrderID(d.orderid);

    // Jetzt noch der Empfaenger
    auftrag.setGegenkonto(HBCIKonto2Address(d.other));
    
    auftrag.setChangable(d.can_change);
    auftrag.setDeletable(d.can_delete);

    // Verwendungszweck
    VerwendungszweckUtil.apply(auftrag,d.usage);
    
    // Es kann wohl Faelle geben, wo der Auftrag keinen Verwendungszweck hat.
    // In dem Fall tragen wir ein "-" ein.
    if (auftrag.getZweck() == null)
      auftrag.setZweck("-");
    
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
		org.kapott.hbci.structures.Konto k = new org.kapott.hbci.structures.Konto(konto.getBLZ(),konto.getKontonummer());
		k.country    = "DE";
		k.curr       = konto.getWaehrung();
		k.customerid = konto.getKundennummer();
    k.type       = konto.getBezeichnung(); // BUGZILLA 338
		k.name       = konto.getName();
    k.subnumber  = konto.getUnterkonto(); // BUGZILLA 355
    k.iban       = konto.getIban();
    k.bic        = konto.getBic();
		return k;  	
	}

	/**
	 * Konvertiert ein HBCI4Java-Konto in ein Hibiscus Konto.
	 * Existiert ein Konto mit dieser Kontonummer und BLZ bereits in Hibiscus,
	 * wird jenes stattdessen zurueckgeliefert.
	 * @param konto das HBCI4Java Konto.
	 * @param passportClass optionale Angabe einer Passport-Klasse. Ist er angegeben wird, nur dann ein existierendes Konto
   * verwendet, wenn neben Kontonummer und BLZ auch die Klasse des Passport uebereinstimmt.
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

    // BUGZILLA 355
    if (konto.subnumber != null && konto.subnumber.length() > 0)
      list.addFilter("unterkonto = ?",new Object[]{konto.subnumber});
    
    // BUGZILLA 338: Wenn das Konto eine Bezeichnung hat, muss sie uebereinstimmen
    if (konto.type != null && konto.type.length() > 0)
      list.addFilter("bezeichnung = ?", new Object[]{konto.type});

    if (list.hasNext())
			return (de.willuhn.jameica.hbci.rmi.Konto) list.next(); // Konto gibts schon

		// Ne, wir erstellen ein neues
		de.willuhn.jameica.hbci.rmi.Konto k = (de.willuhn.jameica.hbci.rmi.Konto) Settings.getDBService().createObject(de.willuhn.jameica.hbci.rmi.Konto.class,null);
		k.setBLZ(konto.blz);
		k.setKontonummer(konto.number);
    k.setUnterkonto(konto.subnumber); // BUGZILLA 355
		k.setKundennummer(konto.customerid);
		k.setName(konto.name);
		k.setBezeichnung(konto.type);
		k.setWaehrung(konto.curr);
		k.setIban(konto.iban);
		k.setBic(konto.bic);
		if (passportClass != null)
		  k.setPassportClass(passportClass.getName());
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
	public static Konto Address2HBCIKonto(Address adresse) throws RemoteException
	{
		Konto k = new Konto("DE",adresse.getBlz(),adresse.getKontonummer());
		k.name = adresse.getName();
		k.iban = adresse.getIban();
		k.bic  = adresse.getBic();
		return k;
	}

	/**
	 * Konvertiert ein HBCI4Java Konto in eine Hibiscus-Adresse.
	 * @param konto das HBCI-Konto.
	 * @return unsere Adresse.
	 * @throws RemoteException
	 */
	public static HibiscusAddress HBCIKonto2Address(Konto konto) throws RemoteException
	{
		HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
		e.setBlz(konto.blz);
		e.setKontonummer(konto.number);
		e.setBic(konto.bic);
		e.setIban(konto.iban);
		String name = konto.name;
		if (konto.name2 != null && konto.name2.length() > 0)
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
      
      Konto other = new Konto("DE",b.getGegenkontoBLZ(),b.getGegenkontoNummer());
      other.name = b.getGegenkontoName();

      tr.otherAccount = other;
			tr.value = new Value(String.valueOf(b.getBetrag()));
      
      String key = b.getTextSchluessel();
      if (key != null && key.length() > 0)
        tr.key = key; // Nur setzen, wenn in der Buchung definiert. Gibt sonst in DTAUS#toString eine NPE

      String[] lines = VerwendungszweckUtil.toArray(b);
      for (String line:lines)
      {
        tr.addUsage(line);
      }
			
			dtaus.addEntry(tr);
		}
		return dtaus;
	}
  
}


/**********************************************************************
 * $Log: Converter.java,v $
 * Revision 1.64  2011/09/12 11:53:26  willuhn
 * @N Support fuer Banken (wie die deutsche Bank), die keine Order-IDs vergeben - BUGZILLA 1129
 *
 * Revision 1.63  2011-07-25 17:17:19  willuhn
 * @N BUGZILLA 1065 - zusaetzlich noch addkey
 *
 * Revision 1.62  2011-07-25 14:42:40  willuhn
 * @N BUGZILLA 1065
 *
 * Revision 1.61  2011-06-09 08:50:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.60  2011-06-09 08:35:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.59  2011-06-07 10:07:50  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.58  2010-09-29 22:39:18  willuhn
 * @N Passport automatisch im neuen Konto speichern
 *
 * Revision 1.57  2010-09-24 12:22:04  willuhn
 * @N Thomas' Patch fuer Textschluessel in Dauerauftraegen
 *
 * Revision 1.56  2010/06/01 11:02:18  willuhn
 * @N Wiederverwendbaren Code zum Zerlegen und Uebernehmen von Verwendungszwecken aus/in Arrays in Util-Klasse ausgelagert
 *
 * Revision 1.55  2010/01/18 17:29:27  willuhn
 * @N IBAN/BIC statt Kontonummer/BLZ uebernehmen, falls keine Kto-Nummer/BLZ angegeben ist
 **********************************************************************/