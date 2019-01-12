package de.willuhn.jameica.hbci.gui.chart.tooltips;

/**
 * Charts mit Tooltips sollten dieses Interface implementieren
 */
public interface TooltipAwareChart
{
  /**
   * 
   * @return eine zu den Chartdaten passende Instanz eines Tooltip Providers
   */
  public ChartTooltipProvider getTooltipProvider();
  /**
   * 
   * @return die SWT-Chart Instanz
   */
  public org.swtchart.Chart getChart();
}
