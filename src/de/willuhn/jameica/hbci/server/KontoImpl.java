/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/03/06 18:25:10 $
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
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Bildet eine Bankverbindung ab.
 */
public class KontoImpl extends AbstractDBObject implements Konto {

	private I18N i18n;
  /**
   * ct.
   * @throws RemoteException
   */
  public KontoImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
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

			if (getPassport() == null)
				throw new ApplicationException("Bitte wählen Sie ein Sicherheitsmedium aus.");

			if (getPassport().isNewObject())
				throw new ApplicationException("Bitte speichern Sie zunächst das Sicherheitsmedium.");

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

		Passport p = (Passport) getField("passport_id");
		if (p == null) return null;

		PassportType type = p.getPassportType();
		if (type == null) return null;

		String impl = type.getImplementor();
		// wir ja gleich die richtige Impl liefern
		try {
			Class implementor = MultipleClassLoader.load(impl);
			return (Passport) Settings.getDatabase().createObject(implementor,p.getID());
		}
		catch (ClassNotFoundException e)
		{
			throw new RemoteException("unable to find class " + impl,e);
		}

  }

  /**?
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
		// Wir muessen auch alle Umsaetze sowie Ueberweisungen mitloeschen
		// da Constraints dorthin existieren.
    try {
      this.transactionBegin();

			DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);
			Ueberweisung u = null;
			while (list.hasNext())
			{
				u = (Ueberweisung) list.next();
				u.delete();
			}

			list = Settings.getDatabase().createList(Umsatz.class);
			Umsatz ums = null;
			while (list.hasNext())
			{
				ums = (Umsatz) list.next();
				ums.delete();
			}

			// Jetzt koennen wir uns selbst loeschen
			super.delete();
      this.transactionCommit();
    }
    catch (RemoteException e)
    {
      this.transactionRollback();
      throw e;
    }
    catch (ApplicationException e2)
    {
      this.transactionRollback();
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSaldo()
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


		// Das machen wir um sicherzugehen, dass alle benoetigten Infos
		// des Kontos vorhanden sind.
		insertCheck();
		if (isNewObject())
		{
			throw new ApplicationException("Bitte speichern Sie zunächst das Konto.");
		}

		double saldo = JobFactory.getInstance().getSaldo(this);

		// Wenn wir fertig sind, muessen wir noch den Saldo und das Datum speichern
		setField("saldo",new Double(saldo));
		setField("saldo_datum",new Date());

		// und wir speichern uns
		store();
  }
  
	/**
   * @see de.willuhn.jameica.hbci.rmi.Konto#refreshUmsaetze()
   */
  public synchronized void refreshUmsaetze() throws ApplicationException,RemoteException {

		insertCheck();
		if (isNewObject())
			throw new ApplicationException("Bitte speichern Sie zunächst das Konto.");

		Umsatz[] umsaetze = JobFactory.getInstance().getAlleUmsaetze(this);

		// wir speichern die Umsaetze gleich noch ab
		try {
			for (int i=0;i<umsaetze.length;++i)
			{
				umsaetze[i].store();
			}
		}
		catch (ApplicationException e)
		{
			// Die fangen wir, weil z.Bsp. beim Speichern der Empfaenger ein
			// Fehler auftreten koennte und dann nur z.Bsp. "Zwecke fehlt" in
			// der Fehlermeldung stehen wuerde.
			throw new ApplicationException(i18n.tr("Fehler beim Aktualisieren der Umsätze.") + 
				" (" + e.getMessage() + ")");
		}
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSaldoDatum()
   */
  public Date getSaldoDatum() throws RemoteException {
    return (Date) getField("saldo_datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze()
   */
  public DBIterator getUmsaetze() throws RemoteException {
		DBIterator list = Settings.getDatabase().createList(Umsatz.class);
		list.addFilter("konto_id = " + getID() + "ORDER BY TONUMBER(datum)");
		return list;
  }
}


/**********************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.13  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.12  2004/03/05 08:38:47  willuhn
 * @N umsaetze works now
 *
 * Revision 1.11  2004/03/05 00:30:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.9  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.8  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
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