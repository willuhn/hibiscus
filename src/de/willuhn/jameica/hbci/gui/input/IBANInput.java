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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Eingabefeldes fuer die IBAN.
 * Erlaubt die Eingabe von kleinen Buchstaben - ersetzt jedoch die
 * ersten beiden gegen Gross-Buchstaben.
 */
public class IBANInput extends TextInput
{
  private Input bicInput = null;

  /**
   * ct.
   * @param value die IBAN.
   * @param bicInput optionale Angabe des zugehoerigen Eingabefeldes mit der BIC.
   * Dessen Wert kann dann bei Eingabe einer IBAN automatisch mit der passenden BIC
   * vervollstaendigt werden.
   */
  public IBANInput(String value, Input bicInput)
  {
    super(HBCIProperties.formatIban(value),HBCIProperties.HBCI_IBAN_MAXLENGTH + 5); // max. 5 Leerzeichen
    this.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
    this.setName("IBAN");
    this.bicInput = bicInput;

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
   * @see de.willuhn.jameica.gui.input.TextInput#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Object value)
  {
    super.setValue(value != null ? HBCIProperties.formatIban(value.toString()) : null);

    if (value == null)
      return;

    // Formatierungsleerzeichen zum Testen entfernen
    String s = StringUtils.trimToNull(StringUtils.deleteWhitespace(value.toString()));
    if (s == null)
      return;

    try
    {
      // 1. IBAN sofort checken
      IBAN iban = HBCIProperties.getIBAN(s);

      if (iban == null) // Keine IBAN
        return;

      if (this.bicInput == null)
        return;

      // 2. Wenn wir ein BICInput haben, dann gleich noch die BIC ermitteln und
      // vervollstaendigen. Aber nur, wenn nicht schon eine BIC eingetragen ist.
      if (StringUtils.trimToNull((String)this.bicInput.getValue()) != null)
        return;
      
      String bic = StringUtils.trimToNull(iban.getBIC());
      if (bic == null)
        return;

      this.bicInput.setValue(bic);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
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

