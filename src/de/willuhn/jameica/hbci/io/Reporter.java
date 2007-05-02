/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Reporter.java,v $
 * $Revision: 1.3 $
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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Kapselt den Export von Daten im PDF-Format.
 */
public class Reporter
{
  private I18N i18n = null;

  private ArrayList headers;

  private ArrayList widths;

  private OutputStream out;

  private Document rpt;

  private PdfPTable table;

  private int maxRecords;

  private int currRecord = 0;

  private ProgressMonitor monitor;

  /**
   * ct.
   * @param out
   * @param monitor
   * @param title
   * @param subtitle
   * @param maxRecords
   * @throws DocumentException
   */
  public Reporter(OutputStream out, ProgressMonitor monitor, String title,
      String subtitle, int maxRecords) throws DocumentException
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();
    this.out = out;
    this.monitor = monitor;
    rpt = new Document();
    PdfWriter.getInstance(rpt, out);
    rpt.setMargins(80, 30, 20, 20); // links, rechts, oben, unten
    if (this.monitor != null)
    {
      this.monitor.setStatusText(i18n.tr("Erzeuge Liste"));
      this.monitor.addPercentComplete(1);
    }
    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    rpt.addAuthor(i18n.tr("{0} - Version {1}",
        new String[] { plugin.getManifest().getName(),
            "" + plugin.getManifest().getVersion() }));
    rpt.addTitle(subtitle);

    Chunk fuss = new Chunk(i18n.tr(title + " | " + subtitle
        + " | erstellt am {0}              Seite:  ", HBCI.LONGDATEFORMAT
        .format(new Date())), FontFactory.getFont(FontFactory.HELVETICA, 8,
        Font.BOLD));
    HeaderFooter hf = new HeaderFooter(new Phrase(fuss), true);
    hf.setAlignment(Element.ALIGN_CENTER);
    rpt.setFooter(hf);

    rpt.open();
    try
    {
      ClassLoader loader = Reporter.class.getClassLoader();
      URL url = loader.getResource("icons/hibiscus-icon-16x16.png");
      rpt.add(Image.getInstance(url));
    }
    catch (Exception e)
    {
      Logger.error("unable to add hibiscus icon, will be ignored",e);
    }

    Paragraph pTitle = new Paragraph(i18n.tr(title), FontFactory.getFont(
        FontFactory.HELVETICA_BOLD, 13));

    pTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(pTitle);
    Paragraph psubTitle = new Paragraph(subtitle, FontFactory.getFont(
        FontFactory.HELVETICA_BOLD, 10));
    psubTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(psubTitle);

    headers = new ArrayList();
    widths = new ArrayList();

    monitor.setPercentComplete(0);
    this.maxRecords = maxRecords;
  }

  /**
   * Fuegt einen neuen Absatz hinzu.
   * @param p
   * @throws DocumentException
   */
  public void add(Paragraph p) throws DocumentException
  {
    rpt.add(p);
  }

  /**
   * Fuegt der Tabelle einen neuen Spaltenkopf hinzu.
   * @param text
   * @param align
   * @param width
   * @param color
   */
  public void addHeaderColumn(String text, int align, int width, Color color)
  {
    headers.add(getDetailCell(text, align, color));
    widths.add(new Integer(width));
  }

  /**
   * Fuegt eine neue Spalte hinzu.
   * @param cell
   */
  public void addColumn(PdfPCell cell)
  {
    table.addCell(cell);
  }

  /**
   * Rueckt den Monitor weiter.
   */
  public void setNextRecord()
  {
    currRecord++;
    if (monitor != null)
      monitor.setPercentComplete(currRecord / maxRecords * 100);
  }

  /**
   * Erzeugt den Tabellen-Header.
   * @throws DocumentException
   */
  public void createHeader() throws DocumentException
  {
    table = new PdfPTable(headers.size());
    float[] w = new float[headers.size()];
    for (int i = 0; i < headers.size(); i++)
    {
      Integer breite = (Integer) widths.get(i);
      w[i] = breite.intValue();
    }
    table.setWidths(w);
    table.setWidthPercentage(100);
    table.setSpacingBefore(10);
    table.setSpacingAfter(0);
    for (int i = 0; i < headers.size(); i++)
    {
      PdfPCell cell = (PdfPCell) headers.get(i);
      table.addCell(cell);
    }
    table.setHeaderRows(1);
  }

  /**
   * Schliesst den Report.
   * @throws IOException
   * @throws DocumentException
   */
  public void close() throws IOException, DocumentException
  {
    try
    {
      if (monitor != null)
      {
        monitor.setPercentComplete(100);
        monitor.setStatusText("PDF-Export beendet");
      }
      rpt.add(table);
      rpt.close();
    }
    finally
    {
      // Es muss sichergestellt sein, dass der OutputStream
      // immer geschlossen wird
      out.close();
    }
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * 
   * @param text
   *          der anzuzeigende Text.
   * @param align
   *          die Ausrichtung.
   * @param backgroundcolor
   *          die Hintergundfarbe.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(String text, int align, Color backgroundcolor)
  {
    PdfPCell cell = new PdfPCell(new Phrase(notNull(text), FontFactory.getFont(
        FontFactory.HELVETICA, 8)));
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * 
   * @param text
   *          der anzuzeigende Text.
   * @param align
   *          die Ausrichtung.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(String text, int align)
  {
    return getDetailCell(text, align, Color.WHITE);
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * @param value
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(Double value)
  {
    return getDetailCell(value.doubleValue());
  }

  /**
   * Erzeugt eine Zelle fuer die uebergebene Zahl.
   * 
   * @param value
   *          die Zahl.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(double value)
  {
    Font f = null;
    if (value >= 0)
      f = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL,
          Color.BLACK);
    else
      f = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.RED);
    PdfPCell cell = new PdfPCell(
        new Phrase(HBCI.DECIMALFORMAT.format(value), f));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    return cell;
  }

  /**
   * Gibt einen Leerstring aus, falls der Text null ist.
   * 
   * @param text
   *          der Text.
   * @return der Text oder Leerstring - niemals null.
   */
  public String notNull(String text)
  {
    return text == null ? "" : text;
  }
}

/*******************************************************************************
 * $Log: Reporter.java,v $
 * Revision 1.3  2007/05/02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 * Revision 1.2  2007/05/01 07:17:21  jost
 * Compilierfehler unter 1.4 verhindert.
 * Revision 1.1 2007/04/29 10:21:49 jost PDF-Ausgabe
 * jetzt zentral in einer Klasse
 * 
 ******************************************************************************/
