/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Enum mit den Status-Codes von Instant-Payments.
 */
@SuppressWarnings("javadoc")
public enum InstantPaymentStatus
{
  STATUS1(1,true,"in Terminierung"),
  STATUS2(2,false,"abgelehnt von erster Inkassostelle"),
  STATUS3(3,true,"in Bearbeitung"),
  STATUS4(4,true,"Creditoren-seitig verarbeitet, Buchung veranlasst"),
  STATUS5(5,true,"R-Transaktion wurde veranlasst"),
  STATUS6(6,false,"Auftrag fehlgeschagen"),
  STATUS7(7,true,"Auftrag ausgeführt; Geld für den Zahlungsempfänger verfügbar"),
  STATUS8(8,false,"Abgelehnt durch Zahlungsdienstleister des Zahlers"),
  STATUS9(9,false,"Abgelehnt durch Zahlungsdienstleister des Zahlungsempfängers"),
  
  ;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private int status = 0;
  private boolean success = false;
  private String description = null;
  
  /**
   * ct.
   * @param status der Status-Code.
   * @param success true, wenn es sich um einen Erfolgsstatus handelt.
   * @param description Beschreibungstext.
   */
  private InstantPaymentStatus(int status, boolean success, String description)
  {
    this.status = status;
    this.success = success;
    this.description = description;
  }
  
  /**
   * Liefert den Statuscode.
   * @return status der Status-Code.
   */
  public int getStatus()
  {
    return status;
  }
  
  /**
   * Liefert einen sprechenden Beschreibungstext.
   * @return description
   */
  public String getDescription()
  {
    return i18n.tr(description);
  }
  
  /**
   * Liefert true, wenn es sich um einen Erfolgsstatus handelt.
   * @return success true, wenn es sich um einen Erfolgsstatus handelt.
   */
  public boolean isSuccess()
  {
    return success;
  }
  
  /**
   * Versucht, den Status anhand des uebergebenen Codes zu ermitteln.
   * @param status der Status.
   * @return der Status oder NULL, wenn er nicht ermittelbar war.
   */
  public static InstantPaymentStatus determine(String status)
  {
    if (StringUtils.trimToNull(status) == null)
      return null;

    try
    {
      int i = Integer.parseInt(status);
      for (InstantPaymentStatus s:values())
      {
        if (s.status == i)
          return s;
      }
    }
    catch (Exception e)
    {
      Logger.error("got invalid status code for sepa instant payment " + status,e);
    }
    
    return null;
  }

}
