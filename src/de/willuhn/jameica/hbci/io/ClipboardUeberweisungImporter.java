/**
 * 
 */
package de.willuhn.jameica.hbci.io;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ebay-Konto-Parser von Frank Fasterling.
 * 
 * Beispiel:
 * 
 * Name der Bank: Postbank Berlin
 * Kontoinhaber: Peter Mustermann
 * Kontonummer: 580938106
 * Bankleitzahl: 10010010
 */
public class ClipboardUeberweisungImporter
{

	/**
   * Versucht eine Ueberweisung aus der Zwischenablage zu erstellen.
   * @return die Ueberweisung, wenn eine erstellt werden konnte oder null.
	 */
	public Ueberweisung getUeberweisung()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

    if (t == null || !t.isDataFlavorSupported(DataFlavor.stringFlavor))
      return null;
    
    try
    {
      String text = (String) t.getTransferData(DataFlavor.stringFlavor);
      if (text == null)
        return null;

      text = text.trim();

      StringTokenizer st = new StringTokenizer(text,System.getProperty("line.separator","\n"));
  		List lines         = new ArrayList();

      while (st.hasMoreTokens())
      {
        String line = st.nextToken();
				if (line == null || line.length() <= 0)
          continue;

        line = line.replaceAll("\\s"," ").trim();

        int doppelpunkt = line.indexOf(":");
        if (doppelpunkt  == -1)
          continue;

        lines.add(line.substring(doppelpunkt + 1).trim());
			}

      // Nur wenn wir mind. 4 Zeilen gefunden haben
      if (lines.size() < 4)
        return null;
          
      String inhaber = (String) lines.get(1);
      String nummer  = (String) lines.get(2);
      String blz     = (String) lines.get(3);

      // Test BLZ
      if (blz.length() != HBCIProperties.HBCI_BLZ_LENGTH)
      	throw new ApplicationException(i18n.tr("Die BLZ ist ungültig: \"{0}\"",blz));

      try
      {
				Long.parseLong(blz);
			}
      catch (NumberFormatException nfe)
      {
        throw new ApplicationException(i18n.tr("Die BLZ ist ungültig: \"{0}\"",blz));
			}
					
      // Test Kontonummer
			try
      {
      	Long.parseLong(nummer);
      }
      catch (NumberFormatException nfe)
      {
        throw new ApplicationException(i18n.tr("Die Kontonummer ist ungültig: \"{0}\"",nummer));
      }
					
			// Test Inhaber
      HBCIProperties.checkLength(inhaber,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
      
      Ueberweisung u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
      u.setGegenkontoBLZ(blz);
      u.setGegenkontoName(inhaber);
      u.setGegenkontoNummer(nummer);
      return u;
		}
    catch (Exception e)
    {
      Logger.debug("unable to parse clipboard data: " + e.getMessage());
		}
    return null;
  }

}
