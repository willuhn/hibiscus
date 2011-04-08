/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUeberweisung.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/08 13:38:43 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.util.Font;
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
  /**
   * ct.
   * @param data die zu druckenden Daten.
   */
  public PrintSupportUeberweisung(Object data)
  {
    super(data);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object data = this.getData();
    
    if (!(data instanceof Ueberweisung))
        throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Überweisung aus"));
    
    try
    {
      Ueberweisung u = (Ueberweisung) data;
      Konto k        = u.getKonto();
      
      FontData h1     = Font.BOLD.getSWTFont().getFontData()[0];
      FontData normal = Font.SMALL.getSWTFont().getFontData()[0];
      FontData bold   = new FontData(normal.getName(),normal.getHeight(),SWT.BOLD);
      
      // Das Haupt-Layout
      GridPrint grid = new GridPrint("l:d:g");
      grid.add(new TextPrint(i18n.tr("Überweisung"),h1));
      grid.add(new LinePrint());
      grid.add(new LineBreakPrint(h1));

      // Die eigentlich Tabelle mit den Werten
      {
        DefaultGridLook look = new DefaultGridLook(5,5);
        GridPrint table = new GridPrint("l:p:n, l:d:g",look);

        // Konto
        table.add(new TextPrint(i18n.tr("Konto"),normal));
        table.add(new TextPrint(notNull(k != null ? k.getLongName() : null),normal));
        
        // Leerzeile
        table.add(new LineBreakPrint(normal));
        table.add(new LineBreakPrint(normal));
        
        // Empfaenger
        {
          String blz = u.getGegenkontoBLZ();
          
          table.add(new TextPrint(i18n.tr("Empfänger"),normal));
          table.add(new TextPrint(notNull(u.getGegenkontoName()),bold));
          table.add(new EmptyPrint());
          if (blz != null && blz.length() > 0)
            table.add(new TextPrint(i18n.tr("{0}, Kto. {1} [BLZ: {2}]",notNull(HBCIUtils.getNameForBLZ(blz)),notNull(u.getGegenkontoNummer()),blz),normal));
          else
            table.add(new EmptyPrint());
        }

        // Leerzeile
        table.add(new LineBreakPrint(normal));
        table.add(new LineBreakPrint(normal));
        
        // Verwendungszweck
        {
          String usage = VerwendungszweckUtil.merge(u.getZweck(),u.getZweck2(),(String)u.getAttribute("zweck3"));
          table.add(new TextPrint(i18n.tr("Verwendungszweck"),normal));
          table.add(new TextPrint(notNull(usage),normal));
        }

        // Leerzeile
        table.add(new LineBreakPrint(normal));
        table.add(new LineBreakPrint(normal));
        
        // Betrag
        {
          double betrag = u.getBetrag();
          String curr = k != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE;
          
          table.add(new TextPrint(i18n.tr("Betrag"),normal));
          table.add(new TextPrint(betrag == 0.0d || Double.isNaN(betrag) ? "-" : (HBCI.DECIMALFORMAT.format(betrag) + " " + curr),bold));
        }

        // Leerzeile
        table.add(new LineBreakPrint(normal));
        table.add(new LineBreakPrint(normal));
        
        // Der Rest
        {
          table.add(new TextPrint(i18n.tr("Textschlüssel"),normal));
          table.add(new TextPrint(notNull(TextSchluessel.get(u.getTextSchluessel())),normal));
          
          String typ = i18n.tr("Überweisung");
          if (u.isTerminUeberweisung())
            typ = "Termin-Überweisung";
          else if (u.isUmbuchung())
            typ = "Umbuchung";
          table.add(new TextPrint(i18n.tr("Auftragstyp"),normal));
          table.add(new TextPrint(typ,normal));
          
          Date termin = u.getTermin();
          table.add(new TextPrint(i18n.tr("Termin"),normal));
          table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),normal));

          // Leerzeile
          table.add(new LineBreakPrint(normal));
          table.add(new LineBreakPrint(normal));

          table.add(new TextPrint(i18n.tr("Ausgführt"),normal));
          table.add(new TextPrint(u.ausgefuehrt() ? "Ja" : "Nein",bold));
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
   * Liefert den Wert oder "-" wenn er NULL/leer ist.
   * @param value der Wert.
   * @return der Wert des Attributes.
   * @throws RemoteException
   */
  private String notNull(Object value) throws RemoteException
  {
    String empty = "-";
    
    if (value == null)
      return empty;
   
    String s = value.toString();
    return (s != null && s.trim().length() > 0) ? s : empty;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getName()
   */
  String getName() throws ApplicationException
  {
    Object data = this.getData();
    
    if (data != null && (data instanceof Ueberweisung))
    {
      Ueberweisung u = (Ueberweisung) data;
      try
      {
        String name = u.getGegenkontoName();
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
 * Revision 1.2  2011/04/08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 * Revision 1.1  2011-04-07 17:29:19  willuhn
 * @N Test-Code fuer Druck-Support
 *
 **********************************************************************/