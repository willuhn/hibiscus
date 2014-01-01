/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/

package de.willuhn.jameica.hbci.passports.pintan.rmi;

import java.rmi.RemoteException;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Interface fuer eine einzelne PIN/TAN-Konfiguration fuer eine
 * spezifische Bank.
 * @author willuhn
 */
public interface PinTanConfig extends GenericObject, Configuration
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
   * Liefert die Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @return Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @throws RemoteException
   */
  public String[] getTanMedias() throws RemoteException;
  
  /**
   * Speichert die Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @param names die Liste der zuletzt eingegebenen TAN-Medien-Bezeichnungen.
   * @throws RemoteException
   */
  public void setTanMedias(String[] names) throws RemoteException;
  
  /**
   * Fuegt ein neues TAN-Medium zur Liste der bekannten hinzu.
   * @param name die neue TAN-Medien-Bezeichnung.
   * @throws RemoteException
   */
  public void addTanMedia(String name) throws RemoteException;

  /**
   * Liefert das zuletzt verwendete TAN-Medium.
   * @return das zuletzt verwendete TAN-Medium.
   * @throws RemoteException
   */
  public String getTanMedia() throws RemoteException;
  
  /**
   * Speichert das zuletzt verwendete TAN-Medium.
   * @param name das zuletzt verwendete TAN-Medium.
   * @throws RemoteException
   */
  public void setTanMedia(String name) throws RemoteException;
  
  /**
   * Liefert ein Custom-Property.
   * @param name Name des Property.
   * @return Wert des Property.
   * @throws RemoteException
   */
  public String getCustomProperty(String name) throws RemoteException;
  
  /**
   * Speichert ein Custom-Property.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @throws RemoteException
   */
  public void setCustomProperty(String name, String value) throws RemoteException;
  
}
