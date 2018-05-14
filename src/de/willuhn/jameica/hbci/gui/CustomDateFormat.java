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
      {
        // Wir muessen noch checken, ob es d.m.yyyy oder ddmmyyyy ist
        if (source.indexOf(".") != -1) // enthaelt Punkte
          return super.parse(source);
        return HBCI.FASTDATEFORMAT.parse(source);
      }
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
 * Revision 1.2  2012/03/31 15:09:33  willuhn
 * @B Zwischen d.m.yyyy und ddmmyyyy unterscheiden
 *
 * Revision 1.1  2010/05/15 19:05:56  willuhn
 * @N BUGZILLA 865
 *
 **********************************************************************/