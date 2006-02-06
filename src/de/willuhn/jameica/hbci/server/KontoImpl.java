/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.60 $
 * $Date: 2006/02/06 14:53:39 $
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
import java.util.Calendar;
import java.util.Date;
import java.util.zip.CRC32;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
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
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitten geben Sie den Namen des Kontoinhabers ein."));

      if (getName().length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
        throw new ApplicationException(i18n.tr("Bitte geben Sie maximal {0} Zeichen für den Namen des Kontoinhabers ein",""+HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));

      if (getKontonummer() == null || getKontonummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kontonummer ein."));

			if (getBLZ() == null || getBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bankleitzahl ein."));

			if (getKundennummer() == null || getKundennummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie Ihre Kundennummer ein."));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      if (getWaehrung() == null || getWaehrung().length() != 3)
        throw new ApplicationException(i18n.tr("Bitte verwenden Sie einen 3-Buchstaben-Währungscode Z.Bsp. \"EUR\"."));

			if (!HBCIProperties.checkAccountCRC(getBLZ(),getKontonummer()))
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#getPassportClass()
   */
  public String getPassportClass() throws RemoteException {
		return (String) getAttribute("passport_class");
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#setPassportClass(java.lang.String)
   */
  public void setPassportClass(String passport) throws RemoteException {
  	setAttribute("passport_class",passport);
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

      // BUGZILLA #70 http://www.willuhn.de/bugzilla/show_bug.cgi?id=70
			// Erst die Umsaetze loeschen
      DBIterator list = getUmsaetze();
      Umsatz um = null;
      while (list.hasNext())
      {
        um = (Umsatz) list.next();
        um.delete();
      }
			
			// dann die Dauerauftraege
			list = getDauerauftraege();
			Dauerauftrag da = null;
			while (list.hasNext())
			{
				da = (Dauerauftrag) list.next();
				da.delete();
			}

			// noch die Lastschriften
			list = getLastschriften();
			Lastschrift ls = null;
			while (list.hasNext())
			{
				ls = (Lastschrift) list.next();
				ls.delete();
			}

			// und die Sammel-Lastschriften
			list = getSammelLastschriften();
			SammelLastschrift sls = null;
			while (list.hasNext())
			{
				sls = (SammelLastschrift) list.next();
				sls.delete();
			}

			// und jetzt die Ueberweisungen
			list = getUeberweisungen();
			Ueberweisung u = null;
			while (list.hasNext())
			{
				u = (Ueberweisung) list.next();
				u.delete();
			}

      // und jetzt die Sammel-Ueberweisungen
      list = getSammelUeberweisungen();
      SammelUeberweisung su = null;
      while (list.hasNext())
      {
        su = (SammelUeberweisung) list.next();
        su.delete();
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
		DBIterator list = getService().createList(Umsatz.class);
		list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY TONUMBER(valuta), id DESC");
		return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze(int)
   */
  public DBIterator getUmsaetze(int days) throws RemoteException {
    if (days <= 0)
      return getUmsaetze();

    long d = days * 24l * 60l * 60l * 1000l;
    DBIterator list = getService().createList(Umsatz.class);
    list.addFilter("konto_id = " + getID());
    list.addFilter("TONUMBER(valuta) > " + (System.currentTimeMillis() - d));
    list.setOrder("ORDER BY TONUMBER(valuta), id DESC");
    return list;
  }

	/**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUeberweisungen()
   */
  public DBIterator getUeberweisungen() throws RemoteException {
		DBIterator list = getService().createList(Ueberweisung.class);
		list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY TONUMBER(termin) DESC");
		return list;
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#getDauerauftraege()
	 */
	public DBIterator getDauerauftraege() throws RemoteException
	{
		DBIterator list = getService().createList(Dauerauftrag.class);
		list.addFilter("konto_id = " + getID());
		return list;
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#getLastschriften()
	 */
	public DBIterator getLastschriften() throws RemoteException
	{
		DBIterator list = getService().createList(Lastschrift.class);
		list.addFilter("konto_id = " + getID());
		return list;
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#getSammelLastschriften()
	 */
	public DBIterator getSammelLastschriften() throws RemoteException
	{
		DBIterator list = getService().createList(SammelLastschrift.class);
		list.addFilter("konto_id = " + getID());
		return list;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSammelUeberweisungen()
   */
  public DBIterator getSammelUeberweisungen() throws RemoteException
  {
    DBIterator list = getService().createList(SammelUeberweisung.class);
    list.addFilter("konto_id = " + getID());
    return list;
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
		DBIterator list = getService().createList(Protokoll.class);
		list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY TONUMBER(datum) DESC");
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
    if (hasChanged())
      addToProtokoll(i18n.tr("Konto-Eigenschaften aktualisiert"), Protokoll.TYP_SUCCESS);
    super.store();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#addToProtokoll(java.lang.String, int)
   */
  public final void addToProtokoll(String kommentar, int protokollTyp) throws RemoteException
  {
		if (kommentar == null || kommentar.length() == 0 || this.getID() == null)
			return;

		try {
			Protokoll entry = (Protokoll) getService().createObject(Protokoll.class,null);
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#setSaldo(double)
   */
  public void setSaldo(double saldo) throws RemoteException
  {
		setAttribute("saldo",new Double(saldo));
		setAttribute("saldo_datum",new Date());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getFirstUmsatz()
   */
  public Umsatz getFirstUmsatz() throws RemoteException
  {
    DBIterator list = getService().createList(Umsatz.class);
    list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY TONUMBER(valuta) ASC");
    if (!list.hasNext())
      return null;
    return (Umsatz) list.next();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getLastUmsatz()
   */
  public Umsatz getLastUmsatz() throws RemoteException
  {
    DBIterator list = getService().createList(Umsatz.class);
    list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY TONUMBER(valuta) DESC");
    if (!list.hasNext())
      return null;
    return (Umsatz) list.next();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSynchronize()
   */
  public boolean getSynchronize() throws RemoteException
  {
    Integer i = (Integer) getAttribute("synchronize");
    if (i == null)
      return true;
    return i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setSynchronize(boolean)
   */
  public void setSynchronize(boolean b) throws RemoteException
  {
    setAttribute("synchronize", new Integer(b ? 1 : 0));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getAusgaben(java.util.Date, java.util.Date)
   */
  public double getAusgaben(Date from, Date to) throws RemoteException
  {
    return getSumme(from, to, true);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getEinnahmen(java.util.Date, java.util.Date)
   */
  public double getEinnahmen(Date from, Date to) throws RemoteException
  {
    return getSumme(from, to, false);
  }

  /**
   * Hilfsfunktion fuer Berechnung der Einnahmen und Ausgaben.
   * @param from
   * @param to
   * @param ausgaben
   * @return Summe.
   * @throws RemoteException
   */
  private double getSumme(Date from, Date to, boolean ausgaben) throws RemoteException
  {
    // Zuruecksetzen der Uhrzeit auf 0:00
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    cal.set(Calendar.HOUR_OF_DAY,0);
    cal.set(Calendar.MINUTE,0);
    cal.set(Calendar.SECOND,0);
    
    Date start = cal.getTime();
    
    // Setzen der Uhrzeit auf 23:59:59
    cal.setTime(to);
    cal.set(Calendar.HOUR_OF_DAY,23);
    cal.set(Calendar.MINUTE,59);
    cal.set(Calendar.SECOND,59);
    
    Date end = cal.getTime();

    double sum = 0d;
    DBIterator buchungen = Settings.getDBService().createList(Umsatz.class);
    buchungen.addFilter("konto_id = " + this.getID());
    buchungen.addFilter("tonumber(datum) >= " + start.getTime() + " AND tonumber(datum) <= " + end.getTime());
    if (ausgaben) buchungen.addFilter("betrag < 0");
    else          buchungen.addFilter("betrag > 0");
    while (buchungen.hasNext())
    {
      Umsatz u = (Umsatz) buchungen.next();
      sum += u.getBetrag();
    }
    return sum;
    
  }
}


/**********************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.60  2006/02/06 14:53:39  willuhn
 * @N new column "#" in umsatzlist
 *
 * Revision 1.59  2006/01/23 11:11:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.58  2005/11/10 23:32:59  willuhn
 * @B foreign key to sueberweisung when deleting a konto
 *
 * Revision 1.57  2005/10/17 13:01:59  willuhn
 * @N Synchronize auf Start-Seite verschoben
 * @N Gesamt-Vermoegensuebersicht auf Start-Seite
 *
 * Revision 1.56  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.55  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 * Revision 1.54  2005/07/11 13:51:49  web0
 * *** empty log message ***
 *
 * Revision 1.53  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.52  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.51  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.50  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.49  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.48  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.47  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.46  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.45  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.44  2005/02/20 19:04:44  web0
 * @B Bug 7
 *
 * Revision 1.43  2005/02/03 23:57:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.42  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.41  2005/02/02 18:19:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.40  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.39  2004/11/15 18:09:18  willuhn
 * @N Login fuer die gesamte Anwendung
 *
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