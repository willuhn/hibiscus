/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfiguriertes Eingabefeld fuer den Verwendungszweck.
 */
public class ZweckInput extends TextInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static String VALID = HBCIProperties.HBCI_SEPA_VALIDCHARS;
  
  private Control control = null;
  private Input preview = null;

  /**
   * ct.
   * @param value der Verwendungszweck.
   * @param preview optionale Angabe der Preview.
   */
  public ZweckInput(String value, Input preview)
  {
    super(value,HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
    this.setValidChars(VALID);
    this.setName(i18n.tr("Verwendungszweck"));
    this.preview = preview;
    
    if (this.preview != null)
    {
      this.addListener(e -> {
        try
        {
          final String text = (String) this.getValue();
          if (StringUtils.trimToNull(text) == null)
          {
            this.preview.setValue("");
            return;
          }
          
          final String r = VerwendungszweckUtil.evaluate(text);
          if (Objects.equals(text,r))
          {
            this.preview.setValue("");
            return;
          }
          this.preview.setValue(i18n.tr("Vorschau") + ": " + r);
        }
        catch (Exception ex)
        {
          Logger.error("unable to evaluate usage",ex);
        }
      });
    }
  }
  
  /**
   * Ueberschrieben, um die in SEPA-Verwendungszwecken erlaubten Zeichen automatisch zu entfernen.
   * Dann kann der User per Zwischenablage auch unerlaubte Zeichen einfuegen. Hibiscus schneidet die
   * dann automatisch raus.
   * @see de.willuhn.jameica.gui.input.TextInput#getControl()
   */
  @Override
  public Control getControl()
  {
    if (this.control != null)
      return this.control;
    
    this.control = super.getControl();
    
    // BUGZILLA 1495 - nicht erlaubte Zeichen automatisch rausschneiden
    this.control.addListener(SWT.Verify, new Listener()
    {
      public void handleEvent(Event e)
      {
        if (e.text == null || e.text.length() == 0)
          return;
        
        String backup = e.text;
        e.text = HBCIProperties.clean(e.text,VALID);
        
        int diff = backup.length() - e.text.length();
        if (diff > 0)
        {
          String msg = diff > 1 ? i18n.tr("Es wurden {0} nicht unterstützte Zeichen entfernt",Integer.toString(diff)) : i18n.tr("Es wurde ein nicht unterstütztes Zeichen entfernt");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_INFO));
        }
      }
    });
    
    return this.control;
    
  }

}
