/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoExport.java,v $
 * $Revision: 1.1 $
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

import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Exporter fuer Konten.
 */
public class KontoExport extends AbstractObjectExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractObjectExport#getExportClass()
   */
  Class getExportClass()
  {
    return Konto.class;
  }

}


/*********************************************************************
 * $Log: KontoExport.java,v $
 * Revision 1.1  2009/07/09 17:08:03  willuhn
 * @N BUGZILLA #740
 *
 **********************************************************************/