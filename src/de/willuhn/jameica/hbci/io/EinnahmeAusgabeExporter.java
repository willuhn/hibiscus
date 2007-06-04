/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/EinnahmeAusgabeExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/04 15:58:45 $
 * $Author: jost $
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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer die Einnahmen/Ausgaben.
 */
public class EinnahmeAusgabeExporter implements Exporter
{
  private I18N i18n = null;

  public EinnahmeAusgabeExporter()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[],
   *      de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream,
   *      de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (objects == null || !(objects instanceof EinnahmeAusgabe[]))
      throw new ApplicationException(i18n
          .tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    EinnahmeAusgabe[] ea = (EinnahmeAusgabe[]) objects;
    if (ea.length == 0)
      throw new ApplicationException(i18n
          .tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    String subTitle = i18n.tr("Zeitraum {0} - {1}", new String[] {
        HBCI.DATEFORMAT.format(ea[0].startdatum),
        HBCI.DATEFORMAT.format(ea[0].enddatum) });
    Reporter reporter = null;

    try
    {
      reporter = new Reporter(os, monitor, "Einnahmen/Ausgaben", subTitle,
          ea.length);

      reporter.addHeaderColumn("Anfangssaldo", Element.ALIGN_CENTER, 60,
          Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Einnahmen", Element.ALIGN_CENTER, 60,
          Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Ausgaben", Element.ALIGN_CENTER, 60,
          Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Endsaldo", Element.ALIGN_CENTER, 60,
          Color.LIGHT_GRAY);
      reporter.addHeaderColumn("Bemerkung", Element.ALIGN_CENTER, 100,
          Color.LIGHT_GRAY);
      reporter.createHeader();

      // Iteration ueber Umsaetze
      for (int i = 0; i < ea.length; i++)
      {
        reporter.addColumn(reporter.getDetailCell(ea[i].anfangssaldo));
        reporter.addColumn(reporter.getDetailCell(ea[i].einnahme));
        reporter.addColumn(reporter.getDetailCell(ea[i].ausgabe));
        reporter.addColumn(reporter.getDetailCell(ea[i].endsaldo));
        reporter.addColumn(reporter.getDetailCell(ea[i].bemerkung,
            Element.ALIGN_LEFT));
        reporter.setNextRecord();

      }
      if (monitor != null)
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (DocumentException e)
    {
      if (monitor != null)
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("error while creating report", e);
      throw new ApplicationException(i18n
          .tr("Fehler beim Erzeugen des Reports"), e);
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
          Logger.error("unable to close report", e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Wir unterstuetzen nur Umsatz-Trees
    if (!EinnahmeAusgabe.class.equals(objectType))
      return null;

    IOFormat myFormat = new IOFormat()
    {

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
       */
      public String getName()
      {
        return i18n.tr("PDF-Format: Einnahmen/Ausgaben");
      }

      public String[] getFileExtensions()
      {
        return new String[] { "pdf" };
      }

    };
    return new IOFormat[] { myFormat };
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format: Einnahmen/Ausgaben");
  }

}

/*******************************************************************************
 * $Log: EinnahmeAusgabeExporter.java,v $
 * Revision 1.1  2007/06/04 15:58:45  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
