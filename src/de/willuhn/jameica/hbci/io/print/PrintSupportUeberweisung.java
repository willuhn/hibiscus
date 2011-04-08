/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUeberweisung.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/04/08 17:41:45 $
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
import net.sf.paperclips.LinePrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer einzelne Ueberweisungen.
 */
public class PrintSupportUeberweisung extends AbstractPrintSupport
{
  private Ueberweisung ueberweisung = null;
  
  /**
   * ct.
   */
  public PrintSupportUeberweisung()
  {
  }

  /**
   * ct.
   * @param u die zu druckende Ueberweisung.
   */
  public PrintSupportUeberweisung(Ueberweisung u)
  {
    this.ueberweisung = u;
  }
  
  /**
   * Legt die zu druckende Ueberweisung fest.
   * @param u die zu druckende Ueberweisung.
   */
  void setUeberweisung(Ueberweisung u)
  {
    this.ueberweisung = u;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#printContent()
   */
  Print printContent() throws ApplicationException
  {
    if (this.ueberweisung == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Überweisung aus"));
    
    try
    {
      Ueberweisung u = this.ueberweisung;
      Konto k        = u.getKonto();
      
      // Das Haupt-Layout
      GridPrint grid = new GridPrint("l:d:g");
      grid.add(new TextPrint(i18n.tr("Überweisung"),fontTitle));
      grid.add(new LinePrint());
      grid.add(new LineBreakPrint(fontTitle));

      // Die eigentlich Tabelle mit den Werten
      {
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
          String blz = u.getGegenkontoBLZ();
          
          table.add(new TextPrint(i18n.tr("Empfänger"),fontNormal));
          table.add(new TextPrint(notNull(u.getGegenkontoName()),fontBold));
          table.add(new EmptyPrint());
          if (blz != null && blz.length() > 0)
            table.add(new TextPrint(i18n.tr("{0}, Kto. {1} [BLZ: {2}]",notNull(HBCIUtils.getNameForBLZ(blz)),notNull(u.getGegenkontoNummer()),blz),fontNormal));
          else
            table.add(new EmptyPrint());
        }

        // Leerzeile
        table.add(new LineBreakPrint(fontNormal));
        table.add(new LineBreakPrint(fontNormal));
        
        // Verwendungszweck
        {
          String usage = VerwendungszweckUtil.merge(u.getZweck(),u.getZweck2(),(String)u.getAttribute("zweck3"));
          table.add(new TextPrint(i18n.tr("Verwendungszweck"),fontNormal));
          table.add(new TextPrint(notNull(usage),fontNormal));
        }

        // Leerzeile
        table.add(new LineBreakPrint(fontNormal));
        table.add(new LineBreakPrint(fontNormal));
        
        // Betrag
        {
          double betrag = u.getBetrag();
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
          table.add(new TextPrint(notNull(TextSchluessel.get(u.getTextSchluessel())),fontNormal));
          
          String typ = i18n.tr("Überweisung");
          if (u.isTerminUeberweisung())
            typ = "Termin-Überweisung";
          else if (u.isUmbuchung())
            typ = "Umbuchung";
          table.add(new TextPrint(i18n.tr("Auftragstyp"),fontNormal));
          table.add(new TextPrint(typ,fontNormal));
          
          Date termin = u.getTermin();
          table.add(new TextPrint(i18n.tr("Termin"),fontNormal));
          table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),fontNormal));

          // Leerzeile
          table.add(new LineBreakPrint(fontNormal));
          table.add(new LineBreakPrint(fontNormal));

          table.add(new TextPrint(i18n.tr("Ausgführt"),fontNormal));
          table.add(new TextPrint(u.ausgefuehrt() ? "Ja" : "Nein",fontBold));
        } 
        
        grid.add(table); // Zum Haupt-Layout hinzufuegen
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
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getName()
   */
  String getName() throws ApplicationException
  {
    if (this.ueberweisung != null)
    {
      try
      {
        String name = this.ueberweisung.getGegenkontoName();
        if (name == null || name.length() == 0)
          return i18n.tr("Neue Überweisung");
        return i18n.tr("Überweisung an {0}",name);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine name",re);
      }
    }
    
    return super.getName();
  }
}



/**********************************************************************
 * $Log: PrintSupportUeberweisung.java,v $
 * Revision 1.3  2011/04/08 17:41:45  willuhn
 * @N Erster Druck-Support fuer Ueberweisungslisten
 *
 * Revision 1.2  2011-04-08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 * Revision 1.1  2011-04-07 17:29:19  willuhn
 * @N Test-Code fuer Druck-Support
 *
 **********************************************************************/