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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.ext.ExportAddSumRowExtension;
import de.willuhn.jameica.hbci.gui.ext.ExportSaldoExtension;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basis-Implementierung fuer den Umsatz-Export im PDF-Format.
 * @param <T> der konkrete Typ fuer die Gruppierung.
 */
public abstract class AbstractPDFUmsatzExporter<T extends GenericObject> implements Exporter
{
  private static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    
    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Umsätze aus"));

    Umsatz first = (Umsatz) objects[0];

    Date startDate     = first.getDatum();
    Date endDate       = first.getDatum();
    Map<String,List> umsaetze = new HashMap<String,List>();
    
    if (monitor != null) 
    {
      monitor.setStatusText(i18n.tr("Ermittle zu exportierende Umsätze und Konten"));
      monitor.addPercentComplete(1);
    }
    
    Map<String,T> groupMap = new HashMap<String,T>();

    for (int i=0;i<objects.length;++i)
    {
      Umsatz u = (Umsatz) objects[i];

      // Wir ermitteln bei der Gelegenheit das Maximal- und Minimal-Datum
      Date date = u.getDatum();
      if (date != null)
      {
        if (endDate != null && date.after(endDate))    endDate = date;
        if (startDate != null && date.before(startDate)) startDate = date;
      }

      // Wir gruppieren die Umsaetze nach Kategorie.
      T group = this.getGroup(u);
      String key = group != null ? group.getID() : null;
      groupMap.put(key,group);
      List<Umsatz> list = umsaetze.computeIfAbsent(key, k -> new ArrayList<Umsatz>());
      list.add(u);
    }

    // Falls wir die Datumsfelder als optionale Parameter erhalten haben,
    // nehmen wir die.
    Date d = (Date) Exporter.SESSION.get("pdf.start");
    if (d != null) startDate = d;
    d = (Date) Exporter.SESSION.get("pdf.end");
    if (d != null) endDate = d;
    Boolean filter    = (Boolean) Exporter.SESSION.get("filtered");
    Boolean b         = (Boolean) Exporter.SESSION.get(ExportSaldoExtension.KEY_SALDO_SHOW);
    boolean showSaldo = (b == null || b.booleanValue());
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
      String subTitle = i18n.tr("{0} - {1}", startDate == null ? "" : HBCI.DATEFORMAT.format(startDate), endDate == null ? "" : HBCI.DATEFORMAT.format(endDate));
      reporter = new Reporter(os,monitor,i18n.tr("Umsätze") + (filter != null && filter.booleanValue() ? (" (" + i18n.tr("gefiltert") + ")") : ""), subTitle, objects.length  );

      reporter.addHeaderColumn(i18n.tr("Valuta / Buchungsdatum"), Element.ALIGN_CENTER, 30, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Empfänger/Einzahler"),    Element.ALIGN_CENTER,100, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Zahlungsgrund"),          Element.ALIGN_CENTER,120, Reporter.COLOR_BG);
      reporter.addHeaderColumn(i18n.tr("Betrag"),                 Element.ALIGN_CENTER, 30, Reporter.COLOR_BG);
      if (showSaldo)
        reporter.addHeaderColumn(i18n.tr("Saldo"),                  Element.ALIGN_CENTER, 30, Reporter.COLOR_BG);
      reporter.createHeader();


      Double sumOverall = 0.0d;
      
      // Iteration ueber Umsaetze
      List<T> groupList = new ArrayList(groupMap.values());
      this.sort(groupList);
      for (T group:groupList)
      {
        Double sumRow = 0.0d;
        
        PdfPCell cell = reporter.getDetailCell(this.toString(group), Element.ALIGN_CENTER,Reporter.COLOR_BG);
        cell.setColspan(showSaldo ? 5 : 4);
        reporter.addColumn(cell);

        List<Umsatz> list = umsaetze.get(group != null ? group.getID() : null);

        if (list.size() == 0)
        {
          PdfPCell empty = reporter.getDetailCell(i18n.tr("Keine Umsätze"), Element.ALIGN_CENTER,Reporter.COLOR_BG);
          empty.setColspan(5);
          reporter.addColumn(empty);
          continue;
        }
        
        for (Umsatz u:list)
        {
          String valuta = (u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "" );
          String datum  = (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : "");
          
          BaseColor color = null;
          int style = Font.NORMAL;
          if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
          {
            color = Reporter.COLOR_GRAY;
            style = Font.ITALIC;
          }
          
          String name = u.getGegenkontoName();
          String name2 = u.getGegenkontoName2();
          if (name != null && name.length() > 0 && name2 != null && name2.length() > 0)
            name = name + "\n" + name2;
          
          String iban = u.getGegenkontoNummer();
          if (iban != null && iban.length() > 0)
          {
            name = name + "\n" + HBCIProperties.formatIban(iban);
            String bic = u.getGegenkontoBLZ();
            if (bic != null && bic.length() > 0)
            {
              bic = HBCIProperties.getNameForBank(bic);
              if (bic != null)
                name = name + "\n" + bic;
            }
          }
          
          String art = u.getArt();
          if (art != null && art.length() > 0)
            name = name + "\n" + art;
          
          reporter.addColumn(reporter.getDetailCell(valuta + "\n" + datum, Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(reporter.notNull(name), Element.ALIGN_LEFT,null,color,style));
          String verwendungszweck = (fullUsage) ? VerwendungszweckUtil.toString(u,"\n") : VerwendungszweckUtil.getTag(u, Tag.SVWZ);
          
          final String comment = u.getKommentar();
          if (comment != null && comment.length() > 0)
            verwendungszweck += "\n" + i18n.tr("Notiz") + ": " + comment;
          reporter.addColumn(reporter.getDetailCell(verwendungszweck, Element.ALIGN_LEFT,null,color,style));
          reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
          sumRow += u.getBetrag();
          if (showSaldo)
            reporter.addColumn(reporter.getDetailCell(u.getSaldo()));
          reporter.setNextRecord();
        }
        
        if (addSumRow) {
          reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(i18n.tr("Summe"), Element.ALIGN_LEFT,null,null,Font.BOLD));
          reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(sumRow,null,Font.BOLD));
          if (showSaldo)
            reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
        }
        
        sumOverall += sumRow;
      }
      
      if (addSumRow) {
        reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(i18n.tr("Gesamtsumme"), Element.ALIGN_LEFT,null,null,Font.BOLD));
        reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
        reporter.addColumn(reporter.getDetailCell(sumOverall,null,Font.BOLD));
        if (showSaldo)
          reporter.addColumn(reporter.getDetailCell(null, Element.ALIGN_LEFT));
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
   * Liefert das Objekt, nach dem gruppiert werden soll.
   * @param u der Umsatz.
   * @return das Gruppierungsobjekt. Kann NULL sein.
   * @throws RemoteException
   */
  protected abstract T getGroup(Umsatz u) throws RemoteException;
  
  /**
   * Ermoeglicht die optionale Sortierung der Gruppen vor der Ausgabe.
   * Leere Dummy-Implementierung.
   * @param groups die Gruppen.
   * @throws RemoteException
   */
  protected void sort(List<T> groups) throws RemoteException
  {
  }
  
  /**
   * Liefert eine sprechende Bezeichnung fuer die Gruppe.
   * @param t die Gruppe. Kann NULL sein.
   * @return sprechende Bezeichnung der Gruppe.
   * @throws RemoteException
   */
  protected abstract String toString(T t) throws RemoteException;

  @Override
  public boolean suppportsExtension(String ext)
  {
    return ext != null && (ExportAddSumRowExtension.KEY_SUMROW_ADD.equals(ext) || ExportSaldoExtension.KEY_SALDO_SHOW.equals(ext));
  }

  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null;
    
    IOFormat format = new IOFormat() {
      @Override
      public String getName()
      {
        return AbstractPDFUmsatzExporter.this.getName();
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[]{"pdf"};
      }
    };
    
    return new IOFormat[]{format};
  }

}
