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
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Eingabefeldes fuer die BIC.
 * Erlaubt die Eingabe von kleinen Buchstaben - ersetzt diese
 * jedoch automatisch gegen Grossbuchstaben.
 */
public class BICInput extends AccountInput
{
  // erlauben wir zwar erstmal, ersetzen wir aber noch
  private final static String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"; 

  /**
   * ct.
   * @param value die BIC.
   */
  public BICInput(String value)
  {
    super(value,HBCIProperties.HBCI_BIC_MAXLENGTH + 4); // max. 4 Leerzeichen
    this.setValidChars(HBCIProperties.HBCI_BIC_VALIDCHARS + LOWERCASE_CHARS);
    this.setName("BIC");
    
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String) getValue();
        if (s == null || s.length() == 0)
          return;
        
        try
        {
          setValue(HBCIProperties.checkBIC(s.toUpperCase()));
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_INFO));
        }
      }
    });
  }
  
}


