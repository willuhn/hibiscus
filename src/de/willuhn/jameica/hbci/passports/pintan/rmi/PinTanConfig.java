/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/rmi/PinTanConfig.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:38:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.pintan.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Interface fuer eine einzelne PIN/TAN-Konfiguration fuer eine
 * spezifische Bank.
 * @author willuhn
 */
public interface PinTanConfig extends GenericObject
{

  /**
   * Liefert die BLZ fuer die diese Config zustaendig ist.
   * @return BLZ.
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;

  /**
   * Liefert eine optionale Liste von hart verdrahteten Konten.
   * Das ist sinnvoll, wenn der User mehrere Konten bei der gleichen
   * Bank mit unterschiedlichen PIN/TAN-Konfigurationen hat. Dann wuerde bei jeder
   * Bank-Abfrage ein Dialog zur Auswahl der Config kommen, weils
   * Hibiscus allein anhand BLZ/Kundenkennung nicht mehr unterscheiden kann.
   * @return Liste der optionalen Konten oder <code>null</code>
   * BUGZILLA 173
   * BUGZILLA 314
   * @throws RemoteException
   */
  public Konto[] getKonten() throws RemoteException;

  /**
   * Speichert eine optionale Liste von festzugeordneten Konten.
   * BUGZILLA 173
   * BUGZILLA 314
   * @param k Liste der Konten.
   * @throws RemoteException
   */
  public void setKonten(Konto[] k) throws RemoteException;

  /**
   * Liefert die HTTPs-URL, ueber die die Bank erreichbar ist.
   * @return URL
   * @throws RemoteException
   */
  public String getURL() throws RemoteException;

  /**
   * Speichert die HTTPs-URL, ueber die die Bank erreichbar ist.
   * Wichtig: Das Protokoll ("https://") wird nicht mit abgespeichert.
   * @param url URL
   * @throws RemoteException
   */
  public void setURL(String url) throws RemoteException;

  /**
   * Liefert den TCP-Port des Servers.
   * Default: "443".
   * @return Port des Servers.
   * @throws RemoteException
   */
  public int getPort() throws RemoteException;

  /**
   * Definiert den TCP-Port.
   * @param port
   * @throws RemoteException
   */
  public void setPort(int port) throws RemoteException;

  /**
   * Liefert den Filter-Typ.
   * Default: "Base64".
   * @return der Filter-Typ.
   * @throws RemoteException
   */
  public String getFilterType() throws RemoteException;

  /**
   * Legt den Filter-Typ fest.
   * @param type
   * @throws RemoteException
   */
  public void setFilterType(String type) throws RemoteException;

  /**
   * Liefert die HBCI-Version.
   * @return HBCI-Version.
   * @throws RemoteException
   */
  public String getHBCIVersion() throws RemoteException;

  /**
   * Speichert die zu verwendende HBCI-Version.
   * @param version HBCI-Version.
   * @throws RemoteException
   */
  public void setHBCIVersion(String version) throws RemoteException;
  
  /**
   * Liefert die Kundenkennung.
   * @return Kundenkennung.
   * @throws RemoteException
   */
  public String getCustomerId() throws RemoteException;

  /**
   * Speichert die Kundenkennung.
   * @param customer
   * @throws RemoteException
   */
  public void setCustomerId(String customer) throws RemoteException;

  /**
   * Liefert die Benutzerkennung.
   * @return Benutzerkennung.
   * @throws RemoteException
   */
  public String getUserId() throws RemoteException;
  
  /**
   * Speichert die Benutzerkennung.
   * @param user
   * @throws RemoteException
   */
  public void setUserId(String user) throws RemoteException;
  
  /**
   * Dateiname der HBCI4Java-Config.
   * @return HBCI4Java-Config.
   * @throws RemoteException
   */
  public String getFilename() throws RemoteException;
  
  /**
   * Liefert den Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public HBCIPassport getPassport() throws RemoteException;
  
  /**
   * Optionale Angabe einer Bezeichnung fuer die Konfig.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;
  
  /**
   * Speichert eine optionale Bezeichnung fuer die Konfig.
   * @param bezeichnung Bezeichnung.
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;
  
  /**
   * Prueft, ob die verbrauchten TANs gespeichert werden sollen.
   * @return true, wenn die TANs gespeichert werden.
   * @throws RemoteException
   * BUGZILLA 62
   */
  public boolean getSaveUsedTan() throws RemoteException;
  
  /**
   * Legt fest, ob die verbrauchten TANs gespeichert werden sollen.
   * @param save true, wenn die TANs gespeichert werden sollen.
   * @throws RemoteException
   * BUGZILLA 62
   */
  public void setSaveUsedTan(boolean save) throws RemoteException;
  
  /**
   * Liefert einen ggf gespeicherten Sicherheitsmechanismus.
   * @return ID des Sicherheitsmechanismus.
   * @throws RemoteException
   */
  public String getSecMech() throws RemoteException;

  /**
   * Speichert einen Sicherheitsmechanismus.
   * @param s der Sicherheitsmechanismus.
   * @throws RemoteException
   */
  public void setSecMech(String s) throws RemoteException;

  /**
   * Prueft, ob die TAN waehrend der Eingabe angezeigt werden soll.
   * @return true, wenn die TANs angezeigt werden sollen.
   * @throws RemoteException
   */
  public boolean getShowTan() throws RemoteException;

  /**
   * Legt fest, ob die TANs bei der Eingabe angezeigt werden sollen.
   * @param show true, wenn sie angezeigt werden sollen.
   * @throws RemoteException
   */
  public void setShowTan(boolean show) throws RemoteException;
  
  /**
   * Speichert eine verbrauchte TAN.
   * @param tan die verbrauchte TAN.
   * @throws RemoteException
   */
  public void saveUsedTan(String tan) throws RemoteException;

  /**
   * Prueft, ob die TAN schon verbraucht wurde und liefert das Datum des Verbrauchs zurueck.
   * @param tan die zu testende TAN.
   * @return null, wenn die TAN noch nicht benutzt wurde, sonst das Datum des Verbrauchs.
   * @throws RemoteException
   */
  public Date getTanUsed(String tan) throws RemoteException;
  
  /**
   * Loescht die Liste der verbrauchten TANs.
   * @throws RemoteException
   */
  public void clearUsedTans() throws RemoteException;
  
  /**
   * Liefert eine Liste der verbrauchten TANs.
   * @return Liste der verbrauchten TANs.
   * @throws RemoteException
   */
  public String[] getUsedTans() throws RemoteException;
}

/*****************************************************************************
 * $Log: PinTanConfig.java,v $
 * Revision 1.1  2010/06/17 11:38:16  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.14  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 * Revision 1.13  2006/08/03 15:31:35  willuhn
 * @N Bug 62 completed
 *
 * Revision 1.12  2006/08/03 13:51:38  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.11  2006/08/03 11:27:36  willuhn
 * @N Erste Haelfte von BUG 62 (Speichern verbrauchter TANs)
 *
 * Revision 1.10  2006/01/10 22:34:07  willuhn
 * @B bug 173
 *
 * Revision 1.9  2005/11/14 11:31:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/07/18 12:53:30  web0
 * @B bug 96
 *
 * Revision 1.7  2005/05/03 22:43:06  web0
 * @B bug39
 *
 * Revision 1.6  2005/04/27 00:30:12  web0
 * @N real test connection
 * @N all hbci versions are now shown in select box
 * @C userid and customerid are changable
 *
 * Revision 1.5  2005/03/11 02:43:59  web0
 * @N PIN/TAN works ;)
 *
 * Revision 1.4  2005/03/11 00:49:30  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/10 18:38:48  web0
 * @N more PinTan Code
 *
 * Revision 1.2  2005/03/07 17:17:30  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/07 14:31:00  web0
 * @N first classes
 *
*****************************************************************************/