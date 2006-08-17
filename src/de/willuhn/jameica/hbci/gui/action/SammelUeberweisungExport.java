/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungExport.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/08/17 10:06:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;

/**
 * Action zum Exportieren von Sammel-Ueberweisungen.
 */
public class SammelUeberweisungExport extends AbstractSammelTransferExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSammelTransferExport#getExportClass()
   */
  Class getExportClass()
  {
    return SammelUeberweisung.class;
  }

}


/*********************************************************************
 * $Log: SammelUeberweisungExport.java,v $
 * Revision 1.2  2006/08/17 10:06:32  willuhn
 * @B Fehler in HTML-Export von Sammeltransfers
 *
 * Revision 1.1  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/