/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.ChipTANDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Startet den ChipTAN-Dialog mit einem Testcode, um die Uebertragung an den
 * TAN-Generator zu testen.
 */
public class ChipTanTest implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    // Beispiel-Code aus der Spec
    // Belegungsrichtlinien TANve1.4  mit Erratum 1-3 final version vom 2010-11-12.pdf
    try
    {
      // 0D85012045201998041234567855
      ChipTANDialog d = new ChipTANDialog(null,"1784011041875F051234567890041203000044302C323015");
      d.setTitle(i18n.tr("chipTAN-Test"));
      d.setText(i18n.tr("Verwenden Sie diese Flicker-Grafik, um die Funktionsfähigkeit Ihres TAN-Generators zu testen.\n\n" +
                        "1. Schieben Sie Ihre Bank-Karte in den TAN-Generator\n" +
                        "2. Halten Sie das Gerät bündig an den Bildschirm vor die blinkenden Balken. Die weißen\n" +
                        "    Positionsdreiecke sollten zwischen Bildschirm und TAN-Generator übereinstimmen\n" +
                        "3. Passen Sie mit den Tasten \"-\" und \"+\" ggf. die Breite der Flicker-Grafik an\n" +
                        "4. Drücken Sie die Taste \"F\", um den Scan-Vorgang zu starten\n" +
                        "5. Wenn der Flicker-Code erfolgreich übertragen wurde, sollte auf dem Gerät der Text\n" +
                        "    \"Überweisung Inland\" angezeigt werden.\n" +
                        "6. Nach mehrmaligem Drücken der Taste \"OK\" sollten\n" +
                        "    \"Konto Empf: 1234567890\"\n" +
                        "    \"BLZ Empf: 12030000\"\n" +
                        "    \"Betrag: 0,20\"\n" +
                        "    angezeigt werden\n" +
                        "7. Nach einer weiterer Bestätigung mit \"OK\" sollte eine TAN generiert werden.\n"));
      String tan = StringUtils.trimToNull((String) d.open());
      if (tan != null)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Eingegebene TAN: {0}",tan),StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine TAN eingegeben"),StatusBarMessage.TYPE_INFO));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("chipTAN-Test abgebrochen");
    }
    catch (Exception e)
    {
      Logger.error("error while testing chipTAN",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("chipTAN-Test fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}
