/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/ProtokollImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/06/17 00:14:10 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung der HBCI-Protokollierung pro Konto.
 */
public class ProtokollImpl extends AbstractDBObject implements Protokoll {

	private I18N i18n;
  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public ProtokollImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "protokoll";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "kommentar";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		// muessen sich loeschen lassen. Naemlich dann, wenn
		// das Konto selbst geloescht wird.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Konto fehlt."));

			if (getKommentar() == null || getKommentar().length() == 0)
				throw new ApplicationException(i18n.tr("Kommentar fehlt."));

			// Damit setzen wir den Typ auf TYP_UNKNOWN, wenn er noch nicht gesetzt war ;)
			setTyp(getTyp());

		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insert check",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
		}
		// beim Insert fuegen wir das Datum ein. Somit muss
		// es nicht von aussen gesetzt werden.
		setField("datum", new Date());
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
    throw new ApplicationException(i18n.tr("Protokoll-Daten dürfen nicht geändert werden."));
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
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#getKonto()
   */
  public Konto getKonto() throws RemoteException {
  	return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#getKommentar()
   */
  public String getKommentar() throws RemoteException {
    return (String) getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#getDatum()
   */
  public Date getDatum() throws RemoteException {
    return (Date) getAttribute("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#getTyp()
   */
  public int getTyp() throws RemoteException {
		Integer i = (Integer) getAttribute("typ");
		if (i == null)
			return TYP_UNKNOWN;
		return i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
    if (konto == null)
    	return;
    setField("konto_id",new Integer(konto.getID()));

  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException {
  	setField("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#setTyp(int)
   */
  public void setTyp(int typ) throws RemoteException {
		if (typ != TYP_ERROR && typ != TYP_SUCCESS)
			typ = TYP_UNKNOWN;
		setField("typ",new Integer(typ));
  }

}


/**********************************************************************
 * $Log: ProtokollImpl.java,v $
 * Revision 1.2  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/