/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/PrintSupportSammelLastschrift.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/11 16:48:33 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;


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

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupportSammelTransfer#getTitle()
   */
  String getTitle()
  {
    return i18n.tr("Sammel-Lastschrift");
  }
}



/**********************************************************************
 * $Log: PrintSupportSammelLastschrift.java,v $
 * Revision 1.1  2011/04/11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 **********************************************************************/