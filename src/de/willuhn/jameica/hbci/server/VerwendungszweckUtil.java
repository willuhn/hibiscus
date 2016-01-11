/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.TypedProperties;




/**
 * Hilfsklasse zum Mergen und Splitten der erweiterten Verwendungszwecke.
 */
public class VerwendungszweckUtil
{
  /**
   * Liste der bekannten Tags.
   */
  public static enum Tag
  {
    /**
     * Ende-zu-Ende Referenz.
     */
    EREF,
    
    /**
     * Kundenreferenz.
     */
    KREF,
    
    /**
     * Mandatsreferenz.
     */
    MREF,
    
    /**
     * Creditor-ID.
     */
    CRED,
    
    /**
     * Debitor-ID.
     */
    DBET,
    
    /**
     * Verwendungszweck.
     */
    SVWZ,
    
    /**
     * Abweichender Auftraggeber.
     */
    ABWA,
    
    ;
    
    /**
     * Sucht das Tag mit dem angegebenen Namen.
     * @param s der Name des Tag.
     * @return das Tag oder NULL, wenn es nicht gefunden wurde.
     */
    public static Tag byName(String s)
    {
      if (s == null)
        return null;
      for (Tag t:Tag.values())
      {
        if (t.name().equals(s))
          return t;
      }
      return null;
    }
  }
  
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
   * Liefert den Wert des angegebenen Tag oder NULL, wenn er nicht gefunden wurde.
   * @param t der Auftrag.
   * @param tag das Tag.
   * @return der Wert des Tag oder NULL, wenn es nicht gefunden wurde.
   * @throws RemoteException
   */
  public static String getTag(Transfer t, Tag tag) throws RemoteException
  {
    if (tag == null)
      return null;
    Map<Tag,String> result = parse(t);

    // Sonderrolle SVWZ.
    // Bei den alten Buchungen gab es die Tags ja noch gar nicht.
    // Heisst: Wenn SVWZ angefordert wurde, der Auftrag aber gar keine
    // Tags enthaelt, wird der komplette originale Verwendungszweck zurueckgeliefert
    if (result.size() == 0)
    {
      if (tag == Tag.SVWZ)
        return toString(t);
      
      return null;
    }
      
    return result.get(tag);
  }

  /**
   * Parst die SEPA-Tags aus den Verwendungszwecken des Auftrages.
   * @param t
   * @return Map mit den geparsten Infos. Niemals NULL sondern hoechstens eine leere Map.
   * @throws RemoteException
   */
  public static Map<Tag,String> parse(Transfer t) throws RemoteException
  {
    if (t == null)
      return  new HashMap<Tag,String>();
    
    return parse(toArray(t));
  }

  /**
   * Parst die SEPA-Tags aus den Verwendungszweck-Zeilen.
   * @param lines die Verwendungszweck-Zeilen.
   * @return Map mit den geparsten Infos. Niemals NULL sondern hoechstens eine leere Map.
   * @throws RemoteException
   */
  public static Map<Tag,String> parse(String... lines) throws RemoteException
  {
    Map<Tag,String> result = new HashMap<Tag,String>();

    if (lines == null || lines.length == 0)
      return result;

    String line = merge(lines);
    
    try
    {

      // Jetzt iterieren wir ueber die bekannten Tags. Wenn wir eines im Text finden, extrahieren
      // wir alles bis zum naechsten Tag.
      for (Tag tag:Tag.values())
      {
        int start = line.indexOf(tag.name());
        if (start == -1)
          continue; // Nicht gefunden

        int next = 0;
        
        while (next < line.length()) // Wir suchen solange, bis wir am Ende angekommen sind.
        {
          // OK, wir haben das Tag. Jetzt suchen wir bis zum naechsten Tag.
          next = line.indexOf("+",start + 5 + next); // "5" = 4 Zeichen Kuerzel und "+" und Offset
          if (next == -1)
          {
            // Kein weiteres Tag mehr da. Gehoert alles zum Tag.
            result.put(tag,StringUtils.trimToEmpty(line.substring(start+5).replace("\n","")));
            break;
          }
          else
          {
            // Checken, ob vor dem "+" ein bekanntes Tag steht
            String s = line.substring(next-4,next);
            Tag found = Tag.byName(s);
            
            // Ist ein bekanntes Tag. Also uebernehmen wir den Text genau bis dahin
            if (found != null)
            {
              result.put(tag,StringUtils.trimToEmpty(line.substring(start+5,next-4).replace("\n","")));
              break;
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to parse line: " + line,e);
    }
    return result;
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
    
    List<String> l = clean(lines);
    if (l.size() > 0) t.setZweck(l.remove(0));  // Zeile 1
    if (l.size() > 0) t.setZweck2(l.remove(0)); // Zeile 2
    if (l.size() > 0) t.setWeitereVerwendungszwecke(l.toArray(new String[l.size()])); // Zeile 3 - x
  }
  
  /**
   * Bricht die Verwendungszweck-Zeilen auf $limit Zeichen lange Haeppchen neu um.
   * Jedoch nur, wenn wirklich Zeilen enthalten sind, die laenger sind.
   * Andernfalls wird nichts umgebrochen.
   * @param limit das Zeichen-Limit pro Zeile.
   * @param lines die Zeilen.
   * @return die neu umgebrochenen Zeilen.
   */
  public static String[] rewrap(int limit, String... lines)
  {
    if (lines == null || lines.length == 0)
      return lines;
    
    boolean found = false;
    for (String s:lines)
    {
      if (s != null && s.length() > limit)
      {
        found = true;
        break;
      }
    }
    if (!found)
      return lines;

    List<String> l = clean(lines);
    
    // Zu einem String mergen
    StringBuffer sb = new StringBuffer();
    for (String line:l)
    {
      sb.append(line);
    }
    String result = sb.toString();

    // und neu zerlegen
    String s = result.replaceAll("(.{" + limit + "})","$1--##--##");
    return s.split("--##--##");
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
    
    List<String> cleaned = clean(lines);
    StringBuffer sb = new StringBuffer();
    for (String line:cleaned)
    {
      sb.append(line);
      sb.append("\n");
    }

    String result = sb.toString();
    return result.length() == 0 ? null : result;
  }
  
  /**
   * Liefert eine bereinigte Liste der Verwendungszweck-Zeilen des Auftrages.
   * @param t der Auftrag.
   * @return bereinigte Liste der Verwendungszweck-Zeilen des Auftrages.
   * @throws RemoteException
   */
  public static String[] toArray(Transfer t) throws RemoteException
  {
    List<String> lines = new ArrayList<String>();
    lines.add(t.getZweck());
    lines.add(t.getZweck2());
    String[] wvz = t.getWeitereVerwendungszwecke();
    if (wvz != null && wvz.length > 0)
    {
      for (String s:wvz)
        lines.add(s);
    }
    
    String[] list = lines.toArray(new String[lines.size()]);
    List<String> result = clean(list);
    return result.toArray(new String[result.size()]);
  }

  /**
   * Merget die Verwendungszweck-Zeilen des Auftrages zu einer Zeile zusammen.
   * Als Trennzeichen fuer die Zeilen wird " " (ein Leerzeichen) verwendet.
   * @param t der Auftrag.
   * @return der String mit einer Zeile, die alle Verwendungszwecke enthaelt.
   * @throws RemoteException
   */
  public static String toString(Transfer t) throws RemoteException
  {
    return toString(t," ");
  }
  
  /**
   * Merget die Verwendungszweck-Zeilen des Auftrages zu einer Zeile zusammen.
   * @param t der Auftrag.
   * @param sep das zu verwendende Trennzeichen fuer die Zeilen. Wenn es null ist, wird " "
   * (ein Leerzeichen) verwendet.
   * @return der String mit einer Zeile, die alle Verwendungszwecke enthaelt.
   * @throws RemoteException
   */
  public static String toString(Transfer t, String sep) throws RemoteException
  {
    if (sep == null)
      sep = " ";
    StringBuffer sb = new StringBuffer();
    
    String[] lines = toArray(t);
    for (int i=0;i<lines.length;++i)
    {
      sb.append(lines[i]);
      
      // Trennzeichen bei der letzten Zeile weglassen
      if (i+1 < lines.length)
        sb.append(sep);
    }

    String result = sb.toString();
    
    // Wenn als Trennzeichen "\n" angegeben ist, kann es
    // bei den weiteren Verwendungszwecken drin bleiben
    if (sep.equals("\n"))
      return result;
    
    // Andernfalls ersetzen wir es gegen das angegebene Zeichen
    return result.replace("\n",sep);
  }
  
  /**
   * Bereinigt die Verwendungszweck-Zeilen.
   * Hierbei werden leere Zeilen oder NULL-Elemente entfernt.
   * Ausserdem werden alle Zeilen getrimt.
   * @param lines die zu bereinigenden Zeilen.
   * @return die bereinigten Zeilen.
   */
  private static List<String> clean(String... lines)
  {
    List<String> result = new ArrayList<String>();
    if (lines == null || lines.length == 0)
      return result;
    
    for (String line:lines)
    {
      if (line == null)
        continue;
      line = line.trim();
      if (line.length() > 0)
        result.add(line);
    }
    
    return result;
  }

  /**
   * Liefert die maximale Anzahl von Verwendungszwecken fuer Ueberweisungen.
   * @param konto das Konto
   * @return Maximale Anzahl der Zeilen.
   * @throws RemoteException
   */
  public final static int getMaxUsageUeb(Konto konto) throws RemoteException
  {
    TypedProperties bpd = DBPropertyUtil.getBPD(konto,DBPropertyUtil.BPD_QUERY_UEB);
    return bpd.getInt("maxusage",HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM);
  }

  /**
   * Prueft, ob die Anzahl der Verwendungszwecke nicht die Maximal-Anzahl aus den BPD uebersteigt.
   * @param transfer der zu testende Transfer.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void checkMaxUsage(HibiscusTransfer transfer) throws RemoteException, ApplicationException
  {
    if (transfer == null)
      return;
    
    VerwendungszweckUtil.checkMaxUsage(transfer.getKonto(),transfer.getWeitereVerwendungszwecke());
  }

  /**
   * Prueft, ob die Anzahl der Verwendungszwecke nicht die Maximal-Anzahl aus den BPD uebersteigt.
   * @param buchung die zu testende Buchung.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void checkMaxUsage(SammelTransferBuchung buchung) throws RemoteException, ApplicationException
  {
    if (buchung == null)
      return;
    
    SammelTransfer t = buchung.getSammelTransfer();
    VerwendungszweckUtil.checkMaxUsage(t == null ? null : t.getKonto(),buchung.getWeitereVerwendungszwecke());
  }

  /**
   * Prueft, ob die Anzahl der Verwendungszwecke nicht die Maximal-Anzahl aus den BPD uebersteigt.
   * @param transfer der zu testende Transfer.
   * @throws RemoteException
   * @throws ApplicationException
   */
  static void checkMaxUsage(Konto konto, String[] lines) throws RemoteException, ApplicationException
  {
    if (lines == null || lines.length == 0)
      return;
    
    // "2" sind die ersten beiden Zeilen, die bei getWeitereVerwendungszwecke nicht mitgeliefert werden
    int allowed = VerwendungszweckUtil.getMaxUsageUeb(konto);
    if ((lines.length + 2) > allowed)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Zuviele Verwendungszweck-Zeilen. Maximal erlaubt: {0}",String.valueOf(allowed)));
    }
  }
}
