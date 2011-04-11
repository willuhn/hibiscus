/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportLastschrift.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/11 14:36:37 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.Lastschrift;

/**
 * Druck-Support fuer einzelne Lastschrift.
 */
public class PrintSupportLastschrift extends AbstractPrintSupportBaseUeberweisung
{
  /**
   * ct.
   * @param l die zu druckende Lastschrift.
   */
  public PrintSupportLastschrift(Lastschrift l)
  {
    super(l);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportBaseUeberweisung#getTitle()
   */
  String getTitle()
  {
    return i18n.tr("Lastschrift");
  }
}



/**********************************************************************
 * $Log: PrintSupportLastschrift.java,v $
 * Revision 1.1  2011/04/11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/