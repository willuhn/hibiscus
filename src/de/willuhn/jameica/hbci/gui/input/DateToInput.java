/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Calendar;
import java.util.Date;

import de.willuhn.jameica.util.DateUtil;

/**
 * Vorkonfigueriertes Eingabefeld fuer ein End-Datum.
 */
public class DateToInput extends AbstractDateInput
{
  /**
   * ct.
   */
  public DateToInput()
  {
    this(null);
  }

  /**
   * ct.
   * @param date
   */
  public DateToInput(Date date)
  {
    this(date,"transferlist.filter.to");
  }

  /**
   * ct.
   * @param date
   * @param parameter Schluessel-Name, unter dem die Eingabe in der Session gecached werden soll.
   */
  public DateToInput(Date date, String parameter)
  {
    super(date,parameter);
    this.setName(i18n.tr("End-Datum"));
    this.setComment(i18n.tr("Spätestes Datum"));
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.input.AbstractDateInput#getDefault()
   */
  Date getDefault()
  {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH,Calendar.DECEMBER);
    cal.set(Calendar.DATE,31);

    return DateUtil.endOfDay(cal.getTime());
  }
}
