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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;

import de.willuhn.jameica.hbci.HBCI;

/**
 * Chart-Feature, welches Tooltips anzeigt.
 */
public class ChartFeatureTooltip implements ChartFeature
{
  private int highlightX;
  private int highlightY;
  private int seriesIndex;
  private String tooltip;
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartFeature#onEvent(de.willuhn.jameica.hbci.gui.chart.ChartFeature.Event)
   */
  @Override
  public boolean onEvent(Event e)
  {
    return e == Event.PAINT;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartFeature#handleEvent(de.willuhn.jameica.hbci.gui.chart.ChartFeature.Event, de.willuhn.jameica.hbci.gui.chart.ChartFeature.Context)
   */
  @Override
  public void handleEvent(Event e, Context ctx)
  {
    final Chart c = ctx.chart.getChart();
    final Control control = c.getPlotArea().getControl();

    control.addMouseTrackListener(new MouseTrackAdapter()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void mouseHover(MouseEvent event)
      {
        final IAxis xAxis = c.getAxisSet().getXAxis(0);
        final IAxis yAxis = c.getAxisSet().getYAxis(0);

        final Collection<SeriesData> foundSeries = findClosestSeries(c, event, xAxis, yAxis);

        if (foundSeries.isEmpty())
          return;
        
        tooltip = getTooltipText(foundSeries);

        final SeriesData data = foundSeries.iterator().next();
        highlightX = xAxis.getPixelCoordinate(data.closestX);
        highlightY = yAxis.getPixelCoordinate(data.closestY);
        seriesIndex = data.seriesIndex;
        control.redraw();
      }
    });

    control.addMouseMoveListener(new MouseMoveListener() {
      @Override
      public void mouseMove(MouseEvent e)
      {
        if (tooltip != null)
        {
          tooltip = null;
          c.getPlotArea().setToolTipText(null);
          control.redraw();
        }
      }
    });

    control.addPaintListener(new PaintListener() {

      @Override
      public void paintControl(PaintEvent event)
      {
        if (tooltip != null)
        {
          final ISeries[] series = c.getSeriesSet().getSeries();
          c.getPlotArea().setToolTipText(tooltip);
          paintChartPoint(event.gc, highlightX, highlightY, series[seriesIndex]);
          c.layout(true);
        }
      }
    });

  }

  /**
   * Hier wird der Tooltip-Text gebaut. Dieser besteht aus den Namen der betroffenen Series (falls an dem 
   * aktuellen Punkt mehrere Serien gefunden werden) und der Angabe des X- und Y-Wertes. Letztere koennen in 
   * Kindklassen typabhängig formatiert werden. Die Default-Implementierung geht davon aus, dass es sich um ein Line-Chart
   * mit Zeitraum auf der X-Achse und Geldbetraegen auf der Y-Achse handelt.
   * @param foundData
   * @return der Tooltip-Text.
   */
  protected String getTooltipText(Collection<SeriesData> foundData)
  {
    StringBuilder text = new StringBuilder();
    
    // Zuerst alle betroffenen Datenserien
    for (SeriesData data : foundData)
    {
      text.append(formatSeriesLabel(data));
      text.append("\n");
    }
    
    // jetzt einmal die Daten
    SeriesData data = foundData.iterator().next();
    text.append(this.formatSeriesValue(data));
    return text.toString();
  }

  /**
   * Liefert den Namen einer Datenserie für den Tooltip.
   * @param data
   * @return
   */
  protected String formatSeriesLabel(SeriesData data)
  {
    return data.closestSerie.getId();
  }
  
  /**
   * Liefert den anzuzeigenden Tooltip-Text.
   * @param data der Datensatz, fuer den der Tooltip angezeigt werden soll.
   * @return der Tooltip-Text,
   */
  protected String formatSeriesValue(SeriesData data)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(HBCI.DATEFORMAT.format(new Date((long) data.closestX)));
    sb.append(": ");
    sb.append(HBCI.DECIMALFORMAT.format(data.closestY));
    return sb.toString();
  }
  
  /**
   * Zeichnet einen Indikator an die aktuelle Chart-Position, auf die sich der Tooltip bezieht.
   * @param gc
   * @param highlightX
   * @param highlightY
   */
  protected void paintChartPoint(GC gc, int highlightX, int highlightY, ISeries series)
  {
    Color color = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
    if (series instanceof ILineSeries) {
      color = ((ILineSeries) series).getLineColor();
    }
    gc.setBackground(color);
    gc.setAlpha(128);
    gc.fillOval(highlightX - 5, highlightY - 5, 10, 10);
  }

  /**
   * Ermittelt das oder die DataSets, die am nächsten zum Mauszeiger liegen.
   * @param chart
   * @param event
   * @param xAxis
   * @param yAxis
   * @return
   */
  protected Collection<SeriesData> findClosestSeries(final Chart chart, MouseEvent event, IAxis xAxis, IAxis yAxis)
  {
    double x = xAxis.getDataCoordinate(event.x);
    double y = yAxis.getDataCoordinate(event.y);

    ISeries[] series = chart.getSeriesSet().getSeries();
    double minDist = Double.MAX_VALUE;
    
    Collection<SeriesData> foundSeries = new ArrayList<SeriesData>();

    for (int k=0;k<series.length;++k)
    {
      ISeries serie = series[k];
      double[] xS = serie.getXSeries();
      double[] yS = serie.getYSeries();

      for (int i=0; i<xS.length;++i)
      {
        // Entfernung errechnen
        double newDist = Math.sqrt(Math.pow((x - xS[i]), 2) + Math.pow((y - yS[i]), 2));

        // Sind wir nah genug dran?
        if (newDist <= minDist)
        {
          // Wenn wir naeher dran sind, die vorherigen vergessen
          if (newDist < minDist)
            foundSeries.clear();

          SeriesData data = new SeriesData(serie, xS[i], yS[i], k);
          foundSeries.add(data);
          minDist = newDist;
        }
      }
    }
    return foundSeries;
  }
  
  /**
   * Haelt die Werte des ausgewaehlten Datensatzes.
   */
  protected class SeriesData
  {
    double closestX = 0;
    double closestY = 0;
    int seriesIndex = 0;
    ISeries closestSerie = null;
    
    /**
     * ct.
     * @param serie
     * @param x
     * @param y
     * @param idx
     */
    private SeriesData(ISeries serie, double x, double y, int idx)
    {
      closestSerie = serie;
      closestX = x;
      closestY = y;
      seriesIndex = idx;
    }
    
  }

}
