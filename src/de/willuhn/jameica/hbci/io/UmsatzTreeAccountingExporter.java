/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/UmsatzTreeAccountingExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/12/12 23:16:16 $
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

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer einen Baum von Umsaetzen nach Kategorien im PDF-Format.
 * Hierbei werden die Summen der einzelnen Kategorien, aufgeschlüsselt nach Einnahmen und Ausgaben exportiert.
 */
public class UmsatzTreeAccountingExporter implements Exporter
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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

    String subTitle = i18n.tr("Zeitraum {0} - {1}, {2}",HBCI.DATEFORMAT.format(tree.getStart()), HBCI.DATEFORMAT.format(tree.getEnd()),k == null ? i18n.tr("alle Konten") : k.getBezeichnung());

    Reporter reporter = null;
    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Umsatzkategorien"), subTitle, list.size());

      reporter.addHeaderColumn(i18n.tr("Kategorie"), Element.ALIGN_CENTER, 130, Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Einnahmen"), Element.ALIGN_CENTER, 30,  Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Ausgaben"),  Element.ALIGN_CENTER, 30,  Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Betrag"),    Element.ALIGN_CENTER, 30,  Color.LIGHT_GRAY);
      reporter.createHeader();

      // Iteration ueber die Kategorien
      for (int i=0; i<list.size(); ++i)
      {
        renderNode(reporter,(UmsatzTreeNode) list.get(i),0);
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
   * Rendert eine einzelne Kategorie sammt Unterkategorien und stellt ihre Einnahmen, Ausgaben und den Betrag dar.
   * @param reporter der Reporter.
   * @param node der Knoten mit evtl vorhanden Unterkategorien und deren Einnahmen, Ausgaben und Betrag.
   * @throws Exception
   */
  private void renderNode(Reporter reporter, UmsatzTreeNode node, int level) throws Exception
  {
    String name = (String) node.getAttribute( "name" );
    for ( int j = 0; j < level; ++j )
    {
      name = "    " + name;
    }
    
    PdfPCell cell = reporter.getDetailCell(name, Element.ALIGN_LEFT);
    reporter.addColumn(cell);

    reporter.addColumn( reporter.getDetailCell( (Double) node.getAttribute("einnahmen")));
    reporter.addColumn( reporter.getDetailCell( (Double) node.getAttribute("ausgaben")));
    reporter.addColumn( reporter.getDetailCell( (Double) node.getAttribute("betrag")));

    List<UmsatzTreeNode> children = node.getSubGroups();
    for (int i=0; i<children.size(); ++i)
    {
      renderNode(reporter, children.get(i), level+1);
    }
  }


  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Wir unterstuetzen nur Umsatz-Trees
    if (!UmsatzTree.class.equals(objectType))
      return null;

    IOFormat myFormat = new IOFormat() {

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
       */
      public String getName()
      {
        return UmsatzTreeAccountingExporter.this.getName();
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
    return i18n.tr("PDF-Format: Summen aller Kategorien mit Einnahmen und Ausgaben" );
  }
}

/*******************************************************************************
 * $Log: UmsatzTreeAccountingExporter.java,v $
 * Revision 1.1  2010/12/12 23:16:16  willuhn
 * @N Alex' Patch mit der Auswertung "Summen aller Kategorien mit Einnahmen und Ausgaben"
 *
 ******************************************************************************/
