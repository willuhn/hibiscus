/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/04/27 22:23:56 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractDBObject implements Umsatz {

	I18N i18n;

  /**
   * @throws RemoteException
   */
  public UmsatzImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatz";
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
		// Die koennen eigentlich getrost geloescht werden.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		// Die Umsaetze werden nicht von Hand eingegeben sondern
		// gelangen via HBCI zu uns. Nichtsdestotrotz duerfen
		// wir nur die speichern, die vollstaendig sind.
		try {

			if (getBetrag() == 0.0)
				throw new ApplicationException(i18n.tr("Betrag fehlt."));

			if (getDatum() == null)
				throw new ApplicationException(i18n.tr("Datum fehlt."));

			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Umsatz muss einem Konto zugewiesen sein."));

			if (getValuta() == null)
				throw new ApplicationException(i18n.tr("Valuta fehlt."));

			if (getZweck() == null || getZweck().length() == 0)
				throw new ApplicationException(i18n.tr("Verwendungszweck fehlt."));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insertcheck in umsatz",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatzes"));
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
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getField("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerName()
   */
  public String getEmpfaengerName() throws RemoteException {
    return (String) getField("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerKonto()
   */
  public String getEmpfaengerKonto() throws RemoteException
  {
    return (String) getField("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerBLZ()
   */
  public String getEmpfaengerBLZ() throws RemoteException
  {
    return (String) getField("empfaenger_blz");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getField("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getDatum()
   */
  public Date getDatum() throws RemoteException {
		return (Date) getField("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getValuta()
   */
  public Date getValuta() throws RemoteException {
		return (Date) getField("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck()
   */
  public String getZweck() throws RemoteException {
		return (String) getField("zweck");
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck2()
	 */
	public String getZweck2() throws RemoteException {
		return (String) getField("zweck2");
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerName(java.lang.String)
   */
  public void setEmpfaengerName(String name) throws RemoteException {
		setField("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerKonto(java.lang.String)
   */
  public void setEmpfaengerKonto(String konto) throws RemoteException {
    setField("empfaenger_konto",konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerBLZ(java.lang.String)
   */
  public void setEmpfaengerBLZ(String blz) throws RemoteException {
    setField("empfaenger_blz",blz);
  }
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setBetrag(double)
   */
  public void setBetrag(double d) throws RemoteException {
		setField("betrag",new Double(d));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setField("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setField("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setDatum(java.util.Date)
   */
  public void setDatum(Date d) throws RemoteException {
		setField("datum",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setValuta(java.util.Date)
   */
  public void setValuta(Date d) throws RemoteException {
		setField("valuta",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto k) throws RemoteException {
		if (k == null)
			return;
		setField("konto_id",new Integer(k.getID()));
  }

  /**
   * Wir ueberschreiben die Funktion hier, weil beim Abrufen der
   * Umsaetze nur diejenigen gespeichert werden sollen, welche noch
   * nicht in der Datenbank existieren.
   * Da ein Umsatz von der Bank scheinbar keinen Identifier mitbringt,
   * muessen wir selbst einen fachlichen Vergleich durchfuehren.
   * @see de.willuhn.datasource.rmi.DBObject#equals(de.willuhn.datasource.rmi.DBObject)
   */
  public boolean equals(DBObject o) throws RemoteException {
		if (o == null)
			return false;
		try {
			Umsatz other = (Umsatz) o;
			return other.getCRC32() == getCRC32();
		}
		catch (Exception e)
		{
			return false;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getSaldo()
   */
  public double getSaldo() throws RemoteException {
		Double d = (Double) getField("saldo");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getPrimanota()
   */
  public String getPrimanota() throws RemoteException {
		return (String) getField("primanota");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getArt()
   */
  public String getArt() throws RemoteException {
		return (String) getField("art");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getCustomerRef()
   */
  public String getCustomerRef() throws RemoteException {
		return (String) getField("customerref");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setSaldo(double)
   */
  public void setSaldo(double s) throws RemoteException {
		setField("saldo",new Double(s));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setPrimanota(java.lang.String)
   */
  public void setPrimanota(String primanota) throws RemoteException {
		setField("primanota",primanota);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setArt(java.lang.String)
   */
  public void setArt(String art) throws RemoteException {
		setField("art",art);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setCustomerRef(java.lang.String)
   */
  public void setCustomerRef(String ref) throws RemoteException {
		setField("customerref",ref);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getCRC32()
   */
  public long getCRC32() throws RemoteException {
		String s = getArt() +
		           getBetrag() +
		           getCustomerRef() +
		           getEmpfaengerBLZ() +
		           getEmpfaengerKonto() +
		           getEmpfaengerName() +
		           getPrimanota() +
		           getSaldo() +
		           getZweck() +
		           getZweck2() +
		           HBCI.DATEFORMAT.format(getDatum()) + // komisch, das Datum wird bei toString() manchmal anders ausgegeben
							 HBCI.DATEFORMAT.format(getValuta());
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

}


/**********************************************************************
 * $Log: UmsatzImpl.java,v $
 * Revision 1.5  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.4  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 08:38:47  willuhn
 * @N umsaetze works now
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/