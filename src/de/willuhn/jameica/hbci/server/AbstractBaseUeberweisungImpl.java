/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractBaseUeberweisungImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/05/30 22:55:27 $
 * $Author: web0 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer Ueberweisungen und Lastschriften.
 */
public abstract class AbstractBaseUeberweisungImpl extends AbstractTransferImpl
  implements Terminable
{

	private I18N i18n;

  /**
   * @throws RemoteException
   */
  public AbstractBaseUeberweisungImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		try {
			if (!whileStore && ausgefuehrt())
				throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt und kann daher nicht mehr geändert werden."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while checking ueberweisung",e);
			throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrags."));
		}
		super.updateCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    if (getTermin() == null)
      setTermin(new Date());
    if (getAttribute("ausgefuehrt") == null) // Status noch nicht definiert
      setAttribute("ausgefuehrt",new Integer(0));
    super.insert();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#getTermin()
   */
  public Date getTermin() throws RemoteException {
    return (Date) getAttribute("termin");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ausgefuehrt()
   */
  public boolean ausgefuehrt() throws RemoteException {
		Integer i = (Integer) getAttribute("ausgefuehrt");
		if (i == null)
			return false;
		return i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setTermin(java.util.Date)
   */
  public void setTermin(Date termin) throws RemoteException {
		setAttribute("termin",termin);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#ueberfaellig()
   */
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
  private boolean whileStore = false;

  /**
   * @see de.willuhn.jameica.hbci.rmi.Terminable#setAusgefuehrt()
   */
  public void setAusgefuehrt() throws RemoteException, ApplicationException
  {
    try
    {
      whileStore = true;
      setAttribute("ausgefuehrt",new Integer(1));
      store();
    }
    finally
    {
      whileStore = false;
    }
  }

}


/**********************************************************************
 * $Log: AbstractBaseUeberweisungImpl.java,v $
 * Revision 1.4  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.2  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.1  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 **********************************************************************/