/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Enum fuer die verfuegbaren Purpose-Codes.
 */
public enum PurposeCode
{
  /**
   * Miete
   */
  RENT("RENT","Miete"),
  
  /**
   * Einzahlung
   */
  DEPT("DEPT","Einzahlung"),
  
  /**
   * Gutschrift/Rücküberweisung
   */
  REFU("REFU","Gutschrift/Rücküberweisung"),
  
  /**
   * Gehaltszahlungen
   */
  SALA("SALA","Gehaltszahlungen"),
  
  /**
   * Lohn
   */
  PAYR("PAYR","Lohn"),
  
  /**
   * Sozialleistung
   */
  SSBE("SSBE","Sozialleistung"),
  
  /**
   * Zahlung an öffentl. Kassen
   */
  GOVT("GOVT","Zahlung an öffentl. Kassen"),
  
  /**
   * Vermögenswirksame Leistungen
   */
  CBFF("CBFF","Vermögenswirksame Leistungen"),
  
  /**
   * Bonuszahlungen.
   */
  BONU("BONU","Bonuszahlungen"),
  
  /**
   * Spendenzahlungen.
   */
  CHAR("CHAR","Spendenzahlungen"),
  
  /**
   * Wiederkehrende Zahlungen / Dauerauftrag
   */
  RINP("RINP","Wiederkehrende Zahlungen / Dauerauftrag"),

  /**
   * Cash Management Transfer
   */
  CASH("CASH","Cash Management Transfer"),
  
  /**
   * Nicht anders vorgeschrieben 
   */
  NOWS("NOWS","Nicht näher definiert"),

  /**
   * Andere
   */
  OTHR("OTHR","Andere"),
  
  ;
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private String code;
  private String name;
  
  /**
   * ct.
   * @param code der Code.
   * @param name die sprechende Bezeichnung.
   */
  private PurposeCode(String code, String name)
  {
    this.code = code;
    this.name = name;
  }
  
  /**
   * Liefert den Code.
   * @return code der Code.
   */
  public String getCode()
  {
    return code;
  }
  
  /**
   * Liefert einen sprechenden Namen fuer den Purpose-Code.
   * @return sprechender Name fuer den Purpose-Code.
   */
  public String getName()
  {
    return i18n.tr(this.name);
  }
  
  /**
   * Ermittelt den Purpose-Code.
   * @param code der Code. Kann NULL sein.
   * @return der Purpose-Code oder NULL, wenn der Code nicht bekannt ist.
   */
  public static PurposeCode find(String code)
  {
    if (StringUtils.trimToNull(code) == null)
      return null;

    for (PurposeCode pc:values())
    {
      if (pc.getCode().equals(code))
        return pc;
    }
    
    return null;
  }

  @Override
  public String toString()
  {
    return this.getName();
  }
  
  /**
   * Liefert die Liste der bekannten Codes.
   * @return die Liste der bekannten Codes.
   */
  public static List<String> codes()
  {
    List<String> result = new ArrayList<String>();
    for (PurposeCode c:values())
    {
      result.add(c.getCode());
    }
    
    return result;
  }

}


