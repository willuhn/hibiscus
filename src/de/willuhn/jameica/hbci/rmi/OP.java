/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/OP.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/11 16:10:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer einen Offenen Posten.
 */
public interface OP extends DBObject
{
  /**
   * Liefert eine Bezeichnung fuer den Offenen Posten.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Speichert die Bezeichnung des Offenen Posten.
   * @param name Bezeichnung.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
  
  /**
   * Liefert den Termin, bis zu dem der offene Posten bezahlt sein sollte.
   * @return Ziel-Termin.
   * @throws RemoteException
   */
  public Date getTermin() throws RemoteException;
  
  /**
   * Speichert den Zieltermin, bis zu dem der offene Posten bezahlt sein sollte.
   * @param termin Zieltermin.
   * @throws RemoteException
   */
  public void setTermin(Date termin) throws RemoteException;

  /**
   * Liefert den erwarteten Betrag.
   * @return Betrag.
   * @throws RemoteException
   */
  public double getBetrag() throws RemoteException;
  
  /**
   * Speichert den erwarteten Betrag.
   * @param betrag der erwartete Betrag.
   * @throws RemoteException
   */
  public void setBetrag(double betrag) throws RemoteException;
  
  /**
   * Liefert einen optionalen Kommentar.
   * @return Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;
  
  /**
   * Liefert eine Liste der bisher zu diesem offenen Posten eingetroffenen Buchungen.
   * @return Liste der bisher eingetroffenen Buchungen vom Typ <code>OPBuchung</code>.
   * @throws RemoteException
   */
  public DBIterator getBuchungen() throws RemoteException;

  /**
   * Liefert das Suchmuster.
   * @return Suchmuster.
   * @throws RemoteException
   */
  public String getPattern() throws RemoteException;

  /**
   * Speichert das Suchmuster.
   * @param pattern das Suchmuster.
   * @throws RemoteException
   */
  public void setPattern(String pattern) throws RemoteException;
  
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

}


/*********************************************************************
 * $Log: OP.java,v $
 * Revision 1.1  2007/12/11 16:10:11  willuhn
 * @N Erster Code fuer "Offene Posten-Verwaltung"
 *
 **********************************************************************/