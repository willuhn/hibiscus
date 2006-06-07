/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/DTAUSImporter.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/06/07 22:42:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import org.eclipse.swt.SWTException;

import de.jost_net.OBanToo.Dtaus.CSatz;
import de.jost_net.OBanToo.Dtaus.DtausDateiParser;
import de.jost_net.OBanToo.Dtaus.ESatz;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Import-Format fuer DTAUS-Dateien.
 */
public class DTAUSImporter extends AbstractDTAUSIO implements Importer
{
  /**
   * ct.
   */
  public DTAUSImporter()
  {
    super();
  }
  
  
  /**
   * Den Context ignorieren wir hier.
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      if (format == null || !(format instanceof MyIOFormat))
        throw new ApplicationException(i18n.tr("Unbekanntes Import-Format"));
      
      DtausDateiParser parser = new DtausDateiParser(is);
      
      // Wir merken uns die Konten, die der User schonmal ausgewaehlt
      // hat, um ihn nicht fuer jede Buchung mit immer wieder dem
      // gleichen Konto zu nerven
      Hashtable ht = new Hashtable();
      
      int files = parser.getAnzahlLogischerDateien();
      
      for (int i=0;i<files;++i)
      {
        monitor.setPercentComplete(0);
        
        monitor.setStatusText(i18n.tr("Importiere logische Datei Nr. {0}",""+(i+1)));
        
        parser.setLogischeDatei(i+1);
        
        // Im E-Satz steht die Anzahl der Datensaetze. Die brauchen wir, um
        // den Fortschrittsbalken mit sinnvollen Daten fuettern zu koennen.
        ESatz e = parser.getESatz();

        double factor = 100d / e.getAnzahlDatensaetze();
        int count = 0;
        int success = 0;
        
        DBService service = Settings.getDBService();

        CSatz c = null;
        while ((c = parser.next()) != null)
        {
          try
          {
            // Mit diesem Factor sollte sich der Fortschrittsbalken
            // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
            monitor.setPercentComplete((int)((++count) * factor));
            
            if (c == null)
            {
              monitor.log(i18n.tr("Auftrag {0} nicht lesbar. Überspringe",""+count));
              continue;
            }
            
            monitor.log(i18n.tr("Importiere Auftrag an {0}",c.getNameEmpfaenger()));
           
            // Neuen Auftrag erstellen
            final Transfer t = (Transfer) service.createObject(((MyIOFormat)format).type,null);

            // Konto suchen
            
            String kontonummer = Long.toString(c.getKontoAuftraggeber());
            String blz         = Long.toString(c.getBlzErstbeteiligt());
            DBIterator konten = service.createList(Konto.class);
            konten.addFilter("kontonummer = '" + kontonummer + "'");
            konten.addFilter("blz = '" + blz + "'");

            Konto k = null;
            if (!konten.hasNext())
            {
              // Das Konto existiert nicht im Hibiscus-Datenbestand.

              // Erstmal schauen, ob der User das Konto schonmal ausgewaehlt hat:
              k = (Konto) ht.get(kontonummer + blz);
              if (k == null)
              {
                // Ne, hat er noch nicht.
                // Also muss der User eins auswaehlen.
                String txt = i18n.tr("Konto {0} [BLZ {1}] nicht gefunden",new String[]{kontonummer,blz});
                monitor.log(txt);
                KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
                d.setText(txt + "\n" + i18n.tr("Bitte wählen Sie das Konto aus, auf dem der Auftrag ausgeführt werden soll."));
                k = (Konto) d.open();
                
                if (k != null)
                  ht.put(kontonummer + blz,k);
              }
            }
            else
            {
              k = (Konto) konten.next();
            }
            t.setKonto(k);
            t.setBetrag(c.getBetragInEuro());
            t.setGegenkontoBLZ(Long.toString(c.getBlzEndbeguenstigt()));
            t.setGegenkontoName(c.getNameEmpfaenger());
            t.setGegenkontoNummer(Long.toString(c.getKontonummer()));
            t.setZweck(c.getVerwendungszweck(1));
            
            int z = c.getAnzahlVerwendungszwecke();
            if (z > 1)
              t.setZweck2(c.getVerwendungszweck(2));
            
            // Ueberweisung speichern
            t.store();
            success++;
            try
            {
              ImportMessage im = new ImportMessage() {
                public GenericObject getImportedObject() throws RemoteException
                {
                  return t;
                }
              };
              Application.getMessagingFactory().sendMessage(im);
            }
            catch (Exception ex)
            {
              Logger.error("error while sending import message",ex);
            }
          }
          catch (ApplicationException ace)
          {
            monitor.log(ace.getMessage());
            monitor.log(i18n.tr("Überspringe Datensatz"));
          }
          catch (Exception e1)
          {
            if (e1 instanceof SWTException)
            {
              if (e1.getCause() instanceof OperationCanceledException)
              {
                Logger.info("operation cancelled");
                monitor.setStatusText(i18n.tr("Import abgebrochen"));
                monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
                return;
              }
            }

            Logger.error("unable to import transfer",e1);
            monitor.log(i18n.tr("Fehler beim Import des Auftrages, überspringe Datensatz"));
          }
        }
        monitor.setStatusText(i18n.tr("{0} Aufträge erfolgreich importiert",""+success));
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
      throw new RemoteException(i18n.tr("Fehler beim Import der DTAUS-Daten"),e);
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
}


/*********************************************************************
 * $Log: DTAUSImporter.java,v $
 * Revision 1.11  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 * Revision 1.10  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.9  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.8  2006/06/06 21:37:55  willuhn
 * @R FilternEngine entfernt. Wird jetzt ueber das Jameica-Messaging-System abgewickelt
 *
 * Revision 1.7  2006/06/05 09:55:50  jost
 * Anpassung an obantoo 0.5
 *
 * Revision 1.6  2006/05/31 09:04:21  willuhn
 * @C Wir merken uns die vom User bereits ausgewaehlten Konten
 *
 * Revision 1.5  2006/05/29 21:20:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/05/29 20:41:21  willuhn
 * @N Import aller logischen Dateien
 *
 * Revision 1.3  2006/05/29 09:16:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/25 13:54:38  willuhn
 * @R removed imports (occurs compile errors in nightly build)
 *
 * Revision 1.1  2006/05/25 13:47:03  willuhn
 * @N Skeleton for DTAUS-Import
 *
 **********************************************************************/