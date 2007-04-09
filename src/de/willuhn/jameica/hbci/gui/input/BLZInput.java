/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/BLZInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/09 22:45:12 $
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;


/**
 * BUGZILLA 380
 * Vorkonfiguriertes Eingabe-Feld fuer BLZ.
 */
public class BLZInput extends TextInput
{
  private Listener listener = null;
  private I18N i18n         = null;

  /**
   * ct.
   * @param value
   */
  public BLZInput(String value)
  {
    super(value, HBCIProperties.HBCI_BLZ_LENGTH);
    
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
   * Aktualisiert den Kommentar mit der BLZ.
   */
  private class BLZListener implements Listener
  {
    public void handleEvent(Event arg0)
    {
      try
      {
        String b = (String)getValue();
        if (b != null && b.length() > 0)
          setComment(HBCIUtils.getNameForBLZ(b));
        else
          setComment("");
      }
      catch (Exception e)
      {
        // ignore
      }
    }
  }
  
}


/**********************************************************************
 * $Log: BLZInput.java,v $
 * Revision 1.1  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 **********************************************************************/
