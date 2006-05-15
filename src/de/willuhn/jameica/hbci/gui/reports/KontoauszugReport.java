/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/reports/Attic/KontoauszugReport.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/05/15 12:05:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.reports;

import java.rmi.RemoteException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Konto-Auszug.
 */
public class KontoauszugReport extends Report
{
  /**
   * ct.
   * 
   * @param subtitle Titel des Reports.
   */
  public KontoauszugReport(String subtitle)
  {
    super("Kontoauszug", subtitle);
  }

  /**
   * Erstellt den Report.
   * 
   * @param umslist
   * @throws DocumentException
   * @throws RemoteException
   */
  public void generate(DBIterator umslist) throws DocumentException,
      RemoteException
  {
    Umsatz um = null;
    PdfPTable table = new PdfPTable(5);
    float[] widths = { 30, 100, 120, 30, 30 };
    table.setWidths(widths);
    table.setWidthPercentage(100);
    table.setSpacingBefore(10);
    table
        .addCell(getDetailCell("Valuta Buchungs- datum", Element.ALIGN_CENTER));
    table.addCell(getDetailCell("Empfänger/Einzahler", Element.ALIGN_CENTER));
    table.addCell(getDetailCell("Zahlungsgrund", Element.ALIGN_CENTER));
    table.addCell(getDetailCell("Betrag", Element.ALIGN_CENTER));
    table.addCell(getDetailCell("Saldo", Element.ALIGN_CENTER));
    table.setHeaderRows(1);
    while (umslist.hasNext())
    {
      um = (Umsatz) umslist.next();
      table.addCell(getDetailCell(HBCI.DATEFORMAT.format(um.getValuta()) + "\n"
          + HBCI.DATEFORMAT.format(um.getDatum()), Element.ALIGN_LEFT));
      table.addCell(getDetailCell(um.getEmpfaengerName() + "\n" + um.getArt(),
          Element.ALIGN_LEFT));
      table
          .addCell(getDetailCell(um.getZweck() + "\n"
              + (um.getZweck2() == null ? "" : um.getZweck2()),
              Element.ALIGN_LEFT));
      table.addCell(getDetailCell(um.getBetrag()));
      table.addCell(getDetailCell(um.getSaldo()));
    }
    rpt.add(table);
  }
}
/*******************************************************************************
 * $Log: KontoauszugReport.java,v $
 * Revision 1.2  2006/05/15 12:05:22  willuhn
 * @N FileDialog zur Auswahl von Pfad und Datei beim Speichern
 * @N YesNoDialog falls Datei bereits existiert
 * @C KontoImpl#getUmsaetze mit tonumber() statt dateob()
 *
 * Revision 1.1  2006/05/14 19:52:46  jost
 * Prerelease Kontoauszug-Report
 * 
 ******************************************************************************/
