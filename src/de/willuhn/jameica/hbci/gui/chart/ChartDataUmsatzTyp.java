/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataUmsatzTyp.java,v $
 * $Revision: 1.6 $
 * $Date: 2010/08/12 17:12:31 $
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
import java.util.List;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Datensatzes fuer die Darstellung der Umsatz-Verteilung.
 */
public class ChartDataUmsatzTyp implements ChartData
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private int type = UmsatzTyp.TYP_EGAL;
  private int days = -1;
  
  /**
   * @param typ Art der Umsaetze.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EGAL
   * @see UmsatzTyp#TYP_EINNAHME
   * ct.
   */
  public ChartDataUmsatzTyp(int typ)
  {
    this(typ,-1);
  }

  /**
   * @param typ Art der Umsaetze.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EGAL
   * @see UmsatzTyp#TYP_EINNAHME
   * @param days Anzahl der Tage.
   * ct.
   */
  public ChartDataUmsatzTyp(int typ, int days)
  {
    this.type = typ;
    this.days = days;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public List getData() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(UmsatzTyp.class);
    if (this.type != UmsatzTyp.TYP_EGAL)
      list.addFilter("umsatztyp = " + this.type);
    return PseudoIterator.asList(list);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return i18n.tr("Umsatz-Verteilung");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
   */
  public String getDataAttribute() throws RemoteException
  {
    if (days > 0)
      return "umsatz:" + days; 
    return "umsatz";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
   */
  public String getLabelAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelFormatter()
   */
  public Formatter getLabelFormatter() throws RemoteException
  {
    return null;
  }
}


/*********************************************************************
 * $Log: ChartDataUmsatzTyp.java,v $
 * Revision 1.6  2010/08/12 17:12:31  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.5  2008/08/29 16:46:23  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.4  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
 * Revision 1.3  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.2  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.1  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.2  2005/12/12 18:53:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/