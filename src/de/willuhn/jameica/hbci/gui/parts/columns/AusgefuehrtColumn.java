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

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.gui.parts.AbstractTablePart.AbstractTableItem;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorformatierte Spalte fuer den Ausfuehrungsstatus.
 */
public class AusgefuehrtColumn extends Column
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   */
  public AusgefuehrtColumn()
  {
    this("ausgefuehrt_am");
  }

  /**
   * ct.
   * @param name der Name das Attributs mit dem Ausführungsdatum.
   */
  public AusgefuehrtColumn(String name)
  {
    super(name,i18n.tr("Ausgeführt?"),null,false,Column.ALIGN_RIGHT);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Column#getFormattedValue(java.lang.Object, java.lang.Object)
   */
  public String getFormattedValue(Object value, Object context)
  {
    if (context != null && (context instanceof Terminable))
    {
      try
      {
        Terminable t = (Terminable) context;

        // Nicht ausgefuehrt
        if (!t.ausgefuehrt())
          return i18n.tr("offen");
        
        // Das sind die neuen mit Ausfuehrungs-Datum
        if (value != null && (value instanceof Date))
          return HBCI.LONGDATEFORMAT.format((Date)value);
        
        // Das sind die alten ohne Ausfuehrungs-Datum
        return i18n.tr("ausgeführt");
      }
      catch (RemoteException re)
      {
        Logger.error("unable to format attribute " + value + " for bean " + context);
      }
    }
    return super.getFormattedValue(value,context);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.Column#compare(de.willuhn.jameica.gui.parts.AbstractTablePart.AbstractTableItem, de.willuhn.jameica.gui.parts.AbstractTablePart.AbstractTableItem)
   */
  @Override
  public int compare(AbstractTableItem i1, AbstractTableItem i2)
  {
    // Auftraege ohne Ausfuehrungsdatum sollen so behandelt werden, als wuerde das Ausfuehrungsdatum
    // in der Zukunft liegen. Denn wenn man nach Ausfuehrungsdatum sortiert, landen die mit leerem
    // Datum ganz unten hinter den aeltesten Auftraegen.
    if (i1.sortValue == null)
      return 1;
    
    if (i2.sortValue == null)
      return -1;
    
    return super.compare(i1, i2);
  }
}
