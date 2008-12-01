/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/AccountUtil.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/12/01 23:54:42 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


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
    if (kd == null || kd.length() == 0 || !kd.trim().matches("[0-9a-zA-Z]{1,30}"))
      return defaultValue;
    
    kd = "bpd." + kd.trim() + ".%UebPar%.ParUeb.maxusage";
    
    String q = "select min(content) from property where name like ?";
    String s = DBPropertyUtil.query(q,new String[]{kd},String.valueOf(defaultValue));
    
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
  
  /**
   * Prueft, ob die Anzahl der Verwendungszwecke nicht die Maximal-Anzahl aus den BPD uebersteigt.
   * @param transfer der zu testende Transfer.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void checkMaxUsage(HibiscusTransfer transfer) throws RemoteException, ApplicationException
  {
    if (transfer == null)
      return;
    
    checkMaxUsage(transfer.getKonto(),transfer.getWeitereVerwendungszwecke());
  }

  /**
   * Prueft, ob die Anzahl der Verwendungszwecke nicht die Maximal-Anzahl aus den BPD uebersteigt.
   * @param buchung die zu testende Buchung.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void checkMaxUsage(SammelTransferBuchung buchung) throws RemoteException, ApplicationException
  {
    if (buchung == null)
      return;
    
    SammelTransfer t = buchung.getSammelTransfer();
    checkMaxUsage(t == null ? null : t.getKonto(),buchung.getWeitereVerwendungszwecke());
  }

  /**
   * Prueft, ob die Anzahl der Verwendungszwecke nicht die Maximal-Anzahl aus den BPD uebersteigt.
   * @param transfer der zu testende Transfer.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private static void checkMaxUsage(Konto konto, String[] lines) throws RemoteException, ApplicationException
  {
    if (lines == null || lines.length == 0)
      return;
    
    // "2" sind die ersten beiden Zeilen, die bei getWeitereVerwendungszwecke nicht mitgeliefert werden
    int allowed = AccountUtil.getMaxUsageUeb(konto);
    if ((lines.length + 2) > allowed)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Zuviele weitere Zeilen Verwendungszweck. Maximal erlaubt: {0}",String.valueOf(allowed)));
    }
  }

}


/**********************************************************************
 * $Log: AccountUtil.java,v $
 * Revision 1.4  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.3  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.2  2008/09/17 23:44:29  willuhn
 * @B SQL-Query fuer MaxUsage-Abfrage korrigiert
 *
 * Revision 1.1  2008/09/16 23:43:32  willuhn
 * @N BPDs fuer Anzahl der moeglichen Zeilen Verwendungszweck auswerten - IN PROGRESS
 *
 **********************************************************************/
