/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UeberweisungImpl.java,v $
 * $Revision: 1.29 $
 * $Date: 2005/01/19 00:16:05 $
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
import java.util.Date;
import java.util.zip.CRC32;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Ueberweisung.
 */
public class UeberweisungImpl extends AbstractTransferImpl implements Ueberweisung
{

	private I18N i18n;

  /**
   * @throws RemoteException
   */
  public UeberweisungImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "ueberweisung";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		try {
			if (ausgefuehrt())
				throw new ApplicationException(i18n.tr("Bereits ausgeführte Aufträge können nicht gelöscht werden."));
		}
		catch (RemoteException e)
		{
			throw new ApplicationException(i18n.tr("Fehler beim Löschen des Auftrags."));
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  	try {
			if (getTermin() == null)
				setTermin(new Date());
  	}
  	catch (RemoteException e)
  	{
  		Logger.error("error while checking ueberweisung",e);
  		throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrags."));
  	}
		super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		try {
			if (ausgefuehrt())
				throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt und kann daher nicht mehr geändert werden."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while checking ueberweisung",e);
			throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrags."));
		}
		super.updateCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getTermin()
   */
  public Date getTermin() throws RemoteException {
    return (Date) getAttribute("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException {
		Integer i = (Integer) getAttribute("ausgefuehrt");
		if (i == null)
			return false;
		return i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException {
		setAttribute("termin",termin);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException {
		if (isNewObject())
		{
			if (getTermin() == null) setTermin(new Date());
			setAttribute("ausgefuehrt",new Integer(0));
		}
    super.store();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#duplicate()
   */
  public Transfer duplicate() throws RemoteException {
    Ueberweisung u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
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
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException {
    if (ausgefuehrt())
    	return false;
    Date termin = getTermin();
    if (termin == null)
    	return false;
    return (termin.before(new Date()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
		String s = getBetrag() +
							 getEmpfaengerBLZ() +
							 getEmpfaengerKonto() +
							 getEmpfaengerName() +
							 getKonto().getChecksum() +
							 getZweck() +
							 getZweck2() +
							 HBCI.DATEFORMAT.format(getTermin());
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setAusgefuehrt()
   */
  public void setAusgefuehrt() throws RemoteException
  {
		setAttribute("ausgefuehrt",new Integer(1));
  }
}


/**********************************************************************
 * $Log: UeberweisungImpl.java,v $
 * Revision 1.29  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 * Revision 1.28  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.24  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.22  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.21  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.20  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.19  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.18  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.16  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.15  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.11  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.10  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.8  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.7  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.6  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.3  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/