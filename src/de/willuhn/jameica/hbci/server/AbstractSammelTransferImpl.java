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
import java.util.ArrayList;
import java.util.Date;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des Containers fuer Sammel-Transfers.
 * @author willuhn
 */
public abstract class AbstractSammelTransferImpl extends AbstractHibiscusDBObject implements SammelTransfer, Terminable
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public AbstractSammelTransferImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "bezeichnung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try {
      if (getKonto() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));
      if (getKonto().isNewObject())
        throw new ApplicationException(i18n.tr("Bitte speichern Sie zunächst das Konto"));

      if (this.getTermin() == null)
        this.setTermin(new Date());

      if (getBezeichnung() == null || getBezeichnung().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammeltransfer",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrags."));
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    try {
      if (!whileStore && ausgefuehrt())
        throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt und kann daher nicht mehr geändert werden."));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammeltransfer",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrags."));
    }
    insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    if (getAttribute("ausgefuehrt") == null) // Status noch nicht definiert
      setAttribute("ausgefuehrt",new Integer(0));
    super.insert();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("konto_id".equals(arg0))
      return Konto.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException
  {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException
  {
    setAttribute("konto_id", konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#getTermin()
   */
  public Date getTermin() throws RemoteException
  {
    return (Date) getAttribute("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#getAusfuehrungsdatum()
   */
  public Date getAusfuehrungsdatum() throws RemoteException
  {
    return (Date) getAttribute("ausgefuehrt_am");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException
  {
    Integer i = (Integer) getAttribute("ausgefuehrt");
    if (i == null)
      return false;
    return i.intValue() == 1;
  }

  // Kleines Hilfsboolean damit uns der Status-Wechsel
  // beim Speichern nicht um die Ohren fliegt.
  private boolean whileStore = false;

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setAusgefuehrt(boolean)
   */
  public void setAusgefuehrt(boolean b) throws RemoteException, ApplicationException
  {
    try
    {
      whileStore = true;
      setAttribute("ausgefuehrt",new Integer(b ? 1 : 0));
      setAttribute("ausgefuehrt_am",new Date());
      store();
      Logger.info("[" + getTableName() + ":" + getID() + "] (" + BeanUtil.toString(this) + ") - executed: " + b);
    }
    finally
    {
      whileStore = false;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException
  {
    setAttribute("termin",termin);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException
  {
    if (ausgefuehrt())
      return false;
    Date termin = getTermin();
    if (termin == null)
      return false;
    return (termin.before(new Date()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#getBezeichnung()
   */
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#setBezeichnung(java.lang.String)
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  /**
   * @see de.willuhn.datasource.rmi.Changeable#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    // Wir muessen auch alle Buchungen mitloeschen
    // da Constraints dorthin existieren.
    try {
      this.transactionBegin();

      int count = 0;
      // dann die Dauerauftraege
      DBIterator list = getBuchungen();
      SammelTransferBuchung b = null;
      while (list.hasNext())
      {
        b = (SammelTransferBuchung) list.next();
        b.delete();
        count++;
      }

      // Jetzt koennen wir uns selbst loeschen
      super.delete();

      // und noch in's Protokoll schreiben.
      Konto k = this.getKonto();
      if (k != null)
        k.addToProtokoll(i18n.tr("Sammel-Auftrag [Bezeichnung: {0}] gelöscht. Enthaltene Buchungen: {1}",getBezeichnung(),Integer.toString(count)), Protokoll.TYP_SUCCESS);

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
   * @see de.willuhn.datasource.rmi.Changeable#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    Konto k = this.getKonto();
    k.addToProtokoll(i18n.tr("Sammel-Auftrag [Bezeichnung: {0}] gespeichert",getBezeichnung()),
      Protokoll.TYP_SUCCESS);
  }

  /**
   * Ueberschrieben, um ein Pseudo-Attribut "buchungen" zu erzeugen, welches
   * eine String-Repraesentation der enthaltenen Buchungen enthaelt.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("summe".equals(arg0))
      return new Double(this.getSumme());

    if ("anzahl".equals(arg0))
    {
      try
      {
        DBIterator l = getBuchungen();
        return new Integer(l.size());
      }
      catch (RemoteException e)
      {
        Logger.error("unable to determine number of buchungen",e);
        return new Integer(0);
      }
    }
    if ("buchungen".equals(arg0))
  	{
			try
			{
				StringBuffer sb = new StringBuffer();
				DBIterator di = getBuchungen();
				while (di.hasNext())
				{
					SammelTransferBuchung b = (SammelTransferBuchung) di.next();
					String[] params = new String[]
					{
            HBCI.DECIMALFORMAT.format(b.getBetrag()),
            getKonto().getWaehrung(),
						b.getGegenkontoName()
					};
					sb.append(i18n.tr("{0} {1} \t {2}",params));
					if (di.hasNext())
						sb.append("\n");
				}
				return sb.toString();
			}
			catch (RemoteException e)
			{
				Logger.error("error while reading buchungen",e);
				return i18n.tr("Buchungen nicht lesbar");
			}
  	}
    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#getSumme()
   */
  public double getSumme() throws RemoteException
  {
    // BUGZILLA 89 http://www.willuhn.de/bugzilla/show_bug.cgi?id=89
    double sum = 0.0;
    DBIterator list = getBuchungen();
    while (list.hasNext())
    {
      SammelTransferBuchung b = (SammelTransferBuchung) list.next();
      sum += b.getBetrag();
    }
    return sum;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#getBuchungenAsArray()
   */
  public SammelTransferBuchung[] getBuchungenAsArray() throws RemoteException
  {
    ArrayList buchungen = new ArrayList();
    DBIterator list = getBuchungen();
    while (list.hasNext())
    {
      buchungen.add(list.next());
    }
    return (SammelTransferBuchung[]) buchungen.toArray(new SammelTransferBuchung[0]);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#hasWarnings()
   */
  public boolean hasWarnings() throws RemoteException
  {
    Integer i = (Integer) getAttribute("warnungen");
    if (i == null)
      return false;
    return i.intValue() == 1;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#setWarning(boolean)
   */
  public void setWarning(boolean b) throws RemoteException
  {
    setAttribute("warnungen",new Integer(b ? 1 : 0));
  }
}
