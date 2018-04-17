/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.Prefix;
import de.willuhn.jameica.hbci.server.VersionUtil;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCITraceMessage;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCITraceMessage.Type;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basis-Implementierung des HBCI-Callback.
 * Ermoeglicht gemeinsamen Code in Hibiscus und Payment-Server.
 */
public abstract class AbstractHibiscusHBCICallback extends AbstractHBCICallback
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Speichert die BPD/UPD des Passports in der Hibiscus-Datenbank zwischen und aktualisiert sie automatisch bei Bedarf.
   * Dadurch stehen sie in Hibiscus zur Verfuegung, ohne dass hierzu ein Passport geoeffnet werden muss.
   * @param passport der betreffende Passport.
   */
  protected void updateBPD(HBCIPassport passport)
  {
    if (passport == null)
      return;
    
    try
    {
      final Properties data = passport.getBPD();
      final String version  = passport.getBPDVersion();
      final Prefix prefix   = DBPropertyUtil.Prefix.BPD;
  
      String user = passport.getUserId();
      if (version == null || version.length() == 0 || user == null || user.length() == 0 || data == null || data.size() == 0)
      {
        Logger.debug("[" + prefix + "] no version, no userid or no data found, skipping update");
        return;
      }
      
      int nv = Integer.parseInt(version);
      Version v = VersionUtil.getVersion(Settings.getDBService(),prefix.value() + "." + user);
      int cv = v.getVersion();
      
      // Keine neue Version
      if (nv == cv)
        return;
      
      if (cv < 0 || nv < 0)
      {
        Logger.warn("SUSPECT - " + prefix + " version smaller than zero. new: " + nv + ", current: " + cv);
        return;
      }
  
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      ProgressMonitor monitor = session != null ? session.getProgressMonitor() : null;

      if (monitor != null)
        monitor.log(i18n.tr("Aktualisiere BPD"));
      Logger.info("got new " + prefix + " version. old: " + cv + ", new: " + nv + ", updating cache");
      Set<String> customerIDs = HBCIProperties.getCustomerIDs(passport);
      
      int count = 0;
      
      for (String customerId:customerIDs)
      {
        int deleted = DBPropertyUtil.deleteScope(prefix,customerId);
        Logger.info("deleted " + deleted + " old BPD cache entries");
        
        for (Enumeration keys = data.keys();keys.hasMoreElements();)
        {
          String name = (String) keys.nextElement();
          if (DBPropertyUtil.insert(prefix,customerId,null,name,data.getProperty(name)))
            count++;
            
          if (count % 20 == 0 && monitor != null)
            monitor.log("  " + i18n.tr("{0} Datensätze",Integer.toString(count)));
        }
      }
      Logger.info("created " + count + " new BPD cache entries");
      
      // Speichern der neuen Versionsnummer
      v.setVersion(nv);
      v.store();
    }
    catch (Exception e)
    {
      Logger.error("error while updating bpd - will be ignored",e);
    }
  }
  
  /**
   * Protokolliert die Status-Info aus dem HBCI-Kernel.
   * @param text zu loggender Text.
   */
  protected abstract void status(String text);
  
  /**
   * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport, int, java.lang.Object[])
   */
  @Override
  public void status(HBCIPassport passport, int statusTag, Object[] o)
  {
    switch (statusTag)
    {
      case STATUS_INST_BPD_INIT:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA"));
        break;
  
      case STATUS_INST_BPD_INIT_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA_DONE",passport.getBPDVersion()));
        updateBPD(passport);
        break;
  
      case STATUS_INST_GET_KEYS:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS"));
        break;
  
      case STATUS_INST_GET_KEYS_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS_DONE"));
        break;
  
      case STATUS_SEND_KEYS:
        status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS"));
        break;
  
      case STATUS_SEND_KEYS_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS_DONE"));
        break;
  
      case STATUS_INIT_SYSID:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID"));
        break;
  
      case STATUS_INIT_SYSID_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID_DONE",o[1].toString()));
        break;
  
      case STATUS_INIT_SIGID:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID"));
        break;
  
      case STATUS_INIT_SIGID_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID_DONE",o[1].toString()));
        break;
  
      case STATUS_INIT_UPD:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA"));
        break;
  
      case STATUS_INIT_UPD_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA_DONE",passport.getUPDVersion()));
        break;
  
      case STATUS_LOCK_KEYS:
        status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK"));
        break;
  
      case STATUS_LOCK_KEYS_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK_DONE"));
        break;
  
      case STATUS_DIALOG_INIT:
        status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT"));
        break;
  
      case STATUS_DIALOG_INIT_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT_DONE",o[1]));
        break;
  
      case STATUS_SEND_TASK:
        status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_NEW_JOB",((HBCIJob)o[0]).getName()));
        break;
  
      case STATUS_SEND_TASK_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_JOB_DONE",((HBCIJob)o[0]).getName()));
        break;
  
      case STATUS_DIALOG_END:
        status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END"));
        break;
  
      case STATUS_DIALOG_END_DONE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END_DONE"));
        break;
  
      case STATUS_MSG_CREATE:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_CREATE",o[0].toString()));
        break;
  
      case STATUS_MSG_SIGN:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_SIGN"));
        break;
  
      case STATUS_MSG_CRYPT:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_CRYPT"));
        break;
  
      case STATUS_MSG_SEND:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_SEND"));
        break;
  
      case STATUS_MSG_RECV:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_RECV"));
        break;

      case STATUS_MSG_PARSE:
        Logger.debug(HBCIUtilsInternal.getLocMsg("STATUS_MSG_PARSE",o[0].toString()));
        break;

      case STATUS_MSG_DECRYPT:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_DECRYPT"));
        break;
  
      case STATUS_MSG_VERIFY:
        status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_VERIFY"));
        break;
  
      case STATUS_MSG_RAW_SEND:
        Application.getMessagingFactory().getMessagingQueue(HBCISynchronizeBackend.HBCI_TRACE).sendMessage(new HBCITraceMessage(Type.SEND,o[0].toString()));
        break;
  
      case STATUS_MSG_RAW_RECV:
        Application.getMessagingFactory().getMessagingQueue(HBCISynchronizeBackend.HBCI_TRACE).sendMessage(new HBCITraceMessage(Type.RECV,o[0].toString()));
        break;
        
      default:
        Logger.warn("unknown callback status: " + statusTag);
    }
  }

}
