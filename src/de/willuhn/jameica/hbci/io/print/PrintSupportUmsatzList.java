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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBorder;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.NoBreakPrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;
import net.sf.paperclips.TextStyle;

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
      
      GridPrint table = new GridPrint("l:46pt:n, r:24pt:n, l:p:g, r:50pt:n, r:50pt:n",look);
      table.addHeader(new TextPrint(i18n.tr("Valuta\nDatum"),fontTinyBold));
      table.addHeader(new TextPrint("Nr.",fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Gegenkonto/Zweck"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Betrag"),fontTinyBold));
      table.addHeader(new TextPrint(i18n.tr("Saldo"),fontTinyBold));

      // Iteration pro Konto
      for (String id : groups.keySet())
      {
        List<Umsatz> umsaetze = groups.get(id);
        
        // Header mit dem Konto
        Konto k = (Konto) Settings.getDBService().createObject(Konto.class,id);
        table.add(new TextPrint(k.getLongName(),fontTinyBold),GridPrint.REMAINDER);
        
        
        // Liste der Umsaetze im Konto
        for (Umsatz u : umsaetze)
        {
          StringBuffer sb = new StringBuffer();
          {
            String name = u.getGegenkontoName();
            if (name != null && name.length() > 0)
              sb.append(name + "\n");
            
            String kto = HBCIProperties.formatIban(u.getGegenkontoNummer());
            String blz = u.getGegenkontoBLZ();
            if (kto != null && kto.length() > 0 && blz != null && blz.length() > 0)
            {
              String gi = HBCIProperties.getNameForBank(blz);
              sb.append(i18n.tr("{0} - {1}",kto,gi != null && gi.length() > 0 ? gi : blz));
            }
            
            String usage = StringUtils.trimToNull(VerwendungszweckUtil.toString(u));
            if (usage != null)
            {
              if (sb.length() > 0)
                sb.append("\n");
              sb.append(usage);
            }
            
          }

          TextStyle typeHaben = new TextStyle().font(fontTiny).foreground(new RGB(0,0,0));
          TextStyle typeSoll = new TextStyle().font(fontTiny).foreground(new RGB(200,0,0));

          table.add(new TextPrint(HBCI.DATEFORMAT.format(u.getValuta()) + "\n" + HBCI.DATEFORMAT.format(u.getDatum()),fontTiny));
          table.add(new NoBreakPrint(new TextPrint(u.getID(),fontTiny)));
          table.add(new TextPrint(sb.toString(),fontTiny));
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
    return i18n.tr("Umsätze");
  }
}
