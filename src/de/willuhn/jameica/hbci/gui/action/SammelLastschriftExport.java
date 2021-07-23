/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SammelLastschrift;

/**
 * Action zum Exportieren von Sammel-Lastschriften.
 */
public class SammelLastschriftExport extends AbstractSammelTransferExport
{

  @Override
  Class getExportClass()
  {
    return SammelLastschrift.class;
  }

}


/*********************************************************************
 * $Log: SammelLastschriftExport.java,v $
 * Revision 1.2  2006/08/17 10:06:32  willuhn
 * @B Fehler in HTML-Export von Sammeltransfers
 *
 * Revision 1.1  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/