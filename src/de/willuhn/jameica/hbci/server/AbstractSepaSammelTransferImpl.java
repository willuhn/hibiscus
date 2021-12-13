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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des Containers fuer SEPA-Sammel-Transfers.
 * @param <T> der konkrete Typ der Einzel-Buchungen.
 */
public abstract class AbstractSepaSammelTransferImpl<T extends SepaSammelTransferBuchung> extends AbstractHibiscusDBObject implements SepaSammelTransfer<T>, Duplicatable, Terminable
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public AbstractSepaSammelTransferImpl() throws RemoteException
  {
    super();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "bezeichnung";
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try {
      
      Konto k = getKonto();

      if (k == null)
        throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus."));
      if (k.isNewObject())
        throw new ApplicationException(i18n.tr("Bitte speichern Sie zun�chst das Konto"));
      
      String kiban = k.getIban();
      if (kiban == null || kiban.length() == 0)
        throw new ApplicationException(i18n.tr("Das ausgew�hlte Konto besitzt keine IBAN"));
      
      String bic = k.getBic();
      if (bic == null || bic.length() == 0)
        throw new ApplicationException(i18n.tr("Das ausgew�hlte Konto besitzt keine BIC"));

      if (getBezeichnung() == null || getBezeichnung().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
      
      HBCIProperties.checkLength(getPmtInfId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getPmtInfId(), HBCIProperties.HBCI_SEPA_PMTINF_VALIDCHARS);

      if (this.getTermin() == null)
        this.setTermin(new Date());
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking job",e);
      if (!this.markingExecuted())
        throw new ApplicationException(i18n.tr("Fehler beim Pr�fen des SEPA-Auftrages."));
    }
    catch (ApplicationException ae)
    {
      if (!this.markingExecuted())
        throw ae;
      
      Logger.warn(ae.getMessage());
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try {
      if (!this.markingExecuted() && this.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgef�hrt und kann daher nicht mehr ge�ndert werden."));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammeltransfer",e);
      throw new ApplicationException(i18n.tr("Fehler beim Pr�fen des Auftrags."));
    }
    insertCheck();
  }

  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    if (getAttribute("ausgefuehrt") == null) // Status noch nicht definiert
      setAttribute("ausgefuehrt",new Integer(0));
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
  public void setKonto(Konto k) throws RemoteException
  {
    setAttribute("konto_id",(k == null || k.getID() == null) ? null : new Integer(k.getID()));
  }

  @Override
  public Date getTermin() throws RemoteException
  {
    return (Date) getAttribute("termin");
  }

  @Override
  public Date getAusfuehrungsdatum() throws RemoteException
  {
    return (Date) getAttribute("ausgefuehrt_am");
  }

  @Override
  public boolean ausgefuehrt() throws RemoteException
  {
    Integer i = (Integer) getAttribute("ausgefuehrt");
    if (i == null)
      return false;
    return i.intValue() == 1;
  }

  // Kleines Hilfsboolean damit uns der Status-Wechsel
  // beim Speichern nicht um die Ohren fliegt.
  private boolean markingExecuted = false;

  /**
   * Liefert true, wenn wir uns gerade dabei befinden, den Vorgang als ausgefuehrt zu markieren.
   * @return true, wenn wir uns gerade dabei befinden, den Vorgang als ausgefuehrt zu markieren.
   */
  protected boolean markingExecuted()
  {
    return this.markingExecuted;
  }

  @Override
  public void setAusgefuehrt(boolean b) throws RemoteException, ApplicationException
  {
    try
    {
      markingExecuted = true;
      setAttribute("ausgefuehrt",new Integer(b ? 1 : 0));
      setAttribute("ausgefuehrt_am",new Date());
      store();
      Logger.info("[" + getTableName() + ":" + getID() + "] (" + BeanUtil.toString(this) + ") - executed: " + b);
    }
    finally
    {
      markingExecuted = false;
    }
  }

  @Override
  public void setTermin(Date termin) throws RemoteException
  {
    setAttribute("termin",termin);
  }

  @Override
  public boolean ueberfaellig() throws RemoteException
  {
    if (ausgefuehrt())
      return false;
    Date termin = getTermin();
    if (termin == null)
      return false;
    return (termin.before(new Date()));
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
  public void delete() throws RemoteException, ApplicationException
  {
    // Wir muessen auch alle Buchungen mitloeschen
    // da Constraints dorthin existieren.
    try {
      this.transactionBegin();

      int count = 0;
      List<T> list = this.getBuchungen();
      for (T b:list)
      {
        b.delete();
        count++;
      }

      // Jetzt koennen wir uns selbst loeschen
      super.delete();

      // und noch in's Protokoll schreiben.
      Konto k = this.getKonto();
      if (k != null)
        k.addToProtokoll(i18n.tr("Sammel-Auftrag [Bezeichnung: {0}] gel�scht. Enthaltene Buchungen: {1}",getBezeichnung(),Integer.toString(count)), Protokoll.TYP_SUCCESS);

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
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    this.getKonto().addToProtokoll(i18n.tr("Sammel-Auftrag [Bezeichnung: {0}] gespeichert",getBezeichnung()),Protokoll.TYP_SUCCESS);
  }

  /**
   * Ueberschrieben, um ein Pseudo-Attribut "buchungen" zu erzeugen, welches
   * eine String-Repraesentation der enthaltenen Buchungen enthaelt.
   */
  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("summe".equals(arg0))
      return this.getSumme();
    
    if ("anzahl".equals(arg0))
      return this.getBuchungen().size();
    
    if ("buchungen".equals(arg0))
    {
      StringBuffer sb = new StringBuffer();
      List<T> list    = getBuchungen();
      
      for (T t:list)
      {
        sb.append(i18n.tr("{0} {1} \t {2}\n",HBCI.DECIMALFORMAT.format(t.getBetrag()),HBCIProperties.CURRENCY_DEFAULT_DE,t.getGegenkontoName()));
      }
      return sb.toString();
    }
    
    if ("konto_id".equals(arg0))
      return getKonto();

    return super.getAttribute(arg0);
  }

  @Override
  public BigDecimal getSumme() throws RemoteException
  {
    BigDecimal sum = new BigDecimal(0);
    
    if (this.isNewObject())
      return sum;

    List<T> list = this.getBuchungen();
    for (T t:list)
    {
      sum = sum.add(new BigDecimal(t.getBetrag()));
    }
    return sum;
  }
  
  @Override
  public String getPmtInfId() throws RemoteException
  {
    return (String) getAttribute("pmtinfid");
  }
  
  @Override
  public void setPmtInfId(String id) throws RemoteException
  {
    setAttribute("pmtinfid",id);
  }

}
