/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.logging.Logger;

/**
 * Genersicher Parser fuer die Zwischenablage.
 * Durchsucht diese nach sinnvollen Daten, um daraus ggf. eine
 * neue SEPA-Ueberweisung mit vorausgefuellten Werten erzeugen zu koennen.
 */
public class ClipboardSepaUeberweisungImporter
{
  private final static Pattern PT_SPLIT = Pattern.compile(":[\n\r]+", Pattern.MULTILINE);
  private final static Pattern PT_KONTO = Pattern.compile("(.*iban.*)|(.*Konto.*)|(.*Kto.*)", Pattern.CASE_INSENSITIVE);
  private final static Pattern PT_BLZ   = Pattern.compile("(.*bic.*)", Pattern.CASE_INSENSITIVE);
  private final static Pattern PT_ZWECK = Pattern.compile("(.*zweck.*)", Pattern.CASE_INSENSITIVE);
  private final static Pattern PT_NAME  = Pattern.compile("(.*Inhaber.*)|(.*Name.*)|(.*Empfänger.*)|(.*Empfaenger.*)", Pattern.CASE_INSENSITIVE);

	/**
   * Versucht eine SEPA-Ueberweisung aus der Zwischenablage zu erstellen.
   * @return die Ueberweisung, wenn eine erstellt werden konnte oder null.
	 */
	public AuslandsUeberweisung getUeberweisung()
  {
    try
    {
      // BUGZILLA 336
      final Clipboard cb = new Clipboard(GUI.getDisplay());
      TextTransfer transfer = TextTransfer.getInstance();
      String text = (String) cb.getContents(transfer);

      if (text == null || text.length() == 0)
        return null;

      text = text.trim();
      
      // Fuer den Fall, dass wir Key+Value nicht nur durch Doppelpunkt sondern zusaetzlich
      // auch noch durch einen Zeilenumbruch getrennt sind, entfernen wir Zeilen-Umbrueche,
      // wenn sie auf einen Doppelpunkt folgen
      // Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=82519#82519
      text = PT_SPLIT.matcher(text).replaceAll(":");

      StringTokenizer st = new StringTokenizer(text,System.getProperty("line.separator","\n"));
      HashMap<String, String> values = new HashMap<>();

      while (st.hasMoreTokens())
      {
        String line = st.nextToken();
				if (line == null || line.length() <= 0)
          continue;

        line = line.replaceAll("\\s"," ");

        int sep = line.indexOf(":");
        if (sep == -1)
          continue;

        values.put(line.substring(0,sep).trim(),line.substring(sep+1).trim());
			}

      AuslandsUeberweisung u = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);

      for (String key : values.keySet())
      {
        String value = values.get(key);
        if (value == null || key == null)
          continue;
        if (PT_BLZ.matcher(key).matches())
          u.setGegenkontoBLZ(value.replaceAll(" ",""));
        else if (PT_KONTO.matcher(key).matches())
          u.setGegenkontoNummer(value.replaceAll(" ",""));
        else if (PT_NAME.matcher(key).matches())
          u.setGegenkontoName(value);
        else if (PT_ZWECK.matcher(key).matches())
          u.setZweck(value);
      }
      return u;
		}
    catch (Throwable t)
    {
      Logger.debug("unable to parse clipboard data: " + t.getMessage());
		}
    return null;
  }

}
