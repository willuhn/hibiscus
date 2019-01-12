package de.willuhn.jameica.hbci.gui.chart.tooltips;

import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;

/**
 * Tooltips für Charts mit Zeit-Geld Datenreihen
 */
public class DateMoneyTooltipProvider extends BaseTooltipProvider
{
  protected String getFormattedX(ClosestSeriesData data)
  {
    Date date = new Date((long) data.closestX);
    return HBCI.DATEFORMAT.format(date);
  }
  protected String getFormattedY(ClosestSeriesData data)
  {
    return String.valueOf(HBCI.DECIMALFORMAT.format(data.closestY));
  }
}
