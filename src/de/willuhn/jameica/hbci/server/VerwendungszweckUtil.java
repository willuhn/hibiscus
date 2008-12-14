/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/VerwendungszweckUtil.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/12/14 23:18:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;



/**
 * Hilfsklasse zum Mergen und Splitten der erweiterten Verwendungszwecke.
 */
public class VerwendungszweckUtil
{
  /**
   * Splittet die Verwendungszweck-Zeilen.
   * @param lines die Zeilen.
   * @return Ein Array mit den Zeilen.
   * Niemals NULL sondern hoechstens ein leeres Array.
   */
  public static String[] split(String lines)
  {
    if (lines == null || lines.length() == 0)
      return new String[0];
    return lines.split("\n");
  }
  
  /**
   * Merget die Verwendungszweck-Zeilen.
   * @param lines die Zeilen.
   * @return die gemergten Zeilen. Wird NULL oder ein leeres
   * Array uebergeben, liefert die Funktion NULL.
   */
  public static String merge(String[] lines)
  {
    if (lines == null || lines.length == 0)
      return null;
    
    // Alle leeren Zeilen ueberspringen
    StringBuffer sb = new StringBuffer();
    for (int i=0;i<lines.length;++i)
    {
      if (lines[i] == null || lines[i].length() == 0)
        continue;
      
      // Wir ignorieren die Zeile auch dann, wenn nur Leerzeichen drin stehen
      String s = lines[i].trim();
      if (s.length() == 0)
        continue;

      sb.append(s);
      sb.append("\n");
    }

    String result = sb.toString();
    return result.length() == 0 ? null : result;
  }
}


/*********************************************************************
 * $Log: VerwendungszweckUtil.java,v $
 * Revision 1.5  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.4  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.3  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.2  2008/02/22 00:52:35  willuhn
 * @N Erste Dialoge fuer erweiterte Verwendungszwecke (noch auskommentiert)
 *
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/