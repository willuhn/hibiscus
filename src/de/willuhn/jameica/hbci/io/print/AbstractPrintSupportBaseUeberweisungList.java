/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/AbstractPrintSupportBaseUeberweisungList.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/11 09:12:07 $
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
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;
import net.sf.paperclips.TextStyle;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Druck-Support fuer eine Liste von Ueberweisungen oder Lastschriften.
 */
public abstract class AbstractPrintSupportBaseUeberweisungList extends AbstractPrintSupport
{
  private Object ctx = null;
  
  /**
   * ct.
   * @param ctx Darf vom Typ <code>BaseUeberweisung[]</code> oder <code>TablePart</code> sein.
   */
  public AbstractPrintSupportBaseUeberweisungList(Object ctx)
  {
    this.ctx = ctx;
  }
  
  /**
   * Liefert den Context.
   * @return der Context.
   */
  Object getContext()
  {
    return this.ctx;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisung#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object data = this.getContext();
    
    if (data instanceof TablePart)
      data = ((TablePart)data).getSelection();
    
    if (!(data instanceof BaseUeberweisung[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens einen Auftrag aus"));

    try
    {
      DefaultGridLook look = new DefaultGridLook();
      look.setHeaderBackground(new RGB(220,220,220));
      
      LineBorder border = new LineBorder(new RGB(100,100,100));
      border.setGapSize(3);
      look.setCellBorder(border);
      
      GridPrint table = new GridPrint("l:p:n, l:d:n, l:d:n, l:p:g, r:p:n, l:p:n",look);
      table.addHeader(new TextPrint(i18n.tr("Datum"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Konto"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Gegenkonto"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Zweck"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Betrag"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Ausgeführt"),fontTinyBold));

      BaseUeberweisung[] list = (BaseUeberweisung[]) data;

      TextStyle typeDone = new TextStyle().font(fontTiny).foreground(new RGB(120,120,120));
      TextStyle typeOpen = new TextStyle().font(fontTiny).foreground(new RGB(0,0,0));

      for (BaseUeberweisung u:list)
      {
        TextStyle style = u.ausgefuehrt() ? typeDone : typeOpen;
        
        Konto k = u.getKonto();
        String usage = VerwendungszweckUtil.toString(u,"\n");
        Date ausgefuehrt = u.getAusfuehrungsdatum();
        
        table.add(new TextPrint(HBCI.DATEFORMAT.format(u.getTermin()),style));
        table.add(new TextPrint(k.getLongName(),style));
        table.add(new TextPrint(i18n.tr("{0}, Kto. {1}, BLZ {2}",u.getGegenkontoName(),u.getGegenkontoNummer(),u.getGegenkontoBLZ()),style));
        table.add(new TextPrint(usage,style));
        table.add(new TextPrint(HBCI.DECIMALFORMAT.format(u.getBetrag()) + " " + k.getWaehrung(),style));
        if (ausgefuehrt != null)
          table.add(new TextPrint(HBCI.DATEFORMAT.format(ausgefuehrt),style));
        else
          table.add(new TextPrint(i18n.tr(u.ausgefuehrt() ? "ja" : "nein"),style));
      }
      return table;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to print data",re);
      throw new ApplicationException(i18n.tr("Druck fehlgeschlagen: {0}",re.getMessage()));
    }
  }
}



/**********************************************************************
 * $Log: AbstractPrintSupportBaseUeberweisungList.java,v $
 * Revision 1.4  2011/05/11 09:12:07  willuhn
 * @C Merge-Funktionen fuer den Verwendungszweck ueberarbeitet
 *
 * Revision 1.3  2011-05-02 11:16:44  willuhn
 * @N Ausfuehrungsdatum mit drucken
 *
 * Revision 1.2  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.1  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.3  2011-04-11 11:28:08  willuhn
 * @N Drucken aus dem Contextmenu heraus
 *
 * Revision 1.2  2011-04-08 17:41:45  willuhn
 * @N Erster Druck-Support fuer Ueberweisungslisten
 *
 * Revision 1.1  2011-04-08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 **********************************************************************/