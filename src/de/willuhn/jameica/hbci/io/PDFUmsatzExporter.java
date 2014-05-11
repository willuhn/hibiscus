/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/PDFUmsatzExporter.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/12/19 22:43:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ext.ExportSaldoExtension;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    
    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Ums�tze aus"));

    Umsatz u = (Umsatz) objects[0];

    Date startDate     = u.getDatum();
    Date endDate       = u.getDatum();
    Hashtable umsaetze = new Hashtable();
    
    if (monitor != null) 
    {
      monitor.setStatusText(i18n.tr("Ermittle zu exportierende Ums�tze und Konten"));
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
        if (endDate != null && date.after(endDate))    endDate = date;
        if (startDate != null && date.before(startDate)) startDate = date;
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
    Boolean filter    = (Boolean) Exporter.SESSION.get("filtered");
    Boolean b         = (Boolean) Exporter.SESSION.get(ExportSaldoExtension.KEY_SALDO_HIDE);
    boolean showSaldo = (b == null || !b.booleanValue());
    
    Reporter reporter = null;
    
    try
    {
      // Der Export
      String subTitle = i18n.tr("{0} - {1}", new String[]{startDate == null ? "" : HBCI.DATEFORMAT.format(startDate),endDate == null ? "" : HBCI.DATEFORMAT.format(endDate)});
      reporter = new Reporter(os,monitor,i18n.tr("Ums�tze") + (filter != null && filter.booleanValue() ? (" (" + i18n.tr("gefiltert") + ")") : ""), subTitle, objects.length  );

      reporter.addHeaderColumn(i18n.tr("Valuta / Buchungsdatum"), Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Empf�nger/Einzahler"),    Element.ALIGN_CENTER,100, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Zahlungsgrund"),          Element.ALIGN_CENTER,120, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Betrag"),                 Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      if (showSaldo)
        reporter.addHeaderColumn(i18n.tr("Saldo"),                  Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      
      // Iteration ueber Umsaetze
      Enumeration konten = umsaetze.keys();
      while (konten.hasMoreElements())
      {
        String id = (String) konten.nextElement();
        Konto konto = (Konto) Settings.getDBService().createObject(Konto.class,id);
        
        PdfPCell cell = reporter.getDetailCell(konto.getLongName(), Element.ALIGN_CENTER,BaseColor.LIGHT_GRAY);
        cell.setColspan(showSaldo ? 5 : 4);
        reporter.addColumn(cell);

        ArrayList list = (ArrayList) umsaetze.get(id);

        if (list.size() == 0)
        {
          PdfPCell empty = reporter.getDetailCell(i18n.tr("Keine Ums�tze"), Element.ALIGN_CENTER,BaseColor.LIGHT_GRAY);
          empty.setColspan(5);
          reporter.addColumn(empty);
          continue;
        }
        
        for (int i=0;i<list.size();++i)
        {

          u = (Umsatz)list.get(i);
          String valuta = (u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "" );
          String datum  = (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : "");
          
          BaseColor color = null;
          int style = Font.NORMAL;
          if (!u.isBooked())
          {
            color = BaseColor.GRAY;
            style = Font.ITALIC;
          }
          
          reporter.addColumn(reporter.getDetailCell(valuta + "\n" + datum, Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getGegenkontoName()) + "\n" + reporter.notNull(u.getArt()), Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(VerwendungszweckUtil.toString(u,"\n"), Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
          if (showSaldo)
            reporter.addColumn(reporter.getDetailCell(u.getSaldo()));
          reporter.setNextRecord();
        }
      }
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (Exception e)
    {
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("error while creating report",e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen der Auswertung"),e);
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
