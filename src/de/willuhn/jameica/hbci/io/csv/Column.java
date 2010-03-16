/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/csv/Column.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/16 00:44:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.Serializable;

import de.willuhn.jameica.hbci.io.ser.Serializer;

/**
 * Bean fuer eine CSV-Spalte.
 */
public class Column implements Serializable, Cloneable
{
  private String property       = null;
  private String name           = null;
  private int column            = -1;
  private Serializer serializer = null;
  
  /**
   * ct.
   * Bean-Konstruktor.
   */
  public Column()
  {
  }

  /**
   * ct.
   * @param property Name des Bean-Attributes.
   * @param name Sprechender Name der Spalte.
   * @param column Nummer der Spalte.
   * @param s Serializer.
   */
  public Column(String property, String name, int column, Serializer s)
  {
    this.property   = property;
    this.name       = name;
    this.column     = column;
    this.serializer = s;
  }
  
  /**
   * Liefert den Namen des Bean-Attributes.
   * @return property Name des Bean-Attributes.
   */
  public String getProperty()
  {
    return property;
  }
  
  /**
   * Speichert den Namen des Bean-Attributes.
   * @param property Name des Bean-Attributes.
   */
  public void setProperty(String property)
  {
    this.property = property;
  }
  
  /**
   * Liefert einen sprechenden Namen fuer die Spalte.
   * @return name sprechender Name fuer die Spalte.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer die Spalte.
   * @param name sprechender Name fuer die Spalte.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Liefert die Spalten-Nummer.
   * @return column die Spalten-Nummer.
   */
  public int getColumn()
  {
    return column;
  }
  
  /**
   * Speichert die Spalten-Nummer.
   * @param column die Spalten-Nummer.
   */
  public void setColumn(int column)
  {
    this.column = column;
  }
  
  /**
   * Liefert den Serializer fuer den Wert.
   * @return serializer der Serializer.
   */
  public Serializer getSerializer()
  {
    return serializer;
  }
  
  /**
   * Speichert den Serializer fuer den Wert.
   * @param serializer der Serializer.
   */
  public void setSerializer(Serializer serializer)
  {
    this.serializer = serializer;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.name;
  }

  /**
   * @see java.lang.Object#clone()
   */
  public Object clone() throws CloneNotSupportedException
  {
    Column clone = (Column) super.clone();
    clone.column = this.column;
    clone.name = this.name;
    clone.property = this.property;
    clone.serializer = this.serializer;
    return clone;
  }
}



/**********************************************************************
 * $Log: Column.java,v $
 * Revision 1.1  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 **********************************************************************/