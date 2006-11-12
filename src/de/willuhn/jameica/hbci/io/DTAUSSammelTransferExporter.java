/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSSammelTransferExporter.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/11/12 22:35:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;

import de.jost_net.OBanToo.Dtaus.CSatz;
import de.jost_net.OBanToo.Dtaus.DtausDateiWriter;
import de.jost_net.OBanToo.Dtaus.DtausException;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Exporters fuer DTAUS-Dateien.
 */
public class DTAUSSammelTransferExporter extends AbstractDTAUSIO implements Exporter
{

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(de.willuhn.datasource.GenericObject[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(GenericObject[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (os == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Ziel für die Datei angegeben"));

    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Format angegeben"));

    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Keine zu exportierenden Daten angegeben"));

    if (!(objects instanceof SammelTransfer[]))
      throw new ApplicationException(i18n.tr("Die zu exportierenden Daten enthalten keine Sammel-Aufträge"));
      
    DtausDateiWriter writer = null;

    try
    {
      writer = new DtausDateiWriter(os);
      for (int i=0;i<objects.length;++i)
      {
        SammelTransfer transfer   = (SammelTransfer) objects[i];
        Konto konto               = transfer.getKonto();
        GenericIterator buchungen = transfer.getBuchungen();

        monitor.setPercentComplete(0);
        monitor.setStatusText(i18n.tr("Exportiere logische Datei Nr. {0}",""+(i+1)));

        double factor = 100d / buchungen.size();
        int count = 0;
        int success = 0;
        

        long kundenNummer = 0;
        String s = konto.getKundennummer();
        try
        {
          kundenNummer = Long.parseLong(s);
        }
        catch (Exception e)
        {
          monitor.log(i18n.tr("Ignoriere Kundennummer {0}: ungültig",s));
        }

        long blz = 0;
        s = konto.getBLZ();
        try
        {
          blz = Long.parseLong(s);
        }
        catch (Exception e)
        {
          monitor.log(i18n.tr("Ignoriere BLZ {0}: ungültig",s));
        }

        
        writer.open();
        writer.setAAusfuehrungsdatum(transfer.getTermin());
        writer.setABLZBank(blz);

        String type = (transfer instanceof SammelUeberweisung) ? "GK" : "LK";
        writer.setAGutschriftLastschrift(type);
        
        writer.setAKonto(Long.parseLong(konto.getKontonummer()));
        writer.setAKundenname(konto.getName());
        writer.writeASatz();
        
        // TODO Nicht schoen. Ausserdem werden die beiden Lastschrift-Typen (Abbuchung:04, Einzugserm. 05) noch nicht unterschieden
        int textSchluessel = (transfer instanceof SammelUeberweisung) ? CSatz.TS_UEBERWEISUNGSGUTSCHRIFT : CSatz.TS_LASTSCHRIFT_EINZUGSERMAECHTIGUNGSVERFAHREN;

        while (buchungen.hasNext())
        {
          // Mit diesem Factor sollte sich der Fortschrittsbalken
          // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
          monitor.setPercentComplete((int)((++count) * factor));

          SammelTransferBuchung buchung = (SammelTransferBuchung) buchungen.next();

          monitor.log(i18n.tr("Exportiere Datensatz {0}",buchung.getGegenkontoName()));
          
          writer.setCBetragInEuro(buchung.getBetrag());
          writer.setCBLZEndbeguenstigt(Long.parseLong(buchung.getGegenkontoBLZ()));
          writer.setCBLZErstbeteiligtesInstitut(blz);
          writer.setCKonto(Long.parseLong(buchung.getGegenkontoNummer()));
          writer.setCName(buchung.getGegenkontoName());
          writer.setCInterneKundennummer(kundenNummer);
          writer.setCTextschluessel(textSchluessel);
          
          writer.addCVerwendungszweck(buchung.getZweck());
          String zweck2 = buchung.getZweck2();
          if (zweck2 != null && zweck2.length() > 0)
            writer.addCVerwendungszweck(zweck2);
          writer.writeCSatz();
          success++;
        }
        monitor.setStatusText(i18n.tr("{0} Aufträge erfolgreich exportiert",""+success));
      }
      writer.writeESatz();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Export abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (DtausException dta)
    {
      Logger.error(dta.getMessage(),dta);
      throw new ApplicationException(i18n.tr(dta.getLocalizedMessage()));
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Export der Daten"),e);
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
          os = null;
        }
        catch (Exception e)
        {
          Logger.error("error while closing dtaus writer",e);
        }
      }
      // Outputstream schliessen, falls das noch nicht geschehen ist
      if (os != null)
      {
        try
        {
          os.close();
        }
        catch (Throwable t)
        {
          Logger.error("unable to close file",t);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
//    return new Class[0];
    return new Class[]{
        SammelUeberweisung.class,
        SammelLastschrift.class
    };
  }
}


/**********************************************************************
 * $Log: DTAUSSammelTransferExporter.java,v $
 * Revision 1.7  2006/11/12 22:35:30  willuhn
 * @B LK/GK-Kennzeichen verkehrtrum
 *
 * Revision 1.6  2006/11/07 14:31:14  willuhn
 * @B DtausDateiwriter wurde nicht geschlossen
 *
 * Revision 1.5  2006/10/23 21:16:51  willuhn
 * @N eBaykontoParser umbenannt und ueberarbeitet
 *
 * Revision 1.4  2006/08/21 23:15:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/08/21 23:15:01  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.2  2006/08/07 21:51:43  willuhn
 * @N Erste Version des DTAUS-Exporters
 *
 * Revision 1.1  2006/08/07 15:19:32  willuhn
 * @N DTAUS-Export
 *
 * Revision 1.2  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 * Revision 1.1  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 **********************************************************************/