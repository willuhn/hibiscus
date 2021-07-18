/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBorder;
import net.sf.paperclips.PagePrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;
import net.sf.paperclips.TextStyle;

/**
 * Abstrakter Druck-Support fuer eine Liste von SEPA-Ueberweisungen oder -Lastschriften.
 */
public abstract class AbstractPrintSupportSepaTransferList extends AbstractPrintSupport
{
  private Object ctx = null;

  /**
   * ct.
   * @param ctx Darf vom Typ <code>BaseUeberweisung[]</code> oder <code>TablePart</code> sein.
   */
  public AbstractPrintSupportSepaTransferList(Object ctx)
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
        table.add(new TextPrint(i18n.tr("{0}\nIBAN {1}, BIC {2} ({3})",u.getGegenkontoName(),HBCIProperties.formatIban(u.getGegenkontoNummer()),u.getGegenkontoBLZ(),HBCIProperties.getNameForBank(u.getGegenkontoBLZ())),style));
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

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#customize(net.sf.paperclips.PagePrint)
   */
  void customize(PagePrint page) throws ApplicationException
  {
    Object ctx = this.getContext();

    // Sind wir in der Tabelle?
    if (ctx instanceof TablePart)
      ctx = ((TablePart)ctx).getSelection();

    // Ist nur ein Einzel-Auftrag. Dann drucken wir keine Seiten-Nummern.
    if (ctx instanceof Transfer)
      page.setFooter(null);
  }
}
