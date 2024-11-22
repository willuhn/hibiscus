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
 * Abstrakte Basis-Klasse f�r die Contextmen�-Eintr�ge f�r die Gelesen/Ungelesen-Markiuerung.
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
    this(text,a,icon,null);
  }
  
  /**
   * ct.
   * @param text Anzuzeigender Text.
   * @param a Action, die beim Klick ausgefuehrt werden soll.
   * @param icon optionale Angabe eines Icons.
   * @param shortcut Tastenkombination.
   */
  public AbstractUmsatzReadContextMenuItem(String text, Action a, String icon, String shortcut)
  {
    super(text,a,icon);
    this.setShortcut(shortcut);
  }
  
  /**
   * Fragt den User, ob er die Ums�tze beim Beenden als gelesen markieren m�chte.
   */
  protected static void askMarkReadOnExit()
  {
    try
    {
      final String s =  i18n.tr("Sollen die Ums�tze beim Beenden des Programms automatisch als gelesen markiert werden?");
      final String s2 = i18n.tr("Sie k�nnen dies sp�ter in den Einstellungen der Umsatzliste (Werkzeug-Symbol oben rechts) �ndern.");
      final boolean yes = Application.getCallback().askUser(s + " " + s2);
      Settings.setMarkReadOnExit(yes);
    }
    catch (Exception ex)
    {
      Logger.error("unable to ask user",ex);
    }
  }
}


