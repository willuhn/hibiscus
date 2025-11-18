/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Enum mit den Auftragstypen.
 */
public enum AuslandsUeberweisungTyp
{
  /**
   * Ueberweisung
   */
  UEBERWEISUNG("Überweisung"),
  
  /**
   * Terminueberweisung
   */
  TERMIN("Bankseitige SEPA-Terminüberweisung"),

  /**
   * Umbuchung
   */
  UMBUCHUNG("Interne Umbuchung (Übertrag)"),
  
  /**
   * Echtzeitueberweisung
   */
  INSTANT("Echtzeitüberweisung (SEPA Instant-Payment)"),
  
  ;
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private String description = null;
  
  /**
   * ct.
   * @param description der Beschreibungstext.
   */
  private AuslandsUeberweisungTyp(String description)
  {
    this.description = description;
  }
  
  /**
   * Liefert den Beschreibungstext.
   * @return description
   */
  public String getDescription()
  {
    return i18n.tr(description);
  }
  
  /**
   * Ermittelt den Typ anhand des Auftrages.
   * @param u der Auftrag.
   * @return der Typ.
   * @throws RemoteException
   */
  public static AuslandsUeberweisungTyp determine(AuslandsUeberweisung u) throws RemoteException
  {
    if (u.isInstantPayment())
      return INSTANT;
    
    if (u.isTerminUeberweisung())
      return TERMIN;
    
    if (u.isUmbuchung())
      return UMBUCHUNG;
    
    return UEBERWEISUNG;
  }
  
  /**
   * Liefert die Enum für den Namen.
   * @param name der Name.
   * @return der Typ oder NULL, wenn keiner oder ein ungültiger angegeben ist.
   */
  public static AuslandsUeberweisungTyp byName(String name)
  {
    name = StringUtils.trimToNull(name);
    if (name == null)
      return null;

    for (AuslandsUeberweisungTyp t:values())
    {
      if (t.name().equalsIgnoreCase(name))
        return t;
    }
    
    return null;
  }
  
  /**
   * Uebernimmt den Typ in den Auftrag.
   * @param u der Auftrag.
   * @throws RemoteException
   */
  public void apply(AuslandsUeberweisung u) throws RemoteException
  {
    u.setInstantPayment(this == INSTANT);
    u.setUmbuchung(this == UMBUCHUNG);
    u.setTerminUeberweisung(this == TERMIN);
  }
}


