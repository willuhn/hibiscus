/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt die Umsaetze von Kategorien im zeitlichen Verlauf.
 */
public class UmsatzTypVerlauf implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private List<GenericObjectNode> data = null;
  private Date start        = null;
  private Date stop         = null;
  private LineChart chart   = null;
  private Interval interval = Interval.MONTH;
  
  /**
   * Enum fuer die Intervall-Varianten.
   */
  private enum Interval
  {
    YEAR(Calendar.DAY_OF_YEAR,Calendar.YEAR,i18n.tr("Jahr")),
    MONTH(Calendar.DAY_OF_MONTH,Calendar.MONTH,i18n.tr("Monat")),
    WEEK(Calendar.DAY_OF_WEEK,Calendar.WEEK_OF_YEAR,i18n.tr("Woche")),
    
    ;
    
    private int type;
    private int size;
    private String name;
    
    /**
     * ct.
     * @param type
     * @param name
     */
    private Interval(int type, int size, String name)
    {
      this.type = type;
      this.size = size;
      this.name = name;
    }
    
    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
      return this.name;
    }
  }
  
  /**
   * Speichert die anzuzeigenden Daten.
   * @param data Liste mit Objekten des Typs "UmsatzGroup".
   * @param start Start-Datum.
   * @param stop Stop-Datum.
   */
  public void setData(List<GenericObjectNode> data, Date start, Date stop)
  {
    this.data  = data;
    this.start = start;
    this.stop  = stop;
  }
  
  /**
   * Aktualisiert das Chart.
   * @throws RemoteException
   */
  public void redraw() throws RemoteException
  {
    if (chart == null)
      return;

    this.chart.removeAllData();

    int count = 0;

    for (GenericObjectNode node : this.data)
    {
      UmsatzTreeNode group = (UmsatzTreeNode) node;
      ChartDataUmsatz cd = new ChartDataUmsatz(group, interval);
      if (cd.hasData)
      {
        this.chart.addData(cd);
        count = cd.entries.size();
      }
    }
    
    if (count <= 1)
      this.chart.setTitle(i18n.tr("Bitte wählen Sie einen größeren Zeitraum"));
    else
      this.chart.setTitle(i18n.tr("Umsätze der Kategorien im Verlauf (gruppiert nach {0})", this.interval.toString()));
    
    this.chart.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      this.chart = new LineChart();
      this.chart.setStacked(false); // TODO Stacked Graph für "Umsätze nach Kategorieren" BUGZILLA 749
      this.chart.setTitle(i18n.tr("Umsätze der Kategorien im Verlauf (gruppiert nach {0})", this.interval.toString()));
      for (GenericObjectNode node : this.data)
      {
        UmsatzTreeNode group = (UmsatzTreeNode) node;
        ChartDataUmsatz cd = new ChartDataUmsatz(group, interval);
        if (cd.hasData)
          this.chart.addData(cd);
      }
      this.chart.paint(parent);
      addGroupingMenu();
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to create chart",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen des Diagramms"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Erweitert das Contextmenu des Chart um einen Menupunkt "Gruppierung nach".
   */
  private void addGroupingMenu()
  {
    Menu m = this.chart.getChart().getPlotArea().getControl().getMenu();
    
    MenuItem groupMenuItem = new MenuItem(m, SWT.CASCADE, 0);
    groupMenuItem.setText(i18n.tr("Gruppierung nach"));
    
    new MenuItem(m,SWT.SEPARATOR,1);

    Menu groupMenu = new Menu(groupMenuItem);
    groupMenuItem.setMenu(groupMenu);

    final List<MenuItem> items = new ArrayList<MenuItem>();
    SelectionListener l = new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        MenuItem item = (MenuItem) e.getSource();
        
        for (MenuItem i:items)
        {
          i.setSelection(i == item);
        }
        
        // aktuellen Wert uebernehmen
        interval = (Interval) item.getData();
        
        // Und neu zeichnen
        try
        {
          redraw();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to redraw chart",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren"), StatusBarMessage.TYPE_ERROR));
        }
      }
    };

    for (Interval i:Interval.values())
    {
      final MenuItem item = new MenuItem(groupMenu, SWT.CHECK);
      item.setText(i.toString());
      item.setSelection(this.interval == i);
      item.setData(i);
      item.addSelectionListener(l);
      items.add(item);
    }
  }

  /**
   * Hilfsklasse zum "Homogenisieren" der Betraege in den Umsatzgruppen.
   */
  private class ChartDataUmsatz implements LineChartData
  {
    private UmsatzTreeNode group = null;
    private List<Entry> entries  = new ArrayList<Entry>();
    private boolean hasData      = false;
    private Date chartStartDate  = null;
    private Date chartStopDate   = null;
    
    private List<Umsatz> getRecursiveUmsaetze(UmsatzTreeNode group) {
      List<Umsatz> result = new ArrayList<Umsatz>();
      result.addAll(group.getUmsaetze());
      for (UmsatzTreeNode unterkategorie: group.getSubGroups()) {
        result.addAll(getRecursiveUmsaetze(unterkategorie));
      }
      return result;
    }
    
    /**
     * Erzeugt eine Liste mit den aggregierten Daten für eine Linie des Charts 
     * @param group
     * @param interval das ausgewaehlte Intervall.
     * @throws RemoteException
     */
    private ChartDataUmsatz(UmsatzTreeNode group, final Interval interval) throws RemoteException
    {
      HashMap<Date,Double> verteilung = new HashMap<Date, Double>();
      Calendar calendar = Calendar.getInstance();
      this.group = group;
      this.entries.clear();

      for (Umsatz umsatz:getRecursiveUmsaetze(group))
      {
        if (umsatz.getDatum() == null)
        {
          Logger.warn("no date found for umsatz, skipping record");
          continue;
        }
        this.hasData = true;
        calendar.setTime(DateUtil.startOfDay(umsatz.getDatum()));
        calendar.set(interval.type, 1);
        
        Date key = calendar.getTime();
        double aggMonatsWert = verteilung.containsKey(key) ? verteilung.get(key) : 0;
        verteilung.put(key, umsatz.getBetrag() + aggMonatsWert);
      }

      calculateChartInterval(verteilung.keySet());

      calendar.setTime(DateUtil.startOfDay(chartStartDate));
      calendar.set(interval.type, 1);
      Date next = calendar.getTime();

      while(!next.after(chartStopDate))
      {
        Entry aktuellerWert = new Entry();
        aktuellerWert.monat = next;
        if (verteilung.containsKey(next))
          aktuellerWert.betrag = verteilung.get(next);
        entries.add(aktuellerWert);

        calendar.setTime(next);
        calendar.add(interval.size, 1);
        next = calendar.getTime();
      }

      //Dummy-Eintrag für das nächste Intervall, da das letzte reguläre sonst
      //aufgrund der Normalisierung auf den ersten Tag des Intervalls
      //ggf. gar nicht angezeigt wird
      Entry lastEntry = new Entry();
      lastEntry.monat = next;
      entries.add(lastEntry);
    }

    /**
     * Ermittelt das Minimum und Maximum aus dem Set von Datumswerten.
     * @param chartDates das Set der Datumswerte.
     */
    private void calculateChartInterval(Set<Date> chartDates)
    {
      Date min = new Date();
      Date max = new Date();
      
      if (!chartDates.isEmpty())
      {
        min = Collections.min(chartDates);
        max = Collections.max(chartDates);
      }

      // Wenn ein explizites Start-Datum angegeben ist und es sich hinter
      // dem ermittelten befindet, nehmen wir das explizit angegebene
      this.chartStartDate = (start != null && start.after(min)) ? start : min;

      // Wenn ein explizites Stop-Datum angegeben ist und es sich vor
      // dem ermittelten befindet, nehmen wir das explizit angegebene
      this.chartStopDate = (stop != null && stop.before(max)) ? start : max;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
     */
    public List getData() throws RemoteException
    {
      return this.entries;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
      return (String) this.group.getAttribute("name");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
     */
    public String getDataAttribute() throws RemoteException
    {
      return "betrag";
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
     */
    public String getLabelAttribute() throws RemoteException
    {
      return "monat";
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
      UmsatzTyp ut = this.group.getUmsatzTyp();
      
      if (ut == null)
        return null; // "nicht zugeordnet"

      if (!ut.isCustomColor())
        return null; // keine benutzerdefinierte Farbe angegeben
      
      return ut.getColor();
    }
    
    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#isFilled()
     */
    public boolean isFilled() throws RemoteException
    {
      return true;
    }
  }
  
  /**
   * Hilfsobjekt zum Gruppieren pro Monat.
   */
  private class Entry implements GenericObject
  {
    private double betrag = 0;
    private Date monat;

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || other.getID() == null)
        return false;
      return other.getID().equals(this.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("betrag".equals(name))
        return new Double(betrag);
      if ("monat".equals(name))
        return monat;
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"betrag","monat"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      if (monat == null)
        return "foo";
      return monat.toString();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "betrag";
    }
    
  }
}
