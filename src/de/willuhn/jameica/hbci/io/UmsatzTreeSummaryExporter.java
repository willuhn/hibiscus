/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/UmsatzTreeSummaryExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/05/02 11:18:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.awt.Color;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.List;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer einen Tree von Umsaetzen im PDF-Format.
 * Hierbei werden nur die Summen der einzelnen Kategorien exportiert.
 */
public class UmsatzTreeSummaryExporter implements Exporter
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public UmsatzTreeSummaryExporter()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
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
    List list = tree.getUmsatzTree();
    Konto k = tree.getKonto();
    
    String subTitle = i18n.tr("Zeitraum {0} - {1}, {2}", new String[] {
        HBCI.DATEFORMAT.format(tree.getStart()), HBCI.DATEFORMAT.format(tree.getEnd()),
        k == null ? i18n.tr("alle Konten") : k.getBezeichnung() });

    Reporter reporter = null;
    try
    {
      reporter = new Reporter(os, monitor, "Umsatzkategorien", subTitle, list.size());

      reporter.addHeaderColumn("Kategorie", Element.ALIGN_CENTER, 130,Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 30,Color.LIGHT_GRAY);
      reporter.createHeader();

      // Iteration ueber die Kategorien
      for (int i = 0; i < list.size(); i++)
      {
        UmsatzGroup ug = (UmsatzGroup) list.get(i);

        PdfPCell cell = reporter.getDetailCell((String) ug.getAttribute("name"), Element.ALIGN_LEFT);
        reporter.addColumn(cell);
        reporter.addColumn(reporter.getDetailCell((Double) ug.getAttribute("betrag")));
        
        reporter.setNextRecord();
      }
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (DocumentException e)
    {
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("error while creating report", e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen des Reports"), e);
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
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    IOFormat myFormat = new IOFormat() {
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
       */
      public String getName()
      {
        return i18n.tr("PDF-Format: Nur Kategorie-Summen");
      }
    
      public String[] getFileExtensions()
      {
        return new String[]{"pdf"};
      }
    
    };
    return new IOFormat[]{myFormat};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format: Nur Kategorie-Summen");
  }
}

/*******************************************************************************
 * $Log: UmsatzTreeSummaryExporter.java,v $
 * Revision 1.1  2007/05/02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 * Revision 1.1  2007/04/29 10:22:11  jost
 * Neu: PDF-Ausgabe der UmsÃ¤tze nach Kategorien
 *
 ******************************************************************************/
