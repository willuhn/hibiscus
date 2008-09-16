/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/AccountUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/09/16 23:43:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;


/**
 * Hilfsklasse zum Abfragen von BPD und UPD aus der Cache-Tabelle.
 */
public class AccountUtil
{
  /**
   * Liefert die maximale Anzahl von Verwendungszwecken fuer Ueberweisungen.
   * @param konto das Konto
   * @return Maximale Anzahl der Zeilen.
   * @throws RemoteException
   */
  public final static int getMaxUsageUeb(Konto konto) throws RemoteException
  {
    int defaultValue = HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM;
    
    // Konto angegeben?
    if (konto == null)
      return defaultValue;
    
    // Kundennummer korrekt?
    String kd = konto.getKundennummer();
    if (kd == null || kd.length() == 0 || !kd.trim().matches("[0-9]{1,30}"))
      return defaultValue;
    
    kd = kd.trim();
    
    // TODO: Das SQL-Statement wirft eine "org.h2.jdbc.JdbcSQLException: Unerlaubter Wert 1 für Parameter parameterIndex"
    String q = "select min(content) from property where name like 'bpd.?.%UebPar%.ParUeb.maxusage'";
    String s = (String) Settings.getDBService().execute(q,new String[]{kd}, new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        return rs.next() ? rs.getString(1) : null;
      }
    });
    if (s == null)
      return defaultValue;
    
    try
    {
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e)
    {
      Logger.error("invalid maxusage: " + s);
    }
    return defaultValue;
  }
}


/**********************************************************************
 * $Log: AccountUtil.java,v $
 * Revision 1.1  2008/09/16 23:43:32  willuhn
 * @N BPDs fuer Anzahl der moeglichen Zeilen Verwendungszweck auswerten - IN PROGRESS
 *
 **********************************************************************/
