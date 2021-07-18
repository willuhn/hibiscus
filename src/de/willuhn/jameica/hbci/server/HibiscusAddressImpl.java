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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer Hibiscus-Adresse.
 */
public class HibiscusAddressImpl extends AbstractHibiscusDBObject implements HibiscusAddress {

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public HibiscusAddressImpl() throws RemoteException {
    super();
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
		try
		{
      //////////////////////////////////////////////////////////////////////////
      // Kontoinhaber
      String name = this.getName();
			if (name == null || name.length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Namen ein."));

      HBCIProperties.checkLength(name, HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
      //
      //////////////////////////////////////////////////////////////////////////

      boolean haveAccount = false;
      //////////////////////////////////////////////////////////////////////////
      // Deutsche Bankverbindung
      String kn = this.getKontonummer();

      if (kn != null && kn.length() > 0)
      {
        HBCIProperties.checkChars(kn, HBCIProperties.HBCI_KTO_VALIDCHARS);
        HBCIProperties.checkLength(kn, HBCIProperties.HBCI_KTO_MAXLENGTH_SOFT);

        String blz = this.getBlz();
        if (blz == null || blz.length() == 0)
          throw new ApplicationException(i18n.tr("Bitte geben Sie eine BLZ ein."));
        // BUGZILLA 280
        HBCIProperties.checkChars(blz, HBCIProperties.HBCI_BLZ_VALIDCHARS);

        // Nur pruefen, wenn ungueltige Bankverbindungen im Adressbuch erlaubt sind
        if (!Settings.getKontoCheckExcludeAddressbook() && !HBCIProperties.checkAccountCRC(blz,kn))
          throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));

        haveAccount = true;
      }
      //
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Auslaendische Bankverbindung
      String iban = this.getIban();
      String bic = this.getBic();
      String bank = this.getBank();
      if (iban != null && iban.length() > 0)
      {
        HBCIProperties.checkLength(iban, HBCIProperties.HBCI_IBAN_MAXLENGTH);
        HBCIProperties.checkChars(iban, HBCIProperties.HBCI_IBAN_VALIDCHARS);
        HBCIProperties.checkIBAN(iban);
        haveAccount = true;
      }
      if (bic != null && bic.length() > 0)
      {
        HBCIProperties.checkBIC(bic);
      }
      if (bank != null && bank.length() > 0)
      {
        HBCIProperties.checkLength(bank, HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
      }
      //
      //////////////////////////////////////////////////////////////////////////

      if (!haveAccount)
        throw new ApplicationException("Geben Sie bitte eine Kontonummer/BLZ oder IBAN ein");
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
   * @see de.willuhn.jameica.hbci.rmi.Address#getBlz()
   */
  public String getBlz() throws RemoteException
  {
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
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setBlz(java.lang.String)
   */
  public void setBlz(String blz) throws RemoteException {
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
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#getBank()
   */
  public String getBank() throws RemoteException
  {
    return (String) getAttribute("bank");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setBank(java.lang.String)
   */
  public void setBank(String name) throws RemoteException
  {
    setAttribute("bank",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#getBic()
   */
  public String getBic() throws RemoteException
  {
    return (String) getAttribute("bic");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setBic(java.lang.String)
   */
  public void setBic(String bic) throws RemoteException
  {
    setAttribute("bic",bic);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#getIban()
   */
  public String getIban() throws RemoteException
  {
    return (String) getAttribute("iban");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setIban(java.lang.String)
   */
  public void setIban(String iban) throws RemoteException
  {
    setAttribute("iban",iban);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Address#getKategorie()
   */
  public String getKategorie() throws RemoteException
  {
    return (String) this.getAttribute("kategorie");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusAddress#setKategorie(java.lang.String)
   */
  public void setKategorie(String kategorie) throws RemoteException
  {
    this.setAttribute("kategorie",kategorie);
  }
}

/**********************************************************************
 * $Log: HibiscusAddressImpl.java,v $
 * Revision 1.12  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.11  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.10  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.9  2009/05/07 09:58:40  willuhn
 * @R deprecated Funktionen getBLZ/setBLZ entfernt - bitte nur noch getBlz/setBlz nutzen!
 *
 * Revision 1.8  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.7  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 * Revision 1.6  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.5  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.4  2008/02/04 18:48:18  willuhn
 * @D javadoc
 *
 * Revision 1.3  2008/01/09 23:32:54  willuhn
 * @B Bug 534
 *
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