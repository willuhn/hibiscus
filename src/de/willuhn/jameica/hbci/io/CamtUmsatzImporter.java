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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.GV.parsers.SEPAParserFactory;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.BTag;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.sepa.SepaVersion;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer Umsaetze im CAMT-Format.
 */
public class CamtUmsatzImporter implements Importer
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor, de.willuhn.jameica.system.BackgroundTask)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {

    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));

    if (format == null || !(format instanceof MyIOFormat))
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

    final MyIOFormat myFormat = (MyIOFormat) format;

    try
    {

      de.willuhn.jameica.hbci.rmi.Konto konto = null;

      if (context != null && context instanceof de.willuhn.jameica.hbci.rmi.Konto)
        konto = (de.willuhn.jameica.hbci.rmi.Konto) context;

      if (konto == null)
      {
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Bitte wählen Sie das zu verwendende Konto aus."));
        konto = (de.willuhn.jameica.hbci.rmi.Konto) d.open();
      }

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      Stats stats = new Stats();

      if (myFormat.isZip())
      {
        Logger.info("reading zip file");
        // Wir kriegen aus dem ZIP-Inputstream nicht raus, wieviele Dateien enthalten sind sondern koennen nur drueber iterieren.
        // Daher rechnen wir fuer den Fortschrittsbalken mal pauschal mit 22 Dateien (pro Buchungstag aka Wochentag eines Monats eine Datei)
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry ze = null;
        double fc = 22d;
        int no = 1;
        while ((ze = zis.getNextEntry()) != null)
        {
          if (ze.isDirectory())
            continue;

          String name = ze.getName();
          if (name == null)
            continue;

          if (!name.toLowerCase().endsWith(".xml"))
            continue;

          Logger.info("  reading " + name);

          int chunk = (int) (100 / fc);
          Stats s = doImport(chunk,no,zis,konto,monitor,t);
          stats.created += s.created;
          stats.error += s.error;
          no++;
        }
      }
      else
      {
        Logger.info("reading xml file");
        stats = doImport(100,1,is,konto,monitor,t);
      }

      monitor.setStatusText(i18n.tr("{0} Umsätze erfolgreich importiert, {1} fehlerhafte übersprungen", new String[]{Integer.toString(stats.created),Integer.toString(stats.error)}));
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Datei"));
    }
    finally
    {
      IOUtil.close(is);
    }
  }

  /**
   * Fuehrt den eigentlichen Import einer Datei durch.
   * @param chunk die prozentuale Groesse des Chunks, den die Datei im Gesamt-Fortschritt einnehmen darf.
   * @param no die Dateinummer.
   * @param is der Stream mit der XML-Datei.
   * @param konto das Konto.
   * @param monitor der Progress-Monitor.
   * @param t der Background-Task.
   * @return die Import-Statistik.
   * @throws Exception
   */
  private Stats doImport(int chunk, int no, InputStream is, Konto konto, ProgressMonitor monitor, BackgroundTask t) throws Exception
  {
    Stats stats = new Stats();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtil.copy(is,bos);

    SepaVersion version = SepaVersion.autodetect(new ByteArrayInputStream(bos.toByteArray()));
    if (version == null)
      throw new ApplicationException(i18n.tr("SEPA-Version der XML-Datei nicht ermittelbar"));

    SepaVersion.Type type = version.getType();
    if (type == null || type != SepaVersion.Type.CAMT_052)
      throw new ApplicationException(i18n.tr("Keine gültige CAMT-Datei"));

    if (monitor != null)
      monitor.log(i18n.tr("Datei {0}, SEPA-Version: {1}",Integer.toString(no),version.getURN()));

    List<BTag> tage = new ArrayList<BTag>();
    ISEPAParser<List<BTag>> parser = SEPAParserFactory.get(version);
    parser.parse(new ByteArrayInputStream(bos.toByteArray()),tage);

    List<UmsLine> lines = new ArrayList<UmsLine>();
    for (BTag tag:tage)
    {
      lines.addAll(tag.lines);
    }

    double factor = chunk / (double) lines.size();

    for (int i=0;i<lines.size();++i)
    {
      if (monitor != null)
      {
        int add = (int)((i+1) * factor);
        monitor.log(i18n.tr("Umsatz {0}", Integer.toString(i+1)));

        int offset = 0;
        if (chunk < 100)
          offset = chunk * no;
        monitor.setPercentComplete(offset + add);
      }

      try
      {
        if (t != null && t.isInterrupted())
          throw new OperationCanceledException();

        final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
        umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt
        umsatz.store();
        stats.created++;
        try
        {
          Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
        }
        catch (Exception ex)
        {
          Logger.error("error while sending import message",ex);
        }
      }
      catch (ApplicationException ae)
      {
        if (monitor != null)
          monitor.log("  " + ae.getMessage());
        stats.error++;
      }
      catch (Exception e)
      {
        Logger.error("unable to import line",e);

        if (monitor != null)
          monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes: {0}",e.getMessage()));
        stats.error++;
      }
    }

    return stats;
  }

  /**
   * Kapselt die Counter mit den erstellten und fehlerhaften Buchungen.
   */
  private class Stats
  {
    private int created = 0;
    private int error = 0;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA CAMT-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null; // Wir bieten uns nur fuer Umsaetze an

    IOFormat fXml = new MyIOFormat() {
      public String getName()
      {
        return CamtUmsatzImporter.this.getName() + " (XML)";
      }

      /**
       * @see de.willuhn.jameica.hbci.io.CamtUmsatzImporter.MyIOFormat#isZip()
       */
      @Override
      public boolean isZip()
      {
        return false;
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.xml"};
      }
    };
    IOFormat fZip = new MyIOFormat() {
      public String getName()
      {
        return CamtUmsatzImporter.this.getName() + " (ZIP)";
      }

      /**
       * @see de.willuhn.jameica.hbci.io.CamtUmsatzImporter.MyIOFormat#isZip()
       */
      @Override
      public boolean isZip()
      {
        return true;
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.zip"};
      }
    };
    return new IOFormat[] { fXml,fZip };
  }

  /**
   * Fuegt einen Marker hinzu, um zu erkennen, ob es eine ZIP-Datei ist.
   */
  private interface MyIOFormat extends IOFormat
  {
    /**
     * Liefert true, wenn es eine ZIP-Datei ist.
     * @return true, wenn es eine ZIP-Datei ist.
     */
    public boolean isZip();
  }

}
