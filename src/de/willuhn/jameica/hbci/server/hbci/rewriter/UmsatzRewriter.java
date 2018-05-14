/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.rewriter;

import java.util.List;

import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Rewriter fuer einen Umsatz.
 * Einige Banken liefern die Umsaetze unstrukturiert. Hier koennen
 * die Felder Gegenkonto/BLZ/Name usw. nicht erkannt werden. Hibiscus
 * schreibt sie daher in den Verwendungszweck. Durch Implementierung
 * des UmsatzRewriter-Interfaces koennen Bank-spezifische Spezialparser
 * fuer die Umsaetze geschrieben werden.
 * Implementierungen dieses Interfaces muessen einen parameterlosen
 * Konstruktor mit public-Modifier oder gar keinen (Default)-Konstruktor
 * besitzen (Bean-Spezifikation).
 */
public interface UmsatzRewriter
{
  /**
   * Liefert die Liste der BLZ, fuer die der Rewriter zustaendig ist.
   * @return Liste der BLZS.
   */
  public List<String> getBlzList();
  
  /**
   * Schreibt den Umsatz um.
   * @param u der zu umschreibende Umsatz.
   * @throws Exception
   */
  public void rewrite(Umsatz u) throws Exception;
}



/**********************************************************************
 * $Log: UmsatzRewriter.java,v $
 * Revision 1.1  2010/04/25 23:09:04  willuhn
 * @N BUGZILLA 244
 *
 **********************************************************************/