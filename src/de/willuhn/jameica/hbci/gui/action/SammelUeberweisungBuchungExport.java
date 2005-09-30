/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/SammelUeberweisungBuchungExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;

/**
 * Action, ueber die die Buchungen einer Sammellastschrift exportiert werden koennen.
 */
public class SammelUeberweisungBuchungExport extends AbstractSammelTransferBuchungExport
{
  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSammelTransferBuchungExport#getExportClass()
   */
  Class getExportClass()
  {
    return SammelUeberweisungBuchung.class;
  }

}

/**********************************************************************
 * $Log: SammelUeberweisungBuchungExport.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/