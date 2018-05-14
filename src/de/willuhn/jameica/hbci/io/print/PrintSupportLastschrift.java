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