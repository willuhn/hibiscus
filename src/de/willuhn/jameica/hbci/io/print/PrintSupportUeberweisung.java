/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUeberweisung.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/07 17:29:19 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;

import net.sf.paperclips.AlignPrint;
import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.ImagePrint;
import net.sf.paperclips.PagePrint;
import net.sf.paperclips.PrintJob;
import net.sf.paperclips.SimplePageDecoration;
import net.sf.paperclips.TextPrint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Druck-Support fuer einzelne Ueberweisungen.
 */
public class PrintSupportUeberweisung implements PrintSupport<Ueberweisung>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.PrintSupport#print(java.lang.Object)
   */
  public PrintJob print(Ueberweisung u) throws ApplicationException, RemoteException
  {
    return null;
    
    // TEST-Code
    
//    ImagePrint ip = new ImagePrint(SWTUtil.getImage("hibiscus-donate.png").getImageData(),new Point(300,300));
//    AlignPrint image = new AlignPrint(ip,SWT.RIGHT,SWT.TOP);
//    
//    FontData font = Font.SMALL.getSWTFont().getFontData()[0];
//    FontData bold = Font.BOLD.getSWTFont().getFontData()[0];
//    
//    DefaultGridLook look = new DefaultGridLook();
////    look.setCellBorder(new LineBorder(new RGB(150,150,150)));
//    look.setHeaderBackground(new RGB(200,200,200));
//    
//    GridPrint grid = new GridPrint("r:d:n, l:d:g, r:p:g",look);
//    
//    grid.addHeader(new TextPrint("ID",bold));
//    grid.addHeader(new TextPrint("Text",bold));
//    grid.addHeader(new TextPrint("Betrag",bold));
//    
//    grid.add(new TextPrint("10",font));
//    grid.add(new TextPrint("Das ist ein etwas längerer Text. Mal schauen, wie lang der werden kann. Geht noch was? Vielleicht noch was?",font));
//    grid.add(new TextPrint("10,00 EUR",font));
//    
//    grid.add(new TextPrint("11",font));
//    grid.add(new TextPrint("Hier steht ein anderer Text.",font));
//    grid.add(new TextPrint("20,50 EUR",font));
//
//    final PagePrint page = new PagePrint(grid);
//    page.setHeader(new SimplePageDecoration(image));
//    
//    PrintJob job = new PrintJob(i18n.tr("Überweisung an {0}",u.getGegenkontoName()),page);
//    // job.setMargins(70); // TODO: Anpassen?
//    return job;
  }
}



/**********************************************************************
 * $Log: PrintSupportUeberweisung.java,v $
 * Revision 1.1  2011/04/07 17:29:19  willuhn
 * @N Test-Code fuer Druck-Support
 *
 **********************************************************************/