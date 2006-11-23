/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/UmsatzTyp.java,v $
 * $Revision: 1.10 $
 * $Date: 2006/11/23 23:24:17 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;

/**
 * Interface zur Einstufung von Umsaetzen in verschiedene Kategorien.
 */
public interface UmsatzTyp extends DBObject
{
  /**
   * Token, der einen Umsatztyp als Einnahme markiert.
   */
  public final static String EINNAHME = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Einnahme");
  
  /**
   * Token, der einen Umsatztyp als Ausgabe markiert.
   */
  public final static String AUSGABE = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Ausgabe");

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
	 * Liefert eine Liste von Umsaetzen, die diesem Umsatz-Typ entsprechen.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public GenericIterator getUmsaetze() throws RemoteException;
  
  /**
   * Liefert eine Liste von Umsaetzen der letzten Tage, die diesem Umsatz-Typ entsprechen.
   * @param days Anzahl der Tage.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public GenericIterator getUmsaetze(int days) throws RemoteException;

  /**
   * Liefert die Hoehe des Umsatzes, der fuer diesen Umsatztyp auf allen Konten vorliegt.
   * @return Hoehe des Umsatzes.
   * @throws RemoteException
   */
  public double getUmsatz() throws RemoteException;
  
  /**
   * Liefert die Hoehe des Umsatzes der letzten Tage, der fuer diesen Umsatztyp auf allen Konten vorliegt.
   * @param days Anzahl der Tage.
   * @return Hoehe des Umsatzes.
   * @throws RemoteException
   */
  public double getUmsatz(int days) throws RemoteException;

  /**
   * Prueft, ob es sich bei dem Pattern um einen regulaeren Ausdruck handelt.
   * @return true, wenn es sich um einen regulaeren Ausdruck handelt.
   * @throws RemoteException
   */
  public boolean isRegex() throws RemoteException;
  
  /**
   * Prueft, ob sich der Filter auf Einnahmen oder Ausgaben bezieht.
   * @return true, wenn es eine Einnahme ist. Sonst false.
   * @throws RemoteException
   */
  public boolean isEinnahme() throws RemoteException;
  
  /**
   * Speichert, ob es sich um eine Einnahme handelt.
   * @param einnahme true, wenn es eine Einnahme ist, sonst false.
   * @throws RemoteException
   */
  public void setEinnahme(boolean einnahme) throws RemoteException;
  
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
 * Revision 1.10  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.9  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.8  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.7  2005/12/29 01:22:12  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
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