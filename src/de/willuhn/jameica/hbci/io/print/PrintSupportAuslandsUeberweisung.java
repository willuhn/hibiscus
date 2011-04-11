/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportAuslandsUeberweisung.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/11 14:36:37 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;

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
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportBaseUeberweisung#getTitle()
   */
  String getTitle()
  {
    return i18n.tr("SEPA-Überweisung");
  }
}



/**********************************************************************
 * $Log: PrintSupportAuslandsUeberweisung.java,v $
 * Revision 1.1  2011/04/11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/