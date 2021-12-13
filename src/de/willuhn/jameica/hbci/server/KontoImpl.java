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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bildet eine Bankverbindung ab.
 */
public class KontoImpl extends AbstractHibiscusDBObject implements Konto
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * 
   * @throws RemoteException
   */
  public KontoImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "konto";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "kontonummer";
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (StringUtils.trimToNull(getName()) == null)
        throw new ApplicationException(i18n.tr("Bitten geben Sie den Namen des Kontoinhabers ein."));

      HBCIProperties.checkLength(getName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);

      if (getKontonummer() == null || getKontonummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kontonummer ein."));

      if (getBLZ() == null || getBLZ().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bankleitzahl ein."));

      // BUGZILLA 280
      HBCIProperties.checkChars(getBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkChars(getKontonummer(),HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkLength(getKontonummer(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
      HBCIProperties.checkLength(getUnterkonto(), HBCIProperties.HBCI_ID_MAXLENGTH);

      if (getKundennummer() == null || getKundennummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie Ihre Kundenkennung ein."));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      if (getWaehrung() == null || getWaehrung().length() != 3)
        setWaehrung(HBCIProperties.CURRENCY_DEFAULT_DE);

      if (!HBCIProperties.checkAccountCRC(getBLZ(), getKontonummer()))
        throw new ApplicationException(i18n.tr("Ung�ltige BLZ/Kontonummer. Bitte pr�fen Sie Ihre Eingaben."));

      
      //////////////////////////////////////////////////////////////////////////
      // Auslaendische Bankverbindung
      String iban = this.getIban();
      String bic = this.getBic();
      if (iban != null && iban.length() > 0)
      {
        HBCIProperties.checkLength(iban, HBCIProperties.HBCI_IBAN_MAXLENGTH);
        HBCIProperties.checkChars(iban, HBCIProperties.HBCI_IBAN_VALIDCHARS);
        HBCIProperties.checkIBAN(iban);
      }
      if (bic != null && bic.length() > 0)
      {
        HBCIProperties.checkBIC(bic);
      }
      //
      //////////////////////////////////////////////////////////////////////////
      
    }
    catch (RemoteException e)
    {
      Logger.error("error while insertcheck", e);
      throw new ApplicationException(i18n.tr("Fehler bei der Pr�fung der Daten"));
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  public String getKontonummer() throws RemoteException
  {
    return (String) getAttribute("kontonummer");
  }

  @Override
  public String getBLZ() throws RemoteException
  {
    return (String) getAttribute("blz");
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public String getPassportClass() throws RemoteException
  {
    return (String) getAttribute("passport_class");
  }
  
  @Override
  public String getBackendClass() throws RemoteException
  {
    return (String) getAttribute("backend_class");
  }
  
  @Override
  public Integer getAccountType() throws RemoteException
  {
    return (Integer) getAttribute("acctype");
  }

  @Override
  public void setKontonummer(String kontonummer) throws RemoteException
  {
    setAttribute("kontonummer", kontonummer);
  }

  @Override
  public void setBLZ(String blz) throws RemoteException
  {
    setAttribute("blz", blz);
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  @Override
  public void setPassportClass(String passport) throws RemoteException
  {
    setAttribute("passport_class", passport);
  }
  
  @Override
  public void setBackendClass(String backend) throws RemoteException
  {
    setAttribute("backend_class", backend);
  }
  
  @Override
  public void setAccountType(Integer i) throws RemoteException
  {
    setAttribute("acctype",i);
  }

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    if (this.isNewObject())
      return;
    
    Cache.clear(Konto.class); // Cache loeschen

    // Wir muessen auch alle Umsaetze, Ueberweisungen und Protokolle mitloeschen
    // da Constraints dorthin existieren.
    try
    {
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
      
      // Die fest zugeordneten Kategorien loesen
      list = getUmsatzTypen();
      UmsatzTyp ut = null;
      while (list.hasNext())
      {
        ut = (UmsatzTyp) list.next();
        ut.setKonto(null);
        ut.store();
      }
      
      // dann die Kontoauszuege
      list = getKontoauszuege();
      Kontoauszug az = null;
      while (list.hasNext())
      {
        az = (Kontoauszug) list.next();
        az.delete();
      }

      // dann die Dauerauftraege
      list = getDauerauftraege();
      Dauerauftrag da = null;
      while (list.hasNext())
      {
        da = (Dauerauftrag) list.next();
        da.delete();
      }

      // dann die SEPA-Dauerauftraege
      list = getSepaDauerauftraege();
      SepaDauerauftrag sda = null;
      while (list.hasNext())
      {
        sda = (SepaDauerauftrag) list.next();
        sda.delete();
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

      // und jetzt die Auslandsueberweisungen
      list = getAuslandsUeberweisungen();
      AuslandsUeberweisung au = null;
      while (list.hasNext())
      {
        au = (AuslandsUeberweisung) list.next();
        au.delete();
      }

      // SEPA-Lastschriften
      list = getSepaLastschriften();
      SepaLastschrift sl = null;
      while (list.hasNext())
      {
        sl = (SepaLastschrift) list.next();
        sl.delete();
      }

      // SEPA-Sammellastschriften
      list = getSepaSammelLastschriften();
      SepaSammelLastschrift ssl = null;
      while (list.hasNext())
      {
        ssl = (SepaSammelLastschrift) list.next();
        ssl.delete();
      }

      // SEPA-Sammelueberweisungen
      list = getSepaSammelUeberweisungen();
      SepaSammelUeberweisung ssu = null;
      while (list.hasNext())
      {
        ssu = (SepaSammelUeberweisung) list.next();
        ssu.delete();
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

  @Override
  public String getWaehrung() throws RemoteException
  {
    return (String) getAttribute("waehrung");
  }

  @Override
  public void setWaehrung(String waehrung) throws RemoteException
  {
    setAttribute("waehrung", waehrung);
  }

  @Override
  public String getKundennummer() throws RemoteException
  {
    return (String) getAttribute("kundennummer");
  }

  @Override
  public void setKundennummer(String kundennummer) throws RemoteException
  {
    setAttribute("kundennummer", kundennummer);
  }

  @Override
  public double getSaldo() throws RemoteException
  {
    Double d = (Double) getAttribute("saldo");
    if (d == null)
      return 0;
    return d.doubleValue();
  }

  @Override
  public double getSaldoAvailable() throws RemoteException
  {
    Double d = (Double) getAttribute("saldo_available");
    if (d == null)
      return Double.NaN;
    return d.doubleValue();
  }

  @Override
  public Date getSaldoDatum() throws RemoteException
  {
    return (Date) getAttribute("saldo_datum");
  }

  @Override
  public void reset() throws RemoteException
  {
    setAttribute("saldo_datum", null);
    setAttribute("saldo", null);
    setAttribute("saldo_available",null);
  }

  @Override
  public DBIterator getUmsaetze() throws RemoteException
  {
    return getUmsaetze(-1);
  }

  @Override
  public DBIterator getUmsaetze(int days) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + getID());

    // BUGZILLA 341
    if (days > 0)
    {
      long d = days * 24l * 60l * 60l * 1000l;
      Date start = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
      list.addFilter("datum >= ?", new Object[] {new java.sql.Date(start.getTime())});
    }
    return list;
  }

  @Override
  public DBIterator getUmsaetze(Date start, Date end) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + getID());
    if (start != null)
      list.addFilter("datum >= ?", new Object[] {new java.sql.Date(DateUtil.startOfDay(start).getTime())});
    if (end != null)
      list.addFilter("datum <= ?", new Object[] {new java.sql.Date(DateUtil.endOfDay(end).getTime())});
    return list;
  }

  @Override
  public DBIterator getUeberweisungen() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(Ueberweisung.class);
    list.addFilter("konto_id = " + getID());

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");
    return list;
  }

  @Override
  public DBIterator getUmsatzTypen() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(UmsatzTyp.class);
    list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY COALESCE(nummer,''),name");
    return list;
  }
  
  @Override
  public DBIterator getAuslandsUeberweisungen() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(AuslandsUeberweisung.class);
    list.addFilter("konto_id = " + getID());

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");
    return list;
  }
  
  @Override
  public DBIterator getSepaLastschriften() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(SepaLastschrift.class);
    list.addFilter("konto_id = " + getID());

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");
    return list;
  }
  
  @Override
  public DBIterator getSepaSammelLastschriften() throws RemoteException
  {
    DBIterator list = getService().createList(SepaSammelLastschrift.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }

  @Override
  public DBIterator getSepaSammelUeberweisungen() throws RemoteException
  {
    DBIterator list = getService().createList(SepaSammelUeberweisung.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }

  @Override
  public DBIterator getDauerauftraege() throws RemoteException
  {
    DBIterator list = getService().createList(Dauerauftrag.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }

  @Override
  public DBIterator getSepaDauerauftraege() throws RemoteException
  {
    DBIterator list = getService().createList(SepaDauerauftrag.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }
  
  @Override
  public DBIterator getLastschriften() throws RemoteException
  {
    DBIterator list = getService().createList(Lastschrift.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }

  @Override
  public DBIterator getSammelLastschriften() throws RemoteException
  {
    DBIterator list = getService().createList(SammelLastschrift.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }

  @Override
  public DBIterator getSammelUeberweisungen() throws RemoteException
  {
    DBIterator list = getService().createList(SammelUeberweisung.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }
  
  @Override
  public DBIterator getKontoauszuege() throws RemoteException
  {
    DBIterator list = getService().createList(Kontoauszug.class);
    list.addFilter("konto_id = " + getID());
    return list;
  }

  @Override
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  @Override
  public DBIterator getProtokolle() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(Protokoll.class);
    list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY " + service.getSQLTimestamp("datum") + " DESC");
    return list;
  }

  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    super.insert();
    addToProtokoll(i18n.tr("Konto angelegt"), Protokoll.TYP_SUCCESS);
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    if (hasChanged())
      addToProtokoll(i18n.tr("Konto-Eigenschaften aktualisiert"),Protokoll.TYP_SUCCESS);
    super.store();
    
    Cache.clear(Konto.class); // Cache loeschen
  }

  @Override
  public final void addToProtokoll(String kommentar, int protokollTyp) throws RemoteException
  {
    if (kommentar == null || kommentar.length() == 0 || this.getID() == null)
      return;

    try
    {
      Protokoll entry = (Protokoll) getService().createObject(Protokoll.class,null);
      entry.setKonto(this);
      entry.setKommentar(kommentar);
      entry.setTyp(protokollTyp);
      entry.store();
    }
    catch (Exception e)
    {
      Logger.error("error while writing protocol", e);
    }
  }

  /**
   * Die Funktion ueberschreiben wir um ein zusaetzliches virtuelles Attribut
   * "longname" einzufuehren. Bei Abfrage dieses Attributs wird "[Kontonummer]
   * Bezeichnung" zurueckgeliefert.
   */
  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("numumsaetze".equals(arg0))
      return new Integer(getNumUmsaetze());

    final boolean extralong = "extralongname".equals(arg0);
    if ("longname".equals(arg0) || extralong)
    {
      String bez  = getBezeichnung();
      String blz  = getBLZ();
      String kto  = getKontonummer();
      String iban = getIban();
      String bic  = getBic();
      
      boolean haveBic = bic != null && bic.length() > 0;
      boolean haveIban = iban != null && iban.length() > 0;
      String name = null;
      
      try
      {
        name = HBCIProperties.getNameForBank(haveBic ? bic : blz);
      }
      catch (Exception e)
      {
        // ignore
      }
      
      if (name == null)
        name = haveBic ? (i18n.tr("BIC") + ": " + bic) : (i18n.tr("BLZ") + ": " + blz);
        
      // Wir muessen die IBAN etwas verkuerzt anzeigen. Das passt sonst nicht hin.
      if (haveIban)
        kto = extralong ? HBCIProperties.formatIban(iban) : StringUtils.abbreviateMiddle(iban,"..",14);
      
      String k = i18n.tr(haveIban ? "IBAN" : "Kto.");

      if (bez != null && bez.length() > 0)
        return bez + ", " + k + " " + kto + " [" + name + "]";
      return k + " " + kto + " [" + name + "]";
    }

    return super.getAttribute(arg0);
  }

  @Override
  public long getChecksum() throws RemoteException
  {
    String s = getBLZ() + getKontonummer() + getKundennummer() + getUnterkonto();
    CRC32 crc = new CRC32();
    crc.update(s.getBytes());
    return crc.getValue();
  }

  @Override
  public void setSaldo(double saldo) throws RemoteException
  {
    setAttribute("saldo", Double.isNaN(saldo) ? null : new Double(saldo));
    setAttribute("saldo_datum", new Date());
  }

  @Override
  public void setSaldoAvailable(double saldo) throws RemoteException
  {
    setAttribute("saldo_available", Double.isNaN(saldo) ? null : new Double(saldo));
  }
  
  @Override
  public int getNumUmsaetze() throws RemoteException
  {
    if (this.isNewObject())
      return 0;

    String sql = "select count(id) from umsatz where konto_id = " + this.getID();

    HBCIDBService service = (HBCIDBService) this.getService();
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (rs.next())
          return Integer.valueOf(rs.getInt(1));
        return Integer.valueOf(0);
      }
    };

    Integer i = (Integer) service.execute(sql, new Object[0], rs);
    return i == null ? 0 : i.intValue();
  }

  @Override
  public String getLongName() throws RemoteException
  {
    return (String) getAttribute("longname");
  }

  @Override
  public String getUnterkonto() throws RemoteException
  {
    return (String) getAttribute("unterkonto");
  }

  @Override
  public void setUnterkonto(String unterkonto) throws RemoteException
  {
    setAttribute("unterkonto",unterkonto);
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
  public int getFlags() throws RemoteException
  {
    Integer i = (Integer) this.getAttribute("flags");
    return i == null ? UmsatzTyp.FLAG_NONE : i.intValue();
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
  public String getBic() throws RemoteException
  {
    return (String) getAttribute("bic");
  }

  @Override
  public void setBic(String bic) throws RemoteException
  {
    setAttribute("bic",bic);
  }

  @Override
  public String getIban() throws RemoteException
  {
    return (String) getAttribute("iban");
  }

  @Override
  public void setIban(String iban) throws RemoteException
  {
    setAttribute("iban",iban);
  }
  
  @Override
  public String getKategorie() throws RemoteException
  {
    return (String) this.getAttribute("kategorie");
  }
  
  @Override
  public void setKategorie(String kategorie) throws RemoteException
  {
    this.setAttribute("kategorie",kategorie);
  }
}
