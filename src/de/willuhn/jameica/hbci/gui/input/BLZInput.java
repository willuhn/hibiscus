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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.util.DelayedListener;
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
  private List<Listener> blzListener = new ArrayList<Listener>();
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
   * Registriert einen Listener, der ausgeloest wird, sobald eine bekannte BLZ eingegeben wurde.
   * Das passiert sofort nach Eingabe, nicht erst bei Focus-Wechsel.
   * @param l der Listener.
   */
  public void addBLZListener(Listener l)
  {
    this.blzListener.add(l);
    
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.TextInput#getControl()
   */
  @Override
  public Control getControl()
  {
    Control c = super.getControl();
    
    // wir haengen noch einen Keyup-Listener an, um sofort bei Eingabe der BLZ ausloesen zu koennen
    c.addListener(SWT.KeyUp, new DelayedListener(this.listener));
    
    return c;
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
          
          if (b.length() == HBCIProperties.HBCI_BLZ_LENGTH)
          {
            String name = HBCIProperties.getNameForBank(b);
            setComment(name);
            if (StringUtils.trimToNull(name) != null)
            {
              arg0.data = b;
              for (Listener l:blzListener)
              {
                l.handleEvent(arg0);
              }
            }
            return;
          }
        }
        setComment("");
      }
      catch (Exception e)
      {
        setComment("");
      }
    }
  }
  
}
