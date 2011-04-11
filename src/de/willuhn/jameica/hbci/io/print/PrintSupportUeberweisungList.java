/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUeberweisungList.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/04/11 11:28:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;

import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBorder;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;
import net.sf.paperclips.TextStyle;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.UeberweisungList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von Ueberweisungen.
 */
public class PrintSupportUeberweisungList extends PrintSupportUeberweisung
{
  private Object ctx = null;
  
  /**
   * ct.
   * @param ctx Kann vom Typ <code>UeberweisungList</code>, <code>Ueberweisung</code> oder <code>Ueberweisung[]</code> sein.
   */
  public PrintSupportUeberweisungList(Object ctx)
  {
    super();
    this.ctx = ctx;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisung#printContent()
   */
  Print printContent() throws ApplicationException
  {
    if (this.ctx == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Überweisung aus"));
    
    if (ctx instanceof UeberweisungList)
      ctx = ((UeberweisungList)ctx).getSelection();

    // Ist nur ne Einzel-Ueberweisung. Dann drucken wir automatisch die Detail-Ansicht
    if (ctx instanceof Ueberweisung)
    {
      super.setUeberweisung((Ueberweisung)ctx);
      return super.printContent();
    }

    if (!(ctx instanceof Ueberweisung[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Überweisung aus"));

    try
    {
      DefaultGridLook look = new DefaultGridLook();
      look.setHeaderBackground(new RGB(220,220,220));
      
      LineBorder border = new LineBorder(new RGB(100,100,100));
      border.setGapSize(3);
      look.setCellBorder(border);
      
      GridPrint table = new GridPrint("l:p:n, l:d:n, l:d:n, r:p:n, l:p:g, l:p:n",look);
      table.addHeader(new TextPrint(i18n.tr("Datum"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Konto"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Gegenkonto"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Betrag"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Zweck"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Status"),fontTinyBold));

      Ueberweisung[] list = (Ueberweisung[]) ctx;

      TextStyle typeDone = new TextStyle().font(fontTiny).foreground(new RGB(120,120,120));
      TextStyle typeOpen = new TextStyle().font(fontTiny).foreground(new RGB(0,0,0));

      for (Ueberweisung u:list)
      {
        TextStyle style = u.ausgefuehrt() ? typeDone : typeOpen;
        
        Konto k = u.getKonto();
        String usage = VerwendungszweckUtil.merge(u.getZweck(),u.getZweck2(),(String)u.getAttribute("zweck3"));
        
        table.add(new TextPrint(HBCI.DATEFORMAT.format(u.getTermin()),style));
        table.add(new TextPrint(k.getLongName(),style));
        table.add(new TextPrint(i18n.tr("{0}, Kto. {1}, BLZ {2}",u.getGegenkontoName(),u.getGegenkontoNummer(),u.getGegenkontoBLZ()),style));
        table.add(new TextPrint(HBCI.DECIMALFORMAT.format(u.getBetrag()) + " " + k.getWaehrung(),style));
        table.add(new TextPrint(usage,style));
        table.add(new TextPrint(i18n.tr(u.ausgefuehrt() ? "ausgeführt" : "offen"),style));
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
 * $Log: PrintSupportUeberweisungList.java,v $
 * Revision 1.3  2011/04/11 11:28:08  willuhn
 * @N Drucken aus dem Contextmenu heraus
 *
 * Revision 1.2  2011-04-08 17:41:45  willuhn
 * @N Erster Druck-Support fuer Ueberweisungslisten
 *
 * Revision 1.1  2011-04-08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 **********************************************************************/