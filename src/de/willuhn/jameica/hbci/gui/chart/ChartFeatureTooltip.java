package de.willuhn.jameica.hbci.gui.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

import de.willuhn.jameica.hbci.HBCI;

/**
 * Chart-Feature, welches Tooltips anzeigt.
 */
public class ChartFeatureTooltip implements ChartFeature
{
  private int highlightX;
  private int highlightY;
  private int seriesIndex;
  protected boolean highlight;

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
    final Composite plotArea = c.getPlotArea();

    plotArea.addListener(SWT.MouseHover, new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        IAxis xAxis = c.getAxisSet().getXAxis(0);
        IAxis yAxis = c.getAxisSet().getYAxis(0);

        Collection<SeriesData> foundSeries = findClosestSeries(c, event, xAxis, yAxis);

        if (foundSeries.isEmpty())
          return;
        
        // set tooltip of closest data point
        c.getPlotArea().setToolTipText(getTooltipText(foundSeries));

        // remember closest data point
        SeriesData data = foundSeries.iterator().next();
        highlightX = xAxis.getPixelCoordinate(data.closestX);
        highlightY = yAxis.getPixelCoordinate(data.closestY);
        seriesIndex = data.seriesIndex;
        highlight = true;

        // trigger repaint (paint highlight)
        c.getPlotArea().redraw();
      }
    });

    plotArea.addListener(SWT.MouseMove, new Listener() {

      @Override
      public void handleEvent(org.eclipse.swt.widgets.Event arg0)
      {
        highlight = false;
        // Tooltip ausblenden
        plotArea.setToolTipText(null);
        plotArea.redraw();
      }
    });

    plotArea.addListener(SWT.Paint, new Listener() {

      @Override
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        if (highlight)
        {
          ISeries[] series = c.getSeriesSet().getSeries();
          paintChartPoint(event.gc, highlightX, highlightY, series[seriesIndex]);
        }
      }
    });

  }

  /**
   * Hier wird der Tooltip-Text gebaut. Dieser besteht aus den Namen der betroffenen Series (falls an dem 
   * aktuellen Punkt mehrere Serien gefunden werden) und der Angabe des X- und Y-Wertes. Letztere koennen in 
   * Kindklassen typabhängig formatiert werden. Die Default-Implementierung geht davon aus, dass es sich um ein Line-Chart
   * mit Zeitraum auf der X-Achse und Geldbetraegen auf der Y-Achse handelt.
   * @param data
   * @return der Tooltip-Text.
   * @see this.getFormattedX()
   * @see this.getFormattedY()
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
  protected Collection<SeriesData> findClosestSeries(final Chart chart, org.eclipse.swt.widgets.Event event, IAxis xAxis, IAxis yAxis)
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
