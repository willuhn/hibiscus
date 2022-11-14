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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IAxisTick;
import org.eclipse.swtchart.IGrid;
import org.eclipse.swtchart.ILegend;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ILineSeries.PlotSymbolType;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.ITitle;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.extensions.charts.InteractiveChart;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ColorGenerator;

/**
 * Implementierung eines Linien-Diagramms.
 */
public class LineChart extends AbstractChart<LineChartData>
{
  private boolean stacked = false;
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#redraw()
   */
  public void redraw() throws RemoteException
  {
    if (getChart() == null || getChart().isDisposed())
      return;
    
    // Cleanup, falls noetig
    {
      ISeriesSet set = getChart().getSeriesSet();
      ISeries[] series = set.getSeries();
      for (ISeries s:series)
        set.deleteSeries(s.getId());
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Neu zeichnen
    List<LineChartData> data = getData();
    for (int i=0;i<data.size();++i)
    {
      final List<Date> labelLine   = new LinkedList<Date>();
      final List<Number> dataLine  = new LinkedList<Number>();
      
      LineChartData cd      = data.get(i);
      List list             = cd.getData();
      String dataAttribute  = cd.getDataAttribute();
      String labelAttribute = cd.getLabelAttribute();

      if (list == null || list.size() == 0 || dataAttribute == null || labelAttribute == null)
      {
        Logger.debug("skipping data line, contains no data");
        dataLine.add(Double.valueOf(0.0d));
        labelLine.add(new Date());
      }
      else
      {
        for (Object o:list)
        {
          Object value = BeanUtil.get(o,dataAttribute);
          Object label = BeanUtil.get(o,labelAttribute);
          
          if (label == null || value == null || !(value instanceof Number) || !(label instanceof Date))
            continue;

          dataLine.add((Number) value);
          labelLine.add((Date) label);
        }
      }

      String id = Integer.toString(i+1) + ". ";
      if (cd.getLabel() != null)
        id += " " + cd.getLabel();
      
      ILineSeries lineSeries = (ILineSeries) getChart().getSeriesSet().createSeries(SeriesType.LINE,id);
      lineSeries.setXDateSeries(labelLine.toArray(new Date[0]));
      lineSeries.setYSeries(toArray(dataLine));
      
      
      //////////////////////////////////////////////////////////////////////////
      // Layout
      lineSeries.setSymbolType(PlotSymbolType.NONE);
      lineSeries.enableArea(true); // Flaeche ausmalen
      lineSeries.setAntialias(SWT.ON);
      lineSeries.enableStack(this.isStacked());
      lineSeries.enableStep(!cd.getCurve());
      lineSeries.setLineWidth(cd.getLineWidth());
      //
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Farben
      int[] cValues = cd.getColor();
      if (cValues == null)
        cValues = ColorGenerator.create(ColorGenerator.PALETTE_OFFICE + i);
      lineSeries.setLineColor(getColor(new RGB(cValues[0],cValues[1],cValues[2])));
      lineSeries.enableArea(cd.isFilled());
      //
      //////////////////////////////////////////////////////////////////////////
    }
    
    getChart().getAxisSet().adjustRange();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (getChart() != null)
      return;
    
    this.addFeature(new ChartFeatureTooltip()); // Bei Linecharts per Default Tooltips unterstuetzen
    this.setChart(new InteractiveChart(parent,SWT.BORDER));
    this.getChart().setLayoutData(new GridData(GridData.FILL_BOTH));

    ////////////////////////////////////////////////////////////////////////////
    // Farben des Charts
    this.getChart().setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.getChart().setBackgroundInPlotArea(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Titel des Charts
    {
      ITitle title = getChart().getTitle();
      title.setText(this.getTitle());
      title.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
      title.setFont(Font.BOLD.getSWTFont());
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Legende
    {
      ILegend legend = getChart().getLegend();
      legend.setFont(Font.SMALL.getSWTFont());
      legend.setPosition(SWT.RIGHT);
      legend.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Layout der Achsen
    Color gray = getColor(new RGB(234,234,234));
    
    // X-Achse
    {
      IAxis axis = getChart().getAxisSet().getXAxis(0);
      axis.getTitle().setFont(Font.SMALL.getSWTFont());
      axis.getTitle().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // wenn wir den auch ausblenden, geht die initiale Skalierung kaputt. Scheint ein Bug zu sein

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);

      IAxisTick tick = axis.getTick();
      tick.setFormat(HBCI.DATEFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }
    
    // Y-Achse
    {
      IAxis axis = getChart().getAxisSet().getYAxis(0);
      axis.getTitle().setVisible(false);

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);
      
      IAxisTick tick = axis.getTick();
      tick.setFormat(HBCI.DECIMALFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    redraw();
    super.paint(parent);
  }
  
  /**
   * Wandelt die Liste in ein Array von doubles um.
   * @param list die Liste.
   * @return das Array.
   */
  private double[] toArray(List<Number> list)
  {
    double[] values = new double[list.size()];
    for (int i=0;i<list.size();++i)
    {
      values[i] = list.get(i).doubleValue();
    }
    return values;
  }
  
  /**
   * Liefert true, wenn die Linien uebereinandergestapelt werden sollen (stacked).
   * @return true, wenn die Linien stacked gezeichnet werden sollen.
   */
  public boolean isStacked()
  {
    return this.stacked;
  }
  
  /**
   * Legt fest, ob die Linien uebereinandergestapelt werden sollen (stacked).
   * @param b true, wenn die Linien stacked gezeichnet werden sollen.
   */
  public void setStacked(boolean b)
  {
    this.stacked = b;
  }
}
