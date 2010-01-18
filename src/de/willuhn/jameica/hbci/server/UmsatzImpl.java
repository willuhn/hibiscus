/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzImpl.java,v $
 * $Revision: 1.69 $
 * $Date: 2010/01/18 22:59:05 $
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
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.CRC32;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractDBObject implements Umsatz
{

	private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
	/**
   * Cache fuer die Umsatz-Kategorien.
   */
  public transient final static Hashtable UMSATZTYP_CACHE = new Hashtable();

  /**
   * @throws RemoteException
   */
  public UmsatzImpl() throws RemoteException {
    super();
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

		  double betrag = getBetrag();
			if (betrag == 0.0 || Double.isNaN(betrag))
				throw new ApplicationException(i18n.tr("Betrag fehlt."));

			if (getDatum() == null)
				throw new ApplicationException(i18n.tr("Datum fehlt."));

			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Umsatz muss einem Konto zugewiesen sein."));

			if (getValuta() == null)
				throw new ApplicationException(i18n.tr("Valuta fehlt."));
			
			// "35" ist das DB-Limit
      HBCIProperties.checkLength(getZweck(),35);
      HBCIProperties.checkLength(getZweck2(),35);
      String[] ewz = getWeitereVerwendungszwecke();
      if (ewz != null && ewz.length > 0)
      {
        for (int i=0;i<ewz.length;++i)
        {
          HBCIProperties.checkLength(ewz[i],35);
        }
      }
			
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
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    // Wir speichern die Checksumme nun grundsaetzlich beim
    // Anlegen des Datensatzes. Dann koennen wir anschliessend
    // beliebig aendern und muessens uns nicht mehr mit
    // "hasChangedByUser" herumschlagen
    setAttribute("checksum",new Long(getChecksum()));
    super.insert();
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
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException {
    return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_blz");
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
	 * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkonto(de.willuhn.jameica.hbci.rmi.Address)
	 */
	public void setGegenkonto(Address empf) throws RemoteException
	{
		setGegenkontoBLZ(empf.getBlz());
		setGegenkontoNummer(empf.getKontonummer());
		setGegenkontoName(empf.getName());
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoNummer(java.lang.String)
	 */
	public void setGegenkontoNummer(String konto) throws RemoteException {
    setAttribute("empfaenger_konto",konto);
  }
  
	/**
	 * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoBLZ(java.lang.String)
	 */
	public void setGegenkontoBLZ(String blz) throws RemoteException {
    setAttribute("empfaenger_blz",blz);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setBetrag(double)
   */
  public void setBetrag(double d) throws RemoteException {
		setAttribute("betrag",new Double(d));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck2(java.lang.String)
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
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
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
		if (o == null || !(o instanceof Umsatz))
			return false;
		try {
			Umsatz other = (Umsatz) o;
			return other.getChecksum() == getChecksum();
		}
		catch (Exception e)
		{
      Logger.error("error while comparing objects",e);
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

    // BUGZILLA 184
    Date datum   = getDatum();
    Date valuta  = getValuta();
    
    String sd  = "";
    String sv  = "";
    
    if (datum != null)
    {
      try {
        sd = HBCI.DATEFORMAT.format(datum);
      }
      catch (Exception e) {
        sd = datum.toString();
      }
    }
    if (valuta != null)
    {
      try {
        sv = HBCI.DATEFORMAT.format(valuta);
      }
      catch (Exception e) {
        sv = valuta.toString();
      }
    }

    // TODO: getKonto().getChecksum() duerfte eigentlich nicht in der Checksumme enthalten sein.
    // Denn wenn das Konto veraendert wird, aendern sich dabei auch die Checksummen der Umsaetze.
    // Das kann die Umsatzdoppler erklaeren. Da die Checksumme aber in der Datenbank gespeichert
    // ist, kann ich das hier nicht einfach umbauen. Ich muesste eine Datenbank-Migration machen,
    // in der die Checksummen ALLER Umsaetze (oder wenigstens die des Merge-Window beim Abruf)
    // neu berechnet werden. Sollte irgendwann mal gemacht werden
    String s = (""+getArt()).toUpperCase() +
		           getBetrag() +
		           getKonto().getChecksum() +
		           getCustomerRef() +
		           getGegenkontoBLZ() +
		           getGegenkontoNummer() +
		           (""+getGegenkontoName()).toUpperCase() +
		           getPrimanota() +
		           getSaldo() +
		           (""+getZweck()).toUpperCase() +
		           (""+getZweck2()).toUpperCase() +
		           sd +
							 sv;
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
    if ("umsatztyp".equals(arg0))
      return getUmsatzTyp();
    
    // Fuer Kategoriebaum
    if ("name".equals(arg0))
      return getGegenkontoName();

    if ("valuta_pseudo".equals(arg0))
      return getPseudoDate(getValuta());
    if ("datum_pseudo".equals(arg0))
      return getPseudoDate(getDatum());

    // Casten der ID nach INT, damit die Sortierung
    // numerisch statt alphanumerisch erfolgt
    // Wird von der Umsatzliste genutzt
    if ("id-int".equals(arg0))
    {
      try
      {
        return new Integer(getID());
      }
      catch (Exception e)
      {
        Logger.error("unable to parse id: " + getID());
        return getID();
      }
    }
    if ("mergedzweck".equals(arg0))
      return getZweck() + (getZweck2() != null ? getZweck2() : "");

    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    if ("empfaenger".equals(arg0))
    {
      String name = getGegenkontoName();
      if (name != null)
        return name;

      String kto = getGegenkontoNummer();
      String blz = getGegenkontoBLZ();
      if (kto == null || kto.length() == 0 || blz == null || blz.length() == 0)
        return null;

      return i18n.tr("Kto. {0}, BLZ {1}", new String[]{kto,blz});
    }

    return super.getAttribute(arg0);
  }
  
  /**
   * BUGZILLA 394
   * Haengt an das Datumsfeld eine Pseudouhrzeit basierend auf der ID des Datensatzes an.
   * Damit wird bei der Sortierung nach diesem Wert auch die "natuerliche Reihenfolge"
   * der Umsaetze beruecksichtigt, in der die Daten von der Bank geliefert werden.
   * Auch dann, wenn nur nach dem Datum sortiert wird.
   * @param date das Datum.
   * @return das Datum, erweitert um eine Pseudo-Uhrzeit.
   * @throws RemoteException
   */
  private Date getPseudoDate(Date date) throws RemoteException
  {
    if (date == null) // Einige Banken liefern weder Datum noch Valuta
      return null;

    try
    {
      return new Date(date.getTime() + Long.parseLong(getID()));
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable append pseudo time to " + date + " for ID: " + getID());
      return date;
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    try
    {
      this.transactionBegin();

      Konto k = this.getKonto();

      super.delete();
      
      if (k != null)
      {
        String[] fields = new String[] {
          getGegenkontoName(),
          getGegenkontoNummer(),
          getGegenkontoBLZ(),
          HBCI.DATEFORMAT.format(getValuta()),
          getZweck(),
          k.getWaehrung() + " " + HBCI.DECIMALFORMAT.format(getBetrag())
        };
        k.addToProtokoll(i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Valuta {3}, Zweck: {4}] {5} gelöscht",fields),Protokoll.TYP_SUCCESS);
      }
      
      this.transactionCommit();
    }
    catch (RemoteException re)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw re;
    }
    catch (ApplicationException ae)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw ae;
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
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setGenericAttribute(java.lang.String, java.lang.String)
   */
  public void setGenericAttribute(String name, String value) throws RemoteException, ApplicationException
  {
    if (name == null)
      return;
    
    if (value == null)
    {
      super.setAttribute(name,value);
      return;
    }

    try
    {
      if ("betrag".equals(name))
      {
        setBetrag(HBCI.DECIMALFORMAT.parse(value).doubleValue());
        return;
      }
      if ("saldo".equals(name))
      {
        setSaldo(HBCI.DECIMALFORMAT.parse(value).doubleValue());
        return;
      }
    }
    catch (ParseException e)
    {
      throw new ApplicationException(i18n.tr("Betrag \"{0}\" besitzt nicht das Format 000,00",value));
    }
    
    try
    {
      if ("datum".equals(name))
      {
        setDatum(HBCI.DATEFORMAT.parse(value));
        return;
      }
      if ("valuta".equals(name))
      {
        setValuta(HBCI.DATEFORMAT.parse(value));
        return;
      }
    }
    catch (ParseException e)
    {
      throw new ApplicationException(i18n.tr("Datum \"{0}\" besitzt nicht das Format TT.MM.JJJJ",value));
    }
    
    super.setAttribute(name,value);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getUmsatzTyp()
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException
  {
    if (UMSATZTYP_CACHE.size() == 0)
    {
      // Wir initialisieren den Cache
      DBIterator list = getService().createList(UmsatzTyp.class);
      while (list.hasNext())
      {
        UmsatzTyp t = (UmsatzTyp) list.next();
        UMSATZTYP_CACHE.put(t.getID(),t);
      }
    }
    
    // ID von fest verdrahteten Kategorien
    Integer i = (Integer) super.getAttribute("umsatztyp_id");

    if (i == null)
    {
      // Nicht zugeordnet, dann schauen wir mal, ob's eine dynamische Zuordnung gibt
      Enumeration typen = UMSATZTYP_CACHE.elements();
      while (typen.hasMoreElements())
      {
        UmsatzTyp ut = (UmsatzTyp) typen.nextElement();
        if (ut.matches(this))
          return ut;
      }
      // keine dynamische Umsatzkategorie gefunden. Dann raus hier
      return null;
    }
   
    // Wir haben eine ID und sie ist fest verdrahtet
    String id = i.toString();
    
    UmsatzTyp ut = (UmsatzTyp) UMSATZTYP_CACHE.get(id);
    if (ut == null)
    {
      // Hu? Nicht im Cache? Dann ist sie waehrend der aktuellen Sitzung dazugekommen
      // und wir laden sie noch in den Cache.
      try
      {
        ut = (UmsatzTyp) getService().createObject(UmsatzTyp.class,id);
        UMSATZTYP_CACHE.put(id,ut);
      }
      catch (ObjectNotFoundException one)
      {
        // inzwischen schon wieder geloescht worden. Ignorieren wir
      }
    }
    return ut;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setUmsatzTyp(de.willuhn.jameica.hbci.rmi.UmsatzTyp)
   */
  public void setUmsatzTyp(UmsatzTyp ut) throws RemoteException
  {
    setAttribute("umsatztyp_id",ut == null ? null : new Integer(ut.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#isAssigned()
   */
  public boolean isAssigned() throws RemoteException
  {
    return super.getAttribute("umsatztyp_id") != null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getTransferTyp()
   */
  public int getTransferTyp() throws RemoteException
  {
    return Transfer.TYP_UMSATZ;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getWeitereVerwendungszwecke()
   */
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    return VerwendungszweckUtil.split((String)this.getAttribute("zweck3"));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setWeitereVerwendungszwecke(java.lang.String[])
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    setAttribute("zweck3",VerwendungszweckUtil.merge(list));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#getFlags()
   */
  public int getFlags() throws RemoteException
  {
    Integer i = (Integer) this.getAttribute("flags");
    return i == null ? Umsatz.FLAG_NONE : i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#setFlags(int)
   */
  public void setFlags(int flags) throws RemoteException
  {
    if (flags < 0)
      return; // ungueltig
    
    this.setAttribute("flags",new Integer(flags));
  }
}


/**********************************************************************
 * $Log: UmsatzImpl.java,v $
 * Revision 1.69  2010/01/18 22:59:05  willuhn
 * @B BUGZILLA 808
 *
 * Revision 1.68  2009/10/29 22:52:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.67  2009/09/15 00:23:35  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.66  2009/03/12 10:56:01  willuhn
 * @B Double.NaN geht nicht
 *
 * Revision 1.65  2009/03/11 17:53:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.64  2009/02/23 17:01:58  willuhn
 * @C Kein Abgleichen mehr bei vorgemerkten Buchungen sondern stattdessen vorgemerkte loeschen und neu abrufen
 *
 * Revision 1.63  2009/02/13 10:52:18  willuhn
 * @N Verwendungszweck mit in Tiny-Checksum uebernehmen, damit die Buchungen auch dann gefunden werden, wenn das Gegenkonto von der Bank nicht gefuellt wird
 *
 * Revision 1.62  2009/02/12 18:37:18  willuhn
 * @N Erster Code fuer vorgemerkte Umsaetze
 *
 * Revision 1.61  2009/02/04 23:06:24  willuhn
 * @N BUGZILLA 308 - Umsaetze als "geprueft" markieren
 *
 * Revision 1.60  2009/01/04 01:32:57  willuhn
 * @N Laengen-Check - ist jetzt noetig, da Umsaetze nun manuell geaendert werden koennen
 *
 * Revision 1.59  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.58  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.57  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.56  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.55  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.54  2008/09/03 21:29:44  willuhn
 * @C BUGZILLA 622 - Debug-Ausgaben
 *
 * Revision 1.53  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.52  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 **********************************************************************/