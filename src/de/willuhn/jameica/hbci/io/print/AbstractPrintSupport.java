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

import net.sf.paperclips.AlignPrint;
import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.ImagePrint;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.LinePrint;
import net.sf.paperclips.Margins;
import net.sf.paperclips.PageNumberPageDecoration;
import net.sf.paperclips.PagePrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.PrintJob;
import net.sf.paperclips.SimplePageDecoration;
import net.sf.paperclips.TextPrint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;

import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.print.PrintSupport;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung fuer den Druck-Support.
 */
public abstract class AbstractPrintSupport implements PrintSupport
{
  private final static Settings settings = new Settings(PrintSupport.class);
  
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  FontData fontTitle  = Font.BOLD.getSWTFont().getFontData()[0];
  FontData fontNormal = Font.SMALL.getSWTFont().getFontData()[0];
  FontData fontBold   = new FontData(fontNormal.getName(),fontNormal.getHeight(),SWT.BOLD);

  FontData fontTiny     = new FontData(fontNormal.getName(),fontNormal.getHeight() - 2,SWT.NORMAL);
  FontData fontTinyBold = new FontData(fontTiny.getName(),fontTiny.getHeight(),SWT.BOLD);
  
  /**
   * @see de.willuhn.jameica.print.PrintSupport#print()
   */
  public PrintJob print() throws ApplicationException
  {
    Print content = printContent();

    // Das Haupt-Layout
    GridPrint grid = new GridPrint("l:d:g");
    grid.add(new TextPrint(getTitle(),fontTitle));
    grid.add(new LinePrint());
    grid.add(new LineBreakPrint(fontTitle));
    grid.add(content);

    final PagePrint page = new PagePrint(grid);

    ////////////////////////////////////////////////////////////////////////////
    // Tabellen-Header
    DefaultGridLook look = new DefaultGridLook(5,5);
    GridPrint table = new GridPrint("l:p:g, r:p:g",look);

    {
      table.add(new TextPrint(i18n.tr("Druck: {0}",HBCI.LONGDATEFORMAT.format(new Date())),fontTiny));
      ImagePrint ip = new ImagePrint(SWTUtil.getImage("hibiscus-donate.png").getImageData(),new Point(300,300));
      table.add(new AlignPrint(ip,SWT.RIGHT,SWT.TOP));
    }
    ////////////////////////////////////////////////////////////////////////////

    page.setHeader(new SimplePageDecoration(table));
    
    PageNumberPageDecoration footer = new PageNumberPageDecoration(SWT.RIGHT);
    footer.setFontData(fontTiny);
    page.setFooter(footer);
    
    this.customize(page);
  
    PrintJob job = new PrintJob(i18n.tr("Hibiscus {0}",HBCI.LONGDATEFORMAT.format(new Date())),page);
    
    // Wenn man den Default-Rand laesst, ist er rechts groesser als links - das ist nicht abheft-freundlich ;)
    // Ich denke, die meisten User brauchen das nicht aendern. Falls doch, kann es der
    // User ja ueber die Properties-Datei anpassen
    Margins margins = job.getMargins();
    margins.left  = settings.getInt("margin.left",100);
    margins.right = settings.getInt("margin.right",50);
    
    return job;
  }
  
  /**
   * Druckt den eigentlichen Inhalt.
   * @return der eigentliche Inhalt.
   * @throws ApplicationException
   */
  abstract Print printContent() throws ApplicationException;

  /**
   * Liefert die Ueberschrift.
   * @return die Ueberschrift.
   */
  abstract String getTitle() throws ApplicationException;
  
  /**
   * Kann ueberschrieben werden, um noch Anpassungen vorzunehmen, direkt bevor der Druck-Job erstellt wird.
   * @param page die zu druckende Seite.
   * @throws ApplicationException
   */
  void customize(PagePrint page) throws ApplicationException
  {
  }

  /**
   * Liefert den Wert oder "-" wenn er NULL/leer ist.
   * @param value der Wert.
   * @return der Wert des Attributes.
   * @throws RemoteException
   */
  String notNull(Object value) throws RemoteException
  {
    String empty = "-";
    
    if (value == null)
      return empty;
   
    String s = value.toString();
    return (s != null && s.trim().length() > 0) ? s : empty;
  }
}
