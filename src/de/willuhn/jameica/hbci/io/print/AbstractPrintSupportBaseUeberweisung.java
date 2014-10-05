/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;
import java.util.Date;

import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.EmptyPrint;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.PagePrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Druck-Support fuer einzelne Ueberweisungen und Lastschriften.
 */
public abstract class AbstractPrintSupportBaseUeberweisung extends AbstractPrintSupport
{
  private BaseUeberweisung auftrag = null;
  
  /**
   * ct.
   * @param a der zu druckende Auftrag.
   */
  public AbstractPrintSupportBaseUeberweisung(BaseUeberweisung a)
  {
    this.auftrag = a;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#printContent()
   */
  Print printContent() throws ApplicationException
  {
    if (this.auftrag == null)
        throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Auftrag aus"));
    
    try
    {
      BaseUeberweisung a = this.auftrag;
      Konto k            = a.getKonto();
      
      // Die eigentlich Tabelle mit den Werten
      DefaultGridLook look = new DefaultGridLook(5,5);
      GridPrint table = new GridPrint("l:p:n, l:d:g",look);

      // Konto
      table.add(new TextPrint(i18n.tr("Konto"),fontNormal));
      table.add(new TextPrint(notNull(k != null ? k.getLongName() : null),fontNormal));
      
      // Leerzeile
      table.add(new LineBreakPrint(fontNormal));
      table.add(new LineBreakPrint(fontNormal));
      
      // Empfaenger
      {
        String blz = a.getGegenkontoBLZ();
        
        table.add(new TextPrint(i18n.tr("Gegenkonto"),fontNormal));
        table.add(new TextPrint(notNull(a.getGegenkontoName()),fontBold));
        table.add(new EmptyPrint());
        if (blz != null && blz.length() > 0)
          table.add(new TextPrint(i18n.tr("{0} [BLZ: {1}]\nKonto: {2}",notNull(HBCIProperties.getNameForBank(blz)),blz,notNull(a.getGegenkontoNummer())),fontNormal));
        else
          table.add(new EmptyPrint());
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontTiny));
      table.add(new LineBreakPrint(fontTiny));
      
      // Verwendungszweck
      {
        table.add(new TextPrint(i18n.tr("Verwendungszweck"),fontNormal));
        table.add(new TextPrint(VerwendungszweckUtil.toString(a,"\n"),fontNormal));
      }

      // Betrag
      {
        double betrag = a.getBetrag();
        String curr = k != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE;
        
        table.add(new TextPrint(i18n.tr("Betrag"),fontNormal));
        table.add(new TextPrint(betrag == 0.0d || Double.isNaN(betrag) ? "-" : (HBCI.DECIMALFORMAT.format(betrag) + " " + curr),fontBold));
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontTiny));
      table.add(new LineBreakPrint(fontTiny));
      
      // Der Rest
      {
        table.add(new TextPrint(i18n.tr("Textschl�ssel"),fontNormal));
        table.add(new TextPrint(notNull(TextSchluessel.get(a.getTextSchluessel())),fontNormal));
        
        // Leerzeile
        table.add(new LineBreakPrint(fontTiny));
        table.add(new LineBreakPrint(fontTiny));
        
        Date termin = a.getTermin();
        table.add(new TextPrint(i18n.tr("F�llig am"),fontNormal));
        table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),fontNormal));
        
        Date ausgefuehrt = a.getAusfuehrungsdatum();
        table.add(new TextPrint(i18n.tr("Ausgef�hrt"),fontNormal));
        if (ausgefuehrt != null)
          table.add(new TextPrint(HBCI.DATEFORMAT.format(ausgefuehrt),fontBold));
        else
          table.add(new TextPrint(a.ausgefuehrt() ? "Ja" : "Nein",fontBold));
        
        customize(table);
      } 
      return table;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to print data",re);
      throw new ApplicationException(i18n.tr("Druck fehlgeschlagen: {0}",re.getMessage()));
    }
  }
  
  /**
   * Kann ueberschrieben werden, um noch Anpassungen vorzunehmen.
   * @param grid das Grid.
   * @throws RemoteException
   * @throws ApplicationException
   */
  void customize(GridPrint grid) throws RemoteException, ApplicationException
  {
    
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#customize(net.sf.paperclips.PagePrint)
   */
  void customize(PagePrint page) throws ApplicationException
  {
    // Footer mit den Seitenzahlen entfernen. Macht bei einer Einzel-Ueberweisung keinen Sinn.
    page.setFooter(null);
  }
}
