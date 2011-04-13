/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUmsatzList.java,v $
 * $Revision: 1.1 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBorder;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.NoBreakPrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;
import net.sf.paperclips.TextStyle;

import org.eclipse.swt.graphics.RGB;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von Umsaetzen.
 */
public class PrintSupportUmsatzList extends AbstractPrintSupport
{
  private Object ctx = null;
  
  /**
   * ct.
   * @param ctx Darf vom Typ <code>Umsatz[]</code> oder <code>UmsatzList</code> sein.
   */
  public PrintSupportUmsatzList(Object ctx)
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
    
    if (data instanceof UmsatzList)
      data = ((UmsatzList)data).getSelection();
    
    if (data instanceof Umsatz)
    {
      // Ist nur ein einzelner, dann machen wir ein Array draus
      data = new Umsatz[]{(Umsatz)data};
    }

    if (!(data instanceof Umsatz[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens einen Umsatz aus"));

    try
    {
      Date startDate = null;
      Date endDate   = null;

      // Gruppieren der Umsaetze nach Konto
      Umsatz[] all = (Umsatz[]) data;
      Map<String,List<Umsatz>> groups = new HashMap<String,List<Umsatz>>();
      
      for (Umsatz u:all)
      {
        // Wir ermitteln bei der Gelegenheit das Maximal- und Minimal-Datum
        Date date = u.getDatum();
        if (date != null)
        {
          if (startDate == null || date.before(startDate)) startDate = date;
          if (endDate   == null || date.after(endDate))    endDate = date;
        }

        Konto k = u.getKonto();
        List<Umsatz> list = groups.get(k.getID());
        if (list == null)
        {
          list = new LinkedList<Umsatz>();
          groups.put(k.getID(),list);
        }
        list.add(u);
      }
      
      
      //////////////////////////////////////////////////////////////////////////
      // Header mit dem Zeitraum
      GridPrint grid = new GridPrint("l:d:g");

      grid.add(new TextPrint(i18n.tr("Zeitraum: {0} - {1}",HBCI.DATEFORMAT.format(startDate),HBCI.DATEFORMAT.format(endDate)),fontTinyBold));
      grid.add(new LineBreakPrint(fontTitle));
      //////////////////////////////////////////////////////////////////////////

      DefaultGridLook look = new DefaultGridLook();
      look.setHeaderBackground(new RGB(220,220,220));
      
      LineBorder border = new LineBorder(new RGB(100,100,100));
      border.setGapSize(3);
      look.setCellBorder(border);
      
      GridPrint table = new GridPrint("l:p:n, l:d:n, l:p:g, r:p:n, r:p:n",look);
      table.addHeader(new NoBreakPrint(new TextPrint(i18n.tr("Valuta/Datum"),fontTinyBold)));
      table.addHeader(new TextPrint(i18n.tr("Gegenkonto"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Verwendungszweck"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Betrag"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Saldo"),fontTinyBold));

      // Iteration pro Konto
      Iterator<String> konten = groups.keySet().iterator();
      
      while (konten.hasNext())
      {
        String id = konten.next();
        List<Umsatz> umsaetze = groups.get(id);
        
        // Header mit dem Konto
        Konto k = (Konto) Settings.getDBService().createObject(Konto.class,id);
        
        // TODO: Druck des Kontos fehlt noch
        
        
        // Liste der Umsaetze im Konto
        for (Umsatz u:umsaetze)
        {
          String usage = VerwendungszweckUtil.merge(u.getZweck(),u.getZweck2(),(String)u.getAttribute("zweck3"));
          StringBuffer sb = new StringBuffer();
          {
            String name = u.getGegenkontoName();
            if (name != null && name.length() > 0)
              sb.append(name + "\n");
            
            String kto = u.getGegenkontoNummer();
            String blz = u.getGegenkontoBLZ();
            if (kto != null && kto.length() > 0 && blz != null && blz.length() > 0)
            {
              String gi = HBCIUtils.getNameForBLZ(blz);
              if (gi != null && gi.length() > 0)
                sb.append(i18n.tr("Kto. {0}, {1}",kto,gi));
              else
                sb.append(i18n.tr("Kto. {0}, BLZ {1}",kto,blz));
            }
          }

          TextStyle typeHaben = new TextStyle().font(fontTiny).foreground(new RGB(0,0,0));
          TextStyle typeSoll = new TextStyle().font(fontTiny).foreground(new RGB(200,0,0));

          table.add(new TextPrint(HBCI.DATEFORMAT.format(u.getValuta()) + "\n" + HBCI.DATEFORMAT.format(u.getDatum()),fontTiny));
          table.add(new TextPrint(sb.toString(),fontTiny));
          table.add(new TextPrint(usage,fontTiny));
          table.add(new NoBreakPrint(new TextPrint(HBCI.DECIMALFORMAT.format(u.getBetrag()) + " " + k.getWaehrung(),u.getBetrag() >= 0 ? typeHaben : typeSoll)));
          table.add(new NoBreakPrint(new TextPrint(HBCI.DECIMALFORMAT.format(u.getSaldo()) + " " + k.getWaehrung(),fontTiny)));
        }
      }
      grid.add(table);
      
      return grid;
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
    return i18n.tr("Kontoauszug");
  }
}



/**********************************************************************
 * $Log: PrintSupportUmsatzList.java,v $
 * Revision 1.1  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 **********************************************************************/