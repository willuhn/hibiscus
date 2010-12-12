/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/UmsatzTreeCompleteExporter.java,v $
 * $Revision: 1.5 $
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
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer einen Tree von Umsaetzen im PDF-Format.
 * Hierbei werden alle Kategorien samt deren Umsaetzen exportiert.
 */
public class UmsatzTreeCompleteExporter implements Exporter
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

    String subTitle = i18n.tr("Zeitraum {0} - {1}, {2}", new String[] {
        HBCI.DATEFORMAT.format(tree.getStart()), HBCI.DATEFORMAT.format(tree.getEnd()),
        k == null ? i18n.tr("alle Konten") : k.getBezeichnung()
    });
    
    Reporter reporter = null;
    
    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Umsatzkategorien"), subTitle, list.size());

      reporter.addHeaderColumn(i18n.tr("Valuta / Buchungsdatum"), Element.ALIGN_CENTER,  30,Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Empfänger/Einzahler"),    Element.ALIGN_CENTER, 100,Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Zahlungsgrund"),          Element.ALIGN_CENTER, 120,Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Betrag"),                 Element.ALIGN_CENTER,  30,Color.LIGHT_GRAY);
      reporter.createHeader();

      // Iteration ueber Umsaetze
      for (int i=0;i<list.size(); ++i)
      {
        renderNode(reporter,(UmsatzTreeNode) list.get(i));
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

      cell = reporter.getDetailCell((String) node.getAttribute("name"),Element.ALIGN_LEFT, Color.LIGHT_GRAY);
      cell.setColspan(4);
      reporter.addColumn(cell);

      for (int i=0;i<umsaetze.size();++i)
      {
        Umsatz u = umsaetze.get(i);
        reporter.addColumn(reporter.getDetailCell(
            (u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "")
                + "\n"
                + (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : ""), Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getGegenkontoName())
            + "\n" + reporter.notNull(u.getArt()), Element.ALIGN_LEFT));

        StringBuffer sb = new StringBuffer();
        sb.append(u.getZweck());
        sb.append("\n");

        String z2 = u.getZweck2();
        if (z2 != null && z2.length() > 0)
        {
          sb.append(z2);
          sb.append("\n");
        }
        
        String[] ewz = u.getWeitereVerwendungszwecke();
        if (ewz != null && ewz.length > 0)
        {
          for (int r=0;r<ewz.length;++r)
          {
            if (ewz[r] == null || ewz.length == 0)
              continue;
            sb.append(ewz[r]);
            sb.append("\n");
          }
        }
        reporter.addColumn(reporter.getDetailCell(sb.toString(), Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
      }

      reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
      reporter.addColumn(reporter.getDetailCell(i18n.tr("Summe {0}",(String) node.getAttribute("name")), Element.ALIGN_LEFT));
      reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
      reporter.addColumn(reporter.getDetailCell((Double) node.getAttribute("betrag")));
    }

    
    // Ggf. vorhandene Unter-Kategorien rendern.
    List<UmsatzTreeNode> children = node.getSubGroups();
    for (int i=0;i<children.size();++i)
    {
      renderNode(reporter,children.get(i));
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
        return UmsatzTreeCompleteExporter.this.getName();
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
    return i18n.tr("PDF-Format: Umsätze der Kategorien");
  }

}

/*******************************************************************************
 * $Log: UmsatzTreeCompleteExporter.java,v $
 * Revision 1.5  2010/12/12 23:16:16  willuhn
 * @N Alex' Patch mit der Auswertung "Summen aller Kategorien mit Einnahmen und Ausgaben"
 *
 * Revision 1.4  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 * Revision 1.3  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.2  2007/05/02 12:40:18  willuhn
 * @C UmsatzTree*-Exporter nur fuer Objekte des Typs "UmsatzTree" anbieten
 * @C Start- und End-Datum in Kontoauszug speichern und an PDF-Export via Session uebergeben
 *
 * Revision 1.1  2007/05/02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 * Revision 1.1  2007/04/29 10:22:28  jost
 * Neu: PDF-Ausgabe der UmsÃ¤tze nach Kategorien
 *
 ******************************************************************************/
