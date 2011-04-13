/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportLastschrift.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/13 17:35:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.util.ApplicationException;

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
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Lastschrift");
  }
}



/**********************************************************************
 * $Log: PrintSupportLastschrift.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/