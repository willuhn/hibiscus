/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

/**
 * Basis-Interface aller Importer und Exporter.
 */
public interface IO
{
  /**
   * Liefert einen sprechenden Namen des Exporters/Importers.
   * Z.Bsp. "Swift MT-940 Format".
   * @return Name
   */
  public String getName();

  /**
   * Liefert eine Liste der von diesem unterstuetzten Datei-Formate.
   * @param objectType Art der zu exportierenden/importierenden Objekte.
   * Z.Bsb.: Umsatz.class oder SammellastBuchung.class.
   * Abhaengig davon kann der Exporter/Importer eine unterschiedliche
   * Liste von Dateiformaten liefern, die er zu dieser Objektart unterstuetzt.
   * @return Liste der Export-Formate.
   */
  public IOFormat[] getIOFormats(Class objectType);

}

/*********************************************************************
 * $Log: IO.java,v $
 * Revision 1.4  2006/10/16 12:51:32  willuhn
 * @B Uebernahme des originalen Datums aus dem Kontoauszug
 *
 * Revision 1.3  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.2  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 *********************************************************************/