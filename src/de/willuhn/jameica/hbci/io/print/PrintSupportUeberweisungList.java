/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportUeberweisungList.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/04/13 17:35:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import net.sf.paperclips.Print;
import de.willuhn.jameica.hbci.gui.parts.UeberweisungList;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von Ueberweisungen.
 */
public class PrintSupportUeberweisungList extends AbstractPrintSupportBaseUeberweisungList
{
  /**
   * ct.
   * @param ctx Kann vom Typ <code>UeberweisungList</code>, <code>Ueberweisung</code> oder <code>Ueberweisung[]</code> sein.
   */
  public PrintSupportUeberweisungList(Object ctx)
  {
    super(ctx);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Überweisungen");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportBaseUeberweisungList#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object ctx = this.getContext();

    // Sind wir in der Tabelle?
    if (ctx instanceof UeberweisungList)
      ctx = ((UeberweisungList)ctx).getSelection();

    // Ist nur ne Einzel-Ueberweisung. Dann drucken wir automatisch die Detail-Ansicht
    if (ctx instanceof Ueberweisung)
    {
      PrintSupportUeberweisung single = new PrintSupportUeberweisung((Ueberweisung)ctx);
      return single.printContent();
    }
    
    return super.printContent();
  }
}



/**********************************************************************
 * $Log: PrintSupportUeberweisungList.java,v $
 * Revision 1.5  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.4  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/