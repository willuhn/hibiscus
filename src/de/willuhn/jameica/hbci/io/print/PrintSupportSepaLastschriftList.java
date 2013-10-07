/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import net.sf.paperclips.Print;
import de.willuhn.jameica.hbci.gui.parts.SepaLastschriftList;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von SEPA-Lastschriften.
 */
public class PrintSupportSepaLastschriftList extends AbstractPrintSupportBaseUeberweisungList
{
  /**
   * ct.
   * @param ctx Kann vom Typ <code>SepaLastschriftList</code>, <code>SepaLastschrift</code> oder <code>SepaLastschrift[]</code> sein.
   */
  public PrintSupportSepaLastschriftList(Object ctx)
  {
    super(ctx);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Lastschriften");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportBaseUeberweisungList#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object ctx = this.getContext();

    // Sind wir in der Tabelle?
    if (ctx instanceof SepaLastschriftList)
      ctx = ((SepaLastschriftList)ctx).getSelection();

    // Ist nur ne Einzel-Lastschrift. Dann drucken wir automatisch die Detail-Ansicht
    if (ctx instanceof SepaLastschrift)
    {
      PrintSupportSepaLastschrift single = new PrintSupportSepaLastschrift((SepaLastschrift)ctx);
      return single.printContent();
    }
    
    return super.printContent();
  }
}
