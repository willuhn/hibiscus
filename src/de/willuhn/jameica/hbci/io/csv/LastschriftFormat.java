/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/csv/LastschriftFormat.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/16 13:43:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import de.willuhn.jameica.hbci.rmi.Lastschrift;

/**
 * Implementierung des CSV-Formats fuer den Import von Lastschriften.
 */
public class LastschriftFormat extends AbstractBaseUeberweisungFormat<Lastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getType()
   */
  public Class<Lastschrift> getType()
  {
    return Lastschrift.class;
  }
}



/**********************************************************************
 * $Log: LastschriftFormat.java,v $
 * Revision 1.1  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 **********************************************************************/