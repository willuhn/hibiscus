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
  
  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Überweisungen");
  }

  @Override
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