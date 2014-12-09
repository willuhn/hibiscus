/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.PurposeCode;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Auswahlfeld fuer den Purpose-Code.
 */
public class PurposeCodeInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @param value der vorausgewaehlte Code.
   */
  public PurposeCodeInput(String value)
  {
    super(getCodes(),value);
    this.setName(i18n.tr("SEPA Purpose-Code"));
    this.setEditable(true); // Man kann auch selbst Werte eingeben
    this.setComment("");
    
    final Listener l = new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        String value = (String) getValue();
        PurposeCode c = PurposeCode.find(value);
        setComment(c != null ? c.getName() : "");
      }
    };
    this.addListener(l);
    
    // Einmal initial ausloesen
    l.handleEvent(null);
  }
  
  /**
   * Liefert eine Liste der moeglichen Purpose-Codes inclusive leerem Code.
   * @return Liste der moeglichen Purpose-Codes.
   */
  private static List<String> getCodes()
  {
    List<String> codes = PurposeCode.codes();
    codes.add(0,"");
    return codes;
  }
}


