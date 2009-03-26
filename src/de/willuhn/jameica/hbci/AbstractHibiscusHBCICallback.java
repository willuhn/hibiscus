/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/AbstractHibiscusHBCICallback.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/03/26 16:16:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;

import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.hbci.server.VersionUtil;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung des HBCI-Callback.
 * Ermoeglicht gemeinsamen Code in Hibiscus und Payment-Server.
 */
public abstract class AbstractHibiscusHBCICallback extends AbstractHBCICallback
{
  /**
   * Speichert die BPD/UPD des Passports in der Hibiscus-Datenbank zwischen und aktualisiert sie automatisch bei Bedarf.
   * Dadurch stehen sie in Hibiscus zur Verfuegung, ohne dass hierzu ein Passport geoeffnet werden muss.
   * @param passport der betreffende Passport.
   */
  protected void cacheData(HBCIPassport passport)
  {
    if (passport == null)
      return;
    
    try
    {
      updateBPD(passport);
      updateUPD(passport);
    }
    catch (Exception e)
    {
      Logger.error("error while updating bpd/upd - will be ignored",e);
    }
  }
  
  /**
   * Aktualisiert die BPD.
   * @param bpd
   */
  private void updateBPD(HBCIPassport passport) throws Exception
  {
    update(passport,passport.getBPD(),passport.getBPDVersion(),"bpd");
  }

  /**
   * Aktualisiert die UPD.
   * @param upd
   */
  private void updateUPD(HBCIPassport passport) throws Exception
  {
    update(passport,passport.getUPD(),passport.getUPDVersion(),"upd");
  }

  /**
   * Aktualisiert die genannten Daten.
   * @param passport Passport.
   * @param data die Daten.
   * @param version Version der Daten.
   * @param prefix Prefix.
   * @throws Exception
   */
  private void update(HBCIPassport passport, Properties data, String version, String prefix) throws Exception
  {
    String user = passport.getUserId();
    if (version == null || version.length() == 0 ||
        user == null || user.length() == 0 ||
        data == null || data.size() == 0)
    {
      Logger.debug("[" + prefix + "] no version, no userid or no data found, skipping update");
      return;
    }
    
    int nv = Integer.parseInt(version);
    Version v = VersionUtil.getVersion(Settings.getDBService(),prefix + "." + user);
    int cv = v.getVersion();
    
    // Keine neue Version
    if (nv == cv)
      return;
    
    if (cv < 0 || nv < 0)
    {
      Logger.warn("SUSPECT - " + prefix + " version smaller than zero. new: " + nv + ", current: " + cv);
      return;
    }

    Logger.info("got new " + prefix + " version. old: " + cv + ", new: " + nv + ", updating cache");
    String[] customerID = getCustomerIDs(passport);
    for (Enumeration keys = data.keys();keys.hasMoreElements();)
    {
      for (int i=0;i<customerID.length;++i)
      {
        String name = (String) keys.nextElement();
        DBPropertyUtil.set(prefix + "." + customerID[i] + "." + name,data.getProperty(name));
      }
    }
    // Speichern der neuen Versionsnummer
    v.setVersion(nv);
    v.store();
  }
  
  /**
   * Ermittelt die Customer-IDs aus dem Passport.
   * @param passport Passport.
   * @return Liste der Customer-IDs.
   */
  private String[] getCustomerIDs(HBCIPassport passport)
  {
    Konto[] accounts = passport.getAccounts();

    // Das macht HBCI4Java in passport.getCustomerId() genauso
    // Wenn keine Customer-IDs vorhanden sind, wird die User-ID genommen
    if (accounts == null || accounts.length == 0)
      return new String[]{passport.getUserId()};

    // Hash zum Vermeiden von Doppeln
    Hashtable values = new Hashtable();
    for (int i=0;i<accounts.length;++i)
    {
      String value = accounts[i].customerid;
      if (value != null)
        values.put(value,value);
    }
    return (String[]) values.values().toArray(new String[values.size()]);
  }

}


/*********************************************************************
 * $Log: AbstractHibiscusHBCICallback.java,v $
 * Revision 1.5  2009/03/26 16:16:15  willuhn
 * @C BPD/UPD auch dann uebernehmen, wenn die neuen eine kleinere Versionsnummer als die aktuellen haben
 *
 * Revision 1.4  2008/11/12 15:50:38  willuhn
 * @C changed loglevel
 *
 * Revision 1.3  2008/05/30 14:23:48  willuhn
 * @N Vollautomatisches und versioniertes Speichern der BPD und UPD in der neuen Property-Tabelle
 *
 * Revision 1.2  2008/05/30 12:31:41  willuhn
 * @N Erster Code fuer gecachte BPD/UPD
 *
 * Revision 1.1  2008/05/30 12:01:37  willuhn
 * @N Gemeinsame Basisimplementierung des HBCICallback in Hibiscus und Payment-Server
 *
 **********************************************************************/