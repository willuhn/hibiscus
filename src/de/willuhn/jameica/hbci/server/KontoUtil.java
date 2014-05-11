/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoUtil.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/01/20 17:13:21 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.util.DateUtil;

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
    // Fuehrende Nullen schneiden wir ab
    if (kontonummer.startsWith("0"))
      kontonummer = kontonummer.replaceAll("^0{1,}","");

    // Kontonummer bestand offensichtlich nur aus Nullen ;)
    if (kontonummer.length() == 0)
      return null;
    
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();
    DBIterator konten = service.createList(Konto.class);
    konten.addFilter("kontonummer like ?", new Object[]{"%" + kontonummer});
    konten.addFilter("blz = ?", new Object[]{blz});
    while (konten.hasNext())
    {
      // Fuehrende Nullen abschneiden und dann vergleichen
      Konto test = (Konto) konten.next();
      int current = test.getFlags();

      if (flag == Konto.FLAG_NONE)
      {
        // Nur Konten ohne Flags zugelassen
        if (current != flag)
          continue;
      }
      else if (flag > 0)
      {
        // Ein Flag ist angegeben. Dann kommt das Konto nur
        // in Frage, wenn es dieses Flag besitzt
        if ((current & flag) != flag)
          continue;
      }
      
      String kTest = test.getKontonummer();
      if (kTest == null || kTest.length() == 0)
        continue;
      if (kTest.startsWith("0"))
        kTest = kTest.replaceAll("^0{1,}","");
      
      // Mal schauen, ob die Kontonummern jetzt uebereinstimmen
      if (kTest.equals(kontonummer))
        return test;
    }
    
    return null;
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
      Konto test = (Konto) konten.next();
      int current = test.getFlags();

      if (flag == Konto.FLAG_NONE)
      {
        // Nur Konten ohne Flags zugelassen
        if (current != flag)
          continue;
      }
      else if (flag > 0)
      {
        // Ein Flag ist angegeben. Dann kommt das Konto nur
        // in Frage, wenn es dieses Flag besitzt
        if ((current & flag) != flag)
          continue;
      }
      
      return test;
    }
    
    return null;
  }

  /**
   * Liefert den Anfangssaldo eines Tages bzw. des 1. Tages nach diesem Datum mit Ums�tzen
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
    java.sql.Date start = new java.sql.Date(DateUtil.startOfDay(datum).getTime());

    DBIterator list = UmsatzUtil.getUmsaetze();
    list.addFilter("konto_id = " + konto.getID());
    list.addFilter("datum >= ?", new Object[] {start});
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (u.isBooked())
        return u.getSaldo() - u.getBetrag(); // Wir ziehen den Betrag noch ab, um den Saldo VOR der Buchung zu kriegen
    }

    // Im angegebenen Zeitraum waren keine Ums�tze zu finden. Deshalb suchen wir
    // fr�here Ums�tze.
    list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + konto.getID());
    list.addFilter("datum < ?", new Object[] {start});
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (u.isBooked())
        return u.getSaldo();
    }
    return 0.0d;
  }

  /**
   * Liefert den Endsaldo eines Tages bzw. des 1. Tages vor diesem Datum mit Ums�tzen oder
   * <code>0.0</code> wenn er noch nie abgefragt wurde.
   * @param konto das Konto.
   * @param datum Datum.
   * @return der Saldo.
   * @throws RemoteException 
   */
  public static double getEndSaldo(Konto konto, Date datum) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + konto.getID());
    list.addFilter("datum <= ?", new Object[] { new java.sql.Date(DateUtil.endOfDay(datum).getTime())});
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      
      // Wir nehmen den ersten Umsatz, der kein Vormerk-Flag hat
      if (u.isBooked())
        return u.getSaldo();
    }
    return 0.0d;
  }

  /**
   * Liefert die Ausgaben auf dem Konto im angegebenen Zeitraum.
   * @param konto das Konto.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @return Summe der Ausgaben.
   * @throws RemoteException
   */
  public static double getAusgaben(Konto konto, Date from, Date to) throws RemoteException
  {
    return getSumme(konto, from, to, true);
  }

  /**
   * Liefert die Einnahmen auf dem Konto im angegebenen Zeitraum.
   * @param konto das Konto.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @return Summe der Einnahmen.
   * @throws RemoteException
   */
  public static double getEinnahmen(Konto konto, Date from, Date to) throws RemoteException
  {
    return getSumme(konto, from, to, false);
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
   * @param ausgaben
   * @return Summe.
   * @throws RemoteException
   */
  private static double getSumme(Konto konto, Date from, Date to, boolean ausgaben) throws RemoteException
  {
    if (konto.isNewObject())
      return 0.0d;

    ArrayList params = new ArrayList();

    String sql = "select SUM(betrag) from umsatz where konto_id = " + konto.getID() + " and betrag " + (ausgaben ? "<" : ">") + " 0";
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

}
