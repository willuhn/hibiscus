/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.hbci.HBCIProperties;

/**
 * Implementierung eines Eingabefeldes fuer die IBAN.
 * Erlaubt die Eingabe von kleinen Buchstaben - ersetzt jedoch die
 * ersten beiden gegen Gross-Buchstaben.
 */
public class IBANInput extends AccountInput
{
  /**
   * ct.
   * @param value die IBAN.
   */
  public IBANInput(String value)
  {
    super(value,HBCIProperties.HBCI_IBAN_MAXLENGTH + 5); // max. 5 Leerzeichen
    this.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
    this.setName("IBAN");
    
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String) getValue();
        if (s == null || s.length() < 2)
          return;

        // Die ersten beiden Buchstaben gegen Gross-Buchstaben ersetzen
        String s2 = s.substring(0,2).toUpperCase();
        setValue(s2 + s.substring(2));
      }
    });
  }
  
}


