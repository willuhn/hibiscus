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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.server.EinnameAusgabeTreeNode;
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
    if (objects == null || (!(objects instanceof EinnahmeAusgabe[]) && !(objects instanceof EinnameAusgabeTreeNode[])))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    if (objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    Reporter reporter = null;

    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Einnahmen/Ausgaben"), getZeitraum(objects), objects.length);
      reporter.addHeaderColumn(i18n.tr("Konto"),        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Anfangssaldo"), Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Einnahmen"),    Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Ausgaben"),     Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Endsaldo"),     Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Plus/Minus"),   Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Differenz"),    Element.ALIGN_CENTER,  60, BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      if(objects instanceof EinnahmeAusgabe[])
      {
        for (int i=0;i<objects.length; ++i)
          report((EinnahmeAusgabe)objects[i], reporter, 0);
      } else
      {
        for (int i=0;i<objects.length; ++i)
          report((EinnameAusgabeTreeNode)objects[i], reporter);
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

  private String getZeitraum(Object[] objects){
    if(objects instanceof EinnahmeAusgabe[])
    {
      EinnahmeAusgabe[] ea = (EinnahmeAusgabe[]) objects;
      if (ea[0].getStartdatum() != null && ea[0].getEnddatum() != null)
        return i18n.tr("Zeitraum {0} - {1}", new String[]{HBCI.DATEFORMAT.format(ea[0].getStartdatum()),HBCI.DATEFORMAT.format(ea[0].getEnddatum())});
    }
    return "";
  }

  private void report(EinnameAusgabeTreeNode treeNode, Reporter reporter) throws RemoteException{
    String range=HBCI.DATEFORMAT.format(treeNode.getFrom())+" - "+HBCI.DATEFORMAT.format(treeNode.getTo());
    PdfPCell cell = reporter.getDetailCell(range, Element.ALIGN_LEFT, null, null, Font.BOLD);
    cell.setColspan(7);
    reporter.addColumn(cell);
    reporter.setNextRecord();
    GenericIterator eas = treeNode.getChildren();
    while(eas.hasNext()){
      GenericObject ea = eas.next();
      report((EinnahmeAusgabe)ea, reporter,5);
    }
  }

  private void report(EinnahmeAusgabe ea, Reporter reporter, float indent)
  {
    PdfPCell cell = reporter.getDetailCell(ea.getText(), Element.ALIGN_LEFT);
    cell.setPaddingLeft(indent);
    reporter.addColumn(cell);
    reporter.addColumn(reporter.getDetailCell(ea.getAnfangssaldo()));
    reporter.addColumn(reporter.getDetailCell(ea.getEinnahmen()));
    reporter.addColumn(reporter.getDetailCell(ea.getAusgaben()));
    reporter.addColumn(reporter.getDetailCell(ea.getEndsaldo()));
    reporter.addColumn(reporter.getDetailCell(ea.getPlusminus()));
    reporter.addColumn(reporter.getDetailCell(ea.getDifferenz()));
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
    if (!EinnahmeAusgabe.class.equals(objectType) && !EinnameAusgabeTreeNode.class.equals(objectType))
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
