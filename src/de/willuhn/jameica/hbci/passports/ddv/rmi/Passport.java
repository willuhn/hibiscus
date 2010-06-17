/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/rmi/Passport.java,v $
 * $Revision: 1.3 $
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

/**
 * Passport fuer das Sicherheitsmedium "Chipkarte" (DDV).
 */
public interface Passport extends de.willuhn.jameica.hbci.passport.Passport {

	/**
	 * Parameter fuer den Port (meist 0)
	 */
	public final static String PORT 		= "client.passport.DDV.port";

	/**
	 * Moegliche Ports fuer den Leser.
	 */
	public final static String[] PORTS = new String[] {"COM/USB","COM2/USB2","USB3","USB4","USB5","USB6"};

	/**
	 * Parameter fuer den Index (normalerweise 0)
	 */
	public final static String CTNUMBER = "client.passport.DDV.ctnumber";

	/**
	 * Parameter ober Biometrie verwendet wird (meist 0)
	 */
	public final static String USEBIO	  = "client.passport.DDV.usebio";

	/**
	 * Parameter ob die Tastatur zur Pin-Eingabe verwendet werden soll.
	 */
	public final static String SOFTPIN  = "client.passport.DDV.softpin";

	/**
	 * Parameter fuer den Index des HBCI-Zugangs (meist 1).
	 */
	public final static String ENTRYIDX = "client.passport.DDV.entryidx";

	/**
	 * Parameter fuer den den Pfad und Dateinamen des CTAPI-Treibers.
	 */
	public final static String CTAPI = "client.passport.DDV.libname.ctapi";

	/**
	 * Pfad zur JNI-Lib.
	 */
	public final static String JNILIB = "client.passport.DDV.libname.ddv";

	/**
	 * Liefert den Port des Kartenlesers.
   * @return Port.
   * @throws RemoteException
   */
  public String getPort() throws RemoteException;

	/**
	 * Liefert die Portnummer anhand des Namens.
   * @param name Name des Ports (COM1-COM2, USB).
   * @return Port.
   * @throws RemoteException
   */
  public int getPortForName(String name) throws RemoteException;

	/**
	 * Speichert den zu verwendenden Port.
   * @param port Port.
   * @throws RemoteException
   */
  public void setPort(String port) throws RemoteException;

	/**
	 * Liefert die Index-Nummer des Readers.
   * @return Index-Nummer des Readers.
   * @throws RemoteException
   */
  public int getCTNumber() throws RemoteException;

	/**
	 * Speichert die Index-Nummer des Readers.
   * @param ctNumber Index-Nummer des Readers.
   * @throws RemoteException
   */
  public void setCTNumber(int ctNumber) throws RemoteException;

	/**
	 * Prueft, ob der Reader biometrische Authentifizierung unterstuetzt.
   * @return true, wenn er es unterstuetzt, sonst false.
   * @throws RemoteException
   */
  public boolean useBIO() throws RemoteException;

	/**
	 * Legt fest, ob der Reader biometrische Authentifizierung verwenden soll.
   * @param bio true, wenn er es verwenden soll, sonst false.
   * @throws RemoteException
   */
  public void setBIO(boolean bio) throws RemoteException;

	/**
	 * Prueft, ob die Tastatur zur Eingabe des PINs verwendet werden soll.
   * @return true, wenn die Tastatur des PCs verwendet werden soll.
   * @throws RemoteException
   */
  public boolean useSoftPin() throws RemoteException;

	/**
	 * Legt fest, ob die Tastatur des PCs zur Eingabe der PIN verwendet werden soll.
   * @param softPin true, wenn die Tastatur des PCs verwendet werden soll.
   * @throws RemoteException
   */
  public void setSoftPin(boolean softPin) throws RemoteException;

	/**
	 * Liefert den Index des HBCI-Zugangs.
   * @return Index des HBCI-Zugangs.
   * @throws RemoteException
   */
  public int getEntryIndex() throws RemoteException;

	/**
	 * Speichert den Index des HBCI-Zugangs.
   * @param index Index des HBCI-Zugangs.
   * @throws RemoteException
   */
  public void setEntryIndex(int index) throws RemoteException;

  /**
   * Liefert den vollstaendigen Pfad und Dateinamen zum CTAPI-Treiber.
   * @return Pfad zum CTAPI-Treiber.
   * @throws RemoteException
   */
  public String getCTAPIDriver() throws RemoteException;
  
  /**
   * Speichert den vollstaendigen Pfad und Dateinamen zum CTAPI-Treiber.
   * @param file Pfad zum CTAPI-Treiber.
   * @throws RemoteException
   */
  public void setCTAPIDriver(String file) throws RemoteException;
  
  /**
   * Liefert den vollstaendigen Pfad und Dateinamen zur JNI-Lib.
   * @return Pfad  zur JNI-Lib.
   * @throws RemoteException
   */
  public String getJNILib() throws RemoteException;
  
  /**
   * Speichert den vollstaendigen Pfad und Dateinamen  zur JNI-Lib.
   * @param file Pfad  zur JNI-Lib.
   * @throws RemoteException
   */
  public void setJNILib(String file) throws RemoteException;

  /**
   * Die Presets, auf dem diese Einstellungen basieren.
   * @return Presets.
   * @throws RemoteException
   */
  public Reader getReaderPresets() throws RemoteException;

	/**
	 * Speichert das Preset, auf dem die aktuellen Einstellungen basieren.
   * @param reader Preset.
   * @throws RemoteException
   */
  public void setReaderPresets(Reader reader) throws RemoteException;

  /**
   * Liefert die HBCI-Version des Schluessels.
   * @return HBCI-Version des Schluessels.
   * @throws RemoteException
   */
  public String getHBCIVersion() throws RemoteException;

  /**
   * Speichert die zu verwendende HBCI-Version.
   * @param version HBCI-Version.
   * @throws RemoteException
   */
  public void setHBCIVersion(String version) throws RemoteException;
}


/**********************************************************************
 * $Log: Passport.java,v $
 * Revision 1.3  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.16  2007/07/24 13:50:27  willuhn
 * @N BUGZILLA 61
 *
 * Revision 1.15  2006/04/05 15:15:43  willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 *
 * Revision 1.14  2005/11/14 11:35:16  willuhn
 * @B bug 148
 *
 * Revision 1.13  2005/06/27 15:29:43  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/06/27 11:24:30  web0
 * @N HBCI-Version aenderbar
 *
 * Revision 1.11  2005/03/09 01:07:27  web0
 * @D javadoc fixes
 *
 * Revision 1.10  2005/02/01 18:26:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/18 23:12:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/17 14:06:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/17 13:17:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/17 12:52:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 * Revision 1.4  2004/07/19 22:37:28  willuhn
 * @B gna - Chipcard funktioniert ja doch ;)
 *
 * Revision 1.2  2004/05/05 22:14:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/04 23:24:34  willuhn
 * @N separated passports into eclipse project
 *
 * Revision 1.1  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.1  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 **********************************************************************/