/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.25 $
 * $Date: 2004/06/30 20:58:28 $
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
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

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
   * @see de.willuhn.datasource.rmi.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
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

			if (!HBCIUtils.checkAccountCRC(getBLZ(),getKontonummer()))
				throw new ApplicationException("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben.");
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while insertcheck",e);
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
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getAttribute("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getAttribute("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getName()
   */
  public String getName() throws RemoteException {
		return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getPassport()
   */
  public Passport getPassport() throws RemoteException {

		String className = (String) getAttribute("passport_class");
		if (className == null)
			return null;

		try {
			return PassportRegistry.findByClass(className);
		}
		catch (Exception e)
		{
			throw new RemoteException("unable to load defined passport",e);
		}
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#setPassport(de.willuhn.jameica.hbci.passport.Passport)
   */
  public void setPassport(Passport passport) throws RemoteException {
		if (passport == null)
			return;
  	setField("passport_class",passport.getClass().getName());
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
		// Wir muessen auch alle Umsaetze, Ueberweisungen und Protokolle mitloeschen
		// da Constraints dorthin existieren.
    try {
      this.transactionBegin();

			// Erst die Ueberweisungen loeschen
			deleteUmsaetze();

			// und jetzt die Umsaetze
			DBIterator list = getUeberweisungen();
			Ueberweisung u = null;
			while (list.hasNext())
			{
				u = (Ueberweisung) list.next();
				u.delete();
			}

			// und noch die Protokolle
			list = getProtokolle();
			Protokoll p = null;
			while (list.hasNext())
			{
				p = (Protokoll) list.next();
				p.delete();
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
    return (String) getAttribute("waehrung");
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
    return (String) getAttribute("kundennummer");
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
		Double d = (Double) getAttribute("saldo");
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

		HBCIFactory factory = HBCIFactory.getInstance();
		HBCISaldoJob job = new HBCISaldoJob(this);
		factory.addJob(job);

		factory.executeJobs(getPassport().getHandle());

		// Wenn wir fertig sind, muessen wir noch den Saldo und das Datum speichern
		setField("saldo",new Double(job.getSaldo()));
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

		// Bei der Gelegenheit koennen wir auch gleich noch den Saldo
		// aktualisieren
		HBCIFactory factory = HBCIFactory.getInstance();
		HBCISaldoJob job1  = new HBCISaldoJob(this);
		HBCIUmsatzJob job2 = new HBCIUmsatzJob(this);

		factory.addJob(job1);
		factory.addJob(job2);

		factory.executeJobs(getPassport().getHandle());

		// Speichern des Saldo
		setField("saldo",new Double(job1.getSaldo()));
		setField("saldo_datum",new Date());
		store();

		Umsatz[] umsaetze = job2.getUmsaetze();

		// Wir vergleichen noch mit den Umsaetzen, die wir schon haben und
		// speichern nur die neuen.
		DBIterator existing = getUmsaetze();

		// wir speichern die Umsaetze gleich noch ab
		try {
			for (int i=0;i<umsaetze.length;++i)
			{
				if (existing.contains(umsaetze[i]) == null)
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
    return (Date) getAttribute("saldo_datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze()
   */
  public DBIterator getUmsaetze() throws RemoteException {
		DBIterator list = Settings.getDatabase().createList(Umsatz.class);
		list.addFilter("konto_id = " + getID() + " ORDER BY TONUMBER(datum) DESC");
		return list;
  }


	/**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUeberweisungen()
   */
  public DBIterator getUeberweisungen() throws RemoteException {
		DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);
		list.addFilter("konto_id = " + getID() + " ORDER BY TONUMBER(termin) DESC");
		return list;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#deleteUmsaetze()
   */
  public void deleteUmsaetze() throws ApplicationException, RemoteException {
		DBIterator list = Settings.getDatabase().createList(Umsatz.class);
		list.addFilter("konto_id = " + getID());
		if (!list.hasNext())
			return;

		while (list.hasNext())
		{
			((DBObject)list.next()).delete();
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getBezeichnung()
   */
  public String getBezeichnung() throws RemoteException {
    return (String) getAttribute("bezeichnung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setBezeichnung(java.lang.String)
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException {
		setField("bezeichnung",bezeichnung);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getProtokolle()
   */
  public DBIterator getProtokolle() throws RemoteException {
		DBIterator list = Settings.getDatabase().createList(Protokoll.class);
		list.addFilter("konto_id = " + getID() + " ORDER BY TONUMBER(datum) DESC");
		return list;
  }
  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException {
    super.insert();
    
    // Wenn das erfolgreich lief, protokollieren wir gleich die Neuanlage
		try {
			Protokoll entry = (Protokoll) Settings.getDatabase().createObject(Protokoll.class,null);
			entry.setKonto(this);
			entry.setKommentar(i18n.tr("Konto angelegt"));
			entry.setTyp(Protokoll.TYP_SUCCESS);
			entry.store();
		}
		catch (Exception e)
		{
			// Es macht keinen Sinn, hier die Exception nach oben zu reichen.
			// Was sollte in diesem Fall sinnvolles gemacht werden? Den gesamten
			// HBCI-Job abbrechen? Nene, dann lieber auf den Log-Eintrag verzichten
			// und nur ins Application-Log schreiben. ;)
			Logger.error("error while writing protocol",e);
		}

  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException {
    super.store();
		try {
			Protokoll entry = (Protokoll) Settings.getDatabase().createObject(Protokoll.class,null);
			entry.setKonto(this);
			entry.setKommentar(i18n.tr("Konto aktualisiert"));
			entry.setTyp(Protokoll.TYP_SUCCESS);
			entry.store();
		}
		catch (Exception e)
		{
			Logger.error("error while writing protocol",e);
		}
  }

}


/**********************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.25  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.23  2004/06/07 22:22:33  willuhn
 * @B Spalte "Passport" in KontoListe entfernt - nicht mehr noetig
 *
 * Revision 1.22  2004/06/03 00:23:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.20  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/05/04 23:07:24  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.18  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.17  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/03/19 01:44:13  willuhn
 * *** empty log message ***
 *
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