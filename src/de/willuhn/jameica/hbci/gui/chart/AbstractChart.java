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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.extensions.charts.Messages;
import org.eclipse.swtchart.internal.Legend;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.ChartFeature.Context;
import de.willuhn.jameica.hbci.gui.chart.ChartFeature.Event;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung der Charts.
 * @param <T> der Typ der Chartdaten.
 */
public abstract class AbstractChart<T extends ChartData> implements Chart<T>
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private String title                = null;
  private Map<RGB,Color> colors       = new HashMap<RGB,Color>();
  private List<T> data                = new ArrayList<T>();
  private org.eclipse.swtchart.Chart chart = null;
  private List<ChartFeature> features = new ArrayList<ChartFeature>();

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#setTitle(java.lang.String)
   */
  public void setTitle(String title)
  {
    this.title = title;
    if (this.chart != null && !this.chart.isDisposed())
      this.chart.getTitle().setText(this.title);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#getTitle()
   */
  public String getTitle()
  {
    return this.title;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#getChart()
   */
  public org.eclipse.swtchart.Chart getChart()
  {
    return this.chart;
  }

  /**
   * Speichert das SWT-Chart-Objekt.
   * @param chart
   */
  protected void setChart(final org.eclipse.swtchart.Chart chart)
  {
    this.chart = chart;
    if (this.chart == null)
      return;
    
    final Legend l = (Legend) chart.getLegend();
    l.addMouseListener(new MouseAdapter()
    {
      /**
       * Schaltet die Sichtbarkeit der Series bei Doppelklick um.
       * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
       */
      @Override
      public void mouseDoubleClick(MouseEvent e)
      {
        ISeries s = getSeries(chart, l, e.x, e.y);
        if (s != null)
        {
          // Sichtbarkeit umschalten
          s.setVisible(!s.isVisible());
          chart.redraw();
        }
      }
    });
    
    l.setMenu(createLegendContextMenu(chart, l));
  }

  /**
   * Erzeugt ein Contextmenu fuer die Legende.
   * @param chart das Chart-Objekt.
   * @param l die Legende.
   * @return das Contextmenu.
   */
  private Menu createLegendContextMenu(final org.eclipse.swtchart.Chart chart, Legend l)
  {
    Menu m = new Menu(l.getParent().getShell(), SWT.POP_UP);
    addShowMenuItem(m, i18n.tr("alle anzeigen"), true);
    addShowMenuItem(m, i18n.tr("alle ausblenden"), false);
    return m;
  }

  /**
   * Erzeugt einen Menu-Eintrag.
   * @param m das zugehoerige Menu.
   * @param text der anzuzeigende Text.
   * @param setVisibleValue das Sichtbarkeitsflag, welches beim Klick auf den Menu-Eintrag ausgefuehrt werden soll.
   */
  private void addShowMenuItem(Menu m, String text, final boolean setVisibleValue)
  {
    MenuItem item = new MenuItem(m, SWT.PUSH);
    item.setText(text);
    item.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        ISeriesSet seriesSet = chart.getSeriesSet();
        for (ISeries s : seriesSet.getSeries())
        {
          s.setVisible(setVisibleValue);
        }
        chart.redraw();
      }
    });
  }

  /**
   * Liefert die Datenreihe fuer die angegebene Position.
   * @param chart das Chart.
   * @param l die Legende.
   * @param x die X-Position.
   * @param y die Y-Position.
   * @return
   */
  private ISeries getSeries(org.eclipse.swtchart.Chart chart, Legend l, int x, int y)
  {
    ISeriesSet seriesSet = chart.getSeriesSet();
    for (ISeries s:seriesSet.getSeries())
    {
      Rectangle sbounds = l.getBounds(s.getId());
      if(sbounds.contains(x, y))
        return s;
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#addData(de.willuhn.jameica.hbci.gui.chart.ChartData)
   */
  public void addData(T data)
  {
    if (data != null)
      this.data.add(data);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeData(de.willuhn.jameica.hbci.gui.chart.ChartData)
   */
  public void removeData(T data)
  {
    if (data != null)
      this.data.remove(data);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeAllData()
   */
  public void removeAllData()
  {
    if (this.data != null)
      this.data.clear();
  }

  /**
   * Liefert die anzuzeigenden Daten.
   * @return Anzuzeigende Daten.
   */
  List<T> getData()
  {
    return this.data;
  }
  
  /**
   * Erzeugt eine Farbe, die automatisch disposed wird.
   * Die Funktion hat einen internen Cache. Wenn die Farbe schon im Cache
   * vorhanden ist, wird diese genommen.
   * @param rgb der Farbcode.
   * @return die Farbe.
   */
  Color getColor(RGB rgb)
  {
    // Schon im Cache?
    Color c = this.colors.get(rgb);
    if (c != null && !c.isDisposed())
      return c;
    
    c = new Color(GUI.getDisplay(),rgb);
    this.colors.put(rgb,c);
    return c;
  }
  
  /**
   * Entfernt den Menu-Eintrag "Properties" aus dem InteractiveChart,
   * weil das nur in Eclipse funktioniert. In Jameica wuerde das
   * eine Exception im Main-Loop ausloesen.
   */
  private void cleanMenu()
  {
    try
    {
      MenuItem[] items = this.chart.getPlotArea().getControl().getMenu().getItems();
      if (items == null || items.length == 0)
        return;

      for (int i=0;i<items.length;++i)
      {
        MenuItem mi = items[i];
        String text = mi.getText();
        if (text == null)
          continue;
        if (text.equals(Messages.PROPERTIES))
        {
          mi.dispose();
          
          // Den Separator davor gleich noch entfernen
          if (i > 0) items[i-1].dispose();
          return;
        }
      }
    }
    catch (Exception e)
    {
      // Dann halt nicht ;)
    }
  }
  

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    parent.addDisposeListener(new DisposeListener() {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      public void widgetDisposed(DisposeEvent e)
      {
        try
        {
          for (Color c : colors.values())
          {
            if (c != null && !c.isDisposed())
              c.dispose();
          }
        }
        finally
        {
          colors.clear();
        }
      }
    });
    cleanMenu();
    this.featureEvent(Event.PAINT);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#addFeature(de.willuhn.jameica.hbci.gui.chart.ChartFeature)
   */
  @Override
  public void addFeature(ChartFeature feature)
  {
    this.features.add(feature);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeFeature(de.willuhn.jameica.hbci.gui.chart.ChartFeature)
   */
  @Override
  public void removeFeature(ChartFeature feature)
  {
    this.features.remove(feature);
  }
  
  /**
   * Loest ein Feature-Event aus.
   * @param e das Event.
   */
  private void featureEvent(ChartFeature.Event e)
  {
    if (this.features.size() == 0)
      return;
    
    Context ctx = new Context();
    ctx.chart = this;
    ctx.event = e;
    
    for (ChartFeature f:this.features)
    {
      if (f.onEvent(e))
      {
        try
        {
          f.handleEvent(e,ctx);
        }
        catch (Exception ex)
        {
          Logger.error("error while handling event " + e,ex);
        }
      }
    }
  }

}
