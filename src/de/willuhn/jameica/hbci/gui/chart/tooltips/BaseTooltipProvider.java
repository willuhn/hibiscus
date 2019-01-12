package de.willuhn.jameica.hbci.gui.chart.tooltips;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

/**
 * 
 */
public class BaseTooltipProvider implements ChartTooltipProvider
{
  class ClosestSeriesData {
    public ClosestSeriesData(ISeries serie, double x, double y, int idx)
    {
      closestSerie = serie;
      closestX = x;
      closestY = y;
      seriesIndex = idx;
    }
    double closestX = 0;
    double closestY = 0;
    int seriesIndex = 0;
    ISeries closestSerie = null;
  }
  /* Used to remember the location of the highlight point */
  private int highlightX;
  private int highlightY;
  private int seriesIndex;
  protected boolean highlight;
  protected TooltipAwareChart tooltipChart = null;

  @Override
  public void apply(TooltipAwareChart tooltipChart)
  {
    this.tooltipChart = tooltipChart;
    final Chart chart = tooltipChart.getChart();
    final Composite plotArea = chart.getPlotArea();

    plotArea.addListener(SWT.MouseHover, new Listener() {

      @Override
      public void handleEvent(Event event)
      {
        IAxis xAxis = chart.getAxisSet().getXAxis(0);
        IAxis yAxis = chart.getAxisSet().getYAxis(0);

        Collection<ClosestSeriesData> foundSeries = findClosestSeries(chart, event, xAxis, yAxis);

        if (foundSeries.isEmpty()) {
          return;
        }
        // set tooltip of closest data point
        chart.getPlotArea().setToolTipText(buildTooltipText(foundSeries));

        // remember closest data point
        ClosestSeriesData data = foundSeries.iterator().next();
        highlightX = xAxis.getPixelCoordinate(data.closestX);
        highlightY = yAxis.getPixelCoordinate(data.closestY);
        seriesIndex = data.seriesIndex;
        highlight = true;

        // trigger repaint (paint highlight)
        chart.getPlotArea().redraw();
      }
    });

    plotArea.addListener(SWT.MouseMove, new Listener() {

      @Override
      public void handleEvent(Event arg0)
      {
        highlight = false;
        // Tooltip ausblenden
        plotArea.setToolTipText(null);
        plotArea.redraw();
      }
    });

    plotArea.addListener(SWT.Paint, new Listener() {

      @Override
      public void handleEvent(Event event)
      {
        if (highlight)
        {
          ISeries[] series = chart.getSeriesSet().getSeries();
          paintChartPoint(event.gc, highlightX, highlightY, series[seriesIndex]);
        }
      }
    });

  }

  /**
   * Hier wird der Tooltip-Text gebaut. Dieser besteht aus den Namen der betroffenen Series (falls an dem 
   * aktuellen Punkt mehrere Serien gefunden werden) und der Angabe des X- und Y-Wertes. Letztere sollten in 
   * Kindklassen typabhängig formatiert werden.
   * 
   * @param data
   * @return
   * @see this.getFormattedX()
   * @see this.getFormattedY()
   */
  protected String buildTooltipText(Collection<ClosestSeriesData> foundData)
  {
    StringBuilder tooltipText = new StringBuilder();
    // Zuerst alle betroffenen Datenserien
    for (ClosestSeriesData data : foundData)
    {
      tooltipText.append(getFormattedSeriesLabel(data));
      tooltipText.append("\n");
    }
    // jetzt einmal die Daten
    ClosestSeriesData data = foundData.iterator().next();
    tooltipText.append(getFormattedX(data));
    tooltipText.append(": ");
    tooltipText.append(getFormattedY(data));

    return tooltipText.toString();
  }

  /**
   * Liefert den Namen einer Datenserie für den Tooltip.
   * @param data
   * @return
   */
  protected String getFormattedSeriesLabel(ClosestSeriesData data)
  {
    return data.closestSerie.getId();
  }
  /**
   * Formatiert den Wert auf der X-Achse zur Darstellung im Tooltip
   * @param data
   * @return
   */
  protected String getFormattedX(ClosestSeriesData data)
  {
    return String.valueOf(data.closestX);
  }
  /**
   * Formatiert den Wert auf der Y-Achse zur Darstellung im Tooltip
   * @param data
   * @return
   */
  protected String getFormattedY(ClosestSeriesData data)
  {
    return String.valueOf(data.closestY);
  }
  
  /**
   * Zeichnet einen Indikator an die aktuelle Chart-Position, auf die sich der Tooltip bezieht.
   * 
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
  protected Collection<ClosestSeriesData> findClosestSeries(final Chart chart, Event event, IAxis xAxis, IAxis yAxis)
  {
    double x = xAxis.getDataCoordinate(event.x);
    double y = yAxis.getDataCoordinate(event.y);

    ISeries[] series = chart.getSeriesSet().getSeries();
    double minDist = Double.MAX_VALUE;
    
    Collection<ClosestSeriesData> foundSeries = new ArrayList<ClosestSeriesData>();

    int seriesIdx = 0;
    /* over all series */
    for (ISeries serie : series)
    {
      double[] xS = serie.getXSeries();
      double[] yS = serie.getYSeries();

      /* check all data points */
      for (int i = 0; i < xS.length; i++)
      {
        /* compute distance to mouse position */
        double newDist = Math.sqrt(Math.pow((x - xS[i]), 2) + Math.pow((y - yS[i]), 2));

        /* if closer to mouse, remember */
        if (newDist <= minDist)
        {
          ClosestSeriesData data = new ClosestSeriesData(serie, xS[i], yS[i], seriesIdx);
          if (newDist < minDist) {
            foundSeries.clear();
          }
          foundSeries.add(data);
          minDist = newDist;
        }
      }
      seriesIdx++;
    }
    return foundSeries;
  }
}
