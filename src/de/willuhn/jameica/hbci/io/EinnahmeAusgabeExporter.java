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
import java.util.Date;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabeTreeNode;
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
    if (objects == null || !(objects instanceof EinnahmeAusgabeZeitraum[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    if (objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    EinnahmeAusgabeZeitraum[] data = (EinnahmeAusgabeZeitraum[]) objects;
    Reporter reporter = null;

    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Einnahmen/Ausgaben"), getZeitraum(data), data.length);
      reporter.addHeaderColumn(i18n.tr("Konto"),        Element.ALIGN_CENTER, 100, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Anfangssaldo"), Element.ALIGN_CENTER,  60, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Einnahmen"),    Element.ALIGN_CENTER,  60, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Ausgaben"),     Element.ALIGN_CENTER,  60, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Endsaldo"),     Element.ALIGN_CENTER,  60, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Plus/Minus"),   Element.ALIGN_CENTER,  60, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Differenz"),    Element.ALIGN_CENTER,  60, Reporter.COLOR_BG);
      reporter.createHeader();

      for (EinnahmeAusgabeZeitraum e:data)
      {
        if (e instanceof EinnahmeAusgabeTreeNode)
          report((EinnahmeAusgabeTreeNode)e, reporter);
        else
          report((EinnahmeAusgabe)e, reporter, 0);
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
   * Liefert einen Text mit dem Zeitraum.
   * @param objects die zu exportierenden Daten.
   * @return der Text mit dem Zeitraum.
   */
  private String getZeitraum(EinnahmeAusgabeZeitraum[] objects)
  {
    Date start = null;
    Date end = null;

    for (EinnahmeAusgabeZeitraum n:objects)
    {
      Date s = n.getStartdatum();
      if (start == null || (s != null && s.before(start)))
        start = s;
      
      Date e = n.getEnddatum();
      if (end == null || (e != null && e.after(end)))
        end = e;
    }

    if (start != null && end != null)
      return i18n.tr("Zeitraum {0} - {1}", HBCI.DATEFORMAT.format(start), HBCI.DATEFORMAT.format(end));
    
    return "";
  }

  /**
   * Erzeugt die Report-Zeilen fuer den Knoten.
   * @param treeNode der Knoten.
   * @param reporter der Reporter.
   * @throws RemoteException
   */
  private void report(EinnahmeAusgabeTreeNode treeNode, Reporter reporter) throws RemoteException
  {
    PdfPCell cell = reporter.getDetailCell(treeNode.getText(), Element.ALIGN_LEFT, null, null, Font.BOLD);
    cell.setColspan(7);
    reporter.addColumn(cell);
    reporter.setNextRecord();
    GenericIterator eas = treeNode.getChildren();
    while(eas.hasNext())
    {
      GenericObject ea = eas.next();
      report((EinnahmeAusgabe)ea, reporter,5);
    }
  }

  /**
   * Erzeugt eine Zeile fuer den Report.
   * @param ea die Zeile.
   * @param reporter der Reporter.
   * @param indent Einrueckungslevel.
   */
  private void report(EinnahmeAusgabe ea, Reporter reporter, float indent)
  {
    PdfPCell cell = reporter.getDetailCell(ea.getText(), Element.ALIGN_LEFT);
    cell.setPaddingLeft(indent);
    reporter.addColumn(cell);
    reporter.addColumn(reporter.getDetailCell(ea.getAnfangssaldo(),Reporter.COLOR_FG));
    reporter.addColumn(reporter.getDetailCell(ea.getEinnahmen(),Reporter.COLOR_FG));
    reporter.addColumn(reporter.getDetailCell(ea.getAusgaben(),Reporter.COLOR_FG));
    reporter.addColumn(reporter.getDetailCell(ea.getEndsaldo(),Reporter.COLOR_FG));
    
    double sum = ea.getPlusminus();
    reporter.addColumn(reporter.getDetailCell(sum,sum >= 0.01d ? Reporter.COLOR_GREEN : (sum <= -0.01d ? Reporter.COLOR_RED : Reporter.COLOR_FG)));
    
    double diff = ea.getDifferenz();
    reporter.addColumn(reporter.getDetailCell(diff,Math.abs(diff) >= 0.01d ? Reporter.COLOR_RED : Reporter.COLOR_FG));
    reporter.setNextRecord();
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
    if (!EinnahmeAusgabeZeitraum.class.equals(objectType))
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
