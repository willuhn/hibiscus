/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChart.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/05/04 11:05:02 $
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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IGrid;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

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
    if (this.chart == null || this.chart.isDisposed())
      return;
    
    // Cleanup, falls noetig
    {
      ISeriesSet set = this.chart.getSeriesSet();
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
        dataLine.add(new Double(0));
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
      
      ILineSeries lineSeries = (ILineSeries) this.chart.getSeriesSet().createSeries(SeriesType.LINE,id);
      lineSeries.setXDateSeries(labelLine.toArray(new Date[labelLine.size()]));
      lineSeries.setYSeries(toArray(dataLine));
      
      
      //////////////////////////////////////////////////////////////////////////
      // Layout
      lineSeries.setSymbolType(PlotSymbolType.NONE);
      lineSeries.enableArea(true); // Flaeche ausmalen
      lineSeries.setAntialias(SWT.ON);
      lineSeries.enableStack(this.isStacked());
      lineSeries.enableStep(!cd.getCurve());
      //
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Farben
      int[] cValues = cd.getColor();
      if (cValues == null)
        cValues = ColorGenerator.create(ColorGenerator.PALETTE_OFFICE + i);
      lineSeries.setLineColor(getColor(new RGB(cValues[0],cValues[1],cValues[2])));
      //
      //////////////////////////////////////////////////////////////////////////
    }
    
    this.chart.getAxisSet().adjustRange();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.chart != null)
      return;
    
    this.chart = new InteractiveChart(parent,SWT.BORDER);
    this.chart.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.chart.getLegend().setFont(Font.SMALL.getSWTFont());

    ////////////////////////////////////////////////////////////////////////////
    // Farben des Charts
    this.chart.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.chart.setBackgroundInPlotArea(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Titel des Charts
    {
      ITitle title = this.chart.getTitle();
      title.setText(this.getTitle());
      title.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      title.setFont(Font.BOLD.getSWTFont());
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Layout der Achsen
    Color gray = getColor(new RGB(230,230,230));
    
    // X-Achse
    {
      IAxis axis = this.chart.getAxisSet().getXAxis(0);
      axis.getTitle().setFont(Font.SMALL.getSWTFont());
      axis.getTitle().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // wenn wir den auch ausblenden, geht die initiale Skalierung kaputt. Scheint ein Bug zu sein

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);

      IAxisTick tick = axis.getTick();
      tick.setFormat(HBCI.DATEFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }
    
    // Y-Achse
    {
      IAxis axis = this.chart.getAxisSet().getYAxis(0);
      axis.getTitle().setVisible(false);

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);
      
      IAxisTick tick = axis.getTick();
      tick.setFormat(HBCI.DECIMALFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
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


/*********************************************************************
 * $Log: LineChart.java,v $
 * Revision 1.18  2011/05/04 11:05:02  willuhn
 * @B Dispose-Check fehlte
 *
 * Revision 1.17  2010-11-29 22:44:30  willuhn
 * @B getCurve() wurde falsch rum interpretiert ;)
 *
 * Revision 1.16  2010-11-24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 **********************************************************************/