/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChart.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/07/13 23:09:36 $
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
import java.util.Vector;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Linien-Diagramms.
 */
public class LineChart extends AbstractChart
{
  private boolean curve  = false;
  private boolean marker = false;

  /**
   * ct.
   * @throws Exception
   */
  public LineChart() throws Exception
  {
    super();
  }
  
  /**
   * Legt fest, ob die Punkte gerade oder zu einer geschwungenen Linie verbunden werden sollen.
   * Default: false.
   * @param curve
   */
  public void setCurve(boolean curve)
  {
    this.curve = curve;
  }
  
  /**
   * Legt fest, ob auf der Linie fuer jeden Messwert noch ein kleines Kaestchen eingezeichnet wird.
   * @param marker true, wenn Kaestchen auf die Linie sollen.
   */
  public void setShowMarker(boolean marker)
  {
    this.marker = marker;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChart#createChart()
   */
  public Chart createChart() throws RemoteException
  {
    // Wir erzeugen ein Chart mit Axen.
    ChartWithAxes chart = ChartWithAxesImpl.create();
    chart.getBlock().setBackground(ColorDefinitionImpl.WHITE()); // Hintergrundfarbe
    chart.getBlock().getOutline().setVisible(true); // Rahmen um alles
    chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL); // Kein 3D
  
    // CUSTOMIZE THE PLOT
    Plot p = chart.getPlot();
    p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());
    p.getOutline().setVisible(false);
    String title = getTitle();
    if (title != null)
    {
      chart.getTitle().getLabel().getCaption().getFont().setSize(11);
      chart.getTitle().getLabel().getCaption().setValue(title);
    }
  
    // CUSTOMIZE THE LEGEND 
    Legend lg = chart.getLegend();
    lg.getText().getFont().setSize(10);
    lg.getInsets().set(10, 5, 0, 0);
    lg.setAnchor(Anchor.NORTH_LITERAL);
  
    // CUSTOMIZE THE X-AXIS
    Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
    xAxisPrimary.setType(AxisType.TEXT_LITERAL);
    xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
    xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
    xAxisPrimary.getTitle().setVisible(false);
  
    // CUSTOMIZE THE Y-AXIS
    Axis yAxisPrimary = chart.getPrimaryOrthogonalAxis(xAxisPrimary);
    yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
    yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
  
    Vector data = getData();
    for (int i=0;i<data.size();++i)
    {
      final Vector labelLine = new Vector();
      final Vector dataLine  = new Vector();
      
      ChartData cd          = (ChartData) data.get(i);
      GenericIterator gi    = cd.getData();
      Formatter format      = cd.getLabelFormatter();
      String label          = cd.getLabel();
      String dataAttribute  = cd.getDataAttribute();
      String labelAttribute = cd.getLabelAttribute();

      if (gi == null || gi.size() == 0 || dataAttribute == null || labelAttribute == null)
      {
        Logger.info("skipping data line, contains no data");
        dataLine.add(new Double(0));
        labelLine.add("");
      }
      else
      {
        while (gi.hasNext())
        {
          GenericObject o = gi.next();
          Object ovalue = o.getAttribute(dataAttribute);
          Object olabel = o.getAttribute(labelAttribute);
          
          if (olabel == null || ovalue == null || !(ovalue instanceof Number))
            continue;
          
          dataLine.add(ovalue);
          labelLine.add(format == null ? olabel : format.format(olabel));
        }
      }

      TextDataSet categoryValues = TextDataSetImpl.create(labelLine);
      NumberDataSet orthoValues1 = NumberDataSetImpl.create(dataLine);

      //   CREATE THE CATEGORY BASE SERIES
      Series seCategory = SeriesImpl.create();
      seCategory.setDataSet(categoryValues);


      //   CREATE THE VALUE ORTHOGONAL SERIES
      LineSeries bs1 = (LineSeries) LineSeriesImpl.create();
      if (label != null) bs1.setSeriesIdentifier(label);
      bs1.setDataSet(orthoValues1);
      bs1.getLabel().setVisible(false);
      bs1.getMarker().setVisible(this.marker);
      bs1.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
      bs1.setCurve(curve);

      //   WRAP THE BASE SERIES IN THE X-AXIS SERIES DEFINITION
      SeriesDefinition sdX = SeriesDefinitionImpl.create();
      sdX.getSeriesPalette().update(0); // SET THE COLORS IN THE PALETTE
      xAxisPrimary.getSeriesDefinitions().add(sdX);
      sdX.getSeries().add(seCategory);
    
      //   WRAP THE ORTHOGONAL SERIES IN THE X-AXIS SERIES DEFINITION
      SeriesDefinition sdY = SeriesDefinitionImpl.create();
      sdY.getSeriesPalette().update(1); // SET THE COLOR IN THE PALETTE
      yAxisPrimary.getSeriesDefinitions().add(sdY);
      sdY.getSeries().add(bs1);
    }
    return chart;
  }
}


/*********************************************************************
 * $Log: LineChart.java,v $
 * Revision 1.3  2006/07/13 23:09:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/