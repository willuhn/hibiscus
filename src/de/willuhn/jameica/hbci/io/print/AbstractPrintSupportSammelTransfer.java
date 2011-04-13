/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/AbstractPrintSupportSammelTransfer.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/13 17:35:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;
import java.util.Date;

import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBorder;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Druck-Support fuer Sammel-Auftraege.
 */
public abstract class AbstractPrintSupportSammelTransfer extends AbstractPrintSupport
{
  private Object ctx = null;
  
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public AbstractPrintSupportSammelTransfer(Object ctx)
  {
    this.ctx = ctx;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object data = this.ctx;
    
    if (data == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Auftrag aus"));
    
    if (data instanceof TablePart)
      data = ((TablePart)data).getSelection();
    
    if (!(data instanceof SammelTransfer))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Auftrag aus"));

    try
    {
      SammelTransfer a = (SammelTransfer) data;
      Konto k          = a.getKonto();
      
      // Das Haupt-Layout
      GridPrint grid = new GridPrint("l:d:g");

      // Die eigentlich Tabelle mit den Werten
      {
        DefaultGridLook look = new DefaultGridLook(5,5);
        GridPrint table = new GridPrint("l:p:n, l:d:g",look);

        // Bezeichnung
        table.add(new TextPrint(i18n.tr("Bezeichnung"),fontNormal));
        table.add(new TextPrint(notNull(a.getBezeichnung()),fontBold));

        // Konto
        table.add(new TextPrint(i18n.tr("Konto"),fontNormal));
        table.add(new TextPrint(notNull(k != null ? k.getLongName() : null),fontNormal));

        // Termin
        Date termin = a.getTermin();
        table.add(new TextPrint(i18n.tr("Termin"),fontNormal));
        table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),fontNormal));

        // Summe
        table.add(new TextPrint(i18n.tr("Summe"),fontNormal));
        table.add(new TextPrint(HBCI.DECIMALFORMAT.format(a.getSumme()) + " " + k.getWaehrung(),fontBold));

        // Ausfuehrungsstatus
        table.add(new TextPrint(i18n.tr("Ausgführt"),fontNormal));
        table.add(new TextPrint(a.ausgefuehrt() ? "Ja" : "Nein",fontBold));

        grid.add(table); // Zum Haupt-Layout hinzufuegen
      }

      // Leerzeile
      grid.add(new LineBreakPrint(fontNormal));
      grid.add(new LineBreakPrint(fontNormal));

      // Liste der Buchungen
      grid.add(new TextPrint(i18n.tr("Enthaltene Buchungen"),fontBold));

      // Leerzeile
      grid.add(new LineBreakPrint(fontNormal));

      DBIterator buchungen = a.getBuchungen();
      if (buchungen.size() > 0)
      {
        DefaultGridLook look = new DefaultGridLook();
        look.setHeaderBackground(new RGB(220,220,220));
        
        LineBorder border = new LineBorder(new RGB(100,100,100));
        border.setGapSize(3);
        look.setCellBorder(border);
        
        GridPrint table = new GridPrint("r:d:n, l:d:n, l:p:g, l:p:n, r:p:n",look);
        table.addHeader(new TextPrint(i18n.tr("Nr."),fontTinyBold));
        table.addHeader(new TextPrint(i18n.tr("Gegenkonto"),fontTinyBold));
        table.addHeader(new TextPrint(i18n.tr("Zweck"),fontTinyBold));
        table.addHeader(new TextPrint(i18n.tr("Typ"),fontTinyBold));
        table.addHeader(new TextPrint(i18n.tr("Betrag"),fontTinyBold));

        int count = 0;
        while (buchungen.hasNext())
        {
          SammelTransferBuchung b = (SammelTransferBuchung) buchungen.next();
          String usage = VerwendungszweckUtil.merge(b.getZweck(),b.getZweck2(),(String)b.getAttribute("zweck3"));

          table.add(new TextPrint(Integer.toString(++count),fontTiny));
          table.add(new TextPrint(i18n.tr("{0}, Kto. {1}, BLZ {2}",b.getGegenkontoName(),b.getGegenkontoNummer(),b.getGegenkontoBLZ()),fontTiny));
          table.add(new TextPrint(usage,fontTiny));
          table.add(new TextPrint(notNull(TextSchluessel.get(b.getTextSchluessel())),fontTiny));
          table.add(new TextPrint(HBCI.DECIMALFORMAT.format(b.getBetrag()) + " " + k.getWaehrung(),fontTiny));
        }
        grid.add(table); // Zum Haupt-Layout hinzufuegen
      }
      else
      {
        grid.add(new TextPrint("- " + i18n.tr("keine") + " -",fontTiny));
      }

      return grid;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to print data",re);
      throw new ApplicationException(i18n.tr("Druck fehlgeschlagen: {0}",re.getMessage()));
    }
  }
}



/**********************************************************************
 * $Log: AbstractPrintSupportSammelTransfer.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 **********************************************************************/