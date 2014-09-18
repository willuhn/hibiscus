/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/BLZInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/09/26 11:07:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;


/**
 * BUGZILLA 380
 * Vorkonfiguriertes Eingabe-Feld fuer BLZ.
 */
public class BLZInput extends AccountInput
{
  private Listener listener = null;
  private I18N i18n         = null;

  /**
   * ct.
   * @param value
   */
  public BLZInput(String value)
  {
    super(value, HBCIProperties.HBCI_BLZ_LENGTH + 3);
    
    this.i18n     = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.listener = new BLZListener();

    this.setValidChars(HBCIProperties.HBCI_BLZ_VALIDCHARS);
    this.setName(i18n.tr("BLZ"));
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
  private class BLZListener implements Listener
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
          // Wir schnipseln gleich noch Leerzeichen raus - aber nur, wenn welche drin stehen
          if (b.indexOf(' ') != -1)
            b = b.replaceAll(" ","");
          setComment(HBCIProperties.getNameForBank(b));
        }
        else
        {
          setComment("");
        }
      }
      catch (Exception e)
      {
        // ignore
      }
    }
  }
  
}
