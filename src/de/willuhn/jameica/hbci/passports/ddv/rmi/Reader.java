/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/rmi/Reader.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:45:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;

/**
 * Um die vielen am Markt erhaeltlichen Chipkarten-Leser flexibel und
 * erweiterbar abbilden und mit sinnvollen Default-Einstellungen
 * anbieten zu koennen, implementieren wir jeden unterstuetzten
 * Reader in einer separaten Klasse.
 */
public interface Reader extends GenericObject
{
	/**
	 * Liefert den Namen des Chipkartenlesers.
   * @return Name des Lesers.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Liefert Pfad und Dateiname des CTAPI-Treibers.
   * @return Pfad und Dateiname des CTAPI-Treibers.
   * @throws RemoteException
   */
  public String getCTAPIDriver() throws RemoteException;
  
  /**
   * Liefert einen vordefinierten Port.
   * @return Port.
   * @throws RemoteException
   */
  public String getPort() throws RemoteException;
  
  /**
   * Liefert den Index des Readers.
   * @return Index des Readers.
   * @throws RemoteException
   */
  public int getCTNumber() throws RemoteException;

  /**
   * Liefert Pfad und Dateiname der JNI-Lib.
   * @return Pfad und Dateiname der JNI-Lib.
   * @throws RemoteException
   */
  public String getJNILib() throws RemoteException;

  /**
	 * Prueft, ob dieser Leser von der aktuellen System-Umgebung unterstuetzt wird.
   * @return <code>true</code>, wenn er unterstuetzt wird.
   * @throws RemoteException
   */
  public boolean isSupported() throws RemoteException;

	/**
	 * Liefert true, wenn der Chipkartenleser mit biometrischen Authentifizierungsverfahren
	 * ausgestattet ist.
   * @return <code>true</code>, wenn er biometrische Authentifizierung kann.
   * @throws RemoteException
   */
  public boolean useBIO() throws RemoteException;

	/**
	 * Liefert true, wenn die Tastatur des PCs zur Eingabe der PIN verwendet werden soll.
   * @return <code>true</code> wenn die Tastatur des PCs zur Eingabe der PIN verwendet werden soll.
   * @throws RemoteException
   */
  public boolean useSoftPin() throws RemoteException;

}


/**********************************************************************
 * $Log: Reader.java,v $
 * Revision 1.1  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.3  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.2  2006/04/05 15:15:43  willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/