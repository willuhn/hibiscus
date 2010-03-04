/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/LastschriftImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/03/04 09:39:40 $
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

import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Transfer;

/**
 * Bildet eine Lastschrift ab. Ist fast das gleiche wie eine
 * Ueberweisung. Nur dass wir in eine andere Tabelle speichern
 * und der Empfaenger hier nicht das Geld sondern die Forderung
 * erhaelt.
 */
public class LastschriftImpl extends AbstractBaseUeberweisungImpl
  implements Lastschrift
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
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
	public Duplicatable duplicate() throws RemoteException {
		Lastschrift u = (Lastschrift) getService().createObject(Lastschrift.class,null);
		u.setBetrag(getBetrag());
		u.setGegenkontoBLZ(getGegenkontoBLZ());
		u.setGegenkontoNummer(getGegenkontoNummer());
		u.setGegenkontoName(getGegenkontoName());
		u.setKonto(getKonto());
		u.setTermin(getTermin());
		u.setZweck(getZweck());
		u.setZweck2(getZweck2());
    u.setWeitereVerwendungszwecke(getWeitereVerwendungszwecke());
    u.setTextSchluessel(getTextSchluessel());
		return u;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getTransferTyp()
   */
  public int getTransferTyp() throws RemoteException
  {
    return Transfer.TYP_LASTSCHRIFT;
  }
}


/**********************************************************************
 * $Log: LastschriftImpl.java,v $
 * Revision 1.8  2010/03/04 09:39:40  willuhn
 * @B BUGZILLA 829
 *
 * Revision 1.7  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.6  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.5  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.4  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
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