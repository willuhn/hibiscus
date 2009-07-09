/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungExport.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/07/09 17:08:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;

/**
 * Exporter fuer Auslandsueberweisungen.
 */
public class AuslandsUeberweisungExport extends AbstractObjectExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractObjectExport#getExportClass()
   */
  Class getExportClass()
  {
    return AuslandsUeberweisung.class;
  }

}


/*********************************************************************
 * $Log: AuslandsUeberweisungExport.java,v $
 * Revision 1.2  2009/07/09 17:08:02  willuhn
 * @N BUGZILLA #740
 *
 * Revision 1.1  2009/03/17 23:44:14  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 **********************************************************************/