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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.CamtSetupDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.BPDUtil.Query;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.TypedProperties;

/**
 * Hilfsklasse mit statischen Funktionen fuer Konten.
 */
public class KontoUtil
{
  /**
   * Sucht das Konto in der Datenbank.
   * Die Funktion entfernt bei der Suche selbstaendig fuehrende Nullen in
   * Kontonummern.
   * @param kontonummer die Kontonummer.
   * @param blz die BLZ.
   * @return das gefundene Konto oder NULL, wenn es nicht existiert.
   * @throws RemoteException
   */
  public static Konto find(String kontonummer, String blz) throws RemoteException
  {
    return find(kontonummer,blz,-1);
  }

  /**
   * Sucht das Konto in der Datenbank.
   * Die Funktion entfernt bei der Suche selbstaendig fuehrende Nullen in
   * Kontonummern.
   * @param kontonummer die Kontonummer.
   * @param blz die BLZ.
   * @param flag das Flag, welches das Konto besitzen muss.
   * @return das gefundene Konto oder NULL, wenn es nicht existiert.
   * @throws RemoteException
   */
  public static Konto find(String kontonummer, String blz, int flag) throws RemoteException
  {
    if (kontonummer == null || kontonummer.length() == 0)
      return null;
    if (blz == null || blz.length() == 0)
      return null;
    
    // BUGZILLA 365
    // Fuehrende Nullen abschneiden
    if (kontonummer.startsWith("0"))
      kontonummer = kontonummer.replaceAll("^0{1,}","");

    // Kontonummer bestand offensichtlich nur aus Nullen ;)
    if (kontonummer.length() == 0)
      return null;
    
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();
    DBIterator konten = service.createList(Konto.class);
    konten.addFilter("kontonummer like ?", "%" + kontonummer);
    konten.addFilter("blz = ?", blz);
    while (konten.hasNext())
    {
      final Konto konto = (Konto) konten.next();

      if (flag == Konto.FLAG_NONE)
      {
        // Nur Konten ohne Flags zugelassen
        if (konto.getFlags() != Konto.FLAG_NONE)
          continue;
      }
      else if (flag > 0)
      {
        // Ein Flag ist angegeben. Dann kommt das Konto nur
        // in Frage, wenn es dieses Flag besitzt
        if (!konto.hasFlag(flag))
          continue;
      }
      
      String kTest = konto.getKontonummer();
      if (kTest == null || kTest.length() == 0)
        continue;

      // Fuehrende Nullen abschneiden
      if (kTest.startsWith("0"))
        kTest = kTest.replaceAll("^0{1,}","");
      
      // Mal schauen, ob die Kontonummern jetzt uebereinstimmen
      if (kTest.equals(kontonummer))
        return konto;
    }
    
    return null;
  }
  
  /**
   * Prueft, ob die Umsaetze eines Kontos per CAMT abgerufen werden sollen.
   * @param k das zu pruefende Konto.
   * @param ask true, wenn der User hier auch gefragt werden darf, falls er die Entscheidung noch nicht getroffen hat.
   * @return true, wenn CAMT verwendet werden soll.
   */
  public static boolean useCamt(Konto k, boolean ask)
  {
    if (k == null)
    {
      Logger.warn("unable to check if CAMT is supported, no account given");
      return false;
    }
    try
    {
      // Erstmal checken, ob wir grundsaetzlich Support fuer CAMT haben
      Logger.debug("checking if account supports CAMT");
      Support support = BPDUtil.getSupport(k,Query.UmsatzCamt);
      if (support == null)
      {
        Logger.debug("unable to determine CAMT support");
        return false;
      }
      
      if (!support.isSupported())
      {
        Logger.debug("account does not support CAMT");
        return false;
      }
      
      // Also grundsaetzlich haben wir Support.
      // Jetzt checken, wir, was fuer das Konto konfiguriert ist.
      String value = StringUtils.trimToNull(MetaKey.UMSATZ_CAMT.get(k));
      
      // Wenn ein Wert drin steht, hat sich der User entschieden, dann halten wir uns dran
      if (value != null)
      {
        Logger.debug("CAMT usage configured as: " + value);
        return Boolean.valueOf(value);
      }
      
      // Wenn als Wert noch nichts drin steht, dann hat der User es noch nicht konfigutiert. In dem Fall entscheiden
      // wir erstmal basierend auf den vorhandenen Umsaetzen. Wenn das Konto noch keine Umsaetze hat, verwenden wir
      // CAMT und speichern das auch als Wert. Damit wird kuenftig bei neuen Usern automatisch CAMT verwendet.
      // Wenn es Umsaetze hat, fragen wir den User, ob er umstellen moechte.
      if (k.getNumUmsaetze() == 0)
      {
        Logger.debug("account does not have bookings yet, auto-activating CAMT");
        MetaKey.UMSATZ_CAMT.set(k,Boolean.TRUE.toString());
        return true;
      }

      if (!ask)
      {
        Logger.debug("CAMT support available but not yet activated");
        return false;
      }

      // Wenn wir keine UI haben, koennen wir den User aber nicht fragen
      if (Application.inServerMode())
      {
        Logger.debug("running in server mode, user cannot be asked if CAMT shall be used");
        return false;
      }

      Logger.debug("asking user if CAMT shall be used");
      CamtSetupDialog d = new CamtSetupDialog(k);
      Boolean b = (Boolean) d.open();
      Logger.debug("user answered for CAMT: " + b);
      return b != null ? b.booleanValue() : false;
    }
    catch (OperationCanceledException oce)
    {
      Logger.debug("operation cancelled");
      return false;
    }
    catch (Exception e)
    {
      Logger.error("unable to check if account supports CAMT",e);
      return false;
    }
  }
  
  /**
   * Extrahiert aus den BPD die Zeitspanne, fuer die laut Bank Umsaetze eines Kontos abgerufen werden koennen.
   * @param k das betreffende Konto.
   * @param askForCamtIfPossible true, wenn der User hier auch gefragt werden darf, ob die Umsaetze per CAMT abgerufen werden sollen, falls er die Entscheidung noch nicht getroffen hat.
   * @return -1, wenn kein Konto angegeben wurde, ansonsten Wert des Parameters "timerange" aus den BPD oder 0, falls der Parameter fehlt.
   */
  public static int getUmsaetzeTimeRange(Konto k, boolean askForCamtIfPossible)
  {
    if (k == null)
    {
      Logger.warn("unable to get time range from BPD, no account given");
      return -1;
    }

    boolean usingCamt = useCamt(k, askForCamtIfPossible);
    Support support = BPDUtil.getSupport(k, usingCamt ? Query.UmsatzCamt : Query.Umsatz);
    TypedProperties bpd = (support != null && support.isSupported()) ? support.getBpd() : null;
    if (bpd == null)
    {
      Logger.debug("unable to get BPD");
      return 0;
    }

    int timeRange = bpd.getInt("timerange", 0);
    if (timeRange > 0)
    {
      Logger.debug("time range from BPD for " + (usingCamt ? "KUmsZeitCamt" : "KUmsZeit") + ": " + timeRange);
    }

    return timeRange;
  }


  /**
   * Sucht das Konto in der Datenbank.
   * @param iban die IBAN.
   * @return das gefundene Konto oder NULL, wenn es nicht existiert.
   * @throws RemoteException
   */
  public static Konto findByIBAN(String iban) throws RemoteException
  {
    return findByIBAN(iban,-1);
  }
  
  /**
   * Sucht das Konto in der Datenbank.
   * @param iban die IBAN.
   * @param flag das Flag, welches das Konto besitzen muss.
   * @return das gefundene Konto oder NULL, wenn es nicht existiert.
   * @throws RemoteException
   */
  public static Konto findByIBAN(String iban, int flag) throws RemoteException
  {
    iban = StringUtils.trimToNull(iban);
    if (iban == null)
      return null;
    
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();
    DBIterator konten = service.createList(Konto.class);
    konten.addFilter("lower(iban) = ?", iban.toLowerCase()); // case insensitive
    while (konten.hasNext())
    {
      final Konto konto = (Konto) konten.next();

      if (flag == Konto.FLAG_NONE)
      {
        // Nur Konten ohne Flags zugelassen
        if (konto.getFlags() != Konto.FLAG_NONE)
          continue;
      }
      else if (flag > 0)
      {
        // Ein Flag ist angegeben. Dann kommt das Konto nur
        // in Frage, wenn es dieses Flag besitzt
        if (!konto.hasFlag(flag))
          continue;
      }
      
      return konto;
    }
    
    return null;
  }

  /**
   * Liefert den Anfangssaldo eines Tages bzw. des 1. Tages nach diesem Datum mit Umsätzen
   * oder <code>0.0</code> wenn er noch nie abgefragt wurde.
   * @param konto das Konto.
   * @param datum Datum.
   * @return der Saldo.
   * @throws RemoteException 
   */
  public static double getAnfangsSaldo(Konto konto, Date datum) throws RemoteException
  {
    // BUGZILLA 844/852: Die Vormerkbuchungen duerfen nicht mit eingerechnet werden,
    // weil die einen Null-Saldo haben. Da es aber auch echte Buchungen gibt, bei
    // denen der Saldo 0 ist, duerfen wir das nicht als Filterkriterium nehmen sondern
    // das NOTBOOKED-Flag pruefen. Leider gibts in SQL keinen standardisierten
    // Binary-AND-Operator, sodass wir das manuell machen muessen.
    java.sql.Date start = datum != null ? new java.sql.Date(DateUtil.startOfDay(datum).getTime()) : null;

    DBIterator list = UmsatzUtil.getUmsaetze();
    list.addFilter("konto_id = " + konto.getID());
    
    if (start != null)
      list.addFilter("datum >= ?", start);
    
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (!u.hasFlag(Umsatz.FLAG_NOTBOOKED))
        return u.getSaldo() - u.getBetrag(); // Wir ziehen den Betrag noch ab, um den Saldo VOR der Buchung zu kriegen
    }

    // Im angegebenen Zeitraum waren keine Umsätze zu finden. Deshalb suchen wir
    // frühere Umsätze.
    list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + konto.getID());
    
    if (start != null)
      list.addFilter("datum < ?", start);
    
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (!u.hasFlag(Umsatz.FLAG_NOTBOOKED))
        return u.getSaldo();
    }
    
    // Keine Umsaetze gefunden. Wir nehmen den Saldo des Kontos selbst
    return konto.getSaldo();
  }

  /**
   * Liefert den Endsaldo eines Tages bzw. des 1. Tages vor diesem Datum mit Umsätzen oder
   * <code>0.0</code> wenn er noch nie abgefragt wurde.
   * @param konto das Konto.
   * @param datum Datum.
   * @return der Saldo.
   * @throws RemoteException 
   */
  public static double getEndSaldo(Konto konto, Date datum) throws RemoteException
  {
    java.sql.Date end = datum != null ? new java.sql.Date(DateUtil.endOfDay(datum).getTime()) : null;

    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + konto.getID());
    
    if (end != null)
      list.addFilter("datum <= ?", end);
    
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (!u.hasFlag(Umsatz.FLAG_NOTBOOKED))
        return u.getSaldo();
    }
    
    // BUGZILLA 1682 Wir checken mal, ob wir eine Buchung direkt dahinter finden. Dann nehmen
    // wir diese und generieren aus Zwischensumme und Betrag den Endsaldo
    list = UmsatzUtil.getUmsaetze();
    list.addFilter("konto_id = " + konto.getID());
    
    if (end != null)
      list.addFilter("datum > ?", end);
    
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (!u.hasFlag(Umsatz.FLAG_NOTBOOKED))
      {
        return u.getSaldo() - u.getBetrag(); // Wir ziehen den Betrag noch ab, um den Saldo VOR der Buchung zu kriegen
      }
    }
    
    // Keine Umsaetze gefunden. Wir nehmen den Saldo des Kontos selbst
    return konto.getSaldo();
  }

  /**
   * Liefert die Ausgaben auf dem Konto im angegebenen Zeitraum.
   * @param konto das Konto.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @param onlyBooked true, wenn nur die gebuchten Umsaetze beruecksichtigt werden sollen. False, wenn auch die Vormerkbuchungen beruecksichtigt werden sollen.
   * @return Summe der Ausgaben.
   * @throws RemoteException
   */
  public static double getAusgaben(Konto konto, Date from, Date to, boolean onlyBooked) throws RemoteException
  {
    return getSumme(konto, from, to, true, onlyBooked);
  }

  /**
   * Liefert die Einnahmen auf dem Konto im angegebenen Zeitraum.
   * @param konto das Konto.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @param onlyBooked true, wenn nur die gebuchten Umsaetze beruecksichtigt werden sollen. False, wenn auch die Vormerkbuchungen beruecksichtigt werden sollen.
   * @return Summe der Einnahmen.
   * @throws RemoteException
   */
  public static double getEinnahmen(Konto konto, Date from, Date to, boolean onlyBooked) throws RemoteException
  {
    return getSumme(konto, from, to, false, onlyBooked);
  }
  
  /**
   * Liefert eine Liste der verfuegbaren Konto-Kategorien.
   * @return Liste der verfuegbaren Konto-Kategorien. Niemals NULL sondern hoechstens eine leere Liste.
   * @throws RemoteException
   */
  public static List<String> getGroups() throws RemoteException
  {
    return (List<String>) Settings.getDBService().execute("select kategorie from konto where kategorie is not null and kategorie != '' group by kategorie order by LOWER(kategorie)",null,new ResultSetExtractor()
    {
      /**
       * @see de.willuhn.datasource.rmi.ResultSetExtractor#extract(java.sql.ResultSet)
       */
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        List<String> list = new ArrayList<String>();
        while (rs.next())
          list.add(rs.getString(1));
        return list;
      }
    });
  }

  /**
   * Hilfsfunktion fuer Berechnung der Einnahmen und Ausgaben.
   * @param konto das Konto.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @param ausgaben true, wenn die Ausgaben summiert werden sollen. Wenn false angegeben ist, werden die Einnahmen summiert.
   * @param onlyBooked true, wenn nur die gebuchten Umsaetze beruecksichtigt werden sollen. False, wenn auch die Vormerkbuchungen beruecksichtigt werden sollen.
   * @return Summe.
   * @throws RemoteException
   */
  private static double getSumme(Konto konto, Date from, Date to, boolean ausgaben, boolean onlyBooked) throws RemoteException
  {
    if (konto.isNewObject())
      return 0.0d;

    ArrayList params = new ArrayList();

    String sql = "select SUM(betrag) from umsatz where konto_id = " + konto.getID() + " and betrag " + (ausgaben ? "<" : ">") + " 0";
    
    if (onlyBooked)
      sql += " and (flags is null or flags < " + Umsatz.FLAG_NOTBOOKED + ")";

    if (from != null)
    {
      params.add(new java.sql.Date(DateUtil.startOfDay(from).getTime()));
      sql += " and datum >= ? ";
    }
    if (to != null)
    {
      params.add(new java.sql.Date(DateUtil.startOfDay(to).getTime()));
      sql += " and datum <= ? ";
    }

    HBCIDBService service = Settings.getDBService();
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (rs.next())
          return new Double(rs.getDouble(1));
        return new Double(0.0d);
      }
    };

    Double d = (Double) service.execute(sql, params.toArray(), rs);
    return d == null ? 0.0d : Math.abs(d.doubleValue());
  }
  
  /**
   * Liefert die Liste der Konten.
   * @param filter optionaler Filter.
   * @return Liste der KOnten.
   * @throws RemoteException
   */
  public static List<Konto> getKonten(KontoFilter filter) throws RemoteException
  {
    DBIterator it = Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY LOWER(kategorie), blz, kontonummer, bezeichnung");
    List<Konto> l = new ArrayList<Konto>();

    while (it.hasNext())
    {
      Konto k = (Konto) it.next();

      if (filter == null || filter.accept(k))
        l.add(k);
    }
    return l;
  }
  
  /**
   * Liefert eine ausfuehrliche String-Repraesentation des Kontos.
   * Sie enthaelt Name, IBAN und BIC.
   * @param k das Konto.
   * @return die ausfuehrliche String-Repraesentation. 
   * @throws RemoteException
   */
  public static String toString(Konto k) throws RemoteException
  {
    return k != null ? (String) k.getAttribute("extralongname") : "";
  }

}
