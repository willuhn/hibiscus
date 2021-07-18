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

import java.io.OutputStream;
import java.rmi.RemoteException;

import de.jost_net.OBanToo.Dtaus.DtausDateiWriter;
import de.jost_net.OBanToo.Dtaus.DtausException;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
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
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
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
          monitor.log(i18n.tr("Ignoriere Kundenkennung {0}: ungültig",s));
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
          writer.setCTextschluessel(mapTextschluesselToDtaus(buchung));

          String[] lines = VerwendungszweckUtil.toArray(buchung);
          for (String line:lines)
            writer.addCVerwendungszweck(line);

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
    return new Class[]{
        SammelUeberweisung.class,
        SammelLastschrift.class
    };
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return false;
  }
}
