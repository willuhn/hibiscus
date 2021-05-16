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
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer einen Tree von Umsaetzen im PDF-Format.
 * Hierbei werden alle Kategorien samt deren Umsaetzen exportiert.
 */
public class UmsatzTreeCompleteExporter extends AbstractUmsatzTreeExporter
{
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

    UmsatzTree tree = t[0];
    List<UmsatzTreeNode> list = tree.getUmsatzTree();

    Reporter reporter = null;
    
    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Umsatzkategorien"), this.getSubTitle(tree), list.size());

      reporter.addHeaderColumn(i18n.tr("Valuta / Buchungsdatum"), Element.ALIGN_CENTER,  30,Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Empfänger/Einzahler"),    Element.ALIGN_CENTER, 100,Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Zahlungsgrund"),          Element.ALIGN_CENTER, 120,Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Betrag"),                 Element.ALIGN_CENTER,  30,Reporter.COLOR_BG);
      reporter.createHeader();

      // Iteration ueber Umsaetze
      for (UmsatzTreeNode node : list)
      {
        renderNode(reporter, node);
        reporter.setNextRecord();
      }
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
   * Rendert eine einzelne Kategorie sammt der Umsaetze.
   * @param reporter der Reporter.
   * @param node der Knoten mit evtl vorhanden Unterkategorien und Umsaetzen.
   * @throws Exception
   */
  private void renderNode(Reporter reporter, UmsatzTreeNode node) throws Exception
  {
    List<Umsatz> umsaetze = node.getUmsaetze();
    
    // Wir rendern die Gruppe nur, wenn was drin steht. Andernfalls suchen
    // wir nur in den Kind-Gruppen weiter.
    if (umsaetze.size() > 0)
    {
      PdfPCell cell = reporter.getDetailCell(null, Element.ALIGN_LEFT);
      cell.setColspan(4);
      reporter.addColumn(cell);

      cell = reporter.getDetailCell((String) node.getAttribute("name"),Element.ALIGN_LEFT, Reporter.COLOR_BG);
      cell.setColspan(4);
      reporter.addColumn(cell);

      for (Umsatz u : umsaetze)
      {
        reporter.addColumn(reporter.getDetailCell(
            (u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "")
                + "\n"
                + (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : ""), Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getGegenkontoName())
            + "\n" + reporter.notNull(u.getArt()), Element.ALIGN_LEFT));

        reporter.addColumn(reporter.getDetailCell(VerwendungszweckUtil.toString(u,"\n"), Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
      }

      reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
      reporter.addColumn(reporter.getDetailCell(i18n.tr("Summe {0}",(String) node.getAttribute("name")), Element.ALIGN_LEFT));
      reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
      reporter.addColumn(reporter.getDetailCell((Double) node.getAttribute("betrag")));
    }

    
    // Ggf. vorhandene Unter-Kategorien rendern.
    for (UmsatzTreeNode child : node.getSubGroups())
    {
      renderNode(reporter, child);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format: Umsätze der Kategorien");
  }

}
