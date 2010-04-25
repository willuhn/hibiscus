/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.101 $
 * $Date: 2010/04/25 20:55:28 $
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.CRC32;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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
public class KontoImpl extends AbstractDBObject implements Konto
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

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "konto";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "kontonummer";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getName() == null || getName().length() == 0)
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
      HBCIProperties.checkLength(getUnterkonto(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);

      if (getKundennummer() == null || getKundennummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie Ihre Kundennummer ein."));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      if (getWaehrung() == null || getWaehrung().length() != 3)
        setWaehrung(HBCIProperties.CURRENCY_DEFAULT_DE);

      if (!HBCIProperties.checkAccountCRC(getBLZ(), getKontonummer()))
        throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));

      
      //////////////////////////////////////////////////////////////////////////
      // Auslaendische Bankverbindung
      String iban = this.getIban();
      String bic = this.getBic();
      if (iban != null && iban.length() > 0)
      {
        HBCIProperties.checkLength(iban, HBCIProperties.HBCI_IBAN_MAXLENGTH);
        HBCIProperties.checkChars(iban, HBCIProperties.HBCI_IBAN_VALIDCHARS);
        if (!HBCIProperties.checkIBANCRC(iban))
          throw new ApplicationException(i18n.tr("Ungültige IBAN. Bitte prüfen Sie Ihre Eingaben."));
      }
      if (bic != null && bic.length() > 0)
      {
        HBCIProperties.checkLength(bic, HBCIProperties.HBCI_BIC_MAXLENGTH);
        HBCIProperties.checkChars(bic, HBCIProperties.HBCI_BIC_VALIDCHARS);
      }
      //
      //////////////////////////////////////////////////////////////////////////
      
    }
    catch (RemoteException e)
    {
      Logger.error("error while insertcheck", e);
      throw new ApplicationException(i18n.tr("Fehler bei der Prüfung der Daten"));
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKontonummer()
   */
  public String getKontonummer() throws RemoteException
  {
    return (String) getAttribute("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getBLZ()
   */
  public String getBLZ() throws RemoteException
  {
    return (String) getAttribute("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getName()
   */
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getPassportClass()
   */
  public String getPassportClass() throws RemoteException
  {
    return (String) getAttribute("passport_class");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException
  {
    setAttribute("kontonummer", kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException
  {
    setAttribute("blz", blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setPassportClass(java.lang.String)
   */
  public void setPassportClass(String passport) throws RemoteException
  {
    setAttribute("passport_class", passport);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
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
    setAttribute("waehrung", waehrung);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKundennummer()
   */
  public String getKundennummer() throws RemoteException
  {
    return (String) getAttribute("kundennummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setKundennummer(java.lang.String)
   */
  public void setKundennummer(String kundennummer) throws RemoteException
  {
    setAttribute("kundennummer", kundennummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSaldo()
   */
  public double getSaldo() throws RemoteException
  {
    Double d = (Double) getAttribute("saldo");
    if (d == null)
      return 0;
    return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getAnfangsSaldo(java.util.Date)
   */
  public double getAnfangsSaldo(Date datum) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetze();
    list.addFilter("konto_id = " + getID());
    list.addFilter("saldo is not null and saldo != 0");

    Date start = HBCIProperties.startOfDay(datum);
    
    list.addFilter("datum >= ?", new Object[] {new java.sql.Date(start.getTime())});
    if (list.size() > 0)
    {
      Umsatz u = (Umsatz) list.next();
      return u.getSaldo() + u.getBetrag() * -1;
    }

    // Im angegebenen Zeitraum waren keine Umsätze zu finden. Deshalb suchen wir
    // frühere Umsätze.
    list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + getID());
    list.addFilter("saldo is not null and saldo != 0");
    list.addFilter("datum < ?", new Object[] { new java.sql.Date(start.getTime())});
    if (list.size() > 0)
    {
      Umsatz u = (Umsatz) list.next();
      return u.getSaldo();
    }
    return 0.0d;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getEndSaldo(java.util.Date)
   */
  public double getEndSaldo(Date datum) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + getID());
    list.addFilter("saldo is not null and saldo != 0");
    list.addFilter("datum <= ?", new Object[] { new java.sql.Date(HBCIProperties.endOfDay(datum).getTime())});
    if (list.size() > 0)
    {
      Umsatz u = (Umsatz) list.next();
      return u.getSaldo();
    }
    return 0.0d;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getSaldoDatum()
   */
  public Date getSaldoDatum() throws RemoteException
  {
    return (Date) getAttribute("saldo_datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#reset()
   */
  public void reset() throws RemoteException
  {
    setAttribute("saldo_datum", null);
    setAttribute("saldo", null);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze()
   */
  public DBIterator getUmsaetze() throws RemoteException
  {
    return getUmsaetze(-1);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze(int)
   */
  public DBIterator getUmsaetze(int days) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + getID());

    // BUGZILLA 341
    if (days > 0)
    {
      long d = days * 24l * 60l * 60l * 1000l;
      Date start = HBCIProperties.startOfDay(new Date(System.currentTimeMillis() - d));
      list.addFilter("valuta >= ?", new Object[] {new java.sql.Date(start.getTime())});
    }
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUmsaetze(Date, Date)
   */
  public DBIterator getUmsaetze(Date start, Date end) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("konto_id = " + getID());
    if (start != null)
      list.addFilter("valuta >= ?", new Object[] { new java.sql.Date(
          HBCIProperties.startOfDay(start).getTime()) });
    if (end != null)
      list.addFilter("valuta <= ?", new Object[] { new java.sql.Date(
          HBCIProperties.endOfDay(end).getTime()) });
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUeberweisungen()
   */
  public DBIterator getUeberweisungen() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(Ueberweisung.class);
    list.addFilter("konto_id = " + getID());

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getAuslandsUeberweisungen()
   */
  public DBIterator getAuslandsUeberweisungen() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(AuslandsUeberweisung.class);
    list.addFilter("konto_id = " + getID());

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC");
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
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setBezeichnung(java.lang.String)
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getProtokolle()
   */
  public DBIterator getProtokolle() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) getService();

    DBIterator list = service.createList(Protokoll.class);
    list.addFilter("konto_id = " + getID());
    list.setOrder("ORDER BY " + service.getSQLTimestamp("datum") + " DESC");
    return list;
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    super.insert();
    addToProtokoll(i18n.tr("Konto angelegt"), Protokoll.TYP_SUCCESS);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    if (hasChanged())
      addToProtokoll(i18n.tr("Konto-Eigenschaften aktualisiert"),
          Protokoll.TYP_SUCCESS);
    super.store();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#addToProtokoll(java.lang.String,
   *      int)
   */
  public final void addToProtokoll(String kommentar, int protokollTyp)
      throws RemoteException
  {
    if (kommentar == null || kommentar.length() == 0 || this.getID() == null)
      return;

    try
    {
      Protokoll entry = (Protokoll) getService().createObject(Protokoll.class,
          null);
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
   * 
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("numumsaetze".equals(arg0))
      return new Integer(getNumUmsaetze());

    if ("longname".equals(arg0))
    {
      String bez = getBezeichnung();
      String blz = getBLZ();
      String kto = getKontonummer();
      try
      {
        String name = HBCIUtils.getNameForBLZ(blz);
        if (name != null && name.length() > 0)
          blz = name;
        else
          blz = i18n.tr("BLZ") + ": " + blz;
      }
      catch (Exception e)
      {
        // ignore
      }

      if (bez != null && bez.length() > 0)
        return i18n.tr("{0}, Kto. {1} [{2}]", new String[] { bez, kto, blz });
      return i18n.tr("Kto. {0} [{1}]", new String[] { kto, blz });
    }

    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
    String s = getBLZ() + getKontonummer() + getKundennummer() + getUnterkonto();
    CRC32 crc = new CRC32();
    crc.update(s.getBytes());
    return crc.getValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setSaldo(double)
   */
  public void setSaldo(double saldo) throws RemoteException
  {
    setAttribute("saldo", Double.isNaN(saldo) ? null : new Double(saldo));
    setAttribute("saldo_datum", new Date());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getAusgaben(java.util.Date,
   *      java.util.Date)
   */
  public double getAusgaben(Date from, Date to) throws RemoteException
  {
    return getSumme(from, to, true);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getEinnahmen(java.util.Date,
   *      java.util.Date)
   */
  public double getEinnahmen(Date from, Date to) throws RemoteException
  {
    return getSumme(from, to, false);
  }

  /**
   * Hilfsfunktion fuer Berechnung der Einnahmen und Ausgaben.
   * 
   * @param from
   * @param to
   * @param ausgaben
   * @return Summe.
   * @throws RemoteException
   */
  private double getSumme(Date from, Date to, boolean ausgaben)
      throws RemoteException
  {
    if (this.isNewObject())
      return 0.0d;

    ArrayList params = new ArrayList();

    String sql = "select SUM(betrag) from umsatz where konto_id = " + this.getID() + " and betrag " + (ausgaben ? "<" : ">") + " 0";
    if (from != null)
    {
      params.add(new java.sql.Date(HBCIProperties.startOfDay(from).getTime()));
      sql += " and datum >= ? ";
    }
    if (to != null)
    {
      params.add(new java.sql.Date(HBCIProperties.startOfDay(to).getTime()));
      sql += " and datum <= ? ";
    }

    HBCIDBService service = (HBCIDBService) this.getService();

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (!rs.next())
          return new Double(0.0d);
        return new Double(rs.getDouble(1));
      }
    };

    Double d = (Double) service.execute(sql, params.toArray(), rs);
    return d == null ? 0.0d : d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getNumUmsaetze()
   */
  public int getNumUmsaetze() throws RemoteException
  {
    if (this.isNewObject())
      return 0;

    String sql = "select count(id) from umsatz where konto_id = "
        + this.getID();

    HBCIDBService service = (HBCIDBService) this.getService();

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (!rs.next())
          return new Integer(0);
        return new Integer(rs.getInt(1));
      }
    };

    Integer i = (Integer) service.execute(sql, new Object[0], rs);
    return i == null ? 0 : i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getLongName()
   */
  public String getLongName() throws RemoteException
  {
    return (String) getAttribute("longname");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getUnterkonto()
   */
  public String getUnterkonto() throws RemoteException
  {
    return (String) getAttribute("unterkonto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setUnterkonto(java.lang.String)
   */
  public void setUnterkonto(String unterkonto) throws RemoteException
  {
    setAttribute("unterkonto",unterkonto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKommentar()
   */
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar",kommentar);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#getFlags()
   */
  public int getFlags() throws RemoteException
  {
    Integer i = (Integer) this.getAttribute("flags");
    return i == null ? Konto.FLAG_NONE : i.intValue();
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
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#getBic()
   */
  public String getBic() throws RemoteException
  {
    return (String) getAttribute("bic");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setBic(java.lang.String)
   */
  public void setBic(String bic) throws RemoteException
  {
    setAttribute("bic",bic);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#getIban()
   */
  public String getIban() throws RemoteException
  {
    return (String) getAttribute("iban");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setIban(java.lang.String)
   */
  public void setIban(String iban) throws RemoteException
  {
    setAttribute("iban",iban);
  }
}

/*******************************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.101  2010/04/25 20:55:28  willuhn
 * @B BUGZILLA 852
 *
 * Revision 1.100  2010/04/22 16:10:43  willuhn
 * @C Saldo kann bei Offline-Konten zwar nicht manuell bearbeitet werden, dafuer wird er aber beim Zuruecksetzen des Kontos (heisst jetzt "Saldo und Datum zuruecksetzen" statt "Kontoauszugsdatum zuruecksetzen") jetzt ebenfalls geloescht
 *
 * Revision 1.99  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.98  2009/09/15 00:23:35  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.97  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.96  2009/01/26 23:17:46  willuhn
 * @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
 *
 * Revision 1.95  2009/01/04 17:43:29  willuhn
 * @N BUGZILLA 532
 *
 * Revision 1.94  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 *
 * Revision 1.93  2009/01/03 23:23:38  willuhn
 * @N Unterkontonummer wird jetzt fuer Checksumme mit beruecksichtigt - konnte vorher dazu fuehren, dass zwei eigentlich verschiedene Konten als identisch angesehen wurden
 *
 * Revision 1.92  2008/12/15 10:28:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.91  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.90  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.89  2007/12/11 12:23:26  willuhn
 * @N Bug 355
 *
 * Revision 1.88  2007/08/12 22:02:10  willuhn
 * @C BUGZILLA 394 - restliche Umstellungen von Valuta auf Buchungsdatum
 *
 * Revision 1.87  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.86  2007/07/16 12:51:15  willuhn
 * @D javadoc
 *
 * Revision 1.85  2007/06/04 15:59:23  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 * Revision 1.84 2007/04/19 18:12:21 willuhn
 * 
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 * 
 * Revision 1.83 2007/04/02 23:01:17 willuhn
 * @D diverse Javadoc-Warnings
 * @C Umstellung auf neues SelectInput
 ******************************************************************************/
