/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.GV_Result.GVRKontoauszug.Format;
import org.kapott.hbci.GV_Result.GVRKontoauszug.GVRKontoauszugEntry;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Hilfeklasse, welche Objekte aus HBCI4Java in unsere Datenstrukturen konvertiert
 * und umgekehrt.
 */
public class Converter
{
  private final static double KURS_EUR = 1.95583;

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

    umsatz.setArt(clean(u.text));
    umsatz.setCustomerRef(clean(u.customerref));
    umsatz.setPrimanota(clean(u.primanota));
    umsatz.setTransactionId(u.id);
    umsatz.setPurposeCode(u.purposecode);
    umsatz.setEndToEndId(u.endToEndId);
    umsatz.setMandateId(u.mandateId);
    
    if (u.other != null)
      umsatz.setCreditorId(u.other.creditorid);
    
    Saldo s = u.saldo;
    if (s != null)
    {
      Value v = s.value;
      if (v != null)
      {
        double saldo = v.getDoubleValue();
        String curr  = v.getCurr();
        if (curr != null && "DEM".equals(curr))
          saldo /= KURS_EUR;
        umsatz.setSaldo(saldo);
      }
    }

    Value v = u.value;
    double betrag = v.getDoubleValue();
    String curr = v.getCurr();

    if (curr != null && "DEM".equals(curr))
      betrag /= KURS_EUR;

    umsatz.setBetrag(betrag);
    umsatz.setDatum(u.bdate);
    umsatz.setValuta(u.valuta);

    // Wir uebernehmen den GV-Code nur, wenn was sinnvolles drin steht.
    // "999" steht hierbei fuer unstrukturiert aka unbekannt.
    // 
    if (u.gvcode != null && !u.gvcode.equals("999") && u.gvcode.length() <= HBCIProperties.HBCI_GVCODE_MAXLENGTH)
      umsatz.setGvCode(u.gvcode);

    if (u.addkey != null && u.addkey.length() > 0 && u.addkey.length() <= HBCIProperties.HBCI_ADDKEY_MAXLENGTH)
      umsatz.setAddKey(u.addkey);

    ////////////////////////////////////////////////////////////////////////////
    // Verwendungszweck

    String[] lines = (String[]) u.usage.toArray(new String[0]);

    if (u.isCamt && u.usage != null)
    {
      // Wenn wir nur eine Zeile haben, koennen wir die 1:1 uebernehmen
      if (u.usage.size() == 1)
        umsatz.setZweck(u.usage.get(0));
      else
        VerwendungszweckUtil.applyCamt(umsatz,u.usage);
    }
    else
    {
      // die Bank liefert keine strukturierten Verwendungszwecke (gvcode=999).
      // Daher verwenden wir den gesamten "additional"-Block und zerlegen ihn
      // in 27-Zeichen lange Haeppchen
      if (lines.length == 0)
        lines = VerwendungszweckUtil.parse(u.additional);
      
      // Es gibt eine erste Bank, die 40 Zeichen lange Verwendungszwecke lieferte.
      // Siehe Mail von Frank vom 06.02.2014
      lines = VerwendungszweckUtil.rewrap(HBCIProperties.HBCI_TRANSFER_USAGE_DB_MAXLENGTH,lines);
      VerwendungszweckUtil.apply(umsatz,lines);
      
      // Wir checken mal, ob wir eine EndToEnd-ID haben. Falls ja, tragen wir die gleich
      // in das dedizierte Feld ein. Aber nur, wenn wir noch keine haben
      String eref = umsatz.getEndToEndId();
      if (eref == null || eref.length() == 0)
      {
        eref = cleanSepaId(VerwendungszweckUtil.getTag(umsatz,Tag.EREF));
        if (eref != null && eref.length() > 0 && eref.length() <= 100)
          umsatz.setEndToEndId(eref);
      }

      // Wir checken mal, ob wir eine Mandatsreferenz haben. Falls ja, tragen wir die gleich
      // in das dedizierte Feld ein. Aber nur, wenn wir noch keine haben
      String mid = umsatz.getMandateId();
      if (mid == null || mid.length() == 0)
      {
        mid = cleanSepaId(VerwendungszweckUtil.getTag(umsatz,Tag.MREF));
        if (mid != null && mid.length() > 0 && mid.length() <= 100)
          umsatz.setMandateId(mid);
      }
      
      // Wir checken mal, ob wir eine GläubigerID haben. Falls ja, tragen wir die gleich
      // in das dedizierte Feld ein. Aber nur, wenn wir noch keine haben
      String creditorId = umsatz.getCreditorId();
      if (creditorId == null || creditorId.length() == 0)
      {
        // MT940
        creditorId = cleanSepaId(VerwendungszweckUtil.getTag(umsatz, Tag.CRED));
        if (creditorId != null && creditorId.length() > 0 && creditorId.length() <= HBCIProperties.HBCI_SEPA_CREDITORID_MAXLENGTH)
          umsatz.setCreditorId(creditorId);
      }

    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Gegenkonto
    // und jetzt noch der Empfaenger (wenn er existiert)
    if (u.other != null)
    {
      umsatz.setGegenkonto(HBCIKonto2Address(u.other,u.isCamt));
      if (u.isCamt)
        umsatz.setGegenkontoName2(u.other.name2);
    }

    if (!HBCIProperties.HBCI_SEPA_PARSE_TAGS)
      return umsatz;

    // Wenn wir noch keine Gegenkonto-Infos haben, versuchen wir mal, sie aus
    // dem Verwendungszweck zu extrahieren
    boolean haveIban = StringUtils.trimToNull(umsatz.getGegenkontoNummer()) != null;
    boolean haveBic  = StringUtils.trimToNull(umsatz.getGegenkontoBLZ()) != null;
    boolean haveName = StringUtils.trimToNull(umsatz.getGegenkontoName()) != null;

    if (!haveIban || !haveBic || !haveName)
    {
      Map<Tag,String> tags = VerwendungszweckUtil.parse(umsatz);

      if (!haveName)
        umsatz.setGegenkontoName(tags.get(Tag.ABWA));

      String iban = tags.get(Tag.IBAN);
      String bic  = tags.get(Tag.BIC);

      IBAN i = null;

      if (!haveIban && StringUtils.trimToNull(iban) != null)
      {
        // Nur uebernehmen, wenn es eine gueltige IBAN ist
        try
        {
          i = HBCIProperties.getIBAN(iban);
          if (i != null)
            umsatz.setGegenkontoNummer(i.getIBAN());
        }
        catch (Exception e)
        {
          Logger.error("invalid IBAN - ignoring: " + iban,e);
        }
      }

      if (!haveBic)
      {
        bic = StringUtils.trimToNull(bic);
        if (bic != null)
        {
          try
          {
            bic = HBCIProperties.checkBIC(bic);
            if (bic != null)
              umsatz.setGegenkontoBLZ(bic);
          }
          catch (Exception e)
          {
            Logger.error("invalid BIC - ignoring: " + bic,e);
          }
        }
        else if (i != null)
        {
          umsatz.setGegenkontoBLZ(i.getBIC());
        }
      }
    }

    //
    ////////////////////////////////////////////////////////////////////////////
    return umsatz;
  }
  
  /**
   * Bereinigt eine SEPA-Kennung.
   * Bei einem User kam es vor, dass die ID nicht korrekt geparst wurde und daher auch den ganzen
   * Rest des Verwendungszwecks (inclusive aller weiteren SEPA-Tags) enthielt. Ich konnte den Fehler
   * nicht reproduzieren. Damit aber das Abrufen des Umsatzes deswegen nicht fehlschlaegt, kuerzen
   * wir in dem Fall die EndToEnd-ID so weit, dass sie rein passt.
   * @param text die SEPA-Kennung.
   * @return die bereinigte SEPA-Kennung.
   */
  public static String cleanSepaId(String text)
  {
    text = clean(text);
    if (text == null || text.length() == 0 || text.length() <= 100)
      return text;
    
    // Wir koennten jetzt hier nach 100 Zeichen abschneiden. Dann wuerde aber vermutlich auch
    // Quatsch mit drin stehen. Da die EREF aber in aller Regel keine Leerzeichen enthaelt,
    // schneiden wir erstmal nach dem ersten Leerzeichen ab. Wenn es dann immer noch zu lang
    // ist, koennen wir den Rest allemal noch abschneiden.
    int pos = text.indexOf(' ');
    if (pos > 0 && pos < 100)
      text = text.substring(0,pos);
    
    if (text.length() > 100)
      text = text.substring(0,100);
    
    return text;
  }

  /**
   * Konvertiert einen einzelnen Umsatz von Hibiscus nach HBCI4Java.
   * @param u der zu convertierende Umsatz.
   * @return das neu erzeugte Umsatz-Objekt.
   * @throws RemoteException
   */
  public static UmsLine HibiscusUmsatz2HBCIUmsatz(Umsatz u) throws RemoteException
  {
    final UmsLine line = new UmsLine();
    
    final de.willuhn.jameica.hbci.rmi.Konto k = u.getKonto();
    
    final String iban = u.getGegenkontoNummer();
    final String bic  = u.getGegenkontoBLZ();
    final boolean isSepa = iban != null && iban.length() > HBCIProperties.HBCI_KTO_MAXLENGTH_HARD;
    final Konto other = new Konto();
    other.name = u.getGegenkontoName();
    if (isSepa)
    {
      other.iban = iban;
      other.bic = bic;
    }
    else
    {
      other.number = iban;
      other.blz = bic;
    }
    
    line.addkey = u.getAddKey();
    line.bdate = u.getDatum();
    line.customerref = u.getCustomerRef();
    line.gvcode = u.getGvCode();
    line.id = u.getTransactionId();
    line.isCamt = line.id != null && line.id.length() > 0;
    line.isSepa = isSepa;
    line.isStorno = false;
    line.other = other;
    line.primanota = u.getPrimanota();
    line.purposecode = u.getPurposeCode();
    line.saldo = new Saldo();
    line.saldo.timestamp = u.getDatum();
    line.saldo.value = new Value(new BigDecimal(u.getSaldo()),k.getWaehrung());
    line.text = u.getArt();
    line.usage = Arrays.asList(VerwendungszweckUtil.toArray(u));
    line.value = new Value(new BigDecimal(u.getBetrag()),k.getWaehrung());
    line.valuta = u.getValuta();
    
    return line;
  }

  /**
   * Entfernt Zeichen, die in den Strings nicht enthalten sein sollten.
   * Typischerweise Zeilenumbrueche.
   * @param s der String.
   * @return der bereinigte String.
   * BUGZILLA 1611
   */
  private static String clean(String s)
  {
    return HBCIProperties.replace(s,HBCIProperties.TEXT_REPLACEMENTS_UMSATZ);
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

    auftrag.setPmtInfId(d.pmtinfid);
    auftrag.setPurposeCode(d.purposecode);

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
   * Konvertiert einen abgerufenen Kontoauszug von HBCI4Java in  das Format von Hibiscus.
   * Achtung: Die Binaer-Daten werden hierbei ignoriert. Es ist Aufgabe des Aufrufers, die
   * passend zu speichern.
   * @param kt das Konto, uber das die Kontoauszuege abgerufen wurden.
   * @param k der Kontoauszug von HBCI4Java.
   * @return der Kontoauszug von Hibiscus.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Kontoauszug HBCIKontoauszug2HibiscusKontoauszug(de.willuhn.jameica.hbci.rmi.Konto kt, GVRKontoauszugEntry k)
      throws RemoteException, ApplicationException
  {
    Kontoauszug kh = Settings.getDBService().createObject(Kontoauszug.class,null);
    
    final Date createDate = k.getDate();
    final Date startDate  = k.getStartDate();
    final Date endDate    = k.getEndDate();
    
    kh.setVon(startDate);
    kh.setBis(endDate);
    kh.setErstellungsdatum(createDate);
    
    Format f = k.getFormat();
    kh.setFormat(f != null ? f.getCode() : null);

    ///////////////////////////////////////////////////////////////////
    //
    int year = k.getYear();
    
    // Kein Jahr mitgeschickt?
    
    // 1. Startdatum versuchen.
    if (year == 0)
      year = getYear(startDate);
    
    // 2. Enddatum versuchen.
    if (year == 0)
      year = getYear(endDate);

    // 3. Erstellungsdatum versuchen.
    if (year == 0)
      year = getYear(createDate);

    // 4. Aktuelles Datum
    if (year == 0)
      year = getYear(new Date());

    kh.setJahr(year > 0 ? Integer.valueOf(year) : null);
    //
    ///////////////////////////////////////////////////////////////////
    
    kh.setKonto(kt);
    kh.setName1(k.getName());
    kh.setName2(k.getName2());
    kh.setName3(k.getName3());
    
    ///////////////////////////////////////////////////////////////////
    // Kontoauszugsnummer
    int number = k.getNumber();
    
    // Wenn wir keine Nummer haben, laden wir die Liste der letzten Auszuege und zaehlen von dort die Nummer hoch,
    // insofern eine vorhanden ist
    if (number == 0)
    {
      Kontoauszug newest = KontoauszugPdfUtil.getNewestWithNumber(kt);
      if (newest != null)
      {
        Integer n = newest.getNummer();
        if (n != null)
          number = n.intValue() + 1;
      }
    }
    kh.setNummer(number > 0 ? Integer.valueOf(number) : null);
    //
    ///////////////////////////////////////////////////////////////////
    
    kh.setDateiname(k.getFilename());
    kh.setQuittungscode(k.getReceipt());
    
    return kh;
  }
  
  /**
   * Extrahiert das Jahr aus dem Datum.
   * @param date das Datum.
   * @return das Jahr oder 0, wenn es nicht ermittelbar ist.
   */
  private static int getYear(Date date)
  {
    if (date == null)
      return 0;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.YEAR);
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

    Integer accType = konto.getAccountType();
    k.acctype    = accType != null ? accType.toString() : null;
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
    list.addFilter("kontonummer = ?", konto.number);
    list.addFilter("blz = ?",         konto.blz);
    if (passportClass != null)
      list.addFilter("passport_class = ?", passportClass.getName());

    // BUGZILLA 355
    if (konto.subnumber != null && konto.subnumber.length() > 0)
      list.addFilter("unterkonto = ?",konto.subnumber);

    if (konto.customerid != null && konto.customerid.length() > 0)
      list.addFilter("kundennummer = ?",konto.customerid);
    
    String type = StringUtils.trimToNull(konto.acctype);
    Integer accType = null;
    if (type != null)
    {
      try
      {
        accType = Integer.parseInt(type);
      }
      catch (Exception e)
      {
        Logger.error("unknown account type: " + type,e);
      }
    }

    // Wenn das Konto einen Typ hat, muss er uebereinstimmen
    if (accType != null)
      list.addFilter("acctype = ?", accType);

    // Konto gibts schon
    if (list.hasNext())
      return (de.willuhn.jameica.hbci.rmi.Konto) list.next();

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
    k.setAccountType(accType);
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
   * @param camt true, wenn es sich um einen CAMT-Umsatz handelt. In dem Fall wird bis auf weiteres erstmal name2 ignoriert.
   * Siehe https://homebanking-hilfe.de/forum/topic.php?p=142206#real142206
   * @return unsere Adresse.
   * @throws RemoteException
   */
  public static HibiscusAddress HBCIKonto2Address(Konto konto, boolean camt) throws RemoteException
  {
    HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
    e.setBlz(konto.blz);
    e.setKontonummer(konto.number);
    e.setBic(konto.bic);
    e.setIban(konto.iban);

    String name  = StringUtils.trimToEmpty(konto.name);
    
    if (!camt)
    {
      String name2 = StringUtils.trimToEmpty(konto.name2);

      if (name2 != null && name2.length() > 0)
        name += (" " + name2);
    }

    if (name != null && name.length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
      name = StringUtils.trimToEmpty(name.substring(0,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)); // Nochmal ein Trim, fuer den Fall, dass nach dem Abschneiden der Text mit Leerzeichen endet
    e.setName(name);
    return e;   
  }

  /**
   * Konvertiert ein HBCI4Java Konto in eine Hibiscus-Adresse.
   * @param konto das HBCI-Konto.
   * @return unsere Adresse.
   * @throws RemoteException
   */
  public static HibiscusAddress HBCIKonto2Address(Konto konto) throws RemoteException
  {
    return HBCIKonto2Address(konto,false);
  }

}
