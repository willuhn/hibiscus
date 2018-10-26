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
import de.willuhn.jameica.hbci.rmi.Protokoll;
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
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));
    
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

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      IOUtil.copy(is,bos);
      
      SepaVersion version = SepaVersion.autodetect(new ByteArrayInputStream(bos.toByteArray()));
      if (version == null)
        throw new ApplicationException(i18n.tr("SEPA-Version der XML-Datei nicht ermittelbar"));

      SepaVersion.Type type = version.getType();
      if (type == null || type != SepaVersion.Type.CAMT_052)
        throw new ApplicationException(i18n.tr("Keine gültige CAMT-Datei"));

      monitor.log(i18n.tr("SEPA-Version: {0}",version.getURN()));

      List<BTag> tage = new ArrayList<BTag>();
      ISEPAParser<List<BTag>> parser = SEPAParserFactory.get(version);
      parser.parse(new ByteArrayInputStream(bos.toByteArray()),tage);
      
      List<UmsLine> lines = new ArrayList<UmsLine>();
      for (BTag tag:tage)
      {
        lines.addAll(tag.lines);
      }

      if (lines.size() == 0)
      {
        konto.addToProtokoll(i18n.tr("Keine Umsätze importiert"),Protokoll.TYP_ERROR);
        return;
      }
      
      double factor = 100d / (double) lines.size();

      int created = 0;
      int error   = 0;

      for (int i=0;i<lines.size();++i)
      {
        if (monitor != null)
        {
          monitor.log(i18n.tr("Umsatz {0}", "" + (i+1)));
          monitor.setPercentComplete((int)((i+1) * factor));
        }

        try
        {
          if (t != null && t.isInterrupted())
            throw new OperationCanceledException();

          final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
          umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt
          umsatz.store();
          created++;
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
          monitor.log("  " + ae.getMessage());
          error++;
        }
        catch (Exception e)
        {
          Logger.error("unable to import line",e);
          monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes: {0}",e.getMessage()));
          error++;
        }
      }
      monitor.setStatusText(i18n.tr("{0} Umsätze erfolgreich importiert, {1} fehlerhafte übersprungen", new String[]{""+created,""+error}));
      monitor.addPercentComplete(1);
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
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA CAMT-Format (XML)");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null; // Wir bieten uns nur fuer Umsaetze an
    
    IOFormat f = new IOFormat() {
      public String getName()
      {
        return CamtUmsatzImporter.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.xml"};
      }
    };
    return new IOFormat[] { f };
  }
}
