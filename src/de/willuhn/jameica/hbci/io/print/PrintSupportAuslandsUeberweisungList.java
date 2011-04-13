/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportAuslandsUeberweisungList.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/13 17:35:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
public class PrintSupportAuslandsUeberweisungList extends AbstractPrintSupportBaseUeberweisungList
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
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportBaseUeberweisungList#printContent()
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



/**********************************************************************
 * $Log: PrintSupportAuslandsUeberweisungList.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/