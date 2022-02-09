/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Set;

import org.kapott.hbci.passport.AbstractHBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.Prefix;
import de.willuhn.jameica.hbci.server.VersionUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Loeschen der BPD eines Passports.
 */
public class PassportDeleteBPD implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Erwartet ein Objekt vom Typ HBCIPassport.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof AbstractHBCIPassport))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Bank-Zugang aus."));
    
    AbstractHBCIPassport passport = (AbstractHBCIPassport) context;

    Logger.info("deleting BPD");
    passport.clearBPD();
    
    // Das triggert beim naechsten Verbindungsaufbau
    // HBCIHandler.<clinit>
    // -> HBCIHandler.registerUser()
    // -> HBCIUser.register()
    // -> HBCIUser.updateUserData()
    // -> HBCIUser.fetchSysId() - und das holt die BPD beim naechsten mal ueber einen nicht-anonymen Dialog
    Logger.info("mark sys id to be synced");
    passport.syncSysId();
    
    // Ausserdem muessen wir noch sicherstellen, dass die UPD-Versionen 0 ist damit *beide*
    // beim naechsten Mal definitiv neu abgerufen werden
    Properties upd = passport.getUPD();
    if (upd != null)
    {
      Logger.info("setting UPD version to 0");
      upd.setProperty("UPA.version","0");
    }
    
    passport.saveChanges();

    // Caches loeschen
    Logger.info("deleting bpd/upd caches for user ids");
    Set<String> customerIds = HBCIProperties.getCustomerIDs(passport);
    for (String customerId:customerIds)
    {
      try
      {
        DBPropertyUtil.deleteScope(DBPropertyUtil.Prefix.BPD,customerId);
        DBPropertyUtil.deleteScope(DBPropertyUtil.Prefix.UPD,customerId);
      }
      catch (Exception e)
      {
        // Auch wenn das fehlschlaegt, soll der Rest trotzdem durchgefuehrt werden
        Logger.error("error while clearing BPD/UPD cache",e);
      }
    }

    // Versionsnummer Caches loeschen, um das Neubefuellen des Cache zu forcieren
    Logger.info("deleting stored bpd/upd version numbers");
    String user = passport.getUserId();
    if (user != null && user.length() > 0)
    {
      try
      {
        VersionUtil.delete(Settings.getDBService(),DBPropertyUtil.Prefix.BPD.value() + "." + user);
      }
      catch (RemoteException re)
      {
        Logger.error("error while deleting bpd cache",re);
      }
      try
      {
        VersionUtil.delete(Settings.getDBService(),DBPropertyUtil.Prefix.UPD.value() + "." + user);
      }
      catch (RemoteException re)
      {
        Logger.error("error while deleting upd cache",re);
      }
      
      // Wir markieren ausserdem auch noch den Cache als expired
      Logger.info("mark upd/bpd caches expired");
      BPDUtil.expireCache(passport,Prefix.BPD);
      BPDUtil.expireCache(passport,Prefix.UPD);
    }
  }

}


