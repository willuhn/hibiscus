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

import de.willuhn.util.ApplicationException;


/**
 * Druck-Support fuer Sammel-Lastschriften.
 */
public class PrintSupportSammelLastschrift extends AbstractPrintSupportSammelTransfer
{
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public PrintSupportSammelLastschrift(Object ctx)
  {
    super(ctx);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Sammel-Lastschrift");
  }
}



/**********************************************************************
 * $Log: PrintSupportSammelLastschrift.java,v $
 * Revision 1.2  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.1  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 **********************************************************************/