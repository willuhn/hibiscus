/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/AbstractPrintSupportBaseUeberweisung.java,v $
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
import net.sf.paperclips.EmptyPrint;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;

import org.kapott.hbci.manager.HBCIUtils;

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
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Auftrag aus"));
    
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
          table.add(new TextPrint(i18n.tr("{0}, Kto. {1} [BLZ: {2}]",notNull(HBCIUtils.getNameForBLZ(blz)),notNull(a.getGegenkontoNummer()),blz),fontNormal));
        else
          table.add(new EmptyPrint());
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontNormal));
      table.add(new LineBreakPrint(fontNormal));
      
      // Verwendungszweck
      {
        String usage = VerwendungszweckUtil.merge(a.getZweck(),a.getZweck2(),(String)a.getAttribute("zweck3"));
        table.add(new TextPrint(i18n.tr("Verwendungszweck"),fontNormal));
        table.add(new TextPrint(notNull(usage),fontNormal));
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontNormal));
      table.add(new LineBreakPrint(fontNormal));
      
      // Betrag
      {
        double betrag = a.getBetrag();
        String curr = k != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE;
        
        table.add(new TextPrint(i18n.tr("Betrag"),fontNormal));
        table.add(new TextPrint(betrag == 0.0d || Double.isNaN(betrag) ? "-" : (HBCI.DECIMALFORMAT.format(betrag) + " " + curr),fontBold));
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontNormal));
      table.add(new LineBreakPrint(fontNormal));
      
      // Der Rest
      {
        table.add(new TextPrint(i18n.tr("Textschlüssel"),fontNormal));
        table.add(new TextPrint(notNull(TextSchluessel.get(a.getTextSchluessel())),fontNormal));
        
        Date termin = a.getTermin();
        table.add(new TextPrint(i18n.tr("Termin"),fontNormal));
        table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),fontNormal));

        // Leerzeile
        table.add(new LineBreakPrint(fontNormal));
        table.add(new LineBreakPrint(fontNormal));

        table.add(new TextPrint(i18n.tr("Ausgführt"),fontNormal));
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
}



/**********************************************************************
 * $Log: AbstractPrintSupportBaseUeberweisung.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.4  2011-04-11 11:28:08  willuhn
 * @N Drucken aus dem Contextmenu heraus
 *
 * Revision 1.3  2011-04-08 17:41:45  willuhn
 * @N Erster Druck-Support fuer Ueberweisungslisten
 *
 * Revision 1.2  2011-04-08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 * Revision 1.1  2011-04-07 17:29:19  willuhn
 * @N Test-Code fuer Druck-Support
 *
 **********************************************************************/