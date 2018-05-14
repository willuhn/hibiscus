/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.filter;

import java.rmi.RemoteException;

/**
 * Mit diesem Filter koennen einzelne Datensaetze bei einer Suche
 * ausgefiltert werden. Das wird z.Bsp. genutzt, um bei
 * Auslandsueberweisungen nur jene Adressen anzuzeigen, die
 * eine IBAN besitzen.
 * @param <T> Beliebiger Typ.
 */
public interface Filter<T>
{
  /**
   * Prueft, ob das Objekt angezeigt werden soll oder nicht.
   * @param object das zu pruefende Objekt.
   * @return true, wenn es ok ist und angezeigt werden soll.
   * False, wenn es uebersprungen werden soll.
   * @throws RemoteException
   */
  public boolean accept(T object) throws RemoteException;
}


/**********************************************************************
 * $Log: Filter.java,v $
 * Revision 1.1  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 **********************************************************************/