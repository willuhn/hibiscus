/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzImpl.java,v $
 * $Revision: 1.92 $
 * $Date: 2012/05/03 21:50:47 $
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
import java.util.Iterator;
import java.util.zip.CRC32;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractHibiscusDBObject implements Umsatz
{

	private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
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
		try
		{
			if (Double.isNaN(getBetrag()))
				throw new ApplicationException(i18n.tr("Betrag ungültig."));

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
      
      String gvCode = this.getGvCode();
      if (gvCode != null && gvCode.length() > 3)
        throw new ApplicationException(i18n.tr("Geschäftsvorfallcode {0} darf maximal 3 Zeichen lang sein",gvCode));

      String addKey = this.getAddKey();
      if (addKey != null && addKey.length() > 3)
        throw new ApplicationException(i18n.tr("Textschlüssel-Zusatz {0} darf maximal 3 Zeichen lang sein",addKey));
      
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
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException
  {
    Integer i = (Integer) super.getAttribute("konto_id");
    if (i == null)
      return null; // Kein Konto zugeordnet
   
    Cache cache = Cache.get(Konto.class,true);
    return (Konto) cache.get(i);
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
    setAttribute("konto_id",(k == null || k.getID() == null) ? null : new Integer(k.getID()));
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
		try
		{
			Umsatz other = (Umsatz) o;

			// Wenn beide eine ID haben, brauchen wir nur anhand der ID vergleichen
			// Die Pruefung via Checksumme ist nur noetig, wenn neue Datensaetze
			// gespeichert werden sollen
			String id1 = this.getID();
			String id2 = other.getID();
			if (id1 != null && id2 != null)
			  return id1.equals(id2);
			  
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

    String s = (""+getArt()).toUpperCase() +
               getKonto().getID() + // wenigstens die ID vom Konto muss mit rein. Andernfalls haben zwei gleich aussehende Umsaetze auf verschiedenen Konten die gleiche Checksumme
		           getBetrag() +
		           getCustomerRef() +
		           getGegenkontoBLZ() +
		           getGegenkontoNummer() +
		           (""+getGegenkontoName()).toUpperCase() +
		           getPrimanota() +
               (Settings.getSaldoInChecksum() ? getSaldo() : "") +
		           ((String)getAttribute("mergedzweck")).toUpperCase() +
		           sd +
							 sv;
    
    // Bei Vormerkbuchungen haengen wir noch was hinten dran. Da der
    // Saldo per Default nicht mehr in der Checksumme ist, geht sonst
    // u.U. das einzige Unterscheidungsmerkmal verloren (die Vormerkbuchungen
    // haben ja einen Saldo von 0,00. Wir nehmen daher das Flag mit auf.
    // Aber nur bei Vormerkbuchungen, damit die Checksummen der valutierten
    // Buchungen gleich bleiben
    if (!isBooked())
      s += "notbooked";
    
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
    
    if ("konto_id".equals(arg0))
      return getKonto();
    
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
      return VerwendungszweckUtil.toString(this);

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
          HBCI.DATEFORMAT.format(getDatum()),
          getZweck(),
          k.getWaehrung() + " " + HBCI.DECIMALFORMAT.format(getBetrag())
        };
        if (isBooked())
          k.addToProtokoll(i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Datum {3}, Zweck: {4}] {5} gelöscht",fields),Protokoll.TYP_SUCCESS);
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
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getUmsatzTyp()
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException
  {
    // ID von fest verdrahteten Kategorien
    Integer i = (Integer) super.getAttribute("umsatztyp_id");

    Cache cache = Cache.get(UmsatzTyp.class, new Cache.ObjectFactory() {
      public DBIterator load() throws RemoteException
      {
        return UmsatzTypUtil.getAll();
      }
    },true);

    // fest zugeordnet
    if (i != null)
      return (UmsatzTyp) cache.get(i);

    // Nicht fest zugeordnet, dann schauen wir mal, ob's eine dynamische Zuordnung gibt
    Iterator typen = cache.values().iterator();
    while (typen.hasNext())
    {
      UmsatzTyp ut = (UmsatzTyp) typen.next();
      if (ut.matches(this))
        return ut;
    }
    return null;
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
  
  public boolean isBooked() throws RemoteException
  {
    return !hasFlag(FLAG_NOTBOOKED);
  }
  
  public boolean isChecked() throws RemoteException
  {
    return hasFlag(FLAG_CHECKED);
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
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#hasFlag(int)
   */
  public boolean hasFlag(int flag) throws RemoteException
  {
    return (this.getFlags() & flag) == flag;
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

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getGvCode()
   */
  public String getGvCode() throws RemoteException
  {
    return (String) this.getAttribute("gvcode");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setGvCode(java.lang.String)
   */
  public void setGvCode(String code) throws RemoteException
  {
    this.setAttribute("gvcode",code);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getAddKey()
   */
  public String getAddKey() throws RemoteException
  {
    return (String) this.getAttribute("addkey");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setAddKey(java.lang.String)
   */
  public void setAddKey(String key) throws RemoteException
  {
    this.setAttribute("addkey",key);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Umsatz duplicate() throws RemoteException
  {
    Umsatz copy = (Umsatz) this.getService().createObject(Umsatz.class,null);
    copy.setArt(this.getArt());
    copy.setBetrag(this.getBetrag());
    copy.setCustomerRef(this.getCustomerRef());
    copy.setDatum(this.getDatum());
    copy.setFlags(this.getFlags());
    copy.setGegenkontoBLZ(this.getGegenkontoBLZ());
    copy.setGegenkontoName(this.getGegenkontoName());
    copy.setGegenkontoNummer(this.getGegenkontoNummer());
    copy.setKommentar(this.getKommentar());
    copy.setKonto(this.getKonto());
    copy.setPrimanota(this.getPrimanota());
    copy.setSaldo(this.getSaldo());
    copy.setUmsatzTyp(this.getUmsatzTyp());
    copy.setValuta(this.getValuta());
    copy.setZweck(this.getZweck());
    copy.setZweck2(this.getZweck2());
    copy.setWeitereVerwendungszwecke(this.getWeitereVerwendungszwecke());
    copy.setGvCode(this.getGvCode());
    return copy;
  }
}


/**********************************************************************
 * $Log: UmsatzImpl.java,v $
 * Revision 1.92  2012/05/03 21:50:47  willuhn
 * @B BUGZILLA 1232 - Saldo des Kontos bei Offline-Konten nur bei neuen Umsaetzen uebernehmen - nicht beim Bearbeiten existierender
 *
 * Revision 1.91  2012/04/29 19:32:07  willuhn
 * @N Reihenfolge der Kategorien bei der Zuordnung beachten
 *
 * Revision 1.90  2012/04/05 21:44:18  willuhn
 * @B BUGZILLA 1219
 *
 * Revision 1.89  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.88  2011-07-25 17:17:19  willuhn
 * @N BUGZILLA 1065 - zusaetzlich noch addkey
 *
 * Revision 1.87  2011-07-25 14:42:40  willuhn
 * @N BUGZILLA 1065
 *
 * Revision 1.86  2011-04-28 12:15:25  willuhn
 * @N Wenn beide Umsaetze eine ID haben, muss nur anhand derer verglichen werden
 *
 * Revision 1.85  2011-04-28 07:50:07  willuhn
 * @B BUGZILLA 692
 *
 * Revision 1.84  2011-03-11 15:05:14  willuhn
 * @C Loeschen von Vormerkbuchungen nicht protokollieren - da Hibiscus die selbst loescht und das irritierende Protokoll-Meldungen fuer den User erzeugt
 *
 * Revision 1.83  2010-11-19 17:02:06  willuhn
 * @N VWZUtil#toString
 *
 * Revision 1.82  2010-09-28 21:40:27  willuhn
 * @C Vormerkbuchungen haben eine andere Checksumme als valutierte Buchungen - auch, wenn sie sonst identisch sind
 *
 * Revision 1.81  2010-09-27 11:51:38  willuhn
 * @N BUGZILLA 804
 *
 * Revision 1.80  2010-08-30 14:25:37  willuhn
 * @B NPE, wenn Konto angegeben, jedoch ohne ID
 *
 * Revision 1.79  2010-08-27 09:24:58  willuhn
 * @B Generics-Deklaration im Cache hat javac nicht akzeptiert (der Eclipse-Compiler hats komischerweise gefressen)
 *
 * Revision 1.78  2010-08-26 12:53:08  willuhn
 * @N Cache nur befuellen, wenn das explizit gefordert wird. Andernfalls wuerde der Cache u.U. unnoetig gefuellt werden, obwohl nur ein Objekt daraus geloescht werden soll
 *
 * Revision 1.77  2010-08-26 11:31:23  willuhn
 * @N Neuer Cache. In dem werden jetzt die zugeordneten Konten von Auftraegen und Umsaetzen zwischengespeichert sowie die Umsatz-Kategorien. Das beschleunigt das Laden der Umsaetze und Auftraege teilweise erheblich
 *
 * Revision 1.76  2010-08-03 11:00:01  willuhn
 * @N Konto-ID mit in Checksumme
 *
 * Revision 1.75  2010-06-17 15:31:27  willuhn
 * @C BUGZILLA 622 - Defaultwert des checksum.saldo-Parameters geaendert - steht jetzt per Default auf false, sodass der Saldo NICHT mit in die Checksumme einfliesst
 * @B BUGZILLA 709 - Konto ist nun ENDLICH nicht mehr Bestandteil der Checksumme, dafuer sind jetzt alle Verwendungszweck-Zeilen drin
 *
 * Revision 1.74  2010/05/30 23:29:31  willuhn
 * @N Alle Verwendungszweckzeilen in Umsatzlist und -tree anzeigen (BUGZILLA 782)
 *
 * Revision 1.73  2010/05/06 22:08:45  willuhn
 * @N BUGZILLA 622
 *
 * Revision 1.72  2010/04/27 11:02:32  willuhn
 * @R Veralteten Verwendungszweck-Code entfernt
 *
 * Revision 1.71  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.70  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
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