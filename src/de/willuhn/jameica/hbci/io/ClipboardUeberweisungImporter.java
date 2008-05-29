/**
 * 
 */
package de.willuhn.jameica.hbci.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.logging.Logger;

/**
 * Genersicher Parser fuer die Zwischenablage.
 * Durchsucht diese nach sinnvollen Daten, um daraus ggf. eine
 * neue Ueberweisung mit vorausgefuellten Werten erzeugen zu koennen.
 */
public class ClipboardUeberweisungImporter
{
  private final static Pattern PT_KONTO = Pattern.compile("(.*nummer.*)|(.*Konto.*)|(.*Kto.*)", Pattern.CASE_INSENSITIVE);
  private final static Pattern PT_BLZ   = Pattern.compile("(.*Bankleitzahl.*)|(.*BLZ.*)", Pattern.CASE_INSENSITIVE);
  private final static Pattern PT_ZWECK = Pattern.compile("(.*zweck.*)", Pattern.CASE_INSENSITIVE);
  private final static Pattern PT_NAME  = Pattern.compile("(.*Inhaber.*)|(.*Name.*)|(.*Empfänger.*)|(.*Empfaenger.*)", Pattern.CASE_INSENSITIVE);

	/**
   * Versucht eine Ueberweisung aus der Zwischenablage zu erstellen.
   * @return die Ueberweisung, wenn eine erstellt werden konnte oder null.
	 */
	public Ueberweisung getUeberweisung()
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

      StringTokenizer st = new StringTokenizer(text,System.getProperty("line.separator","\n"));
      HashMap values = new HashMap();

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

      Ueberweisung u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);

      Iterator i = values.keySet().iterator();
      while (i.hasNext())
      {
        String s = (String) i.next();
        String value = (String) values.get(s);
        if (value == null || s == null)
          continue;
        if (PT_BLZ.matcher(s).matches())
          u.setGegenkontoBLZ(value.replaceAll(" ",""));
        else if (PT_NAME.matcher(s).matches())
          u.setGegenkontoName(value);
        else if (PT_KONTO.matcher(s).matches())
          u.setGegenkontoNummer(value.replaceAll(" ",""));
        else if (PT_ZWECK.matcher(s).matches())
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
