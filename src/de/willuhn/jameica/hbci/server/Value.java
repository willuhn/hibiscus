/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;

/**
 * Hilfsklasse, die einen Saldo oder Betrag zu einem Zeitpunkt kapselt.
 */
public class Value
{
  private double value = 0.0d;
  private Date date    = null;

  /**
   * ct.
   * Parameterloser Konstruktor fuer Bean-Spezifikation.
   */
  public Value()
  {
  }

  /**
   * ct.
   * @param date das Datum.
   * @param value der Betrag.
   */
  public Value(Date date,double value)
  {
    this.value = value;
    this.date  = date;
  }

  /**
   * Liefert den Betrag zu dem Datum.
   * @return der Betrag zu dem Datum.
   */
  public double getValue()
  {
    return this.value;
  }

  /**
   * Liefert das Datum.
   * @return das Datum.
   */
  public Date getDate()
  {
    return this.date;
  }

  /**
   * Speichert den Betrag.
   * @param d der Betrag.
   */
  public void setValue(double d)
  {
    this.value = d;
  }

  /**
   * Speichert das Datum.
   * @param d das Datum.
   */
  public void setDate(Date d)
  {
    this.date = d;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(this.date != null ? HBCI.DATEFORMAT.format(this.date) : " - ");
    sb.append(": ");
    sb.append(HBCI.DECIMALFORMAT.format(this.value));

    return sb.toString();
  }
}

/**********************************************************************
 * $Log: Value.java,v $
 * Revision 1.1  2011/10/27 17:09:29  willuhn
 * @C Saldo-Bean in neue separate (und generischere) Klasse "Value" ausgelagert.
 * @N Saldo-Finder erweitert, damit der jetzt auch mit Value-Objekten arbeiten kann
 *
 **********************************************************************/