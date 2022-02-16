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
import de.willuhn.jameica.hbci.gui.parts.LastschriftList;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine Liste von Lastschriften.
 */
public class PrintSupportLastschriftList extends AbstractPrintSupportBaseUeberweisungList
{
  /**
   * ct.
   * @param ctx Kann vom Typ <code>LastschriftList</code>, <code>Lastschrift</code> oder <code>Lastschrift[]</code> sein.
   */
  public PrintSupportLastschriftList(Object ctx)
  {
    super(ctx);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Lastschriften");
  }

  @Override
  Print printContent() throws ApplicationException
  {
    Object ctx = this.getContext();

    // Sind wir in der Tabelle?
    if (ctx instanceof LastschriftList)
      ctx = ((LastschriftList)ctx).getSelection();

    // Ist nur ne Einzel-Lastschrift. Dann drucken wir automatisch die Detail-Ansicht
    if (ctx instanceof Lastschrift)
    {
      PrintSupportLastschrift single = new PrintSupportLastschrift((Lastschrift)ctx);
      return single.printContent();
    }
    
    return super.printContent();
  }
}



/**********************************************************************
 * $Log: PrintSupportLastschriftList.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/