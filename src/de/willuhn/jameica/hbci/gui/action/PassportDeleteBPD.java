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

import java.util.Properties;

import org.kapott.hbci.passport.AbstractHBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.BPDUtil;
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
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
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
    BPDUtil.deleteCache(passport);
  }
}
