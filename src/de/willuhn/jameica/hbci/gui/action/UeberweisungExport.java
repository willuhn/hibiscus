/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungExport.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/07/09 17:08:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Exporter fuer Ueberweisungen.
 */
public class UeberweisungExport extends AbstractObjectExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractObjectExport#getExportClass()
   */
  Class getExportClass()
  {
    return Ueberweisung.class;
  }

}


/*********************************************************************
 * $Log: UeberweisungExport.java,v $
 * Revision 1.2  2009/07/09 17:08:03  willuhn
 * @N BUGZILLA #740
 *
 * Revision 1.1  2006/10/16 14:46:30  willuhn
 * @N CSV-Export von Ueberweisungen und Lastschriften
 *
 **********************************************************************/