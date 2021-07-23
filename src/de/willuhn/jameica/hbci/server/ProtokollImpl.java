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
public class ProtokollImpl extends AbstractHibiscusDBObject implements Protokoll {

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public ProtokollImpl() throws RemoteException {
    super();
  }

  @Override
  protected String getTableName() {
    return "protokoll";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException {
    return "kommentar";
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    // Kommentar ggf. auf 1000 Zeichen kuerzen - H2 hat sich sonst affig ;)
    String k = getKommentar();
    if (k != null && k.length() > 1000)
      setKommentar(k.substring(0,999));
    super.store();
  }

  @Override
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
      if (getDatum() == null)
        setAttribute("datum", new Date());

		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
		}
  }

  @Override
  protected void updateCheck() throws ApplicationException {
    throw new ApplicationException(i18n.tr("Protokoll-Daten dürfen nicht geändert werden."));
  }

  @Override
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
		return null;
  }

  @Override
  public Konto getKonto() throws RemoteException {
  	return (Konto) getAttribute("konto_id");
  }

  @Override
  public String getKommentar() throws RemoteException {
    return (String) getAttribute("kommentar");
  }

  @Override
  public Date getDatum() throws RemoteException {
    return (Date) getAttribute("datum");
  }

  @Override
  public int getTyp() throws RemoteException {
		Integer i = (Integer) getAttribute("typ");
		if (i == null)
			return TYP_UNKNOWN;
		return i.intValue();
  }

  @Override
  public void setKonto(Konto konto) throws RemoteException {
    setAttribute("konto_id",konto);

  }

  @Override
  public void setKommentar(String kommentar) throws RemoteException {
  	setAttribute("kommentar",kommentar);
  }

  @Override
  public void setTyp(int typ) throws RemoteException {
		if (typ != TYP_ERROR && typ != TYP_SUCCESS)
			typ = TYP_UNKNOWN;
		setAttribute("typ",new Integer(typ));
  }

}


/**********************************************************************
 * $Log: ProtokollImpl.java,v $
 * Revision 1.14  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.13  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.12  2008/01/03 00:15:11  willuhn
 * @B Korrektur der Laenge von Kommentaren in Protokollen
 *
 * Revision 1.11  2007/04/25 15:06:47  willuhn
 * @N Datum nur ueberschreiben, wenn noch nicht gesetzt
 *
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