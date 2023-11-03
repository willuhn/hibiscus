/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse für die Contextmenü-Einträge für die Gelesen/Ungelesen-Markiuerung.
 */
public class AbstractUmsatzReadContextMenuItem extends ContextMenuItem
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @param text Anzuzeigender Text.
   * @param a Action, die beim Klick ausgefuehrt werden soll.
   * @param icon optionale Angabe eines Icons.
   */
  public AbstractUmsatzReadContextMenuItem(String text, Action a, String icon)
  {
    super(text,a,icon);
  }
  
  /**
   * Fragt den User, ob er die Umsätze beim Beenden als gelesen markieren möchte.
   */
  protected static void askMarkReadOnExit()
  {
    try
    {
      final String s =  i18n.tr("Sollen die Umsätze beim Beenden des Programms automatisch als gelesen markiert werden?");
      final String s2 = i18n.tr("Sie können dies später in den Einstellungen der Umsatzliste (Wekzeug-Symbol oben rechts) ändern.");
      final boolean yes = Application.getCallback().askUser(s + " " + s2);
      Settings.setMarkReadOnExit(yes);
    }
    catch (Exception ex)
    {
      Logger.error("unable to ask user",ex);
    }
  }
}


