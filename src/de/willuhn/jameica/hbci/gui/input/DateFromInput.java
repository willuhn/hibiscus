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
    this(date,"transferlist.filter.from");
  }

  /**
   * ct.
   * @param date
   * @param parameter Schluessel-Name, unter dem die Eingabe in der Session gecached werden soll.
   */
  public DateFromInput(Date date, String parameter)
  {
    super(date,parameter);
    this.setName(i18n.tr("Start-Datum"));
    this.setComment(i18n.tr("Frühestes Datum"));
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
 * Revision 1.4  2011/12/18 23:20:20  willuhn
 * @N GUI-Politur
 *
 * Revision 1.3  2011-08-05 12:02:11  willuhn
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