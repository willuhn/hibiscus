/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/TurnusImpl.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/10/25 17:58:56 $
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
import java.util.zip.CRC32;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Implementierung eines Zahlungs-Turnus fuer Geld-Transfers.
 */
public class TurnusImpl extends AbstractDBObject implements Turnus
{

	private I18N i18n = null;

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public TurnusImpl() throws RemoteException
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "turnus";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "bezeichnung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
		try {
			if (isInitial())
				throw new ApplicationException(i18n.tr("Turnus ist Bestandteil der System-Daten und kann nicht gelöscht werden."));
				
			DBIterator list = Settings.getDBService().createList(Dauerauftrag.class);
			list.addFilter("turnus_id = " + this.getID());
			if (list.hasNext())
				throw new ApplicationException(i18n.tr("Turnus kann nicht gelöscht werden, da er einem Dauerauftrag zugewiesen ist."));
		}
		catch (RemoteException e)
		{
			Logger.error("error in turnus deletCheck",e);
			throw new ApplicationException(i18n.tr("Fehler beim Löschen des Turnus"));
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
  	try {

  		if (getZeiteinheit() != Turnus.ZEITEINHEIT_MONATLICH && getZeiteinheit() != Turnus.ZEITEINHEIT_WOECHENTLICH)
  			throw new ApplicationException(i18n.tr("Bitte wählen Sie eine gültige Zeiteinheit aus"));

			if (getIntervall() < 1)
				throw new ApplicationException(i18n.tr("Bitte geben Sie ein gültiges Intervall ein"));

			if (getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH && (getTag() < 1 || getTag() > 31))
				throw new ApplicationException(i18n.tr("Bei monatlicher Zeiteinheit darf der Zahltag nicht kleiner als 1 und nicht größer als 31 sein"));

			if (getZeiteinheit() == Turnus.ZEITEINHEIT_WOECHENTLICH && (getTag() < 1 || getTag() > 7))
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen gültigen Wochentag"));
  	}
  	catch (RemoteException e)
  	{
			Logger.error("error in turnus insertCheck",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Turnus"));
  	}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
  	insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#getBezeichnung()
   */
  public String getBezeichnung() throws RemoteException
  {
    return TurnusHelper.createBezeichnung(this);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#getIntervall()
   */
  public int getIntervall() throws RemoteException
  {
		Integer i = (Integer) getAttribute("intervall");
		if (i == null)
			return 1; // noch nicht definiert, wir nehmen "1" als Default-Wert
		return i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#setIntervall(int)
   */
  public void setIntervall(int intervall) throws RemoteException
  {
  	setAttribute("intervall", new Integer(intervall));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#getZeiteinheit()
   */
  public int getZeiteinheit() throws RemoteException
  {
		Integer i = (Integer) getAttribute("zeiteinheit");
		if (i == null)
			return Turnus.ZEITEINHEIT_MONATLICH; // noch nicht definiert, wir nehmen "monatlich" als Default-Wert
		return i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#setZeiteinheit(int)
   */
  public void setZeiteinheit(int zeiteinheit) throws RemoteException
  {
		setAttribute("zeiteinheit", new Integer(zeiteinheit));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#getTag()
   */
  public int getTag() throws RemoteException
  {
		Integer i = (Integer) getAttribute("tag");
		if (i == null)
			return 1; // noch nicht definiert, wir nehmen "1" als Default-Wert
		return i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#setTag(int)
   */
  public void setTag(int tag) throws RemoteException
  {
		setAttribute("tag", new Integer(tag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Turnus#isInitial()
   */
  public boolean isInitial() throws RemoteException
  {
		return getAttribute("initial") != null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
		String s = ("" + getIntervall()) + 
		           ("" + getTag()) +
		           ("" + getZeiteinheit());
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

  /**
   * Ueberschrieben, um ein virtuelles Attribut "bezeichnung" zu schaffen.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
  	if ("bezeichnung".equals(arg0))
  		return getBezeichnung();
    return super.getAttribute(arg0);
  }

}


/**********************************************************************
 * $Log: TurnusImpl.java,v $
 * Revision 1.6  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.5  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.4  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 **********************************************************************/