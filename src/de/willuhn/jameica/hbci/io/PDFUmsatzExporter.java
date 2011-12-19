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

import java.awt.Color;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
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
    Boolean filter = (Boolean) Exporter.SESSION.get("filtered");
    
    Reporter reporter = null;
    
    try
    {
      // Der Export
      String subTitle = i18n.tr("{0} - {1}", new String[]{startDate == null ? "" : HBCI.DATEFORMAT.format(startDate),endDate == null ? "" : HBCI.DATEFORMAT.format(endDate)});
      reporter = new Reporter(os,monitor,i18n.tr("Umsätze") + (filter != null && filter.booleanValue() ? (" (" + i18n.tr("gefiltert") + ")") : ""), subTitle, objects.length  );

      reporter.addHeaderColumn(i18n.tr("Valuta / Buchungsdatum"), Element.ALIGN_CENTER, 30, Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Empfänger/Einzahler"),    Element.ALIGN_CENTER,100, Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Zahlungsgrund"),          Element.ALIGN_CENTER,120, Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Betrag"),                 Element.ALIGN_CENTER, 30, Color.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Saldo"),                  Element.ALIGN_CENTER, 30, Color.LIGHT_GRAY);
      reporter.createHeader();

      
      // Iteration ueber Umsaetze
      Enumeration konten = umsaetze.keys();
      while (konten.hasMoreElements())
      {
        String id = (String) konten.nextElement();
        Konto konto = (Konto) Settings.getDBService().createObject(Konto.class,id);
        
        PdfPCell cell = reporter.getDetailCell(konto.getLongName(), Element.ALIGN_CENTER,Color.LIGHT_GRAY);
        cell.setColspan(5);
        reporter.addColumn(cell);

        ArrayList list = (ArrayList) umsaetze.get(id);

        if (list.size() == 0)
        {
          PdfPCell empty = reporter.getDetailCell(i18n.tr("Keine Umsätze"), Element.ALIGN_CENTER,Color.LIGHT_GRAY);
          empty.setColspan(5);
          reporter.addColumn(empty);
          continue;
        }
        
        for (int i=0;i<list.size();++i)
        {

          u = (Umsatz)list.get(i);
          reporter.addColumn(reporter.getDetailCell((u.getValuta() != null ? HBCI.DATEFORMAT.format(u.getValuta()) : "" ) + "\n"
                                    + (u.getDatum() != null ? HBCI.DATEFORMAT.format(u.getDatum()) : ""), Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(reporter.notNull(u.getGegenkontoName()) + "\n"
                                    + reporter.notNull(u.getArt()), Element.ALIGN_LEFT));

          reporter.addColumn(reporter.getDetailCell(VerwendungszweckUtil.toString(u,"\n"), Element.ALIGN_LEFT));
          reporter.addColumn(reporter.getDetailCell(u.getBetrag()));
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


/*********************************************************************
 * $Log: PDFUmsatzExporter.java,v $
 * Revision 1.15  2011/12/19 22:43:04  willuhn
 * @N In PDF-Export anzeigen, wenn die Daten gefiltert sind - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=80257#80257
 *
 * Revision 1.14  2011/12/19 22:25:42  willuhn
 * @C Ueberschrift geaendert in "Umsaetze" - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=80257#80257
 *
 * Revision 1.13  2011-06-07 10:07:50  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.12  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 * Revision 1.11  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.10  2007/08/09 10:19:54  willuhn
 * @N Kommentar eines Umsatzes mit exportieren, falls vorhanden. Siehe auch BUGZILLA #445
 *
 * Revision 1.9  2007/05/02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 * Revision 1.8  2007/04/29 10:21:20  jost
 * Umstellung auf neue Reporter-Klasse
 *
 * Revision 1.7  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.6  2006/10/22 19:52:07  jost
 * Bug #301
 *
 * Revision 1.5  2006/10/16 22:53:54  willuhn
 * @N noch Checks auf Nullpointer hinzugefuegt
 *
 * Revision 1.4  2006/10/16 17:34:08  jost
 * Nochmals Randkorrektur
 *
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