/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCIProperties;

/**
 * Implementierung eines Eingabefeldes fuer die IBAN.
 * Erlaubt die Eingabe von kleinen Buchstaben - ersetzt jedoch die
 * ersten beiden gegen Gross-Buchstaben.
 */
public class IBANInput extends TextInput
{
  /**
   * ct.
   * @param value die IBAN.
   */
  public IBANInput(String value)
  {
    super(HBCIProperties.formatIban(value),HBCIProperties.HBCI_IBAN_MAXLENGTH + 5); // max. 5 Leerzeichen
    this.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
    this.setName("IBAN");
    
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String) getValue();
        if (s == null || s.length() < 2)
          return;

        setValue(HBCIProperties.formatIban(s));
      }
    });
  }
  
  /**
   * Ueberschrieben, um sicherzustellen, dass die IBAN keine Leerzeichen enthaelt.
   * @see de.willuhn.jameica.gui.input.TextInput#getValue()
   */
  @Override
  public Object getValue()
  {
    String s = (String) super.getValue();
    if (s == null)
      return s;
    
    return StringUtils.deleteWhitespace(s);
  }
  
  /**
   * Ueberschrieben, um zusaetzlich noch die Leerzeichen zuzulassen.
   * @see de.willuhn.jameica.gui.input.AbstractInput#setValidChars(java.lang.String)
   */
  @Override
  public void setValidChars(String chars)
  {
    super.setValidChars(chars + " ");
  }
  
}


