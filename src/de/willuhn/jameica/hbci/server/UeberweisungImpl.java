/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UeberweisungImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/17 00:53:22 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Eine Ueberweisung.
 */
public class UeberweisungImpl
  extends AbstractDBObject
  implements Ueberweisung {

	private boolean inExecute = false;

  /**
   * @throws RemoteException
   */
  public UeberweisungImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "ueberweisung";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		throw new ApplicationException("Nicht implementiert");
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

			if (getBetrag() > BETRAG_MAX)
				throw new ApplicationException("Maximaler Überweisungsbetrag von " + 
					HBCI.DECIMALFORMAT.format(BETRAG_MAX) + " " + getKonto().getWaehrung() +
					"überschritten.");

			if (getEmpfaenger() == null)
				throw new ApplicationException("Bitte wählen Sie einen Empfänger aus");
			
			if (getEmpfaenger().isNewObject())
				throw new ApplicationException("Bitte speichern Sie zunächst den Empfänger");
			
			if (getZweck() == null)
				throw new ApplicationException("Bitte geben Sie einen Verwendungszweck ein");

			if (getZweck().length() > 35)
				throw new ApplicationException("Bitten geben Sie als Verwendungszweck maximal 35 Zeichen an");
				
			if (getZweck2() != null && getZweck2().length() > 35)
				throw new ApplicationException("Bitten geben Sie als weiteren Verwendungszweck maximal 35 Zeichen an");

			if (getTermin() == null)
				throw new ApplicationException("Bitte geben Sie einen Termin an, zu dem die Überweisung ausgeführt werden soll.");
  	}
  	catch (RemoteException e)
  	{
  		Application.getLog().error("error while checking ueberweisung",e);
  		throw new ApplicationException("Fehler beim Prüfen der Überweisung.");
  	}
			
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		try {
			if (ausgefuehrt() && !inExecute)
				throw new ApplicationException("Die Überweisung wurde bereits ausgeführt und kann daher nicht mehr geändert werden.");
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while checking ueberweisung",e);
			throw new ApplicationException("Fehler beim Prüfen der Überweisung.");
		}
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
		if ("empfaenger_id".equals(field))
			return Empfaenger.class;
		if ("konto_id".equals(field))
			return Konto.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getField("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getEmpfaenger()
   */
  public Empfaenger getEmpfaenger() throws RemoteException {
    return (Empfaenger) getField("empfaenger_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getField("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getZweck()
   */
  public String getZweck() throws RemoteException {
    return (String) getField("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getZweck2()
   */
  public String getZweck2() throws RemoteException {
		return (String) getField("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#getTermin()
   */
  public Date getTermin() throws RemoteException {
    return (Date) getField("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException {
		Integer i = (Integer) getField("ausgefuehrt");
		if (i == null)
			return false;
		return i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
		if (konto == null) return;
		setField("konto_id",new Integer(konto.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setEmpfaenger(de.willuhn.jameica.hbci.rmi.Empfaenger)
   */
  public void setEmpfaenger(Empfaenger empfaenger) throws RemoteException {
		if (empfaenger == null) return;
		setField("empgaenger_id",new Integer(empfaenger.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException {
		setField("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setField("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setField("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException {
		setField("termin",termin);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#execute()
   */
  public synchronized void execute() throws ApplicationException {

		try {
			if (isNewObject())
			{
				store();
			}
		}
		catch (RemoteException e)
		{
			throw new ApplicationException("Fehler beim Speichern der Überweisung.");
		}
	
		Passport p = null;
	
		try {
			p = getKonto().getPassport();
			p.open();

			// TODO: Hier den restlichen Ueberweisungskram rein.

			// wenn alles erfolgreich verlief, koennen wir die Ueberweisung auf
			// Status "ausgefuehrt" setzen.
			inExecute = true; // ist noetig, weil uns sonst das updateCheck() um die Ohren fliegt
			setField("ausgefuehrt",new Integer(1));
			store();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while executing ueberweisung",e);
			throw new ApplicationException("Fehler beim Ausfuehren der Ueberweisung");
		}
		finally {
			try {
				p.close();
			}
			catch (Exception e) {/*useless*/}
			inExecute = false;
		}
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException {
		if (isNewObject())
		{
			if (getTermin() == null) setTermin(new Date());
			setField("ausgefuehrt",new Integer(0));
		}
    super.store();
  }

}


/**********************************************************************
 * $Log: UeberweisungImpl.java,v $
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/