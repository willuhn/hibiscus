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
import de.willuhn.jameica.hbci.gui.ext.ExportAddSumRowExtension;
import de.willuhn.jameica.hbci.gui.ext.ExportSaldoExtension;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
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
  private static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
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
    b                 = (Boolean) Exporter.SESSION.get(ExportAddSumRowExtension.KEY_SUMROW_ADD);
    boolean addSumRow = (b != null && b.booleanValue());
    
    // Ob wir den gesamten Verwendungszweck exportieren, entnehmen wir dem Setting "usage.display.all"
    // Heisst: Die Verwendungszwecke werden genau in der Form exportiert, in der sie derzeit auch
    // angezeigt werden. Das erspart diese missverstaendliche Option "Im Verwendungszweck "SVWZ+" extrahieren"
    boolean fullUsage = settings.getBoolean("usage.display.all",true);
    
    Reporter reporter = null;
    
    try
    {
      // Der Export
      String subTitle = i18n.tr("{0} - {1}", new String[]{startDate == null ? "" : HBCI.DATEFORMAT.format(startDate),endDate == null ? "" : HBCI.DATEFORMAT.format(endDate)});
      reporter = new Reporter(os,monitor,i18n.tr("Umsätze") + (filter != null && filter.booleanValue() ? (" (" + i18n.tr("gefiltert") + ")") : ""), subTitle, objects.length  );

      reporter.addHeaderColumn(i18n.tr("Valuta / Buchungsdatum"), Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Empfänger/Einzahler"),    Element.ALIGN_CENTER,100, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Zahlungsgrund"),          Element.ALIGN_CENTER,120, BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Betrag"),                 Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      if (showSaldo)
        reporter.addHeaderColumn(i18n.tr("Saldo"),                  Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      
      // Iteration ueber Umsaetze
      Enumeration konten = umsaetze.keys();
      while (konten.hasMoreElements())
      {
        Double sumRow = 0.0d;

        String id = (String) konten.nextElement();
        Konto konto = (Konto) Settings.getDBService().createObject(Konto.class,id);
        
        PdfPCell cell = reporter.getDetailCell(KontoUtil.toString(konto), Element.ALIGN_CENTER,BaseColor.LIGHT_GRAY);
        cell.setColspan(showSaldo ? 5 : 4);
        reporter.addColumn(cell);

        ArrayList list = (ArrayList) umsaetze.get(id);

        if (list.size() == 0)
        {
          PdfPCell empty = reporter.getDetailCell(i18n.tr("Keine Umsätze"), Element.ALIGN_CENTER,BaseColor.LIGHT_GRAY);
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
          if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
          {
            color = BaseColor.GRAY;
            style = Font.ITALIC;
          }
          
          reporter.addColumn(reporter.getDetailCell(valuta + "\n" + datum, Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getGegenkontoName()) + "\n" + reporter.notNull(u.getArt()), Element.ALIGN_LEFT,null,color,style));
          String verwendungszweck = (fullUsage) ? VerwendungszweckUtil.toString(u,"\n") : VerwendungszweckUtil.getTag(u, Tag.SVWZ);
          reporter.addColumn(reporter.getDetailCell(verwendungszweck, Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
          sumRow += u.getBetrag();
          if (showSaldo)
            reporter.addColumn(reporter.getDetailCell(u.getSaldo()));
          reporter.setNextRecord();
        }
        
        if (addSumRow) {
          reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(i18n.tr("Summe"), Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(sumRow));
          if (showSaldo)
            reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
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
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return ext != null && (ExportAddSumRowExtension.KEY_SUMROW_ADD.equals(ext) || ExportSaldoExtension.KEY_SALDO_HIDE.equals(ext));
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
