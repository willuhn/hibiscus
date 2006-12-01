/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/ProtokollImpl.java,v $
 * $Revision: 1.10 $
 * $Date: 2006/12/01 00:02:34 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung der HBCI-Protokollierung pro Konto.
 */
public class ProtokollImpl extends AbstractDBObject implements Protokoll {

  private transient I18N i18n = null;

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public ProtokollImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "protokoll";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "kommentar";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
		try {
			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Konto fehlt."));

			if (getKommentar() == null || getKommentar().length() == 0)
				throw new ApplicationException(i18n.tr("Kommentar fehlt."));

			// Damit setzen wir den Typ auf TYP_UNKNOWN, wenn er noch nicht gesetzt war ;)
			setTyp(getTyp());

			// beim Insert fuegen wir das Datum ein. Somit muss
			// es nicht von aussen gesetzt werden.
			setAttribute("datum", new Date());

		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
		}
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
    setAttribute("konto_id",konto);

  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException {
  	setAttribute("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Protokoll#setTyp(int)
   */
  public void setTyp(int typ) throws RemoteException {
		if (typ != TYP_ERROR && typ != TYP_SUCCESS)
			typ = TYP_UNKNOWN;
		setAttribute("typ",new Integer(typ));
  }

}


/**********************************************************************
 * $Log: ProtokollImpl.java,v $
 * Revision 1.10  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.9  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.8  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.6  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.5  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.3  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/