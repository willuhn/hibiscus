/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportChangeRequest;
import de.willuhn.jameica.hbci.passports.pintan.server.PassportImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die das Aendern der Kundendaten uebernimmt.
 */
public class PassportChange implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ PassportChangeRequest.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof PassportChangeRequest))
    {
      Logger.error("expected object type PassportChangeRequest but was " + context);
      return;
    }

    PassportChangeRequest pcr = (PassportChangeRequest) context;
    if (pcr.passport == null)
    {
      Logger.error("no passport given");
      return;
    }
    
    String custOld = pcr.passport.getCustomerId();
    String userOld = pcr.passport.getUserId();
    
    boolean changeCustId = StringUtils.trimToNull(pcr.custId) != null && !StringUtils.trimToEmpty(custOld).equals(StringUtils.trimToEmpty(pcr.custId));
    boolean changeUserId = StringUtils.trimToNull(pcr.userId) != null && !StringUtils.trimToEmpty(userOld).equals(StringUtils.trimToEmpty(pcr.userId));
    
    if (!changeCustId && !changeUserId)
      return;
    
    boolean changed = false;

    try
    {
      
      // 1) User/Customer im Passport selbst
      {
        Logger.info("applying new customerId/userId to passport");
        if (changeCustId)
        {
          Logger.info("applying new customerId to passport");
          pcr.passport.setCustomerId(pcr.custId);
          changed = true;
        }
        if (changeUserId)
        {
          Logger.info("applying new userId to passport");
          pcr.passport.setUserId(pcr.userId);
          changed = true;
        }
      }
      
      // 2) User/Customer in den UPD
      {
        Properties upd = pcr.passport.getUPD();
        Enumeration e = upd.keys();
        int count = 0;
        while (e.hasMoreElements())
        {
          String key = (String) e.nextElement();
          String value = upd.getProperty(key);
          if (value == null || value.length() == 0)
            continue;
          
          if (changeCustId && value.equals(custOld))
          {
            Logger.info("updating UPD entry " + key + " with new customerId");
            upd.setProperty(key,pcr.custId);
            count++;
            continue;
          }
          
          if (changeUserId && value.equals(userOld))
          {
            Logger.info("updating UPD entry " + key + " with new userId");
            upd.setProperty(key,pcr.userId);
            count++;
          }
        }
        Logger.info("updated " + count + " entries in UPD");
        changed |= count > 0;
      }
      
      if (changed)
      {
        Logger.info("saving changed passport");
        pcr.passport.saveChanges();
      }

      // 3) Kundenkennung in zugeordneten Konten aktualisieren
      {
        int count = 0;
        org.kapott.hbci.structures.Konto[] konten = pcr.passport.getAccounts();
        if (konten != null && konten.length > 0)
        {
          for (org.kapott.hbci.structures.Konto konto:konten)
          {
            Konto k = Converter.HBCIKonto2HibiscusKonto(konto, PassportImpl.class);
            if (!k.isNewObject())
            {
              if (changeCustId)
              {
                Logger.info("updating customerid in account ID " + k.getID());
                k.setKundennummer(pcr.custId);
                k.store();
                
                k.addToProtokoll(i18n.tr("Geänderte Kundenkennung - neu: {0}, alt: {1}",pcr.custId,custOld),Protokoll.TYP_SUCCESS);
                count++;
              }

              // Wenn nur die Benutzerkennung geaendert wurde, protokollieren
              // wir das wenigstens im Konto, auch wenn das Konto selbst dabei
              // nicht angefasst wurde
              if (changeUserId)
                k.addToProtokoll(i18n.tr("Geänderte Benutzerkennung - neu: {0}, alt: {1}",pcr.userId,userOld),Protokoll.TYP_SUCCESS);
            }
          }
        }
        Logger.info("updated customerId in " + count + " accounts");
      }
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Geänderte Zugangsdaten erfolgreich übernommen"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while applying new user-/customer data",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der geänderten Zugangsdaten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}


