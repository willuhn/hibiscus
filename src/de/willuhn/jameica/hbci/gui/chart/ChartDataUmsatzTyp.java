/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
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

  @Override
  public List getData() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(UmsatzTyp.class);
    if (this.type != UmsatzTyp.TYP_EGAL)
      list.addFilter("umsatztyp = " + this.type);
    
    List<Entry> result = new ArrayList<Entry>();
    while (list.hasNext())
    {
      result.add(new Entry((UmsatzTyp)list.next()));
    }
    return result;
  }

  @Override
  public String getLabel() throws RemoteException
  {
    return i18n.tr("Umsatz-Verteilung");
  }

  @Override
  public String getDataAttribute() throws RemoteException
  {
    return "umsatz";
  }

  @Override
  public String getLabelAttribute() throws RemoteException
  {
    return "name";
  }
  
  /**
   * Hilfsklasse, weil wir nur Absolut-Werte wollen.
   */
  public class Entry
  {
    private UmsatzTyp ut = null;
    
    /**
     * ct.
     * @param ut der Umsatz-Typ.
     */
    private Entry(UmsatzTyp ut)
    {
      this.ut = ut;
    }
    
    /**
     * Liefert den Umsatz als Absolut-Wert.
     * @return der Umsatz als Absolut-Wert.
     * @throws RemoteException
     */
    public Double getUmsatz() throws RemoteException
    {
      return Math.abs(this.ut.getUmsatz(days));
    }
    
    /**
     * Liefert den Namen der Kategorie.
     * @return der Namen der Kategorie.
     * @throws RemoteException
     */
    public String getName() throws RemoteException
    {
      return this.ut.getName();
    }
  }
}


/*********************************************************************
 * $Log: ChartDataUmsatzTyp.java,v $
 * Revision 1.7  2010/11/24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.6  2010-08-12 17:12:31  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 **********************************************************************/