/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/AbstractPrintSupport.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/08 13:38:43 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.util.Date;

import net.sf.paperclips.AlignPrint;
import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.ImagePrint;
import net.sf.paperclips.Margins;
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
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.print.PrintSupport;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung fuer den Druck-Support.
 */
public abstract class AbstractPrintSupport implements PrintSupport
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Object data = null;
  
  /**
   * ct.
   * @param data die zu druckenden Daten.
   */
  public AbstractPrintSupport(Object data)
  {
    this.data = data;
  }
  
  /**
   * Liefert die zu druckenden Daten.
   * @return
   */
  Object getData()
  {
    return this.data;
  }
  
  /**
   * @see de.willuhn.jameica.print.PrintSupport#print()
   */
  public PrintJob print() throws ApplicationException
  {
    final PagePrint page = new PagePrint(printContent());

    ////////////////////////////////////////////////////////////////////////////
    // Tabellen-Header
    DefaultGridLook look = new DefaultGridLook(5,5);
    GridPrint table = new GridPrint("l:p:g, r:p:g",look);

    {
      FontData normal = Font.SMALL.getSWTFont().getFontData()[0];
      Manifest mf = Application.getPluginLoader().getManifest(HBCI.class);

      table.add(new TextPrint(i18n.tr("{0} {1}\nDruck: {2}",mf.getName(),mf.getVersion().toString(),HBCI.LONGDATEFORMAT.format(new Date())),normal));
      ImagePrint ip = new ImagePrint(SWTUtil.getImage("hibiscus-donate.png").getImageData(),new Point(300,300));
      table.add(new AlignPrint(ip,SWT.RIGHT,SWT.TOP));
    }
    ////////////////////////////////////////////////////////////////////////////

    page.setHeader(new SimplePageDecoration(table));
  
    PrintJob job = new PrintJob(getName(),page);
    Margins margins = job.getMargins(); // TODO: Wenn man den Default-Rand laesst, ist er rechts groesser als links - das ist nicht abheft-freundlich ;)
    margins.left = 100;
    margins.right = 50;
    
    return job;
  }
  
  /**
   * Druckt den eigentlichen Inhalt.
   * @return der eigentliche Inhalt.
   * @throws ApplicationException
   */
  abstract Print printContent() throws ApplicationException;
  
  /**
   * Liefert den Job-Namen.
   * Kann bei Bedarf ueberschrieben werden.
   * @return der Job-Name.
   * @throws ApplicationException
   */
  String getName() throws ApplicationException
  {
    String name = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getName();
    return name + " " + HBCI.LONGDATEFORMAT.format(new Date());
  }

}



/**********************************************************************
 * $Log: AbstractPrintSupport.java,v $
 * Revision 1.1  2011/04/08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 **********************************************************************/