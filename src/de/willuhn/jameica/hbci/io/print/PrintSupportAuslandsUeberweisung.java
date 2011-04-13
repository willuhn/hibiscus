/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportAuslandsUeberweisung.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/13 17:35:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer Auslandsueberweisungen.
 */
public class PrintSupportAuslandsUeberweisung extends AbstractPrintSupportBaseUeberweisung
{
  /**
   * ct.
   * @param u die zu druckende Auslandsueberweisung.
   */
  public PrintSupportAuslandsUeberweisung(AuslandsUeberweisung u)
  {
    super(u);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Überweisung");
  }
}



/**********************************************************************
 * $Log: PrintSupportAuslandsUeberweisung.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/