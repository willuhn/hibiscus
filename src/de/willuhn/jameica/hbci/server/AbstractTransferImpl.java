/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/AbstractTransferImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/08/18 23:13:51 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Abstrakte Basis-Implementierung von Geld-Transfers zwischen Konten.
 */
public abstract class AbstractTransferImpl extends AbstractDBObject implements Transfer
{

	private I18N i18n;

  /**
   * ct.
   * @throws RemoteException
   */
  public AbstractTransferImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  	try {
			if (getBetrag() == 0.0)
				throw new ApplicationException("Bitte geben Sie einen gültigen Betrag ein.");

			if (getKonto() == null)
				throw new ApplicationException("Bitte wählen Sie ein Konto aus.");
			if (getKonto().isNewObject())
				throw new ApplicationException("Bitte speichern Sie zunächst das Konto");

			if (getBetrag() > Settings.getUeberweisungLimit())
				throw new ApplicationException("Limit für Überweisungsbetrag überschritten: " + 
					HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + getKonto().getWaehrung());

			if (getEmpfaengerKonto() == null || "".equals(getEmpfaengerKonto()))
				throw new ApplicationException("Bitte geben Sie die Kontonummer des Empfängers ein");
			
			if (getEmpfaengerBLZ() == null || "".equals(getEmpfaengerBLZ()))
				throw new ApplicationException("Bitte geben Sie die BLZ des Empfängers ein");

			if (!HBCIUtils.checkAccountCRC(getEmpfaengerBLZ(),getEmpfaengerKonto()))
				throw new ApplicationException("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben.");
				
			if (getZweck() == null || "".equals(getZweck()))
				throw new ApplicationException("Bitte geben Sie einen Verwendungszweck ein");

			if (getZweck().length() > 27)
				throw new ApplicationException("Bitten geben Sie als Verwendungszweck maximal 27 Zeichen an");
				
			if (getZweck2() != null && getZweck2().length() > 27)
				throw new ApplicationException("Bitten geben Sie als weiteren Verwendungszweck maximal 27 Zeichen an");
  	}
  	catch (RemoteException e)
  	{
  		Logger.error("error while checking ueberweisung",e);
  		throw new ApplicationException("Fehler beim Prüfen der Überweisung.");
  	}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
   */
  public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
		setAttribute("konto_id",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException {
		setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getEmpfaengerKonto()
   */
  public String getEmpfaengerKonto() throws RemoteException {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getEmpfaengerBLZ()
   */
  public String getEmpfaengerBLZ() throws RemoteException {
		return (String) getAttribute("empfaenger_blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getEmpfaengerName()
   */
  public String getEmpfaengerName() throws RemoteException {
		return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setEmpfaengerKonto(java.lang.String)
   */
  public void setEmpfaengerKonto(String konto) throws RemoteException {
		setAttribute("empfaenger_konto",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setEmpfaengerBLZ(java.lang.String)
   */
  public void setEmpfaengerBLZ(String blz) throws RemoteException {
		setAttribute("empfaenger_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setEmpfaengerName(java.lang.String)
   */
  public void setEmpfaengerName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
  	Konto k = this.getKonto();
    super.delete();
    if (k == null)
    	return;
    k.addToProtokoll(i18n.tr("Überweisung an " + getEmpfaengerName() + " gelöscht"),Protokoll.TYP_SUCCESS);
  }


  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#setEmpfaenger(de.willuhn.jameica.hbci.rmi.Empfaenger)
   */
  public void setEmpfaenger(Empfaenger e) throws RemoteException
  {
  	if (e == null)
  		return;
  	setEmpfaengerBLZ(e.getBLZ());
  	setEmpfaengerKonto(e.getKontonummer());
  	setEmpfaengerName(e.getName());
  }

}


/**********************************************************************
 * $Log: AbstractTransferImpl.java,v $
 * Revision 1.8  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.7  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.4  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.3  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.2  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
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