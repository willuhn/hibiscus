/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/migration/Attic/McKoiToH2MigrationListener.java,v $
 * $Revision: 1.5 $
 * $Date: 2007/10/05 17:14:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.migration;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportMcKoiImpl;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;


/**
 * Wird benachrichtigt, wenn die Anwendung gestartet wurde
 * und zeigt ggf. ein Dialog fuer die Datenmigration von
 * Mckoi zu H2 an.
 */
public class McKoiToH2MigrationListener implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    Settings s = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    return s.getBoolean("migration.h2",false);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof SystemMessage))
      return;
    
    if (((SystemMessage) message).getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;
    
    // Checken, ob Migration schon lief
    if (DatabaseMigrationTask.SETTINGS.getString("migration.mckoi-to-h2",null) != null)
      return; // lief bereits
    
    // Checken, ob ueberhaupt die McKoi-Datenbank genutzt wird
    String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
    if (driver == null || !driver.equals(DBSupportMcKoiImpl.class.getName()))
      return;
    
    Settings s = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    if (!s.getBoolean("migration.h2",false))
      return;

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    
    String text = i18n.tr("Das Datenbank-Format von Hibiscus wurde umgestellt.\nMöchten Sie jetzt die Übernahme der Daten in das neue Format durchführen?");
    if (!Application.getCallback().askUser(text))
      return;
    
    Logger.warn("starting database migration from mckoi to h2");
    Application.getController().start(new McKoiToH2MigrationTask());
  }

}


/**********************************************************************
 * $Log: McKoiToH2MigrationListener.java,v $
 * Revision 1.5  2007/10/05 17:14:18  willuhn
 * @N Start der Migration konfigurierbar gemacht
 *
 * Revision 1.4  2007/10/05 17:07:05  willuhn
 * @N Jetzt aber - Migration fertig ;) ..temporaer aber noch in McKoiToH2MigrationListener deaktiviert
 *
 * Revision 1.3  2007/10/05 16:16:58  willuhn
 * @C temporaer noch deaktiviert, bis hinreichend getestet
 *
 * Revision 1.2  2007/10/05 15:27:14  willuhn
 * @N Migration auf H2 laeuft! ;)
 *
 * Revision 1.1  2007/10/04 23:39:49  willuhn
 * @N Datenmigration McKoi->H2 (in progress)
 *
 **********************************************************************/