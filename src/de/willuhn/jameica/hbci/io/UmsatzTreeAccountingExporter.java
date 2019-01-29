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
import java.util.List;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.gui.ext.ExportAddSumRowExtension;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer einen Baum von Umsaetzen nach Kategorien im PDF-Format.
 * Hierbei werden die Summen der einzelnen Kategorien, aufgeschlüsselt nach Einnahmen und Ausgaben exportiert.
 */
public class UmsatzTreeAccountingExporter extends AbstractUmsatzTreeExporter
{
  private double einnahmen = 0.0d;
  private double ausgaben  = 0.0d;
  private double betrag    = 0.0d;
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (objects == null || !(objects instanceof UmsatzTree[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Umsätze aus"));

    UmsatzTree[] t = (UmsatzTree[]) objects;
    if (t.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Umsätze aus"));

    this.einnahmen = 0.0d;
    this.ausgaben  = 0.0d;
    this.betrag    = 0.0d;

    UmsatzTree tree = t[0];
    List list  = tree.getUmsatzTree();

    Reporter reporter = null;
    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Umsatzkategorien"), this.getSubTitle(tree), list.size());

      reporter.addHeaderColumn(i18n.tr("Kategorie"), Element.ALIGN_LEFT, 130, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Einnahmen"), Element.ALIGN_RIGHT, 30,  Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Ausgaben"),  Element.ALIGN_RIGHT, 30,  Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Betrag"),    Element.ALIGN_RIGHT, 30,  Reporter.COLOR_BG);
      reporter.createHeader();

      // Iteration ueber die Kategorien
      for (int i=0; i<list.size(); ++i)
      {
        this.renderNode(reporter,(UmsatzTreeNode) list.get(i),0);
        reporter.setNextRecord();
      }
      
      Boolean b = (Boolean) Exporter.SESSION.get(ExportAddSumRowExtension.KEY_SUMROW_ADD);
      if (b != null && b.booleanValue())
        this.renderSum(reporter);
      
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (Exception e)
    {
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("error while creating report", e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen der Auswertung: {0}",e.getMessage()), e);
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
          Logger.error("unable to close report",e);
        }
      }
    }
  }

  /**
   * Rendert eine einzelne Kategorie sammt Unterkategorien und stellt ihre Einnahmen, Ausgaben und den Betrag dar.
   * @param reporter der Reporter.
   * @param node der Knoten mit evtl vorhanden Unterkategorien und deren Einnahmen, Ausgaben und Betrag.
   * @throws Exception
   */
  private void renderNode(Reporter reporter, UmsatzTreeNode node, int level) throws Exception
  {
    String name = (String) node.getAttribute("name");
    for ( int j = 0; j < level; ++j )
    {
      name = "    " + name;
    }
    
    PdfPCell cell = reporter.getDetailCell(name, Element.ALIGN_LEFT);
    reporter.addColumn(cell);

    Double de = (Double) node.getAttribute("einnahmen");
    Double da = (Double) node.getAttribute("ausgaben");
    Double db = (Double) node.getAttribute("betrag");
    reporter.addColumn(reporter.getDetailCell(de));
    reporter.addColumn(reporter.getDetailCell(da));
    reporter.addColumn(reporter.getDetailCell(db));
    
    // Summen
    this.einnahmen += de.doubleValue();
    this.ausgaben  += da.doubleValue();
    this.betrag    += db.doubleValue();

    List<UmsatzTreeNode> children = node.getSubGroups();
    for (int i=0; i<children.size(); ++i)
    {
      renderNode(reporter, children.get(i), level+1);
    }
  }
  
  /**
   * Rendert die Summen.
   * @param reporter der Reporter.
   * @throws Exception
   */
  private void renderSum(Reporter reporter) throws Exception
  {
    PdfPCell cell = reporter.getDetailCell(i18n.tr("Summe"), Element.ALIGN_LEFT,null,null,Font.BOLD);
    reporter.addColumn(cell);

    reporter.addColumn(reporter.getDetailCell(this.einnahmen,null,Font.BOLD));
    reporter.addColumn(reporter.getDetailCell(this.ausgaben,null,Font.BOLD));
    reporter.addColumn(reporter.getDetailCell(this.betrag,null,Font.BOLD));

    // Summen nach der Ausgabe resetten - ermoeglicht kuenftig auch Zwischensummen
    this.einnahmen = 0.0d;
    this.ausgaben  = 0.0d;
    this.betrag    = 0.0d;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return ext != null && (ExportAddSumRowExtension.KEY_SUMROW_ADD.equals(ext));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format: Summen aller Kategorien mit Einnahmen und Ausgaben");
  }
}
