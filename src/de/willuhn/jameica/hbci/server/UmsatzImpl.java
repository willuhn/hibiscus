/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;

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
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
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

  @Override
  protected String getTableName() {
    return "umsatz";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
  }

  @Override
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
			
      HBCIProperties.checkLength(getZweck(),255);
      
      int limit = HBCIProperties.HBCI_TRANSFER_USAGE_DB_MAXLENGTH;
      HBCIProperties.checkLength(getZweck2(),limit);
      String[] ewz = getWeitereVerwendungszwecke();
      if (ewz != null && ewz.length > 0)
      {
        for (int i=0;i<ewz.length;++i)
        {
          HBCIProperties.checkLength(ewz[i],limit);
        }
      }

      HBCIProperties.checkLength(this.getGvCode(),HBCIProperties.HBCI_GVCODE_MAXLENGTH);
      HBCIProperties.checkLength(this.getAddKey(),HBCIProperties.HBCI_ADDKEY_MAXLENGTH);
      
      // Bei TX-ID und PurposeCode muessen wir die Laenge nicht checken. Das sind CAMT-Umsaetze.
      // Und die kommen schema-validiert im XML-Format. Hier extra nochmal zu pruefen, waere redundant.
      // Zumal die korrespondierenden Datebank-Felder vorsorglich ohnehin deutlich laenger definiert
      // sind als es das Schema zulaesst
		}
		catch (RemoteException e)
		{
			Logger.error("error while insertcheck in umsatz",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatzes"));
		}
  }

  @Override
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    // Wir speichern die Checksumme nun grundsaetzlich beim
    // Anlegen des Datensatzes. Dann koennen wir anschliessend
    // beliebig aendern und muessen uns nicht mehr mit
    // "hasChangedByUser" herumschlagen
    setAttribute("checksum",new Long(getChecksum()));
    super.insert();
  }

  @Override
  public Konto getKonto() throws RemoteException
  {
    Integer i = (Integer) super.getAttribute("konto_id");
    if (i == null)
      return null; // Kein Konto zugeordnet
   
    Cache cache = Cache.get(Konto.class,true);
    return (Konto) cache.get(i);
  }

  @Override
  public String getGegenkontoName() throws RemoteException {
    return (String) getAttribute("empfaenger_name");
  }

  @Override
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("empfaenger_konto");
  }

  @Override
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_blz");
  }
  
  @Override
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  @Override
  public Date getDatum() throws RemoteException {
		return (Date) getAttribute("datum");
  }

  @Override
  public Date getValuta() throws RemoteException {
		return (Date) getAttribute("valuta");
  }

  @Override
  public String getZweck() throws RemoteException {
		return (String) getAttribute("zweck");
  }

	@Override
	public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
	}

	@Override
	public void setGegenkonto(Address e) throws RemoteException
	{
    if (e == null)
      return;
    
    // IBAN und BIC haben Vorrang.
    String kto = e.getIban();
    String blz  = e.getBic();
    
    // Fallback auf alte Kontonummer, wenn die IBAN fehlt
    if (kto == null || kto.length() == 0)
      kto = e.getKontonummer();
    
    // Fallback auf alte BLZ, wenn die BIC fehlt.
    if (blz == null || blz.length() == 0)
      blz = e.getBlz();
    
    this.setGegenkontoNummer(kto);
    this.setGegenkontoBLZ(blz);
    this.setGegenkontoName(e.getName());
	}

  @Override
  public void setGegenkontoName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

	@Override
	public void setGegenkontoNummer(String konto) throws RemoteException {
    setAttribute("empfaenger_konto",konto);
  }
  
	@Override
	public void setGegenkontoBLZ(String blz) throws RemoteException {
    setAttribute("empfaenger_blz",blz);
  }
  
  @Override
  public void setBetrag(double d) throws RemoteException {
		setAttribute("betrag",new Double(d));
  }

  @Override
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  @Override
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  @Override
  public void setDatum(Date d) throws RemoteException {
		setAttribute("datum",d);
  }

  @Override
  public void setValuta(Date d) throws RemoteException {
		setAttribute("valuta",d);
  }

  @Override
  public void setKonto(Konto k) throws RemoteException {
    setAttribute("konto_id",(k == null || k.getID() == null) ? null : new Integer(k.getID()));
  }

  /**
   * Wir ueberschreiben die Funktion hier, weil beim Abrufen der
   * Umsaetze nur diejenigen gespeichert werden sollen, welche noch
   * nicht in der Datenbank existieren.
   * Da ein Umsatz von der Bank scheinbar keinen Identifier mitbringt,
   * muessen wir selbst einen fachlichen Vergleich durchfuehren.
   */
  @Override
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
			
			// Wenn beide eine TX-ID haben, brauchen wir nur anhand der TX-ID vergleichen.
			id1 = this.getTransactionId();
			id2 = other.getTransactionId();
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

  @Override
  public double getSaldo() throws RemoteException {
		Double d = (Double) getAttribute("saldo");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  @Override
  public String getPrimanota() throws RemoteException {
		return (String) getAttribute("primanota");
  }

  @Override
  public String getArt() throws RemoteException {
		return (String) getAttribute("art");
  }

  @Override
  public String getCustomerRef() throws RemoteException {
		return (String) getAttribute("customerref");
  }

  @Override
  public void setSaldo(double s) throws RemoteException {
		setAttribute("saldo",new Double(s));
  }

  @Override
  public void setPrimanota(String primanota) throws RemoteException {
		setAttribute("primanota",primanota);
  }

  @Override
  public void setArt(String art) throws RemoteException {
		setAttribute("art",art);
  }

  @Override
  public void setCustomerRef(String ref) throws RemoteException {
		setAttribute("customerref",ref);
  }

  @Override
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
    if (hasFlag(FLAG_NOTBOOKED))
      s += "notbooked";
    
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
    return crc.getValue();
  }

  /**
   * Ueberschrieben, um ein synthetisches Attribute "mergedzweck" zu erzeugen.
   */
  @Override
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

    Tag tag = Tag.byName(arg0);
    if (tag != null)
      return VerwendungszweckUtil.getTag(this,tag);

    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    if ("empfaenger".equals(arg0))
    {
      final String name = getGegenkontoName();
      final String name2 = getGegenkontoName2();
      
      final boolean hasName = StringUtils.isNotBlank(name);
      final boolean hasName2 = StringUtils.isNotBlank(name2);
      
      // Wenn wir nur einen von beiden Namen haben, liefern wir jeweils den einen
      // Wenn beide vorhanden sind, liefern wir erst den zweiten, dann den ersten.
      // Denn bei SEPA enthaelt name2 den "Ultimate Debitor". Wenn ein Zahlungsdienstleister
      // (z.Bsp. bei einer Kartenzahlung) involviert ist, dann steht im ersten Namen
      // i.d.R. nur der Name des Zahlungsdienstleisters und erst in Name2 der eigentliche Empfaenger.
      if (hasName || hasName2)
      {
        // Beide Felder vorhanden
        if (hasName && hasName2)
          return name2 + " - " + name;

        // Nur eins vorhanden
        return hasName ? name : name2;
      }
      
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

  @Override
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
        if (!this.hasFlag(Umsatz.FLAG_NOTBOOKED))
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

  @Override
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  @Override
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar",kommentar);
  }

  @Override
  public UmsatzTyp getUmsatzTyp() throws RemoteException
  {
    // ID von fest verdrahteten Kategorien
    Integer i = (Integer) super.getAttribute("umsatztyp_id");

    Cache cache = Cache.get(UmsatzTyp.class, new Cache.ObjectFactory() {
      @Override
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

  @Override
  public void setUmsatzTyp(UmsatzTyp ut) throws RemoteException
  {
    setAttribute("umsatztyp_id",ut == null ? null : new Integer(ut.getID()));
  }

  @Override
  public boolean isAssigned() throws RemoteException
  {
    return super.getAttribute("umsatztyp_id") != null;
  }

  @Override
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    return VerwendungszweckUtil.split((String)this.getAttribute("zweck3"));
  }

  @Override
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    String s = VerwendungszweckUtil.merge(list);
    try
    {
      if (s != null && s.length() > 1000)
        s = s.substring(0,1000);
    }
    catch (Exception e)
    {
      // Das catch hier nur zur Sicherheit, weil das ein Quickfix ist. Ein User hatte CAMT-Umsaetze mit
      // extrem langen Verwendungszwecken. Wir kuerzen die hier auf 1000 Zeichen.
      Logger.error("invalid usage",e);
    }
    setAttribute("zweck3",s);
  }

  @Override
  public int getFlags() throws RemoteException
  {
    Integer i = (Integer) this.getAttribute("flags");
    return i == null ? Umsatz.FLAG_NONE : i.intValue();
  }

  @Override
  public boolean hasFlag(int flag) throws RemoteException
  {
    return (this.getFlags() & flag) == flag;
  }

  @Override
  public void setFlags(int flags) throws RemoteException
  {
    if (flags < 0)
      return; // ungueltig
    
    this.setAttribute("flags",new Integer(flags));
  }

  @Override
  public String getGvCode() throws RemoteException
  {
    return (String) this.getAttribute("gvcode");
  }

  @Override
  public void setGvCode(String code) throws RemoteException
  {
    this.setAttribute("gvcode",code);
  }

  @Override
  public String getAddKey() throws RemoteException
  {
    return (String) this.getAttribute("addkey");
  }

  @Override
  public void setAddKey(String key) throws RemoteException
  {
    this.setAttribute("addkey",key);
  }

  @Override
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
    copy.setGegenkontoName2(this.getGegenkontoName2());
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
    copy.setPurposeCode(this.getPurposeCode());
    copy.setEndToEndId(this.getEndToEndId());
    copy.setMandateId(this.getMandateId());

    // Das Duplizieren von Umsatzbuchungen machen wir z.Bsp. dann, wenn ein User
    // per Hand eine Gegenbuchung erzeugt (per Kontextmenu-Eintrag "Gegenbuchung erzeugen auf...").
    // Das Duplikat darf nicht die selbe Transaction-ID haben, dann waere sie
    // nicht mehr eindeutig. Daher wird die TX-ID nicht mit dupliziert
    // copy.setTransactionId(this.getTransactionId());

    return copy;
  }
  
  @Override
  public String getTransactionId() throws RemoteException
  {
    return (String) this.getAttribute("txid");
  }
  
  @Override
  public void setTransactionId(String id) throws RemoteException
  {
    this.setAttribute("txid",id);
  }
  
  @Override
  public String getPurposeCode() throws RemoteException
  {
    return (String) this.getAttribute("purposecode");
  }
  
  @Override
  public void setPurposeCode(String code) throws RemoteException
  {
    this.setAttribute("purposecode",code);
  }
  
  @Override
  public String getEndToEndId() throws RemoteException
  {
    return (String) this.getAttribute("endtoendid");
  }
  
  @Override
  public void setEndToEndId(String id) throws RemoteException
  {
    this.setAttribute("endtoendid",id);
  }
  
  @Override
  public String getMandateId() throws RemoteException
  {
    return (String) this.getAttribute("mandateid");
  }
  
  @Override
  public void setMandateId(String id) throws RemoteException
  {
    this.setAttribute("mandateid",id);
  }
  
  @Override
  public String getGegenkontoName2() throws RemoteException
  {
    return (String) this.getAttribute("empfaenger_name2");
  }
  
  @Override
  public void setGegenkontoName2(String name) throws RemoteException
  {
    this.setAttribute("empfaenger_name2",name);
  }
}
