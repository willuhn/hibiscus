/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
   * Typ-Definition fuer die verschiedenen Arten von Karten.
   */
  public static enum Type
  {
    /**
     * DDV-Karte via CTAPI.
     */
    DDV_CTAPI("DDV"),
    
    /**
     * DDV-Karte via PCSC.
     */
    DDV_PCSC("DDVPCSC","DDV"),
    
    /**
     * RDH-Karte via PCSC.
     */
    RDH_PCSC("RSA"),
    
    ;

    private final String id;
    private final String headerParam;
    
    /**
     * ct.
     * @param id der Identifier des Passports in HBCI4Java.
     */
    private Type(String id)
    {
      this(id,null);
    }
    
    /**
     * ct.
     * @param id der Identifier des Passports in HBCI4Java.
     * @param headerParam ggf abweichender Name fuer das Lookup der Parameter fuer HBCI4Java.
     */
    private Type(String id, String headerParam)
    {
      this.id = id;
      this.headerParam = headerParam;
    }
    
    /**
     * Liefert den Identifier des Passports in HBCI4Java.
     * @return der Identifier des Passports in HBCI4Java.
     */
    public String getIdentifier()
    {
      return this.id;
    }
    
    /**
     * Liefert den Namen fuer das Lookup der Parameter in HBCI4Java.
     * @return der Name fuer das Lookup der Parameter in HBCI4Java.
     */
    public String getHeaderParam()
    {
      return this.headerParam != null ? this.headerParam : this.id;
    }
    
    /**
     * Liefert true, wenn das ein PCSC-Kartenleser ist.
     * @return true, wenn das ein PCSC-Kartenleser ist.
     */
    public boolean isPCSC()
    {
      return this == DDV_PCSC || this == RDH_PCSC;
    }
    
    /**
     * Liefert true, wenn das ein CTAPI-Kartenleser ist.
     * @return true, wenn das ein CTAPI-Kartenleser ist.
     */
    public boolean isCTAPI()
    {
      return this == DDV_CTAPI;
    }
  }
  
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
   * Liefert die Art des Kartenlesers.
   * @return die Art des Kartenlesers.
   */
  public Type getType();
  
  /**
   * Liefert die Default-HBCI-Version, die verwendet werden soll, wenn keine bekannt ist.
   * Wird zum Beispiel bei der Suche nach Kartenlesern verwendet.
   * @return die Default-HBCI-Version.
   */
  public String getDefaultHBCIVersion();
  

}
