/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof Column))
      return false;

    Column other = (Column) obj;
    return this.property.equals(other.property);
  }
}
