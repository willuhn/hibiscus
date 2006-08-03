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
 * 
 * @author Frank
 * FIXME: Nochmal ueberarbeiten und konfigurierbar machen
 */
public class EbayKontoParser {

	/**
   * Liest die Daten aus der Zwischenablage und liefert sie geparset zurueck.
	 * @return die geparsten Daten.
	 * @throws ApplicationException
	 */
	public EbayKontoData readFromClipboard() throws ApplicationException
  {

		try
    {
      Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor))
      {
				String text = (String) t.getTransferData(DataFlavor.stringFlavor);
				if (text != null)
        {
					text = text.trim();

          String newLine     = System.getProperty("line.separator","\n");
					StringTokenizer st = new StringTokenizer(text,newLine);
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

          I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
          int maxlength= HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH;
					if (inhaber.length() > maxlength)
						throw new ApplicationException(i18n.tr("Der Name des Konto-Inhabers \"{0}\" ist zu lang (max. {1} Zeichen)",new String[]{inhaber,""+maxlength}));
					
					return new EbayKontoData(nummer, inhaber, blz);
				}
			}
		}
		catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to parse clipboard data",e);
		}
		
    return null;

  }

}
