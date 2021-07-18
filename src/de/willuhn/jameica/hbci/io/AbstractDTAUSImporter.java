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

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.jost_net.OBanToo.Dtaus.DtausDateiParser;
import de.jost_net.OBanToo.Dtaus.ESatz;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basis-Klasse fuer DTAUS-Import/Export.
 */
public abstract class AbstractDTAUSImporter extends AbstractDTAUSIO implements Importer
{
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private Hashtable kontenCache = new Hashtable();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor, de.willuhn.jameica.system.BackgroundTask)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {
    // Wir merken uns die Konten, die der User schonmal ausgewaehlt
    // hat, um ihn nicht fuer jede Buchung mit immer wieder dem
    // gleichen Konto zu nerven
    this.kontenCache.clear();

    try
    {
      if (format == null || !(format instanceof MyIOFormat))
        throw new ApplicationException(i18n.tr("Unbekanntes Import-Format"));

      int toleranz = settings.getInt("dtaus.fehlertoleranz",DtausDateiParser.UMLAUTUMSETZUNG);

      /* Aus http://de.wikipedia.org/wiki/Datenträgeraustauschverfahren:
       * "Bei der Kodierung der Zeichen schreibt die "Spezifikation der Datenformate",
       * Version 2.2 vom 29. Oktober 2007 (Final Version) des Zentralen Kreditausschusses
       * (ZKA) die DIN-66003-Kodierung vor, bei der die deutschen Umlaute und das ß im 
       * Bereich der ASCII-Kodierung definiert sind.[1] DIN 66003 ist die deutsche
       * Bezeichnung für den deutschen Teil der internationalen Norm ISO 646. Die Bundesbank
       * erwähnt in ihrer Spezifikation abweichend hierzu eine Kodierung der Zeichen mittels
       * der MS-DOS Codepage 437. Beide Kodierungen entsprechen nicht der weitläufig
       * verwendeten ISO-8859-Kodierung, die in keiner der beiden Spezifikationen als
       * gültige Kodierung einer DTAUS-Datei spezifiziert ist."
       *
       * Insbesondere aufgrund des letzten Satzes ist es meiner Meinung nach
       * zu viel des Guten, hier noch einen extra Auswahl-Dialog fuer den
       * Zeichensatz anzubieten. Zumal es den User unnoetig verunsichern wuerde.
       * Welcher User weiss schon, mit welchem Zeichensatz seine DTAUS-Datei
       * erzeugt wurde und was ueberhaupt ein Zeichensatz ist?
       * 
       * Wir nehmen daher per Default Latin1. Wenn das fehlschlaegt, kann der User 
       * den Parameter "dtaus.encoding" manuell setzen.
       */
      String encoding = settings.getString("dtaus.encoding","ISO-8859-1");

      Logger.info("dtaus tolerance: " + toleranz);
      Logger.info("dtaus encoding : " + encoding);
      DtausDateiParser parser = new DtausDateiParser(is,toleranz,encoding);

      int files = parser.getAnzahlLogischerDateien();

      for (int i=0;i<files;++i)
      {
        monitor.setPercentComplete(0);

        monitor.setStatusText(i18n.tr("Importiere logische Datei Nr. {0}",""+(i+1)));

        parser.setLogischeDatei(i+1);

        // Im E-Satz steht die Anzahl der Datensaetze. Die brauchen wir, um
        // den Fortschrittsbalken mit sinnvollen Daten fuettern zu koennen.
        ASatz a = parser.getASatz();
        ESatz e = parser.getESatz();

        double factor = 100d / e.getAnzahlDatensaetze();
        int count = 0;
        int success = 0;
        int error = 0;

        DBService service = de.willuhn.jameica.hbci.Settings.getDBService();

        CSatz c = null;
        while ((c = parser.next()) != null)
        {
          try
          {
            // Mit diesem Factor sollte sich der Fortschrittsbalken
            // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
            monitor.setPercentComplete((int)((++count) * factor));
            monitor.log(i18n.tr("Importiere Datensatz {0}",c.getNameEmpfaenger()));

            if (t != null && t.isInterrupted())
              throw new OperationCanceledException();

            // Gewuenschtes Objekt erstellen
            final DBObject skel = service.createObject(((MyIOFormat)format).type,null);

            // Mit Daten befuellen lassen
            create(skel,context,c,a);

            success++;

            // Jetzt noch ggf. andere ueber das neue Objekt informieren
            try
            {
              Application.getMessagingFactory().sendMessage(new ImportMessage(skel));
            }
            catch (Exception ex)
            {
              Logger.error("error while sending import message",ex);
            }
          }
          catch (ApplicationException ace)
          {
            error++;

            StringBuffer sb = new StringBuffer();
            sb.append(i18n.tr("BLZ: {0}",Long.toString(c.getBlzEndbeguenstigt())));
            sb.append("\n");
            sb.append(i18n.tr("Kontonummer: {0}",Long.toString(c.getKontonummer())));
            sb.append("\n");
            sb.append(i18n.tr("Name: {0}",c.getNameEmpfaenger()));
            sb.append("\n");
            sb.append(i18n.tr("Betrag: {0}",HBCI.DECIMALFORMAT.format(c.getBetragInEuro())));

            String s = i18n.tr("Fehler beim Import eines Datensatzes\n\n{0}\n\n{1}\n\nDatensatz überspringen und Import fortsetzen?",new String[]{sb.toString(),ace.getMessage()});
            if (!Application.getCallback().askUser(s))
            {
              monitor.setStatusText(i18n.tr("Import abgebrochen"));
              monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
              return;
            }
            monitor.log("  " + ace.getMessage());
            monitor.log("  " + i18n.tr("Überspringe Datensatz"));
          }
          catch (OperationCanceledException oce)
          {
            throw oce;
          }
          catch (Exception e1)
          {
            error++;
            Logger.error("unable to import transfer",e1);
            monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes, überspringe Datensatz"));
          }
        }
        if (error > 0)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText("  " + i18n.tr("{0} Datensätze importiert, {1} wegen Fehlern übersprungen",new String[]{""+success,""+error}));
        }
        else
        {
          monitor.setStatusText("  " + i18n.tr("{0} Datensätze erfolgreich importiert",""+success));
        }
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Import abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (Exception e)
    {
      // Fehlermeldung von obantoo durchreichen - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=64455#64455
      String msg = e.getMessage();
      if (msg == null || msg.length() == 0)
        throw new RemoteException(i18n.tr("Fehler beim Import der DTAUS-Daten"),e);
      throw new RemoteException(msg,e);
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (IOException ioe)
        {
          Logger.error("unable to close inputstream",ioe);
        }
      }
    }
  }

  /**
   * Sucht nach dem Konto mit der angegebenen Kontonummer und BLZ.
   * @param kontonummer
   * @param blz
   * @return das gefundene Konto oder wenn es nicht gefunden wurde, dann das vom Benutzer ausgewaehlte.
   * Die Funktion liefert nie <code>null</code> sondern wirft eine ApplicationException, wenn kein Konto ausgewaehlt wurde.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  protected Konto findKonto(String kontonummer, String blz) throws RemoteException, ApplicationException
  {
    // Erstmal schauen, ob der User das Konto schonmal ausgewaehlt hat:
    Konto k = (Konto) kontenCache.get(kontonummer + blz);

    // Haben wir im Cache
    if (k != null)
      return k;

    // In der Datenbank suchen
    k = KontoUtil.find(kontonummer,blz);

    // Nichts gefunden. Dann fragen wir den User
    if (k == null)
    {
      // Das Konto existiert nicht im Hibiscus-Datenbestand. Also soll der
      // User eines auswaehlen
      KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
      d.setText(i18n.tr("Konto {0} [BLZ {1}] nicht gefunden\n" +
                        "Bitte wählen Sie das zu verwendende Konto aus.",
                        new String[]{kontonummer == null || kontonummer.length() == 0 ? i18n.tr("<unbekannt>") : kontonummer,blz}));

      try
      {
        k = (Konto) d.open();
      }
      catch (OperationCanceledException oce)
      {
        throw new ApplicationException(i18n.tr("Auftrag wird übersprungen"));
      }
      catch (Exception e)
      {
        throw new ApplicationException(i18n.tr("Fehler beim Auswählen des Kontos"),e);
      }
    }

    if (k != null)
    {
      kontenCache.put(kontonummer + blz,k);
      return k;
    }
    throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));
  }

  /**
   * Muss von den abgeleiteten Klassen implementiert werden, damit sie dort das Hibiscus-Fachobjekt befuellen
   * und speichern.
   * @param skel das schon vorbereitete Hibiscus-Fachobjekt.
   * @param context der Kontext. Kann zB ein Konto sein.
   * @param csatz der C-Satz mit den auszulesenden Daten.
   * @param asatz der A-Satz.
   * @throws RemoteException
   * @throws ApplicationException
   */
  abstract void create(DBObject skel, Object context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException;

}

/*********************************************************************
 * $Log: AbstractDTAUSImporter.java,v $
 * Revision 1.18  2011/04/26 12:15:51  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.17  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 * Revision 1.16  2010/01/20 10:35:12  willuhn
 * @C Obantoo-Fehlermeldung durchreichen
 *
 * Revision 1.15  2009/10/28 10:21:01  willuhn
 * @N BUGZILLA 773
 *
 * Revision 1.14  2009/06/15 08:51:16  willuhn
 * @N BUGZILLA 736
 *
 * Revision 1.13  2009/03/01 23:17:04  willuhn
 * @B BUGZILLA 707
 *
 * Revision 1.12  2008/08/29 21:58:39  willuhn
 * @N Encoding via Config-Datei einstellbar - per Default wird "Latin1" verwendet.
 *
 * Revision 1.11  2007/12/21 14:13:15  willuhn
 * @C Default-Format auf Umlautumsetzung (CP850) geaendert
 *
 * Revision 1.10  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.9  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.8  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.7  2007/03/05 15:38:43  willuhn
 * @B Bug 365
 *
 * Revision 1.6  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.5  2006/10/06 14:18:01  willuhn
 * @N neuer Parameter "dtaus.fehlertoleranz" in de.willuhn.jameica.hbci.HBCI.properties
 *
 * Revision 1.4  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.3  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 * Revision 1.2  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.1  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 **********************************************************************/