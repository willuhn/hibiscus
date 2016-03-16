/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.hbci.server.VersionUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Raeumt beim Start die DB-Properties auf.
 */
public class CleanDBPropertiesMessageConsumer implements MessageConsumer
{
  private final static long bpdMaxAge = 14 * 24 * 60 * 60 * 1000L;
  private final static Settings settings = new Settings(CleanDBPropertiesMessageConsumer.class);
  
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;
    
    // Wir lassen das im Hintergrund laufen, damit der GUI-Thread nicht blockiert wird
    Thread t = new Thread("UPD/BPD cleanup")
    {
      /**
       * @see java.lang.Thread#run()
       */
      @Override
      public void run()
      {
        clean();
      }
    };
    
    t.start();
  }
  
  /**
   * Fuehrt das eigentliche Aufraeumen durch.
   */
  private void clean()
  {
    try
    {
      // UPD loeschen. Werden nicht mehr gebraucht
      if (settings.getString("upd.deleted",null) == null)
      {
        Logger.info("deleting upd cache, no longer needed");
        int count = DBPropertyUtil.deleteAll(DBPropertyUtil.PREFIX_UPD);
        int v     = VersionUtil.deleteAll(de.willuhn.jameica.hbci.Settings.getDBService(),DBPropertyUtil.PREFIX_UPD);
        Logger.info("deleted upd cache entries: " + count + ", versions: " + v);
        settings.setAttribute("upd.deleted",HBCI.LONGDATEFORMAT.format(new Date()));
      }

      // BPD loeschen, die aelter als 7 Tage sind. Werden ja bei naechster Gelegenheit wieder geladen
      long now = System.currentTimeMillis();
      long timestamp = settings.getLong("bpd.deleted",0L);
      if (timestamp == 0L || timestamp < (now - bpdMaxAge))
      {
        Logger.info("expiring bpd cache");
        int count = DBPropertyUtil.deleteAll(DBPropertyUtil.PREFIX_BPD);
        int v     = VersionUtil.deleteAll(de.willuhn.jameica.hbci.Settings.getDBService(),DBPropertyUtil.PREFIX_BPD);
        Logger.info("deleted bpd cache entries: " + count + ", versions: " + v);
        settings.setAttribute("bpd.deleted",now);
      }
      else
      {
        Logger.info("bpd cache up-to-date, last expiry " + HBCI.LONGDATEFORMAT.format(timestamp));
      }
    }
    catch (Exception e)
    {
      Logger.error("error while cleaning upd/bpd cache",e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    return true;
  }

}
