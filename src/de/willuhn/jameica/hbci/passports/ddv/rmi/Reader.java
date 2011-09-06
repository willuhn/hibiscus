/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/rmi/Reader.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/09/06 11:54:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.rmi;


/**
 * Um die vielen am Markt erhaeltlichen Chipkarten-Leser flexibel und
 * erweiterbar abbilden und mit sinnvollen Default-Einstellungen
 * anbieten zu koennen, implementieren wir jeden unterstuetzten
 * Reader in einer separaten Klasse.
 */
public interface Reader
{
	/**
	 * Liefert den Namen des Chipkartenlesers.
   * @return Name des Lesers.
   */
  public String getName();

	/**
	 * Liefert Pfad und Dateiname des CTAPI-Treibers.
   * @return Pfad und Dateiname des CTAPI-Treibers.
   */
  public String getCTAPIDriver();
  
  /**
   * Liefert einen vordefinierten Port.
   * @return Port.
   */
  public String getPort();
  
  /**
   * Liefert den Index des Readers.
   * @return Index des Readers.
   */
  public int getCTNumber();

  /**
	 * Prueft, ob dieser Leser von der aktuellen System-Umgebung unterstuetzt wird.
   * @return <code>true</code>, wenn er unterstuetzt wird.
   */
  public boolean isSupported();

	/**
	 * Liefert true, wenn die Tastatur des PCs zur Eingabe der PIN verwendet werden soll.
   * @return <code>true</code> wenn die Tastatur des PCs zur Eingabe der PIN verwendet werden soll.
   */
  public boolean useSoftPin();
  
  /**
   * Prueft, ob es eine Kartenleser-Konfiguration via javax.smartcardio ist.
   * @return true, wenn es eine ist.
   */
  public boolean isPCSCReader();

}


/**********************************************************************
 * $Log: Reader.java,v $
 * Revision 1.6  2011/09/06 11:54:25  willuhn
 * @C JavaReader in PCSCReader umbenannt - die PIN-Eingabe fehlt noch
 *
 * Revision 1.5  2011-09-01 12:16:08  willuhn
 * @N Kartenleser-Suche kann jetzt abgebrochen werden
 * @N Erster Code fuer javax.smartcardio basierend auf dem OCF-Code aus HBCI4Java 2.5.8
 *
 * Revision 1.4  2011-09-01 09:40:53  willuhn
 * @R Biometrie-Support bei Kartenlesern entfernt - wurde nie benutzt
 *
 * Revision 1.3  2010-09-07 15:28:06  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.2  2010-07-22 22:36:24  willuhn
 * @N Code-Cleanup
 *
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