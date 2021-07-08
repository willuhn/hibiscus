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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Definition der verschiedenen Konto-Arten.
 * Siehe FinTS_3.0_Formals_2011-06-14_final_version.pdf - Data Dictionary "Kontoart",
 * Seite 94.
 */
public enum KontoType
{
  /**
   * Kontokorrent-/Girokonto.
   */
  GIRO(1,9,"Kontokorrent-/Girokonto"),

  /**
   * Sparkonto.
   */
  SPAR(10,19,"Sparkonto"),

  /**
   * Festgeldkonto (Termineinlagen).
   */
  FESTGELD(20,29,"Festgeldkonto (Termineinlagen)"),

  /**
   * Wertpapierdepot.
   */
  WERTPAPIERDEPOT(30,39,"Wertpapierdepot"),

  /**
   * Kredit-/Darlehenskonto.
   */
  DARLEHEN(40,49,"Kredit-/Darlehenskonto"),

  /**
   * Kreditkartenkonto.
   */
  KREDITKARTE(50,59,"Kreditkartenkonto"),

  /**
   * Fonds-Depot bei einer Kapitalanlagegesellschaft.
   */
  FONDSDEPOT(60,69,"Fonds-Depot bei einer Kapitalanlagegesellschaft"),

  /**
   * Bausparvertrag.
   */
  BAUSPAR(70,79,"Bausparvertrag"),

  /**
   * Versicherungsvertrag.
   */
  VERSICHERUNG(80,89,"Versicherungsvertrag"),

  /**
   * Sonstige (nicht zuordenbar).
   */
  SONSTIGE(90,99,"Sonstige (nicht zuordenbar)"),

  ;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Die Default-Kontoart.
   */
  public final static KontoType DEFAULT = GIRO;

  private int min;
  private int max;
  private String name;

  /**
   * ct.
   * @param min Mindest-ID.
   * @param max Maximal-ID.
   * @param name scprechende Bezeichnung der Kontoart.
   */
  private KontoType(int min, int max, String name)
  {
    this.min = min;
    this.max = max;
    this.name = name;
  }

  /**
   * Liefert einen sprechenden Namen fuer die Kontoart.
   * @return sprechender Name fuer die Kontoart.
   */
  public String getName()
  {
    return i18n.tr(this.name);
  }

  /**
   * Liefert den zu verwendenden Wert, wenn diese Kontoart manuell ausgewaehlt wurde.
   * @return der zu verwendende Wert, wenn diese Kontoart manuell ausgewaehlt wurde.
   */
  public int getValue()
  {
    return this.min;
  }

  /**
   * Ermittelt die Kontoart fuer die ID.
   * @param id die ID. Kann NULL sein.
   * @return die Kontoart oder NULL, wenn die ID nicht bekannt ist.
   */
  public static KontoType find(Integer id)
  {
    if (id == null)
      return null;

    int i = id.intValue();
    for (KontoType type:values())
    {
      if (i >= type.min && i <= type.max)
        return type;
    }

    return null;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString()
  {
    return this.getName();
  }
}
