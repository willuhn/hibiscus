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
import de.willuhn.jameica.hbci.gui.parts.AuslandsUeberweisungList;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von Auslands-Ueberweisungen.
 */
public class PrintSupportAuslandsUeberweisungList extends AbstractPrintSupportSepaTransferList
{
  /**
   * ct.
   * @param ctx Kann vom Typ <code>AuslandsUeberweisungList</code>, <code>AuslandsUeberweisung</code> oder <code>AuslandsUeberweisung[]</code> sein.
   */
  public PrintSupportAuslandsUeberweisungList(Object ctx)
  {
    super(ctx);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Überweisungen");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportSepaTransferList#printContent()
   */
  Print printContent() throws ApplicationException
  {
    Object ctx = this.getContext();

    // Sind wir in der Tabelle?
    if (ctx instanceof AuslandsUeberweisungList)
      ctx = ((AuslandsUeberweisungList)ctx).getSelection();

    // Ist nur ne Einzel-Ueberweisung. Dann drucken wir automatisch die Detail-Ansicht
    if (ctx instanceof AuslandsUeberweisung)
    {
      PrintSupportAuslandsUeberweisung single = new PrintSupportAuslandsUeberweisung((AuslandsUeberweisung)ctx);
      return single.printContent();
    }
    
    return super.printContent();
  }
}
