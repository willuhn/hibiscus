/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/02/17 01:01:38 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.ApplicationException;

/**
 * Bildet eine Bankverbindung ab.
 */
public class KontoImpl extends AbstractDBObject implements Konto {

  /**
   * ct.
   * @throws RemoteException
   */
  public KontoImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "konto";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "kontonummer";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
    // TODO Pruefen, ob Buchungen o.ae. vorliegen
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || "".equals(getName()))
				throw new ApplicationException("Bitten geben Sie den Namen des Kontoinhabers ein.");

			if (getKontonummer() == null || "".equals(getKontonummer()))
				throw new ApplicationException("Bitte geben Sie eine Kontonummer ein.");

			if (getBLZ() == null || "".equals(getBLZ()))
				throw new ApplicationException("Bitte geben Sie eine Bankleitzahl ein.");

			if (!HBCIUtils.checkAccountCRC(getBLZ(),getKontonummer()))
				throw new ApplicationException("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben.");
			
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insertcheck",e);
			throw new ApplicationException("Fehler bei der Prüfung der Daten");
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
  	if ("passport_id".equals(field))
  		return Passport.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getField("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getField("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getName()
   */
  public String getName() throws RemoteException {
		return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getPassport()
   */
  public Passport getPassport() throws RemoteException {
		if (isNewObject())
			return (Passport) getField("passport_id");

		return PassportFactory.create(this);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException {
		setField("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setField("blz",blz);
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#setName(java.lang.String)
	 */
	public void setName(String name) throws RemoteException {
		setField("name",name);
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setPassport(de.willuhn.jameica.hbci.rmi.Passport)
   */
  public void setPassport(Passport passport) throws RemoteException {
		if (passport == null)
			return;
  	setField("passport_id",new Integer(passport.getID()));
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    // Wir muessen die PassportParameter mit loeschen
    try {
      transactionBegin();
      getPassport().delete();
      super.delete();
      transactionCommit();
    }
    catch (RemoteException e)
    {
      transactionRollback();
      throw e;
    }
    catch (ApplicationException e2)
    {
      transactionRollback();
      throw e2;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getWaehrung()
   */
  public String getWaehrung() throws RemoteException
  {
    return (String) getField("waehrung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setWaehrung(java.lang.String)
   */
  public void setWaehrung(String waehrung) throws RemoteException
  {
    setField("waehrung",waehrung);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKundennummer()
   */
  public String getKundennummer() throws RemoteException {
    return (String) getField("kundennummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setKundennummer(java.lang.String)
   */
  public void setKundennummer(String kundennummer) throws RemoteException {
		setField("kundennummer",kundennummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#readFromPassport()
   */
  public void readFromPassport() throws RemoteException {

		String own = getKontonummer();
		if (own == null || own.length() == 0)
			return;

		Passport p = getPassport();
		
		Konto[] konten = p.getKonten();
		if (konten == null || konten.length == 0)
			return;

		for (int i=0;i<konten.length;++i)
		{
			if (own.equals(konten[i].getKontonummer()))
			{
				// Konto gefunden. Wir ueberschreiben unsere Einstellungen mit denen des Kontos
				this.setBLZ(konten[i].getBLZ()); 
				this.setKundennummer(konten[i].getKundennummer());
				this.setName(konten[i].getName());
				this.setWaehrung(konten[i].getWaehrung());
			}
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSald()
   */
  public double getSaldo() throws RemoteException {
		Double d = (Double) getField("saldo");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#refreshSaldo()
   */
  public synchronized void refreshSaldo() throws ApplicationException,RemoteException {


		if (isNewObject())
		{
			// Das machen wir um sicherzugehen, dass alle benoetigten Infos
			// des Kontos vorhanden sind.
			throw new ApplicationException("Bitte speichern Sie zunächst das Konto.");
		}

		// Bevor wir anfangen, muessen wir erstmal das Konto speichern
		// Das checkt gleich, dass alles eingegeben wurde.
		store();

		double saldo = JobFactory.getSaldo(this);

		// Wenn wir fertig sind, muessen wir noch den Saldo und das Datum speichern
		setField("saldo",new Double(saldo));
		setField("saldo_datum",new Date());
		store();
  }
  
  /**
   * Liefert die Konto-Repraesentation des Kontos im HBCI-Kernel.
   * @return Konto.
   */
  protected org.kapott.hbci.structures.Konto getHBCIKonto() throws RemoteException
  {
		
  	org.kapott.hbci.structures.Konto k =
  		new org.kapott.hbci.structures.Konto(getBLZ(),getKontonummer());
  	k.country = "DE";
  	k.curr = getWaehrung();
  	k.customerid = getKundennummer();
  	k.name = getName();
		return k;  	
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSaldoDatum()
   */
  public Date getSaldoDatum() throws RemoteException {
    return (Date) getField("saldo_datum");
  }
}


/**********************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.7  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 10:33:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/