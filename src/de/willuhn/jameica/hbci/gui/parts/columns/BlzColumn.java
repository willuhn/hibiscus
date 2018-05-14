/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts.columns;

import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCIProperties;

/**
 * Vorformatierte Blz-Spalte.
 */
public class BlzColumn extends Column
{
  /**
   * ct.
   * @param id Name der Spalte.
   * @param name Beschriftung.
   */
  public BlzColumn(String id, String name)
  {
    super(id,name,new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        String name = HBCIProperties.getNameForBank(blz);
        if (name != null && name.length() > 0)
          blz += " [" + name + "]";
        return blz;
      }
    });
  }
}


/**********************************************************************
 * $Log: BlzColumn.java,v $
 * Revision 1.2  2011/04/29 15:33:28  willuhn
 * @N Neue Spalte "ausgefuehrt_am", in der das tatsaechliche Ausfuehrungsdatum von Auftraegen vermerkt wird
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/
