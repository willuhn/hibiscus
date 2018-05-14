/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.formatter;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.HBCIProperties;

/**
 * Formatiert eine IBAN.
 */
public class IbanFormatter implements Formatter
{
  /**
   * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
   */
  @Override
  public String format(Object o)
  {
    if (o == null)
      return "";

    String s = o.toString();
    if (StringUtils.trimToEmpty(s).length() > 10) // IBAN
      return HBCIProperties.formatIban(s);
    
    return s;
  }

}


