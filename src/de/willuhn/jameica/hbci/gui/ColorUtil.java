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

import org.eclipse.swt.graphics.Color;

import de.willuhn.jameica.hbci.Settings;

/**
 * Util-Klasse fuer Farb-Berechnungen.
 */
public class ColorUtil
{
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
}



/**********************************************************************
 * $Log: ColorUtil.java,v $
 * Revision 1.1  2012/04/23 21:03:41  willuhn
 * @N BUGZILLA 1227
 *
 **********************************************************************/