/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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