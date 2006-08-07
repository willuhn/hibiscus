/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelLastschriftExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/07 14:31:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SammelLastschrift;

/**
 * Action zum Exportieren von Sammel-Lastschriften.
 */
public class SammelLastschriftExport extends AbstractSammelTransferExport
{

  Class getExportClass()
  {
    return SammelLastschrift.class;
  }

}


/*********************************************************************
 * $Log: SammelLastschriftExport.java,v $
 * Revision 1.1  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/