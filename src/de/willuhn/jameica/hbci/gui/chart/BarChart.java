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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IAxisTick;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.IGrid;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesLabel;
import org.eclipse.swtchart.ITitle;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.extensions.charts.InteractiveChart;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ColorGenerator;

/**
 * Implementierung eines Balken-Diagramms.
 */
public class BarChart extends AbstractChart
{
  private Composite comp = null;

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#redraw()
   */
  public void redraw() throws RemoteException
  {
    // redraw ohne paint() Weia ;)
    if (this.comp == null || this.comp.isDisposed())
      return;

    // Cleanup
    SWTUtil.disposeChildren(this.comp);
    this.comp.setLayout(SWTUtil.createGrid(1,false));

    setChart(new InteractiveChart(this.comp,SWT.BORDER));
    getChart().setLayoutData(new GridData(GridData.FILL_BOTH));
    getChart().getLegend().setVisible(false);
    getChart().setOrientation(SWT.VERTICAL);

    ////////////////////////////////////////////////////////////////////////////
    // Farben des Charts
    getChart().setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    getChart().setBackgroundInPlotArea(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Titel des Charts
    {
      ITitle title = getChart().getTitle();
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
      IAxis axis = getChart().getAxisSet().getXAxis(0);
      axis.getTitle().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // wenn wir den auch ausblenden, geht die initiale Skalierung kaputt. Scheint ein Bug zu sein

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);

      axis.getTick().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
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
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Neu zeichnen
    List<ChartData> data = getData();
    for (int i=0;i<data.size();++i)
    {
      final List<String> labelLine = new LinkedList<String>();
      final List<Number> dataLine  = new LinkedList<Number>();

      ChartData cd          = (ChartData) data.get(i);
      List list             = cd.getData();
      String dataAttribute  = cd.getDataAttribute();
      String labelAttribute = cd.getLabelAttribute();

      if (list == null || list.size() == 0 || dataAttribute == null || labelAttribute == null)
      {
        Logger.debug("skipping data line, contains no data");
        dataLine.add(new Double(0));
        labelLine.add("");
      }
      else
      {
        for (Object o:list)
        {
          Object value = BeanUtil.get(o,dataAttribute);
          Object label = BeanUtil.get(o,labelAttribute);

          if (label == null || value == null || !(value instanceof Number))
            continue;

          Number n = (Number) value;
          if (Math.abs(n.doubleValue()) < 0.01d)
            continue; // ueberspringen, nix drin
          dataLine.add(n);
          labelLine.add(label.toString());
        }
      }
      if (dataLine.size() == 0)
        continue; // wir haben gar keine Werte

      IAxis axis = getChart().getAxisSet().getXAxis(0);
      axis.setCategorySeries(labelLine.toArray(new String[labelLine.size()]));
      axis.enableCategory(true);

      IBarSeries barSeries = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR,Integer.toString(i));
      barSeries.setYSeries(toArray(dataLine));

      //////////////////////////////////////////////////////////////////////////
      // Layout
      int[] cValues = ColorGenerator.create(ColorGenerator.PALETTE_ECLIPSE + i);
      Color color = getColor(new RGB(cValues[0],cValues[1],cValues[2]));
      barSeries.setBarColor(color);

      ISeriesLabel label = barSeries.getLabel();
      label.setFont(Font.SMALL.getSWTFont());
      label.setFormat(HBCI.DECIMALFORMAT.toPattern()); // BUGZILLA 1123
      label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      label.setVisible(true);
      //
      //////////////////////////////////////////////////////////////////////////
    }

    // Titel aktualisieren
    ITitle title = getChart().getTitle();
    title.setText(this.getTitle());

    this.comp.layout();
    getChart().getAxisSet().adjustRange();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.comp != null)
      return;

    this.comp = new Composite(parent,SWT.NONE);
    this.comp.setLayoutData(new GridData(GridData.FILL_BOTH));

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
}

/*********************************************************************
 * $Log: BarChart.java,v $
 * Revision 1.3  2011/08/29 08:04:58  willuhn
 * @B Dispose-Check fehlte
 * @N Umsatzverteilung verzoegert neu berechnen - der Schieberegler hakelt sonst
 *
 * Revision 1.2  2011-08-28 20:47:56  willuhn
 * @B BUGZILLA 1123
 *
 * Revision 1.1  2010-11-24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 **********************************************************************/