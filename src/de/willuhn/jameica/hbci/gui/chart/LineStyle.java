/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

/**
 * Enum mit den Line-Styles.
 */
public enum LineStyle
{
  /**
   * Durchgezogene Linie.
   */
  SOLID(org.eclipse.swtchart.LineStyle.SOLID),
  
  /**
   * Gepunktete Linie.
   */
  DOT(org.eclipse.swtchart.LineStyle.DOT),
  
  ;
  
  private org.eclipse.swtchart.LineStyle swtStyle = null;
  
  /**
   * ct.
   * @param swtStyle der in SWT zu verwendende Style.
   */
  private LineStyle(org.eclipse.swtchart.LineStyle swtStyle)
  {
    this.swtStyle = swtStyle;
  }
  
  /**
   * Liefert den in SWT zu verwendenden Style.
   * @return swtStyle der in SWT zu verwendende Style.
   */
  public org.eclipse.swtchart.LineStyle getSwtStyle()
  {
    return swtStyle;
  }
}


