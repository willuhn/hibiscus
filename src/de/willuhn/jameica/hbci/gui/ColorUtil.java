/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.logging.Logger;

/**
 * Util-Klasse fuer Farb-Berechnungen.
 */
public class ColorUtil
{
  private final static Map<String,Color> colorCache = new HashMap<String,Color>();

  /**
   * Liefert die Farbe, in der der angegebene Wert gezeichnet werden soll.
   * @param value der Wert.
   * @return die Farbe.
   */
  public static Color getForeground(double value)
  {
    return ColorUtil.getColor(value,Settings.getBuchungSollForeground(),
                                    Settings.getBuchungHabenForeground(),
                                    de.willuhn.jameica.gui.util.Color.FOREGROUND.getSWTColor());
  }
  
  /**
   * Markiert die Zeile je nach Konfiguration entweder komplett farbig oder nur den Betrag in der angegebenen Spalte.
   * @param item die Zeile.
   * @param col die Spalte.
   * @param value der Wert.
   */
  public static void setForeground(TableItem item, int col, double value)
  {
    final boolean colorValue = Settings.getColorValues();
    
    if (colorValue)
      item.setForeground(col,getForeground(value));
    else
      item.setForeground(getForeground(value));
  }

  /**
   * Markiert die Zeile je nach Konfiguration entweder komplett farbig oder nur den Betrag in der angegebenen Spalte.
   * @param item die Zeile.
   * @param col die Spalte.
   * @param value der Wert.
   */
  public static void setForeground(TreeItem item, int col, double value)
  {
    final boolean colorValue = Settings.getColorValues();
    
    if (colorValue)
      item.setForeground(col,getForeground(value));
    else
      item.setForeground(getForeground(value));
  }
  
  /**
   * Markiert die angegebene Spalte in der Farbe der Umsatzkategorie.
   * @param item die Zeile.
   * @param col die Spalte.
   * @param ut die Umsatz-Kategorie.
   */
  public static void setForeground(TreeItem item, int col, UmsatzTyp ut)
  {
    final Color color = getColor(ut);
    if (color == null)
      return;
    
    if (col < 0)
      item.setForeground(color);
    else
      item.setForeground(col,color);
  }
  
  /**
   * Markiert die angegebene Spalte in der Farbe der Umsatzkategorie.
   * @param item die Zeile.
   * @param col die Spalte.
   * @param ut die Umsatz-Kategorie.
   */
  public static void setForeground(TableItem item, int col, UmsatzTyp ut)
  {
    final Color color = getColor(ut);
    if (color == null)
      return;
    
    if (col < 0)
      item.setForeground(color);
    else
      item.setForeground(col,color);
  }

  /**
   * Liefert die Farbe, in der der angegebene Wert gezeichnet werden soll.
   * @param value der Wert.
   * @param negative die Farbe fuer negative Werte.
   * @param positive die Farbe fuer positive Werte.
   * @param zero die Farbe fuer Null-Werte.
   * @return die Farbe.
   */
  public static <T> T getColor(double value, T negative, T positive, T zero)
  {
    if (value <= -0.01)
      return negative;
    
    if (value >= 0.01)
      return positive;
    
    return zero;
  }
  
  /**
   * Liefert die zu verwendende Farbe für die Umsatz-Kategorie.
   * @param ut die Kategorie.
   * @return die Farbe oder NULL, wenn keine Farbe verwendet werden soll.
   */
  public static Color getColor(UmsatzTyp ut)
  {
    try
    {
      if (ut == null || !ut.isCustomColor())
        return null;
      
      final int[] color = ut.getColor();
      if (color == null || color.length != 3)
        return null;
      
      final RGB rgb = new RGB(color[0],color[1],color[2]);
      return colorCache.computeIfAbsent(rgb.toString(), s -> new Color(GUI.getDisplay(),rgb));
    }
    catch (Exception e)
    {
      Logger.error("unable to determine color",e);
      return null;
    }
  }
}
