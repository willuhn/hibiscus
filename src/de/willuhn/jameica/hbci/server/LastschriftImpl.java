/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/LastschriftImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/02/19 16:49:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Transfer;

/**
 * Bildet eine Lastschrift ab. Ist fast das gleiche wie eine
 * Ueberweisung. Nur dass wir in eine andere Tabelle speichern
 * und der Empfaenger hier nicht das Geld sondern die Forderung
 * erhaelt.
 */
public class LastschriftImpl extends AbstractBaseUeberweisungImpl implements Lastschrift
{

  /**
   * @throws RemoteException
   */
  public LastschriftImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "lastschrift";
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Transfer#duplicate()
	 */
	public Transfer duplicate() throws RemoteException {
		Lastschrift u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
		u.setBetrag(getBetrag());
		u.setEmpfaengerBLZ(getEmpfaengerBLZ());
		u.setEmpfaengerKonto(getEmpfaengerKonto());
		u.setEmpfaengerName(getEmpfaengerName());
		u.setKonto(getKonto());
		u.setTermin(getTermin());
		u.setZweck(getZweck());
		u.setZweck2(getZweck2());
		return u;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Lastschrift#getTyp()
   */
  public String getTyp() throws RemoteException
  {
    return (String) getAttribute("typ");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Lastschrift#setTyp(java.lang.String)
   */
  public void setTyp(String typ) throws RemoteException
  {
  	if (typ == null)
			setAttribute("typ",null);
		else
		{
			if (!typ.equals("04") && !typ.equals("05"))
				throw new RemoteException("type " + typ + " not allowed");

			setAttribute("typ",typ);
		}
  }

}


/**********************************************************************
 * $Log: LastschriftImpl.java,v $
 * Revision 1.3  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.2  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.1  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 **********************************************************************/