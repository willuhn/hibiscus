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

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer Ueberweisungen und Lastschriften.
 */
public abstract class AbstractBaseUeberweisungImpl extends AbstractHibiscusTransferImpl
  implements BaseUeberweisung, Terminable
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public AbstractBaseUeberweisungImpl() throws RemoteException {
    super();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
  }

  
  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      super.insertCheck();
      
      if (this.getTermin() == null)
        this.setTermin(new Date());
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking job",e);
      if (!this.markingExecuted())
        throw new ApplicationException(i18n.tr("Fehler beim Prüfen des SEPA-Auftrages."));
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
		try
		{
			if (!this.markingExecuted() && this.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt und kann daher nicht mehr geändert werden."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while checking transfer",e);
			throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrags."));
		}
		super.updateCheck();
  }

  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    if (getAttribute("ausgefuehrt") == null) // Status noch nicht definiert
      setAttribute("ausgefuehrt", Integer.valueOf(0));
    super.insert();
  }

  @Override
  public Date getTermin() throws RemoteException {
    return (Date) getAttribute("termin");
  }

  @Override
  public Date getAusfuehrungsdatum() throws RemoteException
  {
    return (Date) getAttribute("ausgefuehrt_am");
  }

  @Override
  public boolean ausgefuehrt() throws RemoteException {
		Integer i = (Integer) getAttribute("ausgefuehrt");
		if (i == null)
			return false;
		return i.intValue() == 1;
  }

  @Override
  public void setTermin(Date termin) throws RemoteException {
		setAttribute("termin",termin);
  }

  @Override
  public boolean ueberfaellig() throws RemoteException {
    if (ausgefuehrt())
    	return false;
    Date termin = getTermin();
    if (termin == null)
    	return false;
    return (termin.before(new Date()));
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
      setAttribute("ausgefuehrt", Integer.valueOf(b ? 1 : 0));
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
  public String getTextSchluessel() throws RemoteException
  {
    return (String) getAttribute("typ");
  }

  @Override
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    setAttribute("typ",schluessel);
  }
}
