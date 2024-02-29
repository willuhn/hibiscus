/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.forecast;

import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Definiert ein Saldo-Limit.
 */
public class SaldoLimit
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Konto konto     = null;
  private boolean enabled = false;
  private boolean notify  = false;
  private double value    = 0.0d;
  private int days        = 0;
  private Type type       = null;
  private Date date       = null;

  /**
   * Enum mit den Arten von Limits.
   */
  public static enum Type
  {
    /**
     * Unteres Limit.
     */
    LOWER(-1,"Unteres Limit"),
    
    /**
     * Oberes Limit.
     */
    UPPER(+1,"Oberes Limit"),
    
    ;
    
    private int value = 0;
    private String desc = null;
    
    /**
     * ct.
     * @param value das Vergleichsergebnis.
     * @param desc der Beschreibungstext.
     */
    private Type(int value, String desc)
    {
      this.value = value;
      this.desc = desc;
    }
    
    /**
     * Liefert einen Beschreibungstext des Limits.
     * @return Beschreibungstext.
     */
    public String getDescription()
    {
      return i18n.tr(this.desc);
    }
    
    /**
     * Liefert true, wenn der Saldo das Limit erreicht hat
     * @param saldo der Saldo.
     * @param limit das Limit.
     * @return true, wenn das Limit erreicht ist.
     */
    public boolean reached(double saldo, double limit)
    {
      final int cmp = Double.compare(saldo,limit);
      return cmp == 0 || cmp == this.value;
    }
    
  }
  
  /**
   * ct.
   * @param konto das Konto.
   * @param type die Art des Limits.
   */
  public SaldoLimit(Konto konto, Type type)
  {
    this.konto = konto;
    this.type = type;
  }
  
  /**
   * Liefert das Konto.
   * @return das Konto.
   */
  public Konto getKonto()
  {
    return konto;
  }

  /**
   * Liefert die Art des Limits.
   * @return type die Art des Limits.
   */
  public Type getType()
  {
    return type;
  }

  /**
   * Liefert ein konkretes Datum, an dem der Saldo erreicht wurde.
   * @return date ein konkretes Datum, an dem der Saldo erreicht wurde.
   */
  public Date getDate()
  {
    return date;
  }
  
  /**
   * Speichert das konkrete Datum, an dem der Saldo erreicht wurde.
   * @param date das konkrete Datum, an dem der Saldo erreicht wurde.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }
  
  /**
   * Liefert true, wenn das Limit aktiv ist.
   * @return enabled true, wenn das Limit aktiv ist.
   */
  public boolean isEnabled()
  {
    return enabled;
  }
  
  /**
   * Legt fest, ob das Limit aktiv ist.
   * @param enabled true, wenn das Limit aktiv ist.
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }
  
  /**
   * Liefert true, wenn benachrichtigt werden soll.
   * @return true, wenn benachrichtigt werden soll.
   */
  public boolean isNotify()
  {
    return notify;
  }
  
  /**
   * Speichert, ob benachrichtigt werden soll.
   * @param notify true, wenn benachrichtigt werden soll.
   */
  public void setNotify(boolean notify)
  {
    this.notify = notify;
  }
  
  /**
   * Liefert die Anzahl der Tage fuer das Limit.
   * @return days die Anzahl der Tage fuer das Limit.
   */
  public int getDays()
  {
    return days;
  }
  
  /**
   * Speichert die Anzahl der Tage fuer das Limit.
   * @param days die Anzahl der Tage fuer das Limit.
   */
  public void setDays(int days)
  {
    this.days = days;
  }
  
  /**
   * Liefert den Betrag.
   * @return value der Betrag.
   */
  public double getValue()
  {
    return value;
  }
  
  /**
   * Speichert den Betrag.
   * @param value der Betrag.
   */
  public void setValue(double value)
  {
    this.value = value;
  }

}
