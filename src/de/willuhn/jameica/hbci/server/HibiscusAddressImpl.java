/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HibiscusAddressImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/23 21:03:48 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class HibiscusAddressImpl extends AbstractDBObject implements HibiscusAddress {

  private transient I18N i18n = null;

  /**
   * @throws RemoteException
   */
  public HibiscusAddressImpl() throws RemoteException {
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
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {

			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Namen ein."));

      HBCIProperties.checkLength(getName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);

			if (getBLZ() == null || getBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine BLZ ein."));

			if (getKontonummer() == null || getKontonummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kontonummer ein."));

      // BUGZILLA 280
      HBCIProperties.checkChars(getBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkChars(getKontonummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);

			if (!HBCIProperties.checkAccountCRC(getBLZ(),getKontonummer()))
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
   * @see de.willuhn.jameica.hbci.rmi.Address#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getAttribute("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Address#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getAttribute("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Address#getName()
   */
  public String getName() throws RemoteException {
		return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException {
  	setAttribute("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setAttribute("blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
  	setAttribute("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Address#getKommentar()
   */
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setKommentar(java.lang.String)
   */
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setGenericAttribute(java.lang.String, java.lang.String)
   */
  public void setGenericAttribute(String name, String value) throws RemoteException, ApplicationException
  {
    if (name == null)
      return;
    
    super.setAttribute(name,value);
  }
}


/**********************************************************************
 * $Log: HibiscusAddressImpl.java,v $
 * Revision 1.2  2007/04/23 21:03:48  willuhn
 * @R "getTransfers" aus Address entfernt - hat im Adressbuch eigentlich nichts zu suchen
 *
 * Revision 1.1  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.17  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.16  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.15  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.14  2006/10/07 19:50:08  willuhn
 * @D javadoc
 *
 * Revision 1.13  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.12  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 * Revision 1.11  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.10  2006/05/11 10:57:35  willuhn
 * @C merged Bug 232 into HEAD
 *
 * Revision 1.9.4.1  2006/05/11 10:44:43  willuhn
 * @B bug 232
 *
 * Revision 1.9  2005/10/03 16:17:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.7  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.6  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
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