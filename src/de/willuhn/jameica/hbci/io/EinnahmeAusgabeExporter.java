/**********************************************************************
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 * 
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer die Einnahmen/Ausgaben.
 */
public class EinnahmeAusgabeExporter implements Exporter
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (objects == null || !(objects instanceof EinnahmeAusgabe[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    EinnahmeAusgabe[] ea = (EinnahmeAusgabe[]) objects;
    if (ea.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    Reporter reporter = null;

    try
    {
      String sub = "";

      if (ea[0].getStartdatum() != null && ea[0].getEnddatum() != null)
        sub = i18n.tr("Zeitraum {0} - {1}", new String[]{HBCI.DATEFORMAT.format(ea[0].getStartdatum()),HBCI.DATEFORMAT.format(ea[0].getEnddatum())});

      reporter = new Reporter(os, monitor, i18n.tr("Einnahmen/Ausgaben"), sub,ea.length);
      reporter.addHeaderColumn(i18n.tr("Konto"),        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Anfangssaldo"), Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Einnahmen"),    Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Ausgaben"),     Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Endsaldo"),     Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Plus/Minus"),   Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Differenz"),    Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      // Iteration ueber Umsaetze
      for (int i=0;i<ea.length; ++i)
      {
        reporter.addColumn(reporter.getDetailCell(ea[i].getText(), Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(ea[i].getAnfangssaldo()));
        reporter.addColumn(reporter.getDetailCell(ea[i].getEinnahmen()));
        reporter.addColumn(reporter.getDetailCell(ea[i].getAusgaben()));
        reporter.addColumn(reporter.getDetailCell(ea[i].getEndsaldo()));
        reporter.addColumn(reporter.getDetailCell(ea[i].getPlusminus()));
        reporter.addColumn(reporter.getDetailCell(ea[i].getDifferenz()));
        reporter.setNextRecord();
      }
      if (monitor != null)
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (DocumentException e)
    {
      if (monitor != null)
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      
      Logger.error("error while creating report", e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen der Auswertung"), e);
    }
    finally
    {
      if (reporter != null)
      {
        try
        {
          reporter.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close report", e);
        }
      }
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Wir unterstuetzen nur Umsatz-Trees
    if (!EinnahmeAusgabe.class.equals(objectType))
      return null;

    IOFormat myFormat = new IOFormat()
    {

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
       */
      public String getName()
      {
        return i18n.tr("PDF-Format: Einnahmen/Ausgaben");
      }

      public String[] getFileExtensions()
      {
        return new String[] { "pdf" };
      }

    };
    return new IOFormat[] { myFormat };
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format: Einnahmen/Ausgaben");
  }
}
