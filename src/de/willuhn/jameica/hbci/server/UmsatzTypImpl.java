/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/25 23:23:17 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Umsatz-Typs.
 */
public class UmsatzTypImpl extends AbstractDBObject implements UmsatzTyp {

	private I18N i18n;

  /**
   * ct.
   * @throws RemoteException
   */
  public UmsatzTypImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatztyp";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		// Die Dinger koennen getrost geloescht werden.
		// Wir muessen nur in der delete()-Methode alle evtl. vorhandenen
		// Verknuepfungen zu Umsaetzen loeschen.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getField() == null || getField().length() == 0)
				throw new ApplicationException(i18n.tr("Name des zu durchsuchenden Feldes fehlt."));

			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Name des Umsatztyps fehlt."));

			if (getPattern() == null || getPattern().length() == 0)
				throw new ApplicationException(i18n.tr("Suchmuster fehlt."));

		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insert check",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
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
  protected Class getForeignObject(String arg0) throws RemoteException {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getName()
   */
  public String getName() throws RemoteException {
    return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getField()
   */
  public String getField() throws RemoteException {
		return (String) getField("field");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getPattern()
   */
  public String getPattern() throws RemoteException {
		return (String) getField("pattern");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
		setField("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setField(java.lang.String)
   */
  public void setField(String field) throws RemoteException {
		setField("field",field);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setPattern(java.lang.String)
   */
  public void setPattern(String pattern) throws RemoteException {
		setField("pattern",pattern);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException {

		try {
			this.transactionBegin();

		
			// wir entfernen uns aus allen Umsaetzen
			DBIterator list = getUmsaetze();
			Umsatz u = null;
			while (list.hasNext())
			{
				u = (Umsatz) list.next();
				u.setUmsatzTyp(null);
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
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsaetze()
   */
  public DBIterator getUmsaetze() throws RemoteException {
		DBIterator list = Settings.getDatabase().createList(Umsatz.class);
		list.addFilter("umsatztyp_id = " + getID() + " ORDER BY TONUMBER(datum)");
		return list;
  }

}


/**********************************************************************
 * $Log: UmsatzTypImpl.java,v $
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/