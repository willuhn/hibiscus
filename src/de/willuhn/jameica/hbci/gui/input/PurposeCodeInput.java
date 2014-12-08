/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.PurposeCode;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Auswahlfeld fuer den Purpose-Code.
 */
public class PurposeCodeInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @param value der vorausgewaehlte Code.
   */
  public PurposeCodeInput(String value)
  {
    super(PurposeCode.codes(),value);
    this.setName(i18n.tr("SEPA Purpose-Code"));
    this.setPleaseChoose(i18n.tr("<Nicht angegeben>"));
    this.setEditable(true); // Man kann auch selbst Werte eingeben
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  @Override
  protected String format(Object bean)
  {
    // Wenn wir einen Namen fuer den Code haben, liefern wir den formatiert zurueck
    if (bean == null)
      return super.format(bean);
    
    String s = StringUtils.trimToNull(bean.toString());
    if (s == null)
      return super.format(bean);
    
    PurposeCode pc = PurposeCode.find(s);
    if (pc != null)
      return pc.getCode() + " (" + pc.getName() + ")"; // Den Code kennen wir
    
    // kennen wir nicht
    return s;
  }
  
}


