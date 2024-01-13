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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Hilfsklasse zum Verarbeiten von Umsaetzen.
 * 
 */
public class UmsatzUtil
{
  /**
   * Liefert alle Umsaetze in chronologischer Reihenfolge (alte zuerst) sortiert nach Datum, ID.
   * Weitere Filter-Kriterien wie Zeitraum und Konto muessen noch hinzugefuegt werden.
   * Die Funktion sortiert lediglich vereinheitlicht.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator getUmsaetze() throws RemoteException
  {
    return getUmsaetze(false);
  }

  /**
   * Liefert alle Umsaetze in umgekehrt chronologischer Reihenfolge (neue zuerst) sortiert nach Datum, ID.
   * Weitere Filter-Kriterien wie Zeitraum und Konto muessen noch hinzugefuegt werden.
   * Die Funktion sortiert lediglich vereinheitlicht.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator getUmsaetzeBackwards() throws RemoteException
  {
    return getUmsaetze(true);
  }

  /**
   * Liefert alle Umsaetze in chronologischer Reihenfolge (alte zuerst) sortiert nach Datum, ID.
   * Weitere Filter-Kriterien wie Zeitraum und Konto muessen noch hinzugefuegt werden.
   * Die Funktion sortiert lediglich vereinheitlicht.
   * @param days die Anzahl der Tage.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator getUmsaetze(int days) throws RemoteException
  {
    return getUmsaetze(days,false);
  }

  /**
   * Liefert alle Umsaetze in umgekehrt chronologischer Reihenfolge (neue zuerst) sortiert nach Datum, ID.
   * Weitere Filter-Kriterien wie Zeitraum und Konto muessen noch hinzugefuegt werden.
   * Die Funktion sortiert lediglich vereinheitlicht.
   * @param days die Anzahl der Tage.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator getUmsaetzeBackwards(int days) throws RemoteException
  {
    return getUmsaetze(days,true);
  }

  /**
   * Liefert das Datum des aeltesten Umsatzes auf dem Konto oder der Kontogruppe.
   * @param kontoOrGroup Konto oder Name einer Kontogruppe.
   * Optional. Wenn nichts angegeben ist, wird der aelteste Umsatz ueber alle Konten ermittelt.
   * @return das Datum des aeltesten Umsatzes oder NULL, wenn keiner gefunden wurde.
   * @throws RemoteException
   */
  public static Date getOldest(Object kontoOrGroup) throws RemoteException
  {
    String   query  = "select min(datum) from umsatz";
    Object[] params = null;
    if (kontoOrGroup != null && (kontoOrGroup instanceof Konto))
      query += " where konto_id = " + ((Konto) kontoOrGroup).getID();
    else if (kontoOrGroup != null && (kontoOrGroup instanceof String))
    {
      query += " where konto_id in (select id from konto where kategorie = ?)";
      params = new String[]{(String) kontoOrGroup};
    }
    
    return (Date) Settings.getDBService().execute(query,params,new ResultSetExtractor() {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (!rs.next())
          return null;
        return rs.getDate(1);
      }
    });
  }
  
  /**
   * Liefert alle Umsaetze in ugekehrt chronologischer Reihenfolge (neue zuerst), die den Kriterien entsprechen.
   * @param konto das Konto. Optional.
   * @param kategorie Konto-Kategorie. Optional.
   * @param from das Start-Datum. Optional.
   * @param to das End-Datum. Optional.
   * @param query Suchbegriff. Optional.
   * @return Liste der gefundenen Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator find(Konto konto, String kategorie, Date from, Date to, String query) throws RemoteException
  {
    DBIterator list = getUmsaetzeBackwards();
    
    if (konto != null)
      list.addFilter("konto_id = " + konto.getID());
    else if (StringUtils.trimToNull(kategorie) != null)
      list.addFilter("konto_id in (select id from konto where kategorie = ?)", kategorie);
    
    if (from != null)
      list.addFilter("datum >= ?", new java.sql.Date(DateUtil.startOfDay(from).getTime()));
    if (to != null)
      list.addFilter("datum <= ?", new java.sql.Date(DateUtil.endOfDay(to).getTime()));
    
    if (StringUtils.trimToNull(query) != null)
    {
      final String text = "%" + query.toLowerCase() + "%";
      String search = "(LOWER(CONCAT(COALESCE(zweck,''),COALESCE(zweck2,''),COALESCE(zweck3,''))) LIKE ? OR " +
                      "LOWER(empfaenger_name) LIKE ? OR " +
                      "empfaenger_konto LIKE ? OR " +
                      "empfaenger_blz LIKE ? OR " +
                      "LOWER(primanota) LIKE ? OR " +
                      "LOWER(art) LIKE ? OR " +
                      "LOWER(customerref) LIKE ? OR " +
                      "LOWER(purposecode) LIKE ? OR " +
                      "LOWER(kommentar) LIKE ? OR " +
                      "LOWER(endtoendid) LIKE ? OR " +
                      "LOWER(mandateid) LIKE ? OR " +
                      "LOWER(creditorid) LIKE ? OR " +
                      "LOWER(empfaenger_name2) LIKE ? OR " +
                      "LOWER(art) LIKE ?)";

      List<String> params = new ArrayList<String>(Arrays.asList(text,text,text,text,text,text,text,text,text,text,text,text,text,text));
      if (query.matches("^[0-9]{1,10}$"))
      {
        search = "(id = ? or " + search + ")";
        params.add(0,query);
      }
      list.addFilter(search,params.toArray());
    }
    return list;
  }

  /**
   * Liefert alle Umsaetze in umgekehrt chronologischer Reihenfolge (neue zuerst), in denen
   * der genannte Suchbegriff auftaucht.
   * @param query Suchbegriff.
   * @return Liste der gefundenen Umsaetze.
   * @throws RemoteException
   * @throws ApplicationException wird geworfen, wenn kein Suchbegriff angegeben ist.
   */
  public static DBIterator find(String query) throws RemoteException, ApplicationException
  {
    if (query == null || query.length() == 0)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Bitte geben Sie einen Suchbegriff an"));
    }
    
    return find(null,null,null,null,query);
  }

  /**
   * Liefert alle Umsaetze, jedoch mit vereinheitlichter Vorsortierung.
   * @param backwards chronologisch (alte zuerst) = false.
   * umgekehrt chronologisch (neue zuerst) = true.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  private static DBIterator getUmsaetze(boolean backwards) throws RemoteException
  {
    return getUmsaetze(-1,backwards);
  }


  /**
   * Liefert alle Umsaetze, jedoch mit vereinheitlichter Vorsortierung.
   * @param backwards chronologisch (alte zuerst) = false.
   * umgekehrt chronologisch (neue zuerst) = true.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  private static DBIterator getUmsaetze(int days, boolean backwards) throws RemoteException
  {
    String s = backwards ? "DESC" : "ASC";
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    final DBIterator list = service.createList(Umsatz.class);
    list.setOrder("ORDER BY " + service.getSQLTimestamp("datum") + " " + s + ", id " + s);

    if (days > 0)
    {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR,-days);
      final Date from = cal.getTime();
      list.addFilter("datum >= ?", new java.sql.Date(DateUtil.startOfDay(from).getTime()));
    }
    return list;
  }
}
