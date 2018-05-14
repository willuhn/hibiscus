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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Eingabefeldes fuer die BIC.
 * Erlaubt die Eingabe von kleinen Buchstaben - ersetzt diese
 * jedoch automatisch gegen Grossbuchstaben.
 */
public class BICInput extends AccountInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Listener listener = null;

  // erlauben wir zwar erstmal, ersetzen wir aber noch
  private final static String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"; 

  /**
   * ct.
   * @param value die BIC.
   */
  public BICInput(String value)
  {
    super(value,HBCIProperties.HBCI_BIC_MAXLENGTH + 4); // max. 4 Leerzeichen
    
    this.listener = new BICListener();

    this.setValidChars(HBCIProperties.HBCI_BIC_VALIDCHARS + LOWERCASE_CHARS);
    this.setName("BIC");
    this.setComment("");
    this.addListener(this.listener);
    
    // und einmal ausloesen
    this.listener.handleEvent(null);
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.TextInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    super.setValue(value);
    // Bei der Gelegenheit aktualisieren wir den Kommentar
    this.listener.handleEvent(null);
  }
  
  /**
   * Aktualisiert den Kommentar mit der Bankbezeichnung.
   */
  private class BICListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event arg0)
    {
      try
      {
        String b = (String) getValue();
        if (b != null && b.length() > 0)
        {
          // 1. Leerzeichen entfernen
          if (b.indexOf(' ') != -1)
            b = b.replaceAll(" ","");

          // 2. In Grossbuchstaben umwandeln
          b = b.toUpperCase();
          
          // 3. BIC pruefen, ggf "XXX" anhaengen
          b = HBCIProperties.checkBIC(b);
          
          // 4. Aktualisierten Wert uebernehmen.
          // Achtung: Super-Klasse aufrufen - sonst gibts eine Rekursion - siehe setValue oben
          BICInput.super.setValue(b);
          
          // 5. Kommentar aktualisieren
          setComment(HBCIProperties.getNameForBank(b));
        }
        else
        {
          setComment("");
        }
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_INFO));
      }
      catch (Exception e)
      {
        Logger.error("error while checking BIC",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen der BIC: {}",e.getMessage()),StatusBarMessage.TYPE_INFO));
      }
    }
  }

}


