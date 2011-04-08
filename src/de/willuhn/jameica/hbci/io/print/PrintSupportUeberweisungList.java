/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUeberweisungList.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/08 13:38:43 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import net.sf.paperclips.Print;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von Ueberweisungen.
 */
public class PrintSupportUeberweisungList extends PrintSupportUeberweisung
{
  /**
   * ct.
   * @param data die zu druckenden Daten.
   */
  public PrintSupportUeberweisungList(Object data)
  {
    super(data);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisung#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object data = this.getData();
    
    if (data == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Überweisung aus"));
    
    // Ist nur ne Einzel-Ueberweisung. Dann drucken wir automatisch die Detail-Ansicht
    if (data instanceof Ueberweisung)
      return super.printContent();
      
//    FontData font = Font.SMALL.getSWTFont().getFontData()[0];
//    FontData bold = Font.BOLD.getSWTFont().getFontData()[0];
//    
//    DefaultGridLook look = new DefaultGridLook();
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
//    return grid;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisung#getName()
   */
  String getName() throws ApplicationException
  {
    Object data = this.getData();
    
    if (data instanceof Ueberweisung)
      return super.getName();
    
    return i18n.tr("Überweisungsliste");
  }
}



/**********************************************************************
 * $Log: PrintSupportUeberweisungList.java,v $
 * Revision 1.1  2011/04/08 13:38:43  willuhn
 * @N Druck-Support fuer Einzel-Ueberweisungen. Weitere werden folgen.
 *
 **********************************************************************/