/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/VerwendungszweckUtil.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/11/19 17:02:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;




/**
 * Hilfsklasse zum Mergen und Splitten der erweiterten Verwendungszwecke.
 */
public class VerwendungszweckUtil
{
  /**
   * Splittet die Verwendungszweck-Zeilen am Zeilenumbruch.
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
   * Zerlegt einen langen Verwendungszweck in 27 Zeichen lange Haeppchen.
   * @param line die zu parsende Zeile.
   * @return die 27 Zeichen langen Schnippsel.
   */
  public static String[] parse(String line)
  {
    if (line == null || line.length() == 0)
      return new String[0];

    // Java's Regex-Implementierung ist sowas von daemlich.
    // String.split() macht nur Rotz, wenn man mit Quantifierern
    // arbeitet. Also ersetzten wir erst mal alles gegen nen
    // eigenen String und verwenden den dann zum Splitten.
    String s = line.replaceAll("(.{27})","$1--##--##");
    return s.split("--##--##");
  }
  
  /**
   * Verteilt die angegebenen Verwendungszweck-Zeilen auf zweck, zweck2 und zweck3.
   * @param t der Auftrag, in dem die Verwendungszweck-Zeilen gespeichert werden sollen.
   * @param lines die zu uebernehmenden Zeilen.
   * @throws RemoteException
   */
  public static void apply(HibiscusTransfer t, String[] lines) throws RemoteException
  {
    if (t == null || lines == null || lines.length == 0)
      return;
    
    List<String> l = new ArrayList(Arrays.asList(lines));
    
    if (l.size() > 0) t.setZweck(l.remove(0));  // Zeile 1
    if (l.size() > 0) t.setZweck2(l.remove(0)); // Zeile 2
    if (l.size() > 0) t.setWeitereVerwendungszwecke(l.toArray(new String[l.size()])); // Zeile 3 - x
  }
  
  /**
   * Merget die Verwendungszweck-Zeilen zu einem String zusammen.
   * Die Zeilen sind mit Zeilenumbruch versehen.
   * @param lines die Zeilen.
   * @return die gemergten Zeilen. Wird NULL oder ein leeres
   * Array uebergeben, liefert die Funktion NULL.
   */
  public static String merge(String... lines)
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
  
  /**
   * Merget die Verwendungszweck-Zeilen des Auftrages zu einer Zeile zusammen.
   * Statt Zeilenumbruch wird Leerzeichen verwendet.
   * @param t der Auftrag.
   * @return der String mit einer Zeile, die alle Verwendungszwecke enthaelt.
   * @throws RemoteException
   */
  public static String toString(HibiscusTransfer t) throws RemoteException
  {
    StringBuffer sb = new StringBuffer();
    String s1 = t.getZweck();
    String s2 = t.getZweck2();
    String s3 = merge(t.getWeitereVerwendungszwecke());

    if (s1 != null)
    {
      sb.append(s1);
      sb.append(' ');
    }
    if (s2 != null)
    {
      sb.append(s2);
      sb.append(' ');
    }
    if (s3 != null) sb.append(s3);
    return sb.toString().replace('\n',' ');
  }
}


/*********************************************************************
 * $Log: VerwendungszweckUtil.java,v $
 * Revision 1.7  2010/11/19 17:02:06  willuhn
 * @N VWZUtil#toString
 *
 * Revision 1.6  2010/06/01 11:02:18  willuhn
 * @N Wiederverwendbaren Code zum Zerlegen und Uebernehmen von Verwendungszwecken aus/in Arrays in Util-Klasse ausgelagert
 *
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