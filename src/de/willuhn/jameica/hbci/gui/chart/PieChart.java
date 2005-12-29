/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/Attic/PieChart.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/12/29 01:22:11 $
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
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Torten-Diagramms.
 */
public class PieChart extends AbstractChart
{

  /**
   * ct.
   * @throws Exception
   */
  public PieChart() throws Exception
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChart#createChart()
   */
  public Chart createChart() throws RemoteException
  {
    // Wir erzeugen ein Chart mit Axen.
    ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
    chart.getBlock().setBackground(ColorDefinitionImpl.WHITE()); // Hintergrundfarbe
    chart.getBlock().getOutline().setVisible(true); // Rahmen um alles
    chart.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
  
    // CUSTOMIZE THE PLOT
//    Plot p = chart.getPlot();
//    p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());
//    p.getOutline().setVisible(false);
    chart.getTitle().setVisible(false);
//    String title = getTitle();
//    if (title != null)
//    {
//      chart.getTitle().getLabel().getCaption().getFont().setSize(11);
//      chart.getTitle().getLabel().getCaption().setValue(title);
//    }
  
    // CUSTOMIZE THE LEGEND 
    Legend lg = chart.getLegend();
    lg.getText().getFont().setSize(10);
    lg.getInsets().set(10, 5, 0, 0);
    lg.setAnchor(Anchor.NORTH_LITERAL);
  
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

          Double d = (Double) ovalue;
          if (d.doubleValue() < 0)
            d = new Double(-d.doubleValue());
          dataLine.add(new Double(d.intValue()));
          System.out.println(olabel + ":" + d);

          labelLine.add(format == null ? olabel : format.format(olabel));
        }
      }

      TextDataSet categoryValues = TextDataSetImpl.create(labelLine);
      NumberDataSet orthoValues1 = NumberDataSetImpl.create(dataLine);

      //   CREATE THE CATEGORY BASE SERIES
      Series seCategory = SeriesImpl.create();
      seCategory.setDataSet(categoryValues);
    
      //   CREATE THE VALUE ORTHOGONAL SERIES
      PieSeries ps1 = (PieSeries) PieSeriesImpl.create();
      if (label != null) ps1.setSeriesIdentifier(label);
      ps1.setDataSet(orthoValues1);
      ps1.getLabel().setVisible(false);
//      ps1.setLabelPosition(Position.INSIDE_LITERAL);
    
      SeriesDefinition sd = SeriesDefinitionImpl.create();
      sd.getSeriesPalette().update(0); // SET THE COLOR IN THE PALETTE
      chart.getSeriesDefinitions().add(sd);
      sd.getSeries().add(ps1);
    
    }
    return chart;
  }
}


/*********************************************************************
 * $Log: PieChart.java,v $
 * Revision 1.2  2005/12/29 01:22:11  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.1  2005/12/20 00:03:26  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 **********************************************************************/