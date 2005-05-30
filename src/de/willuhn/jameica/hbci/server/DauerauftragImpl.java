/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DauerauftragImpl.java,v $
 * $Revision: 1.18 $
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
import java.util.zip.CRC32;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Checksum;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Dauer-Auftrags.
 */
public class DauerauftragImpl extends AbstractTransferImpl
  implements Dauerauftrag
{

	private I18N i18n;
	
  /**
   * ct.
   * @throws RemoteException
   */
  public DauerauftragImpl() throws RemoteException
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "dauerauftrag";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getErsteZahlung()
   */
  public Date getErsteZahlung() throws RemoteException
  {
    return (Date) getAttribute("erste_zahlung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getLetzteZahlung()
   */
  public Date getLetzteZahlung() throws RemoteException
  {
		return (Date) getAttribute("letzte_zahlung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getTurnus()
   */
  public Turnus getTurnus() throws RemoteException
  {
		// Zwischen Dauerauftrag und Turnus existiert kein Constraint.
		// Folglich kann auch kein Turnus via Fremd-Schluessel geladen
		// werden. Hintergrund: Wuerde o.g. der Fall sein, dann wuerde
		// die Aenderung eines Zahlungsturnus bei einem Dauerauftrag
		// gleichzeitig die Aenderung bei einem anderen bedeuten, der
		// auf den gleichen Fremdschluessel verweist.
		// Daher existiert die Turnus-Tabelle eher als Sammlung von
		// Templates. Dennoch wollen wir das Turnus-Objekt des
		// Komforts halber benutzen und erstellen daher einfach diese
		// synthetischen Turnus-Objekte.
		Integer ze        = (Integer)getAttribute("zeiteinheit");
		Integer intervall = (Integer)getAttribute("intervall");
		Integer tag				= (Integer)getAttribute("tag");
		if (ze == null || intervall == null || tag == null)
			return null;
  	Turnus t = (Turnus) getService().createObject(Turnus.class,null);
  	t.setIntervall(intervall.intValue());
		t.setZeiteinheit(ze.intValue());
		t.setTag(tag.intValue());
		return t;
  }

	/**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#isActive()
   */
	public boolean isActive() throws RemoteException
	{
		return getOrderID() != null && getOrderID().length() > 0;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setErsteZahlung(java.util.Date)
   */
  public void setErsteZahlung(Date datum) throws RemoteException
  {
  	setAttribute("erste_zahlung",datum);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setLetzteZahlung(java.util.Date)
   */
  public void setLetzteZahlung(Date datum) throws RemoteException
  {
		setAttribute("letzte_zahlung",datum);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setTurnus(de.willuhn.jameica.hbci.rmi.Turnus)
   */
  public void setTurnus(Turnus turnus) throws RemoteException
  {
  	if (turnus == null)
  		return;
  	
		setAttribute("zeiteinheit",	new Integer(turnus.getZeiteinheit()));
		setAttribute("intervall",		new Integer(turnus.getIntervall()));
		setAttribute("tag",					new Integer(turnus.getTag()));
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException
  {
    return super.getForeignObject(field);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
		try {
			if (getTurnus() == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Zahlungsturnus aus"));

			if (getErsteZahlung() == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie ein Datum für die erste Zahlung an"));

			// Jetzt muessen wir noch checken, ob sich das Datum nicht in der Vergangenheit
			// befindet. Hierzu koennen wir aber nicht das aktuelle Datum als Vergleich nehmen
			// da das bereits einige Sekunden _nach_ dem Datum der ersten Zahlung liegt.
			// Daher lassen wir 1 Tag Toleranz zu.
			// Allerdings checken wir das nur bei Dauerauftraegen, die noch nicht aktiv sind.
			// Aktive kommen von der Bank und werden daher ganz sicher ein Datum in der Vergangenheit haben
			if (!isActive())
			{
				Date today = new Date(System.currentTimeMillis() - (1000l * 60 * 60 * 24));
				if (getErsteZahlung().before(today))
					throw new ApplicationException(i18n.tr("Bitte wählen Sie für die erste Zahlung ein Datum in der Zukunft"));
			}

			// Und jetzt noch checken, dass sich das Datum der letzten Zahlung
			// hinter der ersten Zahlung befindet
			if (getLetzteZahlung() != null && !getLetzteZahlung().after(getErsteZahlung()))
				throw new ApplicationException(i18n.tr("Bei Angabe eines Datum für die letzte Zahlung muss dieses nach der ersten Zahlung liegen"));
		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check in DauerAuftrag",e);
			throw new ApplicationException(i18n.tr("Fehler bei der Prüfung des Dauerauftrags"));
		}
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    super.updateCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
		String ersteZahlung  = getErsteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getErsteZahlung());
		String letzteZahlung = getLetzteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getLetzteZahlung());
		String s = getTurnus().getChecksum() +
							 getBetrag() +
							 getGegenkontoBLZ() +
							 getGegenkontoNummer() +
							 getGegenkontoName() +
							 getKonto().getChecksum() +
							 getZweck() +
							 getZweck2() +
							 ersteZahlung +
							 letzteZahlung;
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject o) throws RemoteException
  {
		if (o == null)
			return false;
		try {
			Checksum other = (Checksum) o;
			return other.getChecksum() == getChecksum();
		}
		catch (ClassCastException e)
		{
			return false;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getOrderID()
   */
  public String getOrderID() throws RemoteException
  {
    return (String) getAttribute("orderid");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setOrderID(java.lang.String)
   */
  public void setOrderID(String id) throws RemoteException
  {
  	setAttribute("orderid",id);
  }

  /**
   * Ueberschreiben wir, um beim synthetischen Attribut "turnus_id" ein
   * Turnus-Objekt liefern zu koennen.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
  	if ("turnus_id".equals(arg0))
  		return getTurnus();
    return super.getAttribute(arg0);
  }

}


/**********************************************************************
 * $Log: DauerauftragImpl.java,v $
 * Revision 1.18  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.17  2005/03/04 00:16:43  web0
 * @B Bugzilla http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
 *
 * Revision 1.16  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.15  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.14  2004/11/26 01:23:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.10  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.7  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.6  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.5  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.2  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/