/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DauerauftragImpl.java,v $
 * $Revision: 1.35 $
 * $Date: 2011/04/28 07:50:07 $
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
import java.util.zip.CRC32;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Dauer-Auftrags.
 */
public class DauerauftragImpl extends AbstractHibiscusTransferImpl
  implements Dauerauftrag
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @throws RemoteException
   */
  public DauerauftragImpl() throws RemoteException
  {
    super();
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
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
		try {
      Date ersteZahlung = getErsteZahlung();
      Date letzteZahlung = getLetzteZahlung();
      
      // BUGZILLA 197
      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getTurnus() == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Zahlungsturnus aus"));

			if (ersteZahlung == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie ein Datum für die erste Zahlung an"));

			// Und jetzt noch checken, dass sich das Datum der letzten Zahlung
			// nicht vor der ersten Zahlung befindet
      // BUGZILLA 371
			if (letzteZahlung != null && letzteZahlung.before(ersteZahlung))
				throw new ApplicationException(i18n.tr("Bei Angabe eines Datum für die letzte Zahlung ({0}) muss dieses nach der ersten Zahlung ({1}) liegen", new String[]{HBCI.DATEFORMAT.format(letzteZahlung), HBCI.DATEFORMAT.format(ersteZahlung)}));
		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check in DauerAuftrag",e);
			throw new ApplicationException(i18n.tr("Fehler bei der Prüfung des Dauerauftrags"));
		}
    super.insertCheck();
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
							 getTextSchluessel() +
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
		if (o == null || !(o instanceof Dauerauftrag))
			return false;
		
		try
		{
      Dauerauftrag other = (Dauerauftrag) o;

		  // Wenn die ID uebereinstimmt, sind sie auf jeden Fall gleich. Egal, was die Checksumme sagt
      String id1 = this.getID();
      String id2 = other.getID();
      if (id1 != null && id2 != null && id1.equals(id2))
        return true;
		  
			return other.getChecksum() == getChecksum();
		}
    catch (Exception e)
    {
      Logger.error("error while comparing objects",e);
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
    if ("naechste_zahlung".equals(arg0))
      return getNaechsteZahlung();
    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getNaechsteZahlung()
   */
  public Date getNaechsteZahlung() throws RemoteException
  {
    return TurnusHelper.getNaechsteZahlung(this.getErsteZahlung(),
                                           this.getLetzteZahlung(),
                                           this.getTurnus(),
                                           new Date());
  }
  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getTextSchluessel()
   */
  public String getTextSchluessel() throws RemoteException
  {
    return (String) getAttribute("typ");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setTextSchluessel(java.lang.String)
   */
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    setAttribute("typ",schluessel);
  }
}


/**********************************************************************
 * $Log: DauerauftragImpl.java,v $
 * Revision 1.35  2011/04/28 07:50:07  willuhn
 * @B BUGZILLA 692
 *
 * Revision 1.34  2010-09-24 12:22:04  willuhn
 * @N Thomas' Patch fuer Textschluessel in Dauerauftraegen
 *
 * Revision 1.33  2010/04/27 11:02:32  willuhn
 * @R Veralteten Verwendungszweck-Code entfernt
 *
 * Revision 1.32  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.31  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.30  2008/09/04 23:42:33  willuhn
 * @N Searchprovider fuer Sammel- und Dauerauftraege
 * @N Sortierung von Ueberweisungen und Lastschriften in Suchergebnissen
 * @C "getNaechsteZahlung" von DauerauftragUtil nach TurnusHelper verschoben
 *
 * Revision 1.29  2008/09/02 22:10:26  willuhn
 * @B BUGZILLA 617 - Berechnungsfunktion grundlegend ueberarbeitet.
 *
 * Revision 1.28  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.27  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.26  2008/01/04 23:42:33  willuhn
 * @N Bemaengeltes Datum in Dauerauftraegen mit ausgeben (Debugging)
 *
 * Revision 1.25  2007/10/18 10:24:49  willuhn
 * @B Foreign-Objects in AbstractDBObject auch dann korrekt behandeln, wenn sie noch nicht gespeichert wurden
 * @C Beim Abrufen der Dauerauftraege nicht mehr nach Konten suchen sondern hart dem Konto zuweisen, ueber das sie abgerufen wurden
 *
 * Revision 1.24  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.23  2007/03/05 10:21:20  willuhn
 * @B Bug 371
 *
 * Revision 1.22  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.21  2006/03/02 13:47:14  willuhn
 * @C replaced "99" with HBCI_LAST_OF_MONTH
 *
 * Revision 1.20  2006/02/28 23:05:59  willuhn
 * @B bug 204
 *
 * Revision 1.19  2006/02/20 17:33:08  willuhn
 * @B bug 197
 *
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