/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/tests/Restriction.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/29 00:32:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci.tests;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer alle Tests auf Restriktionen von HBCI-Jobs.
 */
public interface Restriction
{
	/**
	 * Fuehrt den Test aus.
   * @throws ApplicationException wird geworfen, wenn der Test fehlschlug.
   * Die Exception muss eine fuer den Benutzer verstaendliche Fehlermeldung enthalten.
   */
  public void test() throws ApplicationException;
}


/**********************************************************************
 * $Log: Restriction.java,v $
 * Revision 1.1  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 **********************************************************************/