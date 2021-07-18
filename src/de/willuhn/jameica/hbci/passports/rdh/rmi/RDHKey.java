/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh.rmi;

import java.rmi.RemoteException;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Bildet einen importierten oder erstellten RDH-Schluessel in Hibiscus ab. 
 * @author willuhn
 */
public interface RDHKey extends GenericObject, Configuration
{
  /**
   * Liefert Pfad- und Dateiname des Schluessels.
   * @return Pfad- und Dateiname des Schluessels.
   * @throws RemoteException
   */
  public String getFilename() throws RemoteException;

  /**
   * Speichert den Pfad- und Dateinamen des Schluessels.
   * @param filename Pfad- und Dateiname des Schluessels.
   * @throws RemoteException
   */
  public void setFilename(String filename) throws RemoteException;

  /**
   * Liefert eine optionale Liste von hart verdrahteten Konten.
   * Das ist sinnvoll, wenn der User mehrere Konten bei der gleichen
   * Bank mit unterschiedlichen Dateien hat. Dann wuerde bei jeder
   * Bank-Abfrage ein Dialog zur Auswahl der Datei kommen, weils
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
   * Liefert die HBCI-Version des Schluessels.
   * @return HBCI-Version des Schluessels oder null, wenn noch keine bekannt ist.
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
	 * Prueft, ob der Schluessel zum aktiven Datenbestand gehoert und somit
	 * benutzt werden kann.
   * @return true, wenn er verwendet werden kann.
   * @throws RemoteException
   */
  public boolean isEnabled() throws RemoteException;

	/**
	 * Aktiviert oder deaktiviert den Schluessel fuer die Verwendung.
   * @param enabled true, wenn der Schluessel aktiv ist
   * @throws RemoteException
   */
  public void setEnabled(boolean enabled) throws RemoteException;

  /**
   * Liefert einen optionalen Alias-Namen fuer den Schluessel.
   * @return Alias-Name.
   * @throws RemoteException
   */
  public String getAlias() throws RemoteException;

  /**
   * Speichert einen zusaetzlichen Alias-Namen fuer den Schluessel.
   * @param alias Alias-Name.
   * @throws RemoteException
   */
  public void setAlias(String alias) throws RemoteException;

  /**
   * Liefert den Passport des Schluessels.
   * @return der Passport.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public HBCIPassport load() throws RemoteException, ApplicationException, OperationCanceledException;
}
