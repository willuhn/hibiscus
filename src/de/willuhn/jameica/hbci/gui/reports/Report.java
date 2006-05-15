/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/reports/Attic/Report.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/05/15 20:14:12 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.reports;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

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
import com.lowagie.text.pdf.PdfWriter;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Masterklasse für die PDF-Reports
 * 
 * @author Heiner Jostkleigrewe
 */
public class Report
{
  /**
   * iText-Document-Klasse
   */
  protected Document rpt;

  /**
   * Haupttitel
   */
  protected String title;

  /**
   * Untertitel
   */
  protected String subtitle;

  public Report(String title, String subtitle)
  {
    this.title = title;
    this.subtitle = subtitle;
  }

  public void open(String file) throws FileNotFoundException, DocumentException
  {
    rpt = new Document();

    PdfWriter.getInstance(rpt, new FileOutputStream(file));
    rpt.setMargins(50, 10, 50, 30); // links, rechts, oben, unten

    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    rpt.addAuthor("Hibiscus - Version " + plugin.getManifest().getVersion());
    rpt.addTitle(title + " " + subtitle);

    // Fußzeile
    Chunk fuss = new Chunk("Ausgegeben am "
        + HBCI.LONGDATEFORMAT.format(new Date()) + "              Seite:  ",
        FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD));
    HeaderFooter hf = new HeaderFooter(new Phrase(fuss), true);
    hf.setAlignment(Element.ALIGN_CENTER);
    rpt.setFooter(hf);

    rpt.open();
    Paragraph pTitle = new Paragraph(title, FontFactory.getFont(
        FontFactory.HELVETICA_BOLD, 13));
    pTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(pTitle);
    Paragraph psubTitle = new Paragraph(subtitle, FontFactory.getFont(
        FontFactory.HELVETICA_BOLD, 10));
    psubTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(psubTitle);
  }

  public void close()
  {
    if (rpt != null)
      rpt.close();
  }

  protected PdfPCell getDetailCell(String text, int align, Color backgroundcolor)
  {
    PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(
        FontFactory.HELVETICA, 8)));
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  protected PdfPCell getDetailCell(String text, int align)
  {
    return getDetailCell(text, align, Color.WHITE);
  }

  protected PdfPCell getDetailCell(double value)
  {
    Font f = null;
    if (value >= 0)
    {
      f = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL,
          Color.BLACK);
    }
    else
    {
      f = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.RED);
    }
    PdfPCell cell = new PdfPCell(
        new Phrase(HBCI.DECIMALFORMAT.format(value), f));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    return cell;
  }

}
/*******************************************************************************
 * $Log: Report.java,v $
 * Revision 1.3  2006/05/15 20:14:12  jost
 * Detailverbesserungen des Reports
 * Revision 1.2 2006/05/15 12:05:22 willuhn
 * 
 * @N FileDialog zur Auswahl von Pfad und Datei beim Speichern
 * @N YesNoDialog falls Datei bereits existiert
 * @C KontoImpl#getUmsaetze mit tonumber() statt dateob()
 * 
 * Revision 1.1 2006/05/14 19:52:46 jost Prerelease Kontoauszug-Report
 * 
 ******************************************************************************/
