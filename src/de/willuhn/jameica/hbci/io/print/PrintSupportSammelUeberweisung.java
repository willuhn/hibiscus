/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportSammelUeberweisung.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/13 17:35:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.util.ApplicationException;


/**
 * Druck-Support fuer Sammel-Ueberweisungen.
 */
public class PrintSupportSammelUeberweisung extends AbstractPrintSupportSammelTransfer
{
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public PrintSupportSammelUeberweisung(Object ctx)
  {
    super(ctx);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Sammel-Überweisung");
  }
}



/**********************************************************************
 * $Log: PrintSupportSammelUeberweisung.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 **********************************************************************/