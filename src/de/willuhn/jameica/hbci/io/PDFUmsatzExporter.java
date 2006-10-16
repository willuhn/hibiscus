/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/PDFUmsatzExporter.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/10/16 17:12:14 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.awt.Color;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Umsaetze im PDF-Format.
 * Der Exporter kann Umsaetze mehrerer Konten exportieren. Sie werden
 * hierbei nach Konto gruppiert.
 */
public class PDFUmsatzExporter implements Exporter
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public PDFUmsatzExporter()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(de.willuhn.datasource.GenericObject[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(GenericObject[] objects, IOFormat format,
      OutputStream os, ProgressMonitor monitor) throws RemoteException,
      ApplicationException
  {
    
    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Umsätze aus"));

    Umsatz u = (Umsatz) objects[0];

    Date startDate     = u.getDatum();
    Date endDate       = u.getDatum();
    Hashtable umsaetze = new Hashtable();
    
    if (monitor != null) 
    {
      monitor.setStatusText(i18n.tr("Ermittle zu exportierende Umsätze und Konten"));
      monitor.addPercentComplete(1);
    }

    for (int i=0;i<objects.length;++i)
    {
      u = (Umsatz) objects[i];
      Konto k  = u.getKonto();

      // Wir ermitteln bei der Gelegenheit das Maximal- und Minimal-Datum
      Date date = u.getDatum();
      if (date != null)
      {
        if (date.after(endDate))    endDate = date;
        if (date.before(startDate)) startDate = date;
      }

      // Wir gruppieren die Umsaetze nach Konto.
      ArrayList list = (ArrayList) umsaetze.get(k.getID());
      if (list == null)
      {
        list = new ArrayList();
        umsaetze.put(k.getID(),list);
      }
      list.add(u);
    }

    // Falls wir die Datumsfelder als optionale Parameter erhalten haben,
    // nehmen wir die.
    Date d = (Date) Exporter.SESSION.get("pdf.start");
    if (d != null) startDate = d;
    d = (Date) Exporter.SESSION.get("pdf.end");
    if (d != null) endDate = d;
    
    // Der Export
    Document rpt = null;
    
    try
    {
      //////////////////////////////////////////////////////////////////////////
      // Header erzeugen
      if (monitor != null) 
      {
        monitor.setStatusText(i18n.tr("Erzeuge PDF-Datei"));
        monitor.addPercentComplete(1);
      }
      String subTitle = i18n.tr("Kontoauszug {0} - {1}", new String[]{HBCI.DATEFORMAT.format(startDate),HBCI.DATEFORMAT.format(endDate)});

      rpt = new Document();

      PdfWriter.getInstance(rpt,os);
      rpt.setMargins(80, 30, 20, 20); // links, rechts, oben, unten

      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
      rpt.addAuthor(i18n.tr("Hibiscus - Version {0}",""+plugin.getManifest().getVersion()));

      rpt.addTitle(subTitle);
      //////////////////////////////////////////////////////////////////////////


      //////////////////////////////////////////////////////////////////////////
      // Footer erzeugen
      Chunk fuss = new Chunk(i18n.tr("Ausgegeben am {0}              Seite:  ",HBCI.LONGDATEFORMAT.format(new Date())),FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD));
      HeaderFooter hf = new HeaderFooter(new Phrase(fuss), true);
      hf.setAlignment(Element.ALIGN_CENTER);
      rpt.setFooter(hf);

      rpt.open();
      
      Paragraph pTitle = new Paragraph(i18n.tr("Kontoauszug"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
      pTitle.setAlignment(Element.ALIGN_CENTER);
      rpt.add(pTitle);
      Paragraph psubTitle = new Paragraph(subTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
      psubTitle.setAlignment(Element.ALIGN_CENTER);
      rpt.add(psubTitle);
      //////////////////////////////////////////////////////////////////////////

    
      //////////////////////////////////////////////////////////////////////////
      // Iteration ueber Umsaetze
      PdfPTable table = new PdfPTable(5);
      float[] widths = { 30, 100, 120, 30, 30 };
      table.setWidths(widths);
      table.setWidthPercentage(100);
      table.setSpacingBefore(10);
      table.setSpacingAfter(0);

      table.addCell(getDetailCell("Valuta / Buchungs- datum",Element.ALIGN_CENTER, Color.LIGHT_GRAY));
      table.addCell(getDetailCell("Empfänger/Einzahler",     Element.ALIGN_CENTER,Color.LIGHT_GRAY));
      table.addCell(getDetailCell("Zahlungsgrund",           Element.ALIGN_CENTER,Color.LIGHT_GRAY));
      table.addCell(getDetailCell("Betrag",                  Element.ALIGN_CENTER,Color.LIGHT_GRAY));
      table.addCell(getDetailCell("Saldo",                   Element.ALIGN_CENTER, Color.LIGHT_GRAY));
      table.setHeaderRows(2);


      int count = 0;
      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
        monitor.setStatusText(i18n.tr("Exportiere Umsätze"));
      }

      Enumeration konten = umsaetze.keys();
      while (konten.hasMoreElements())
      {
        String id = (String) konten.nextElement();
        Konto konto = (Konto) Settings.getDBService().createObject(Konto.class,id);
        
        PdfPCell cell = getDetailCell(konto.getLongName(), Element.ALIGN_CENTER,Color.LIGHT_GRAY);
        cell.setColspan(5);
        table.addCell(cell);

        ArrayList list = (ArrayList) umsaetze.get(id);

        if (list.size() == 0)
        {
          PdfPCell empty = getDetailCell(i18n.tr("Keine Umsätze"), Element.ALIGN_CENTER,Color.LIGHT_GRAY);
          empty.setColspan(5);
          table.addCell(empty);
          continue;
        }
        
        for (int i=0;i<list.size();++i)
        {
          if (monitor != null)  monitor.setPercentComplete((int)((count++) * factor));

          u = (Umsatz)list.get(i);
          table.addCell(getDetailCell((u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "" ) + "\n"
                                    + (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : ""), Element.ALIGN_LEFT));
          table.addCell(getDetailCell(notNull(u.getEmpfaengerName()) + "\n"
                                    + notNull(u.getArt()), Element.ALIGN_LEFT));
          table.addCell(getDetailCell(notNull(u.getZweck()) + "\n"
                                   + notNull(u.getZweck2()), Element.ALIGN_LEFT));
          table.addCell(getDetailCell(u.getBetrag()));
          table.addCell(getDetailCell(u.getSaldo()));
        }

      }
      rpt.add(table);
    
    }
    catch (DocumentException e)
    {
      Logger.error("error while creating report",e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen des Reports"),e);
    }
    finally
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Schließe PDF-Datei"));
      }
      if (rpt != null)
        rpt.close();
    }

  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * @param text der anzuzeigende Text.
   * @param align die Ausrichtung.
   * @param backgroundcolor die Hintergundfarbe.
   * @return die erzeugte Zelle.
   */
  private PdfPCell getDetailCell(String text, int align, Color backgroundcolor)
  {
    PdfPCell cell = new PdfPCell(new Phrase(notNull(text), FontFactory.getFont(FontFactory.HELVETICA, 8)));
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * @param text der anzuzeigende Text.
   * @param align die Ausrichtung.
   * @return die erzeugte Zelle.
   */
  private PdfPCell getDetailCell(String text, int align)
  {
    return getDetailCell(text, align, Color.WHITE);
  }

  /**
   * Erzeugt eine Zelle fuer die uebergebene Zahl.
   * @param value die Zahl.
   * @return die erzeugte Zelle.
   */
  private PdfPCell getDetailCell(double value)
  {
    Font f = null;
    if (value >= 0)
      f = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL,Color.BLACK);
    else
      f = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.RED);
    PdfPCell cell = new PdfPCell(new Phrase(HBCI.DECIMALFORMAT.format(value), f));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    return cell;
  }

  /**
   * Gibt einen Leerstring aus, falls der Text null ist.
   * @param text der Text.
   * @return der Text oder Leerstring - niemals null.
   */
  private String notNull(String text)
  {
    return text == null ? "" : text;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null;
    
    IOFormat format = new IOFormat() {
      public String getName()
      {
        return i18n.tr("PDF-Format");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"pdf"};
      }
    };
    
    return new IOFormat[]{format};
  }

}


/*********************************************************************
 * $Log: PDFUmsatzExporter.java,v $
 * Revision 1.3  2006/10/16 17:12:14  jost
 * 1. Randeintellungen korrigiert
 * 2. Korrekte Datumsangabe bei Aufruf des Exports aus der Umsatzliste
 *
 * Revision 1.2  2006/10/16 12:51:32  willuhn
 * @B Uebernahme des originalen Datums aus dem Kontoauszug
 *
 * Revision 1.1  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 **********************************************************************/