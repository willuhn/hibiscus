/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/UmsatzKategorieKategorieliste.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/29 10:22:11 $
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Liste der Kategorien mit Beträgen im PDF-Format.
 */
public class UmsatzKategorieKategorieliste
{
  private Reporter reporter;

  private I18N i18n = null;

  public UmsatzKategorieKategorieliste(OutputStream out,
      ProgressMonitor monitor, List list, Konto k, Date start, Date end)
      throws DocumentException, IOException, ApplicationException
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();
    String subTitle = i18n.tr("Zeitraum {0} - {1}, {2}", new String[] {
        HBCI.DATEFORMAT.format(start), HBCI.DATEFORMAT.format(end),
        k == null ? "alle Konten" : k.getBezeichnung() });
    reporter = new Reporter(out, monitor, "Umsatzkategorien", subTitle, list
        .size());

    reporter.addHeaderColumn("Kategorie", Element.ALIGN_CENTER, 130,
        Color.LIGHT_GRAY);
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 30,
        Color.LIGHT_GRAY);
    reporter.createHeader();

    // Iteration ueber die Kategorien

    try
    {
      for (int i = 0; i < list.size(); i++)
      {
        UmsatzGroup ug = (UmsatzGroup) list.get(i);

        PdfPCell cell = reporter.getDetailCell(
            (String) ug.getAttribute("name"), Element.ALIGN_LEFT);
        reporter.addColumn(cell);

        reporter.addColumn(reporter.getDetailCell((Double) ug
            .getAttribute("betrag")));
      }
      reporter.close();

    }
    catch (DocumentException e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException(i18n
          .tr("Fehler beim Erzeugen des Reports"), e);
    }

  }

}

/*******************************************************************************
 * $Log: UmsatzKategorieKategorieliste.java,v $
 * Revision 1.1  2007/04/29 10:22:11  jost
 * Neu: PDF-Ausgabe der UmsÃ¤tze nach Kategorien
 *
 ******************************************************************************/
