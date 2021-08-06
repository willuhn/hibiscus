/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.HBCIUtils;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Konvertiert die uebergebene Liste von Adressen nach SEPA.
 * Heisst: BIC und IBAN wird automatisch eingetragen.
 */
public class SepaConvertAddress implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    List<HibiscusAddress> list = new ArrayList<HibiscusAddress>();
    if (context instanceof HibiscusAddress)
      list.add((HibiscusAddress) context);
    else if (context instanceof HibiscusAddress[])
      list.addAll(Arrays.asList((HibiscusAddress[])context));
      
    if (list.size() == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Adresse aus"));
      

    try
    {
      // User nochmal fragen
      String q = i18n.tr("Hierbei werden bei allen ausgewählten Adressen die IBAN und BIC\n" +
                         "anhand der Kontonummer und BLZ errechnet und vervollständigt,\n" +
                         "insofern diese nicht bereits eingetragen sind.\n\nVorgang fortsetzen?");
      if (!Application.getCallback().askUser(q))
        return;
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("error while asking user",e);
      return;
    }
    

    // Wir machen hier keine DB-Transaktion, da der Vorgang nicht atomar sein sollte
    int count = 0;
    Logger.info("auto-completing iban/bic for " + list.size() + " selected addresses");
    for (HibiscusAddress a:list)
    {
      try
      {
        String blz  = StringUtils.trimToNull(a.getBlz());
        String bic  = StringUtils.trimToNull(a.getBic());
        
        String kto  = StringUtils.trimToNull(a.getKontonummer());
        String iban = StringUtils.trimToNull(a.getIban());

        // hat schon IBAN/BIC
        if (bic != null && iban != null)
          continue;
   
        // hat keine BLZ, dann koennen wir weder BIC noch IBAN berechnen
        if (blz == null || blz.length() != HBCIProperties.HBCI_BLZ_LENGTH)
          continue;
        
        String newBic = null;

        if (kto != null && iban == null) // Wenn wir eine Kontonummer und noch keine IBAN haben, dann errechnen
        {
          IBAN newIban = HBCIProperties.getIBAN(blz,kto);
          newBic = newIban.getBIC();
          a.setIban(newIban.getIBAN());
        }
        
        if (bic == null) // Wenn wir noch keine BIC haben, dann errechnen
        {
          if (newBic == null) // nur wenn sie nicht schon von obantoo ermittelt wurde
            newBic = HBCIUtils.getBICForBLZ(blz);
          a.setBic(newBic);
        }
        
        a.store();
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(a));
        count++;
      }
      catch (ApplicationException ae)
      {
        Logger.warn("unable to complete IBAN/BIC: " + ae.getMessage());
      }
      catch (Exception e)
      {
        Logger.error("unable to auto-complete IBAN/BIC",e);
      }
    }
    Logger.info("auto-completed addresses: " + count);
    
    if (count > 0)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("IBAN/BIC errechnet für {0} Adressen",Integer.toString(count)),StatusBarMessage.TYPE_SUCCESS));
  }

}


