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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Container fuer die EinnahmeAusgabe-Daten.
 */
//für die Anzeige im Baum implementieren wir das absolute Minimum aus GenericObject 
public class EinnahmeAusgabe implements GenericObject
{
  private String text;
  private double anfangssaldo;
  private double einnahmen;
  private double ausgaben;
  private double endsaldo;
  
  private Date startdatum;
  private Date enddatum;
  
  private boolean isSumme = false;

  /**
   * ct.
   */
  public EinnahmeAusgabe()
  {
  }
  
  /**
   * ct.
   * @param k das Konto.
   * @param start Start-Datum.
   * @param end End-Datum.
   * @throws RemoteException
   */
  public EinnahmeAusgabe(Konto k, Date start, Date end) throws RemoteException
  {
    this.startdatum   = start;
    this.enddatum     = end;
    this.text         = k.getLongName();
    
    this.anfangssaldo = KontoUtil.getAnfangsSaldo(k,start);
    this.einnahmen    = KontoUtil.getEinnahmen(k,start,end);
    this.ausgaben     = KontoUtil.getAusgaben(k,start,end);
    this.endsaldo     = KontoUtil.getEndSaldo(k,end);
  }
  
  /**
   * Liefert den Beschreibungstext der Zeile.
   * @return der Beschreibungstext der Zeile.
   */
  public String getText()
  {
    return this.text;
  }

  /**
   * Speichert den Beschreibungstext der Zeile.
   * @param text der Beschreibungstext.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * Liefert den Anfangssaldo.
   * @return der Anfangssaldo.
   */
  public double getAnfangssaldo()
  {
    return this.anfangssaldo;
  }

  /**
   * Speichert den Anfangssaldo.
   * @param anfangssaldo der Anfangssaldo.
   */
  public void setAnfangssaldo(double anfangssaldo)
  {
    this.anfangssaldo = anfangssaldo;
  }

  /**
   * Liefert die Einnahmen.
   * @return die Einnahmen.
   */
  public double getEinnahmen()
  {
    return this.einnahmen;
  }

  /**
   * Speichert die Einnahmen.
   * @param einnahmen die Einnahmen.
   */
  public void setEinnahmen(double einnahmen)
  {
    this.einnahmen = einnahmen;
  }

  /**
   * Liefert die Ausgaben.
   * @return die Ausgaben.
   */
  public double getAusgaben()
  {
    return this.ausgaben;
  }

  /**
   * Speichert die Ausgaben.
   * @param ausgaben die Ausgaben.
   */
  public void setAusgaben(double ausgaben)
  {
    this.ausgaben = ausgaben;
  }

  /**
   * Liefert den End-Saldo.
   * @return endsaldo der End-Saldo.
   */
  public double getEndsaldo()
  {
    return this.endsaldo;
  }

  /**
   * Speichert den End-Saldo.
   * @param endsaldo der End-Saldo.
   */
  public void setEndsaldo(double endsaldo)
  {
    this.endsaldo = endsaldo;
  }

  /**
   * Liefert das Start-Datum.
   * @return das Start-Datum.
   */
  public Date getStartdatum()
  {
    return this.startdatum;
  }

  /**
   * Speichert das Start-Datum.
   * @param startdatum das Start-Datum.
   */
  public void setStartdatum(Date startdatum)
  {
    this.startdatum = startdatum;
  }

  /**
   * Liefert das End-Datum.
   * @return das End-Datum.
   */
  public Date getEnddatum()
  {
    return this.enddatum;
  }

  /**
   * Speichert das End-Datum.
   * @param enddatum das End-Datum.
   */
  public void setEnddatum(Date enddatum)
  {
    this.enddatum = enddatum;
  }

  /**
   * Liefert den Differenz aus errechnetem Saldo und tatsaechlichem Saldo.
   * @return der Differenz-Betrag.
   */
  public double getDifferenz()
  {
    BigDecimal v1 = new BigDecimal(this.anfangssaldo + this.einnahmen - this.ausgaben);
    BigDecimal v2 = new BigDecimal(endsaldo);
    return v1.subtract(v2).setScale(2,BigDecimal.ROUND_HALF_EVEN).doubleValue();
  }

  /**
   * Liefert true, wenn eine Differenz aus berechnetem und tatsaechlichem Saldo vorliegt.
   * @return true, wenn eine Differenz aus berechnetem und tatsaechlichem Saldo vorliegt.
   */
  public boolean hasDiff()
  {
    return Math.abs(this.getDifferenz()) >= 0.01;
  }

  /**
   * Liefert die Differenz aus Einnahmen und Ausgaben. 
   * @return die Differenz aus Einnahmen und Ausgaben.
   */
  public double getPlusminus()
  {
    return this.einnahmen - this.ausgaben;
  }
  
  /**
   * Liefert true, wenn es eine Summen-Zeile ist.
   * @return true, wenn es eine Summen-Zeile ist.
   */
  public boolean isSumme()
  {
    return this.isSumme;
  }
  
  /**
   * Legt fest, ob es sich um eine Summen-Zeile handelt.
   * @param b true, wenn es eine Summen-Zeile ist.
   */
  public void setIsSumme(boolean b)
  {
    this.isSumme = b;
  }

  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    return arg0==this;
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    try
    {
      //TODO typischerweise wird de nicht zu viele Zeilen in der Tabelle geben
      //so dass sich der Reflection-Overhead in Grenzen hält
      //besser wäre ein explizites Mapping...
      return BeanUtil.invoke(this, BeanUtil.toGetMethod(arg0), new Object[]{});
    } catch (Exception e)
    {
      throw new RemoteException("no property with name "+arg0,e);
    }
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getID() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    throw new UnsupportedOperationException();
  }
}

/*******************************************************************************
 * $Log: EinnahmeAusgabe.java,v $
 * Revision 1.1  2010/08/24 17:38:04  willuhn
 * @N BUGZILLA 896
 *
 * Revision 1.5  2010/06/07 22:41:13  willuhn
 * @N BUGZILLA 844/852
 *
 * Revision 1.4  2010/04/06 22:49:54  willuhn
 * @B BUGZILLA 844
 *
 * Revision 1.3  2010/02/17 10:43:41  willuhn
 * @N Differenz in Einnahmen/Ausgaben anzeigen, Cleanup
 ******************************************************************************/
