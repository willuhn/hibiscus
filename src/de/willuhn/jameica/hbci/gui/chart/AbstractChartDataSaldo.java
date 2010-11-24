/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/AbstractChartDataSaldo.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/11/24 16:27:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Basis-Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public abstract class AbstractChartDataSaldo implements LineChartData
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
   */
  public String getDataAttribute() throws RemoteException
  {
    return "saldo";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
   */
  public String getLabelAttribute() throws RemoteException
  {
    return "datum";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
   */
  public boolean getCurve()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
   */
  public int[] getColor() throws RemoteException
  {
    return null;
  }

  /**
   * Hilfsklasse, die einen Saldo zu einem Zeitpunkt kapselt.
   */
  public static class Saldo
  {
    private double saldo = 0.0d;
    private Date datum   = null;
    
    /**
     * ct.
     * @param datum
     * @param saldo
     */
    public Saldo(Date datum,double saldo)
    {
      this.saldo = saldo;
      this.datum = datum;
    }
    
    /**
     * Liefert den Saldo zu dem Datum.
     * @return der Saldo zu dem Datum.
     */
    public double getSaldo()
    {
      return this.saldo;
    }
    
    /**
     * Liefert das Datum.
     * @return das Datum.
     */
    public Date getDatum()
    {
      return this.datum;
    }
    
    /**
     * Speichert den Saldo.
     * @param d der Saldo.
     */
    public void setSaldo(double d)
    {
      this.saldo = d;
    }
    
    /**
     * Speichert das Datum.
     * @param d das Datum.
     */
    public void setDatum(Date d)
    {
      this.datum = d;
    }
  }
}


/*********************************************************************
 * $Log: AbstractChartDataSaldo.java,v $
 * Revision 1.2  2010/11/24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.1  2010-08-12 17:12:31  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 **********************************************************************/