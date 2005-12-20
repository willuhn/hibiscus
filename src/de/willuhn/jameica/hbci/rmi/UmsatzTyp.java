/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/UmsatzTyp.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/12/20 00:03:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.regex.PatternSyntaxException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.filter.UmsatzFilter;

/**
 * Interface zur Einstufung von Umsaetzen in verschiedene Kategorien.
 */
public interface UmsatzTyp extends DBObject, UmsatzFilter
{

	/**
	 * Liefert den Namen des Umsatz-Typs.
   * @return Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Speichert den Namen des Umsatz-Typs.
   * @param name Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
	
  /**
   * Liefert das Suchmuster fuer den Umsatztyp.
   * @return Suchmuster.
   * @throws RemoteException
   */
  public String getPattern() throws RemoteException;

  /**
   * Speichert das Suchmuster fuer den Umsatztyp.
   * @param pattern das Suchmuster.
   * @throws RemoteException
   */
  public void setPattern(String pattern) throws RemoteException;
  
	/**
	 * Liefert eine Liste der Umsatz-Zuordnungen fuer diesen Umsatz.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public DBIterator getUmsatzZuordnungen() throws RemoteException;
  
  /**
   * Liefert die Hoehe des Umsatzes, der fuer diesen Umsatztyp auf allen Konten vorliegt.
   * @return Hoehe des Umsatzes.
   * @throws RemoteException
   */
  public double getUmsatz() throws RemoteException;
  
  /**
   * Prueft, ob der Umsatz diesem Typ bereits zugeordnet ist.
   * @param u der zu pruefende Umsatz.
   * @return true, wenn er bereits zugeordnet ist.
   * @throws RemoteException
   */
  public boolean isZugeordnet(Umsatz u) throws RemoteException;
  
  /**
   * Prueft, ob es sich bei dem Pattern um einen regulaeren Ausdruck handelt.
   * @return true, wenn es sich um einen regulaeren Ausdruck handelt.
   * @throws RemoteException
   */
  public boolean isRegex() throws RemoteException;
  
  /**
   * Speichert, ob es sich bei dem Pattern um einen regulaeren Ausdruck handelt.
   * @param regex true, wenn es sich um einen regulaeren Ausdruck handelt.
   * @throws RemoteException
   */
  public void setRegex(boolean regex) throws RemoteException;

  /**
   * Prueft, ob der Umsatz diesem Pattern entspricht.
   * @param umsatz zu pruefender Umsatz.
   * @return true, wenn er dem Pattern entspricht.
   * @throws RemoteException
   * @throws PatternSyntaxException wird geworden, wenn es ein regulaerer Ausdruck mit Fehlern ist.
   */
  public boolean matches(Umsatz umsatz) throws RemoteException, PatternSyntaxException;
}


/**********************************************************************
 * $Log: UmsatzTyp.java,v $
 * Revision 1.6  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.5  2005/12/13 00:06:38  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.4  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.3  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.2  2005/11/14 23:47:21  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/