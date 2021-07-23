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

import net.sf.paperclips.Print;
import de.willuhn.jameica.hbci.gui.parts.SepaLastschriftList;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von SEPA-Lastschriften.
 */
public class PrintSupportSepaLastschriftList extends AbstractPrintSupportSepaTransferList
{
  /**
   * ct.
   * @param ctx Kann vom Typ <code>SepaLastschriftList</code>, <code>SepaLastschrift</code> oder <code>SepaLastschrift[]</code> sein.
   */
  public PrintSupportSepaLastschriftList(Object ctx)
  {
    super(ctx);
  }
  
  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Lastschriften");
  }

  @Override
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
