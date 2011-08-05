/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/DateFromInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/08/05 12:02:11 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Calendar;
import java.util.Date;

import de.willuhn.jameica.util.DateUtil;

/**
 * Vorkonfigueriertes Eingabefeld fuer ein Start-Datum.
 */
public class DateFromInput extends AbstractDateInput
{
  /**
   * ct.
   */
  public DateFromInput()
  {
    this(null);
  }

  /**
   * ct.
   * @param date
   */
  public DateFromInput(Date date)
  {
    super(date);
    this.setName(i18n.tr("Start-Datum"));
    this.setComment(i18n.tr("Frühestes Datum"));
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.input.AbstractDateInput#getParameter()
   */
  String getParameter()
  {
    return "transferlist.filter.from";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.input.AbstractDateInput#getDefault()
   */
  Date getDefault()
  {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH,Calendar.JANUARY);
    cal.set(Calendar.DATE,1);

    return DateUtil.startOfDay(cal.getTime());
  }
}



/**********************************************************************
 * $Log: DateFromInput.java,v $
 * Revision 1.3  2011/08/05 12:02:11  willuhn
 * @B Konstruktor falsch
 *
 * Revision 1.2  2011-08-05 11:34:39  willuhn
 * @N Gemeinsame Basis-Klasse
 *
 * Revision 1.1  2011-08-05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 **********************************************************************/