/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportDauerauftrag.java,v $
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

import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer Dauerauftraege.
 */
public class PrintSupportDauerauftrag extends AbstractPrintSupport
{
  private Object ctx = null;
  
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public PrintSupportDauerauftrag(Object ctx)
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
    
    if (!(data instanceof Dauerauftrag))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Auftrag aus"));
    
    try
    {
      Dauerauftrag a = (Dauerauftrag) data;
      Konto k        = a.getKonto();
      
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
        
        Date first = a.getErsteZahlung();
        table.add(new TextPrint(i18n.tr("Erste Zahlung"),fontNormal));
        table.add(new TextPrint(first == null ? "-" : HBCI.DATEFORMAT.format(first),fontNormal));

        Date last = a.getLetzteZahlung();
        table.add(new TextPrint(i18n.tr("Letzte Zahlung"),fontNormal));
        table.add(new TextPrint(last == null ? "-" : HBCI.DATEFORMAT.format(last),fontNormal));

        Date next = a.getNaechsteZahlung();
        table.add(new TextPrint(i18n.tr("Nächste Zahlung"),fontNormal));
        table.add(new TextPrint(next == null ? "-" : HBCI.DATEFORMAT.format(next),fontNormal));

        Turnus turnus = a.getTurnus();
        table.add(new TextPrint(i18n.tr("Turnus"),fontNormal));
        table.add(new TextPrint(turnus == null ? "-" : turnus.getBezeichnung(),fontBold));

        // Leerzeile
        table.add(new LineBreakPrint(fontNormal));
        table.add(new LineBreakPrint(fontNormal));

        table.add(new TextPrint(i18n.tr("Aktiv"),fontNormal));
        table.add(new TextPrint(a.isActive() ? "Ja" : "Nein",fontBold));
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
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Dauerauftrag");
  }
}



/**********************************************************************
 * $Log: PrintSupportDauerauftrag.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 **********************************************************************/