/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzImpl.java,v $
 * $Revision: 1.30 $
 * $Date: 2005/12/13 00:06:31 $
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

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.rmi.UmsatzZuordnung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractDBObject implements Umsatz
{

	private I18N i18n;

  /**
   * @throws RemoteException
   */
  public UmsatzImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatz";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
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
		}
		catch (RemoteException e)
		{
			Logger.error("error while insertcheck in umsatz",e);
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
		if ("umsatztyp_id".equals(field))
			return UmsatzTyp.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerName()
   */
  public String getEmpfaengerName() throws RemoteException {
    return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerKonto()
   */
  public String getEmpfaengerKonto() throws RemoteException
  {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaengerBLZ()
   */
  public String getEmpfaengerBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_blz");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getDatum()
   */
  public Date getDatum() throws RemoteException {
		return (Date) getAttribute("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getValuta()
   */
  public Date getValuta() throws RemoteException {
		return (Date) getAttribute("valuta");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck()
   */
  public String getZweck() throws RemoteException {
		return (String) getAttribute("zweck");
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck2()
	 */
	public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaenger(de.willuhn.jameica.hbci.rmi.Adresse)
	 */
	public void setEmpfaenger(Adresse empf) throws RemoteException
	{
		setEmpfaengerBLZ(empf.getBLZ());
		setEmpfaengerKonto(empf.getKontonummer());
		setEmpfaengerName(empf.getName());
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerName(java.lang.String)
   */
  public void setEmpfaengerName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerKonto(java.lang.String)
   */
	public void setEmpfaengerKonto(String konto) throws RemoteException {
    setAttribute("empfaenger_konto",konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaengerBLZ(java.lang.String)
   */
	public void setEmpfaengerBLZ(String blz) throws RemoteException {
    setAttribute("empfaenger_blz",blz);
  }
  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setBetrag(double)
   */
  public void setBetrag(double d) throws RemoteException {
		setAttribute("betrag",new Double(d));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setDatum(java.util.Date)
   */
  public void setDatum(Date d) throws RemoteException {
		setAttribute("datum",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setValuta(java.util.Date)
   */
  public void setValuta(Date d) throws RemoteException {
		setAttribute("valuta",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto k) throws RemoteException {
		setAttribute("konto_id",k);
  }

  /**
   * Wir ueberschreiben die Funktion hier, weil beim Abrufen der
   * Umsaetze nur diejenigen gespeichert werden sollen, welche noch
   * nicht in der Datenbank existieren.
   * Da ein Umsatz von der Bank scheinbar keinen Identifier mitbringt,
   * muessen wir selbst einen fachlichen Vergleich durchfuehren.
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject o) throws RemoteException {
		if (o == null)
			return false;
		try {
			Umsatz other = (Umsatz) o;
			return other.getChecksum() == getChecksum();
		}
		catch (ClassCastException e)
		{
			return false;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getSaldo()
   */
  public double getSaldo() throws RemoteException {
		Double d = (Double) getAttribute("saldo");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getPrimanota()
   */
  public String getPrimanota() throws RemoteException {
		return (String) getAttribute("primanota");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getArt()
   */
  public String getArt() throws RemoteException {
		return (String) getAttribute("art");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getCustomerRef()
   */
  public String getCustomerRef() throws RemoteException {
		return (String) getAttribute("customerref");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setSaldo(double)
   */
  public void setSaldo(double s) throws RemoteException {
		setAttribute("saldo",new Double(s));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setPrimanota(java.lang.String)
   */
  public void setPrimanota(String primanota) throws RemoteException {
		setAttribute("primanota",primanota);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setArt(java.lang.String)
   */
  public void setArt(String art) throws RemoteException {
		setAttribute("art",art);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setCustomerRef(java.lang.String)
   */
  public void setCustomerRef(String ref) throws RemoteException {
		setAttribute("customerref",ref);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException {

    Number n = (Number) this.getAttribute("checksum");
    if (n != null && n.longValue() != 0)
      return n.longValue();

    String s = (""+getArt()).toUpperCase() +
		           getBetrag() +
		           getKonto().getChecksum() +
		           getCustomerRef() +
		           getEmpfaengerBLZ() +
		           getEmpfaengerKonto() +
		           (""+getEmpfaengerName()).toUpperCase() +
		           getPrimanota() +
		           getSaldo() +
		           (""+getZweck()).toUpperCase() +
		           (""+getZweck2()).toUpperCase() +
		           HBCI.DATEFORMAT.format(getDatum()) +
							 HBCI.DATEFORMAT.format(getValuta());
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
    return crc.getValue();
  }

  /**
   * Ueberschrieben, um ein synthetisches Attribute "mergedzweck" zu erzeugen.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("mergedzweck".equals(arg0))
      return getZweck() + (getZweck2() != null ? getZweck2() : "");

    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    if ("empfaenger".equals(arg0))
    {
      String name = getEmpfaengerName();
      if (name != null)
        return name;

      String kto = getEmpfaengerKonto();
      String blz = getEmpfaengerBLZ();
      if (kto == null || blz == null)
        return null;

      return i18n.tr("Kto. {0}, BLZ {1}", new String[]{kto,blz});
    }

    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.rmi.Changeable#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    // BUGZILLA #70 http://www.willuhn.de/bugzilla/show_bug.cgi?id=70
    Konto k = getKonto();
    String[] fields = new String[]
    {
      getEmpfaengerName(),
      getEmpfaengerKonto(),
      getEmpfaengerBLZ(),
      HBCI.DATEFORMAT.format(getValuta()),
      getZweck(),
      k.getWaehrung() + " " + HBCI.DECIMALFORMAT.format(getBetrag())
    };
    String msg = i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Valuta {3}, Zweck: {4}] {5} gelöscht",fields);

    try {
      this.transactionBegin();
    
      // wir entfernen auch alle Zuordnungen
      DBIterator list = getUmsatzZuordnungen();
      UmsatzZuordnung u = null;
      while (list.hasNext())
      {
        u = (UmsatzZuordnung) list.next();
        u.delete();
      }

      // Jetzt koennen wir uns selbst loeschen
      super.delete();
      this.transactionCommit();
      k.addToProtokoll(msg,Protokoll.TYP_SUCCESS);
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
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getKommentar()
   */
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#hasChangedByUser()
   */
  public boolean hasChangedByUser() throws RemoteException
  {
    Number n = (Number) this.getAttribute("checksum");
    return (n != null && n.longValue() != 0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setChangedByUser()
   */
  public void setChangedByUser() throws RemoteException
  {
    if (hasChangedByUser())
      return; // wurde schon markiert
    setAttribute("checksum",new Long(getChecksum()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getUmsatzZuordnungen()
   */
  public DBIterator getUmsatzZuordnungen() throws RemoteException
  {
    DBIterator list = getService().createList(UmsatzZuordnung.class);
    list.addFilter("umsatz_id = " + getID());
    return list;
  }
}


/**********************************************************************
 * $Log: UmsatzImpl.java,v $
 * Revision 1.30  2005/12/13 00:06:31  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.29  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.28  2005/11/14 23:47:20  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.27  2005/06/30 21:48:56  web0
 * @B bug 75
 *
 * Revision 1.26  2005/06/27 14:37:14  web0
 * @B bug 75
 *
 * Revision 1.25  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.24  2005/06/13 23:11:01  web0
 * *** empty log message ***
 *
 * Revision 1.23  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.22  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.21  2005/05/30 14:25:48  web0
 * *** empty log message ***
 *
 * Revision 1.20  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.19  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.18  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.17  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.14  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.13  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.12  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.11  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.9  2004/07/04 17:07:59  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
 * Revision 1.8  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.6  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
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