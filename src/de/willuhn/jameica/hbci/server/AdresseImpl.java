/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/AdresseImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/03/09 01:07:02 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class AdresseImpl extends AbstractDBObject implements Adresse {

  private I18N i18n = null;
  /**
   * @throws RemoteException
   */
  public AdresseImpl() throws RemoteException {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "empfaenger";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {

			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Namen ein."));

			if (getName().length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
				throw new ApplicationException(i18n.tr("Bitte geben Sie maximal {0} Zeichen für den Namen ein.",""+HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));

			if (getBLZ() == null || getBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine BLZ ein."));

			if (getKontonummer() == null || getKontonummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kontonummer ein."));

			if (!HBCIUtils.checkAccountCRC(getBLZ(),getKontonummer()))
				throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));

		}
		catch (RemoteException e)
		{
			Logger.error("error while checking empfaenger",e);
			throw new ApplicationException(i18n.tr("Fehler bei der Prüfung des Empfängers"));
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
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getAttribute("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getAttribute("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getName()
   */
  public String getName() throws RemoteException {
		return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException {
  	setAttribute("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setAttribute("blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
  	setAttribute("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Adresse#getUeberweisungen()
   */
  public DBIterator getUeberweisungen() throws RemoteException {
		DBIterator list = getService().createList(Ueberweisung.class);
		list.addFilter("empfaenger_blz = " + this.getBLZ());
		list.addFilter("empfaenger_konto = " + this.getKontonummer());
		return list;
  }

}


/**********************************************************************
 * $Log: AdresseImpl.java,v $
 * Revision 1.4  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.3  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.2  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.11  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/15 20:09:43  willuhn
 * @B Laengen-Pruefung bei Empfaengername
 *
 * Revision 1.8  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.7  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.6  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.3  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/