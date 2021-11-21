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
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Container fuer die EinnahmeAusgabe-Daten.
 */
public class EinnahmeAusgabe implements EinnahmeAusgabeZeitraum
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Konto konto;
  private String text;
  private double anfangssaldo;
  private double einnahmen;
  private double ausgaben;
  private double endsaldo;
  
  private Date startdatum;
  private Date enddatum;
  
  private boolean isSumme = false;
  private boolean anfangsSaldoDurchUmsatzGesetzt = false;

  /**
   * ct.
   */
  public EinnahmeAusgabe()
  {
  }

  /**
   * Konstruktor f�r ein Konto, f�r das die Werte �ber {@link #addUmsatz(Umsatz)} hinzugef�gt werden
   * 
   * @param k
   *          das Konto
   * @throws RemoteException
   */
  public EinnahmeAusgabe(Konto k) throws RemoteException
  {
    this.konto = k;
    this.text = k.getLongName();
  }

  /**
   * aktualisisere Betr�ge und Salden anhand des Umsatzes, es wird davon ausgegangen, dass nur Ums�tze des passenden Kontos in der richtigen Reihenfolge
   * hinzugef�gt werden
   * 
   * @param umsatz
   *          der Umsatz
   * @throws RemoteException
   */
  public void addUmsatz(Umsatz umsatz) throws RemoteException
  {
    if (!konto.equals(umsatz.getKonto()))
    {
      throw new IllegalStateException("programming error - account mismatch");
    }
    if (startdatum != null && umsatz.getDatum().before(startdatum))
    {
      throw new IllegalStateException("programming error - wrong interval chosen");
    }
    if (enddatum != null && umsatz.getDatum().after(enddatum))
    {
      throw new IllegalStateException("programming error - wrong interval chosen");
    }
    if (!anfangsSaldoDurchUmsatzGesetzt)
    {
      // die Logik in KontoUtil#getAnfangssaldo war, dass ein Umsatz im Zeitraum die h�chste Priorit�t hat
      // d.h. der erste Umsatz bestimmt den Anfangssaldo
      this.anfangsSaldoDurchUmsatzGesetzt = true;
      this.anfangssaldo = umsatz.getSaldo() - umsatz.getBetrag();
    }
    this.endsaldo = umsatz.getSaldo();
    if (umsatz.getBetrag() > 0.0d)
    {
      this.einnahmen += umsatz.getBetrag();
    } else
    {
      this.ausgaben += -umsatz.getBetrag();
    }
  }

  /**
   * Liefert das Konto.
   * @return das Konto.
   */
  public Konto getKonto()
  {
    return this.konto;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getText()
   */
  public String getText()
  {
    return this.isSumme ? i18n.tr("Summe") : this.text;
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
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getEinnahmen()
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
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getAusgaben()
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
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getStartdatum()
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
   * @see de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum#getEnddatum()
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
    BigDecimal va = new BigDecimal(this.anfangssaldo);
    BigDecimal vin = new BigDecimal(this.einnahmen);
    BigDecimal vout = new BigDecimal(this.ausgaben);
    BigDecimal ve = new BigDecimal(this.endsaldo);
    return va.add(vin).subtract(vout).subtract(ve).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
  }

  /**
   * Liefert true, wenn eine Differenz aus berechnetem und tatsaechlichem Saldo vorliegt.
   * @return true, wenn eine Differenz aus berechnetem und tatsaechlichem Saldo vorliegt.
   */
  public boolean hasDiff()
  {
    return Math.abs(this.getDifferenz()) >= 0.01d;
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

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    return arg0 == this;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    try
    {
      // Wir koennen hier nicht direkt "BeanUtil.get(this,arg0)" aufrufen,
      // da EinnahmeAusgabe selbst ja ein GenericObject ist und "BeanUtil.get()"
      // in dem Fall intern wieder "this.getAttribute(arg0)" aufrufen wuerde.
      // Das gaebe eine Endlos-Rekursion.
      return BeanUtil.invoke(this,BeanUtil.toGetMethod(arg0),null);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to get value for attribute " + arg0,e);
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {
      "text",
      "anfangssaldo",
      "einnahmen",
      "ausgaben",
      "endsaldo",
      "startdatum",
      "enddatum"
    };

  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  @Override
  public String getID() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "text";
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return this.text + ":" +
           this.anfangssaldo + ":" +
           this.einnahmen + ":" +
           this.ausgaben + ":" +
           this.endsaldo + ":" +
           this.getPlusminus() + ":" +
           this.getDifferenz();
  }
}
