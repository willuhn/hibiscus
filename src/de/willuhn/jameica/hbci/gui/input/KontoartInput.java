/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.KontoType;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Auswahlfeld fuer die Kontoart.
 */
public class KontoartInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Integer current = null;
  
  /**
   * ct.
   * @param value die vorausgewaehlte ID.
   */
  public KontoartInput(Integer value)
  {
    super(KontoType.values(),KontoType.find(value));
    this.setName(i18n.tr("Kontoart"));
    this.setPleaseChoose(i18n.tr("<Nicht angegeben>"));
    
    this.current = value;
  }
  
  /**
   * Liefert nicht die enum zurueck sonderen den Integer-Wert.
   */
  @Override
  public Object getValue()
  {
    KontoType type = (KontoType) super.getValue();
    if (type == null)
      return null; // Explizit nichts ausgewaehlt
    
    // Wenn immer noch die selbe Art ausgewaehlt ist, wie die von "current", dann liefern
    // wir den originalen Int-Wert, nicht den der Enum
    KontoType currentType = current != null ? KontoType.find(current) : null;
    
    if (currentType != null && currentType.equals(type))
      return current;
    
    // Ansonsten den neuen
    return type.getValue();
  }

}


