/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import de.willuhn.jameica.services.VelocityService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;




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
    
    /**
     * IBAN des Gegenkontos.
     */
    IBAN,
    
    /**
     * BIC des Gegenkontos.
     */
    BIC,
    
    /**
     * TAN1.
     */
    TAN1,
    
    /**
     * TAN.
     */
    TAN,
    
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
      
    // Sonderrolle SVWZ.
    // Es kann sein, dass der Verwendungszweck so aussieht:
    // "EREF+1234 MREF+1234 SVWZ+"
    // Sprich: Das Tag ist zwar da, aber leer. Macht die "S-Bahn Berlin GmbH".
    // In dem Fall liefern wir ebenfalls den kompletten Text
    String value = result.get(tag);
    if (tag == Tag.SVWZ && StringUtils.trimToNull(value) == null)
      return toString(t);
    
    return value;
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
    // Wir parsen erstmal alles mit "+".
    Map<Tag,String> result = parse('+',lines);
    if (result == null || result.size() == 0)
    {
      // Vielleicht enthaelt es ja nur Tags mit Doppelpunkt?
      return parse(':',lines);
    }
    
    // Jetzt schauen wir, ob wir den Verwendungszweck per ":" noch weiter zerlegen koennen
    String svwz = result.get(Tag.SVWZ);
    if (StringUtils.trimToNull(svwz) != null)
      result.putAll(parse(':',svwz));
    
    return result;
  }
  
  /**
   * Parst die SEPA-Tags aus den Verwendungszweck-Zeilen.
   * @param sep das zu verwendende Trennzeichen.
   * @param lines die Verwendungszweck-Zeilen.
   * @return Map mit den geparsten Infos. Niemals NULL sondern hoechstens eine leere Map.
   * @throws RemoteException
   */
  private static Map<Tag,String> parse(char sep, String... lines) throws RemoteException
  {
    Map<Tag,String> result = new HashMap<Tag,String>();

    if (lines == null || lines.length == 0)
      return result;

    String line = merge(lines);
    int first = -1;

    try
    {

      // Jetzt iterieren wir ueber die bekannten Tags. Wenn wir eines im Text finden, extrahieren
      // wir alles bis zum naechsten Tag.
      for (Tag tag:Tag.values())
      {
        int start = line.indexOf(tag.name()+sep); // Trenner dahinter, um sicherzustellen, dass sowas wie "EREF" nicht mitten im Text steht
        if (start == -1)
          continue; // Nicht gefunden

        // Position des ersten Tag merken - brauchen wir weiter unten eventuell noch
        if (first == -1 || start < first)
          first = start;
        
        int next = 0;
        
        while (next < line.length()) // Wir suchen solange, bis wir am Ende angekommen sind.
        {
          int tagLen = tag.name().length() + 1; // Laenge des Tag + Trennzeichen
          
          // OK, wir haben das Tag. Jetzt suchen wir bis zum naechsten Tag.
          next = line.indexOf(sep,start + tagLen + next);
          if (next == -1)
          {
            // Kein weiteres Tag mehr da. Gehoert alles zum Tag.
            result.put(tag,StringUtils.trimToEmpty(line.substring(start + tagLen).replace("\n","")));
            break;
          }
          else
          {
            // Checken, ob vor dem "+" ein bekanntes Tag steht
            String s = line.substring(next-4,next);
            Tag found = Tag.byName(s);
            if (found == null)
            {
              // Sonderfall BIC - nur 3 Zeichen lang?
              found = Tag.byName(line.substring(next-3,next));
            }
            
            // Ist ein bekanntes Tag. Also uebernehmen wir den Text genau bis dahin
            if (found != null)
            {
              result.put(tag,StringUtils.trimToEmpty(line.substring(start + tagLen,next - found.name().length()).replace("\n","")));
              break;
            }
          }
        }
      }
      
      // Noch eine Sonderrolle bei SVWZ. Es gibt Buchungen, die so aussehen:
      // "Das ist eine Zeile ohne Tag\nKREF+Und hier kommt noch ein Tag".
      // Sprich: Der Verwendungszweck enthaelt zwar Tags, der Verwendungszweck selbst hat aber keines
      // sondern steht nur vorn dran.
      // Wenn wir Tags haben, SVWZ aber fehlt, nehmen wir als SVWZ den Text bis zum ersten Tag
      if (result.size() > 0 && !result.containsKey(Tag.SVWZ) && first > 0)
      {
        result.put(Tag.SVWZ,StringUtils.trimToEmpty(line.substring(0,first).replace("\n","")));
      }
      
      // Sonderrolle IBAN. Wir entfernen alles bis zum ersten Leerzeichen. Siehe "testParse012". Da hinter der
      // IBAN kein vernuenftiges Tag mehr kommt, wuerde sonst der ganze Rest da mit reinfallen. Aber nur, wenn
      // es erst nach 22 Zeichen kommt. Sonst steht es mitten in der IBAN drin. In dem Fall entfernen wir die
      // Leerzeichen aus der IBAN (siehe "testParse013")
      String iban = StringUtils.trimToNull(result.get(Tag.IBAN));
      if (iban != null)
      {
        int space = iban.indexOf(" ");
        if (space > 21) // Wir beginnen ja bei 0 mit dem Zaehlen
          result.put(Tag.IBAN,StringUtils.trimToEmpty(iban.substring(0,space)));
        else if (space != -1)
          result.put(Tag.IBAN,StringUtils.deleteWhitespace(iban));
      }
      
      // testParse013: Leerzeichen aus der BIC entfernen
      String bic = StringUtils.trimToNull(result.get(Tag.BIC));
      if (bic != null)
        result.put(Tag.BIC,StringUtils.deleteWhitespace(bic));
        
    }
    catch (Exception e)
    {
      Logger.error("unable to parse line: " + line,e);
      e.printStackTrace();
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
    
    List<String> l = clean(true,lines);
    if (l.size() > 0) t.setZweck(l.remove(0));  // Zeile 1
    if (l.size() > 0) t.setZweck2(l.remove(0)); // Zeile 2
    if (l.size() > 0) t.setWeitereVerwendungszwecke(l.toArray(new String[0])); // Zeile 3 - x
  }

  /**
   * Verteilt die angegebenen Verwendungszweck-Zeilen auf zweck, zweck2 und zweck3.
   * @param t der Auftrag, in dem die Verwendungszweck-Zeilen gespeichert werden sollen.
   * @param lines die zu uebernehmenden Zeilen.
   * @throws RemoteException
   */
  public static void applyCamt(HibiscusTransfer t, List<String> lines) throws RemoteException
  {
    if (t == null || lines == null || lines.size() == 0)
      return;
    
    List<String> l = clean(true,lines);
    
    // Wir werfen alles in einen String und verteilen es dann ohne weitere Trennungen auf die Zeilen
    String all = StringUtils.join(l,"");

    // 1. Passt alles in Zeile 1?
    if (all.length() <= 255)
    {
      t.setZweck(all);
      return;
    }

    // Ne, dann Zweck 1 abschneiden
    t.setZweck(all.substring(0,255));
    all = all.substring(255);

    // 2. Passt der Rest in Zeile 2?
    int limit = HBCIProperties.HBCI_TRANSFER_USAGE_DB_MAXLENGTH;
    if (all.length() <= limit)
    {
      t.setZweck2(all);
      return;
    }
    
    // Ne, dann Zweck 2 abschneiden
    t.setZweck2(all.substring(0,limit));
    all = all.substring(limit);
    
    // Den Rest verteilen
    t.setWeitereVerwendungszwecke(parse(all));
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

    List<String> l = clean(true,lines);
    
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
    
    List<String> cleaned = clean(false,lines);
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

    String[] list = lines.toArray(new String[0]);
    List<String> result = clean(false,list);
    return result.toArray(new String[0]);
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
   * @param trim wenn die Zeilen-Enden getrimmt werden sollen.
   * @param lines die zu bereinigenden Zeilen.
   * @return die bereinigten Zeilen.
   */
  private static List<String> clean(boolean trim, String... lines)
  {
    return clean(trim,lines != null ? Arrays.asList(lines) : null);
  }

  /**
   * Bereinigt die Verwendungszweck-Zeilen.
   * Hierbei werden leere Zeilen oder NULL-Elemente entfernt.
   * Ausserdem werden alle Zeilen getrimt.
   * @param trim wenn die Zeilen-Enden getrimmt werden sollen.
   * @param lines die zu bereinigenden Zeilen.
   * @return die bereinigten Zeilen.
   */
  private static List<String> clean(boolean trim, List<String> lines)
  {
    List<String> result = new ArrayList<String>();
    if (lines == null || lines.size() == 0)
      return result;
    
    for (String line:lines)
    {
      if (line == null)
        continue;
      
      if (trim)
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
    return HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM;
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
  
  /**
   * Ersetzt in dem Text die angegebenen Platzhalter gegen die entsprechenden Werte.
   * @param text der Text mit den Platzhaltern.
   * @return der Text mit den Ersetzungen.
   */
  public static String evaluate(String text)
  {
    try
    {
      // Map mit den Ersetzungen erstellen
      final Map<String,Object> ctx = new HashMap<>();
      final Calendar cal = Calendar.getInstance();
      final String year = Integer.toString(cal.get(Calendar.YEAR));
      final String month = String.format("%02d",cal.get(Calendar.MONTH) + 1);
      final String day = String.format("%02d",cal.get(Calendar.DATE));

      // Wir erlauben verschiedene Schreibweisen
      ctx.put("jahr",year);
      ctx.put("Jahr",year);
      ctx.put("year",year);
      ctx.put("Year",year);
      ctx.put("yyyy",year);
      ctx.put("jjjj",year);
      ctx.put("YYYY",year);
      ctx.put("JJJJ",year);
      ctx.put("monat",month);
      ctx.put("Monat",month);
      ctx.put("month",month);
      ctx.put("Month",month);
      ctx.put("mm",month);
      ctx.put("MM",month);
      ctx.put("tag",day);
      ctx.put("Tag",day);
      ctx.put("day",day);
      ctx.put("Day",day);
      ctx.put("dd",day);
      ctx.put("DD",day);
      ctx.put("tt",day);
      ctx.put("TT",day);
      
      final VelocityService velocity = Application.getBootLoader().getBootable(VelocityService.class);
      return velocity.merge(text,ctx);
    }
    catch (Exception e)
    {
      Logger.error("unable to replace placeholders",e);
      return text;
    }
  }
}
