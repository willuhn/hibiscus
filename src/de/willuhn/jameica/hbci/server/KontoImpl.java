/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.38 $
 * $Date: 2004/11/12 18:25:07 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "konto";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
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
			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitten geben Sie den Namen des Kontoinhabers ein."));

			if (getKontonummer() == null || getKontonummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kontonummer ein."));

			if (getBLZ() == null || getBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bankleitzahl ein."));

			if (getKundennummer() == null || getKundennummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie Ihre Kundennummer ein."));

			if (getPassport() == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Sicherheitsmedium aus."));

			if (!HBCIUtils.checkAccountCRC(getBLZ(),getKontonummer()))
				throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while insertcheck",e);
			throw new ApplicationException(i18n.tr("Fehler bei der Prüfung der Daten"));
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
		setAttribute("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setAttribute("blz",blz);
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#setName(java.lang.String)
	 */
	public void setName(String name) throws RemoteException {
		setAttribute("name",name);
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setPassport(de.willuhn.jameica.hbci.passport.Passport)
   */
  public void setPassport(Passport passport) throws RemoteException {
		if (passport == null)
			return;
  	setAttribute("passport_class",passport.getClass().getName());
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

			// Erst die Umsaetze loeschen
			deleteUmsaetze();
			
			// dann die Dauerauftraege
			DBIterator list = getDauerauftraege();
			if (!list.hasNext())
				return;

			while (list.hasNext())
			{
				((DBObject)list.next()).delete();
			}

			// und jetzt die Ueberweisungen
			list = getUeberweisungen();
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
    setAttribute("waehrung",waehrung);
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
		setAttribute("kundennummer",kundennummer);
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSaldoDatum()
   */
  public Date getSaldoDatum() throws RemoteException {
    return (Date) getAttribute("saldo_datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze()
   */
  public DBIterator getUmsaetze() throws RemoteException {
		DBIterator list = Settings.getDBService().createList(Umsatz.class);
		list.addFilter("konto_id = " + getID() + " ORDER BY TONUMBER(datum) DESC");
		return list;
  }


	/**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUeberweisungen()
   */
  public DBIterator getUeberweisungen() throws RemoteException {
		DBIterator list = Settings.getDBService().createList(Ueberweisung.class);
		list.addFilter("konto_id = " + getID() + " ORDER BY TONUMBER(termin) DESC");
		return list;
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#getDauerauftraege()
	 */
	public DBIterator getDauerauftraege() throws RemoteException
	{
		DBIterator list = Settings.getDBService().createList(Dauerauftrag.class);
		list.addFilter("konto_id = " + getID());
		return list;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#deleteUmsaetze()
   */
  public void deleteUmsaetze() throws ApplicationException, RemoteException {
		DBIterator list = Settings.getDBService().createList(Umsatz.class);
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
		setAttribute("bezeichnung",bezeichnung);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getProtokolle()
   */
  public DBIterator getProtokolle() throws RemoteException {
		DBIterator list = Settings.getDBService().createList(Protokoll.class);
		list.addFilter("konto_id = " + getID() + " ORDER BY TONUMBER(datum) DESC");
		return list;
  }
  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException {
    super.insert();
		addToProtokoll(i18n.tr("Konto angelegt"), Protokoll.TYP_SUCCESS);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException {
    super.store();
		addToProtokoll(i18n.tr("Konto aktualisiert"), Protokoll.TYP_SUCCESS);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#addToProtokoll(java.lang.String, int)
   */
  public final void addToProtokoll(String kommentar, int protokollTyp) throws RemoteException
  {
		if (kommentar == null || kommentar.length() == 0)
			return;

		try {
			Protokoll entry = (Protokoll) Settings.getDBService().createObject(Protokoll.class,null);
			entry.setKonto(this);
			entry.setKommentar(kommentar);
			entry.setTyp(protokollTyp);
			entry.store();
		}
		catch (Exception e)
		{
			Logger.error("error while writing protocol",e);
		}
  }

  /**
   * Die Funktion ueberschreiben wir um ein zusaetzliches virtuelles
   * Attribut "longname" einzufuehren. Bei Abfrage dieses Attributs
   * wird "[Kontonummer] Bezeichnung" zurueckgeliefert.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
  	if ("longname".equals(arg0))
  		return "[" + getKontonummer() + "] " + getBezeichnung();

    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
		String s = getBLZ() +
							 getKontonummer() +
							 getKundennummer();
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setSaldo(double, java.util.Date)
   */
  public void setSaldo(double saldo) throws RemoteException
  {
		setAttribute("saldo",new Double(saldo));
		setAttribute("saldo_datum",new Date());
  }
}


/**********************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.38  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.34  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.32  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.31  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.30  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.29  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.27  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.26  2004/07/04 17:07:59  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
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