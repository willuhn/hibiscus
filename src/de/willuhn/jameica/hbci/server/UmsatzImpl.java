/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/03/05 08:38:47 $
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
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Repraesentiert eine Zeile in den Umsaetzen.
 */
public class UmsatzImpl extends AbstractDBObject implements Umsatz {

	I18N i18n;
	private Empfaenger empfaenger;

  /**
   * @throws RemoteException
   */
  public UmsatzImpl() throws RemoteException {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatz";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		// Die koennen eigentlich getrost geloescht werden.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		// Die Umsaetze werden nicht von Hand eingegeben sondern
		// gelangen via HBCI zu uns. Nichtsdestotrotz duerfen
		// wir nur die speichern, die vollstaendig sind.
		try {

			// Empfaenger nur speichern, wenn er existiert und nicht neu ist
			if (empfaenger != null && !empfaenger.isNewObject())
				setField("empfaenger_id",new Integer(empfaenger.getID()));
			else
				setField("empfaenger_id",null);

			if (getBetrag() == 0.0)
				throw new ApplicationException(i18n.tr("Betrag fehlt."));

			if (getDatum() == null)
				throw new ApplicationException(i18n.tr("Datum fehlt."));

			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Umsatz muss einem Konto zugewiesen sein."));

			if (getValuta() == null)
				throw new ApplicationException(i18n.tr("Valuta fehlt."));

			if (getZweck() == null || getZweck().length() == 0)
				throw new ApplicationException(i18n.tr("Verwendungszweck fehlt."));
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insertcheck in umsatz",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatzes"));
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
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
		if ("empfaenger_id".equals(field))
			return Empfaenger.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getField("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getEmpfaenger()
   */
  public Empfaenger getEmpfaenger() throws RemoteException {

		if (empfaenger != null)
			return empfaenger;

    empfaenger = (Empfaenger) getField("empfaenger_id");
		return empfaenger;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getField("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getDatum()
   */
  public Date getDatum() throws RemoteException {
		return (Date) getField("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getValuta()
   */
  public Date getValuta() throws RemoteException {
		return (Date) getField("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck()
   */
  public String getZweck() throws RemoteException {
		return (String) getFieldType("zweck");
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Umsatz#getZweck2()
	 */
	public String getZweck2() throws RemoteException {
		return (String) getFieldType("zweck2");
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setEmpfaenger(de.willuhn.jameica.hbci.rmi.Empfaenger)
   */
  public void setEmpfaenger(Empfaenger e) throws RemoteException {
		if (e == null)
			return;
		empfaenger = e;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setBetrag(double)
   */
  public void setBetrag(double d) throws RemoteException {
		setField("betrag",new Double(d));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setField("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setField("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setDatum(java.util.Date)
   */
  public void setDatum(Date d) throws RemoteException {
		setField("datum",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setValuta(java.util.Date)
   */
  public void setValuta(Date d) throws RemoteException {
		setField("valuta",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Umsatz#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto k) throws RemoteException {
		if (k == null)
			return;
		setField("konto_id",new Integer(k.getID()));
  }

}


/**********************************************************************
 * $Log: UmsatzImpl.java,v $
 * Revision 1.2  2004/03/05 08:38:47  willuhn
 * @N umsaetze works now
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/