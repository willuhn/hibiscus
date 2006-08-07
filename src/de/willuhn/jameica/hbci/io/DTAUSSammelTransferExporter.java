/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSSammelTransferExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/07 15:19:32 $
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

import de.jost_net.OBanToo.Dtaus.DtausDateiWriter;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.rmi.Konto;
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
      
    try
    {
      DtausDateiWriter writer = new DtausDateiWriter(os);
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
        
        writer.open();
        writer.setAAusfuehrungsdatum(transfer.getTermin());
        writer.setABLZBank(Long.parseLong(konto.getBLZ()));

        // TODO Ist das richtig rum?
        String type = (transfer instanceof SammelUeberweisung) ? "LK" : "GK";
        writer.setAGutschriftLastschrift(type);
        
        writer.setAKonto(Long.parseLong(konto.getKontonummer()));
        writer.writeASatz();
        
        while (buchungen.hasNext())
        {
          // Mit diesem Factor sollte sich der Fortschrittsbalken
          // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
          monitor.setPercentComplete((int)((++count) * factor));

          SammelTransferBuchung buchung = (SammelTransferBuchung) buchungen.next();

          monitor.log(i18n.tr("Exportiere Datensatz {0}",buchung.getGegenkontoName()));
          
          writer.setCBetragInEuro(buchung.getBetrag());
          writer.setCBLZEndbeguenstigt(Long.parseLong(buchung.getGegenkontoBLZ()));
          writer.setCBLZErstbeteiligtesInstitut(Long.parseLong(konto.getBLZ()));
          writer.setCKonto(Long.parseLong(buchung.getGegenkontoNummer()));
          writer.setCName(buchung.getGegenkontoName());
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
      os = null; // wird vokm DTAUSWriter geschlossen
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Export abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Export der Daten"),e);
    }
    finally
    {
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
    return new Class[]{SammelTransfer.class};
  }
}


/**********************************************************************
 * $Log: DTAUSSammelTransferExporter.java,v $
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