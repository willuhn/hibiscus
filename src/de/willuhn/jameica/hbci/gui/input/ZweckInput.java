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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorkonfiguriertes Eingabefeld fuer den Verwendungszweck.
 */
public class ZweckInput extends TextInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static String VALID = HBCIProperties.HBCI_SEPA_VALIDCHARS;

  private Control control = null;

  /**
   * ct.
   * @param value der Verwendungszweck.
   */
  public ZweckInput(String value)
  {
    super(value,HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
    this.setValidChars(VALID);
    this.setName(i18n.tr("Verwendungszweck"));
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
