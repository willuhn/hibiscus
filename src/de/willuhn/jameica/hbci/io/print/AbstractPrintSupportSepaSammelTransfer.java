/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBorder;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Druck-Support fuer SEPA-Sammelauftraege.
 * @param <T> der konkrete Typ des SEPA-Sammelauftrages.
 */
public abstract class AbstractPrintSupportSepaSammelTransfer<T extends SepaSammelTransfer> extends AbstractPrintSupport
{
  private Object ctx = null;
  
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public AbstractPrintSupportSepaSammelTransfer(Object ctx)
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
        throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Auftrag aus"));
    
    if (data instanceof TablePart)
      data = ((TablePart)data).getSelection();
    
    if (!(data instanceof SepaSammelTransfer))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Auftrag aus"));

    try
    {
      T a = (T) data;
      Konto k = a.getKonto();
      
      // Das Haupt-Layout
      GridPrint grid = new GridPrint("l:d:g");

      // Die eigentlich Tabelle mit den Werten
      GridPrint table = this.createTransferTable(a);
      grid.add(table); // Zum Haupt-Layout hinzufuegen

      // Leerzeile
      grid.add(new LineBreakPrint(fontNormal));
      grid.add(new LineBreakPrint(fontNormal));

      // Liste der Buchungen
      grid.add(new TextPrint(i18n.tr("Enthaltene Buchungen"),fontBold));

      // Leerzeile
      grid.add(new LineBreakPrint(fontNormal));

      List<SepaSammelTransferBuchung> buchungen = a.getBuchungen();
      if (buchungen.size() > 0)
      {
        DefaultGridLook look = new DefaultGridLook();
        look.setHeaderBackground(new RGB(220,220,220));
        
        LineBorder border = new LineBorder(new RGB(100,100,100));
        border.setGapSize(3);
        look.setCellBorder(border);
        
        GridPrint children = new GridPrint("r:d:n, l:d:n, l:p:g, r:p:n",look);
        children.addHeader(new TextPrint(i18n.tr("Nr."),fontTinyBold));
        children.addHeader(new TextPrint(i18n.tr("Gegenkonto"),fontTinyBold));
        children.addHeader(new TextPrint(i18n.tr("Zweck"),fontTinyBold));
        children.addHeader(new TextPrint(i18n.tr("Betrag"),fontTinyBold));

        int count = 0;
        for (SepaSammelTransferBuchung b:buchungen)
        {
          String usage = VerwendungszweckUtil.toString(b,"\n");

          children.add(new TextPrint(Integer.toString(++count),fontTiny));
          children.add(new TextPrint(i18n.tr("{0}, IBAN. {1}, BIC {2}",b.getGegenkontoName(),b.getGegenkontoNummer(),b.getGegenkontoBLZ()),fontTiny));
          children.add(new TextPrint(usage,fontTiny));
          children.add(new TextPrint(HBCI.DECIMALFORMAT.format(b.getBetrag()) + " " + k.getWaehrung(),fontTiny));
        }
        grid.add(children); // Zum Haupt-Layout hinzufuegen
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
  
  /**
   * Kann zum Customizen in abgeleiteten Klassen ueberschrieben werden.
   * @param a der Auftrag.
   * @throws RemoteException
   * @throws ApplicationException
   */
  GridPrint createTransferTable(T a) throws RemoteException, ApplicationException
  {
    Konto k = a.getKonto();
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
    table.add(new TextPrint(i18n.tr("F�llig am"),fontNormal));
    table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),fontNormal));

    // Summe
    table.add(new TextPrint(i18n.tr("Summe"),fontNormal));
    table.add(new TextPrint(HBCI.DECIMALFORMAT.format(a.getSumme()) + " " + k.getWaehrung(),fontBold));

    // Ausfuehrungsstatus
    Date ausgefuehrt = a.getAusfuehrungsdatum();
    table.add(new TextPrint(i18n.tr("Ausgef�hrt"),fontNormal));
    if (ausgefuehrt != null)
      table.add(new TextPrint(HBCI.DATEFORMAT.format(ausgefuehrt),fontBold));
    else
      table.add(new TextPrint(a.ausgefuehrt() ? "Ja" : "Nein",fontBold));
    
    return table;
  }
  
}
