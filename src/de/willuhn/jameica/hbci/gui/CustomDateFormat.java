/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/CustomDateFormat.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/05/15 19:05:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;

/**
 * Wir haben das Java-Dateformat ueberschrieben, damit wir mehrere
 * Datumsformate en bloc testen koennen.
 */
public class CustomDateFormat extends SimpleDateFormat
{
  /**
   * ct.
   * @param format das Date-Format.
   */
  public CustomDateFormat(String format)
  {
    super(format);
  }

  /**
   * @see java.text.DateFormat#parse(java.lang.String)
   */
  public Date parse(String source) throws ParseException
  {
    if (source == null || source.length() == 0)
      return null;
    
    switch (source.length())
    {
      case 8:
        return HBCI.FASTDATEFORMAT.parse(source);
      case 4:
        Calendar cal = Calendar.getInstance();
        source += cal.get(Calendar.YEAR);
        return HBCI.FASTDATEFORMAT.parse(source);
    }

    return super.parse(source);
  }
}


/*********************************************************************
 * $Log: CustomDateFormat.java,v $
 * Revision 1.1  2010/05/15 19:05:56  willuhn
 * @N BUGZILLA 865
 *
 **********************************************************************/