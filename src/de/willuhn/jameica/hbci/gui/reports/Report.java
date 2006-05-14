/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/reports/Attic/Report.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/05/14 19:52:46 $
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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;

public class Report
{
  protected Document rpt;

  protected String title;

  protected String subtitle;

  public Report(String title, String subtitle)
  {
    this.title = title;
    this.subtitle = subtitle;
  }

  public void open(String file) throws FileNotFoundException, DocumentException
  {
    GUI.getStatusBar().startProgress();
    rpt = new Document();
    PdfWriter.getInstance(rpt, new FileOutputStream(file));
    rpt.setMargins(50, 10, 50, 50);
    rpt.setFooter(new HeaderFooter(new Phrase("Ausgegeben am "
        + HBCI.LONGDATEFORMAT.format(new Date()) + "              Seite:"),
        true));
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
    rpt.close();
    GUI.getStatusBar().stopProgress();
  }

  protected PdfPCell getDetailCell(String text, int align)
  {
    PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(
        FontFactory.HELVETICA, 8)));
    cell.setHorizontalAlignment(align);
    return cell;
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
 * Revision 1.1  2006/05/14 19:52:46  jost
 * Prerelease Kontoauszug-Report
 *
 ******************************************************************************/
