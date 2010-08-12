/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChart.java,v $
 * $Revision: 1.14 $
 * $Date: 2010/08/12 10:13:41 $
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
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.PaletteImpl;
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
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.emf.common.util.EList;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.logging.Logger;
import de.willuhn.util.ColorGenerator;

/**
 * Implementierung eines Linien-Diagramms.
 */
public class LineChart extends AbstractChart
{
  private boolean stacked = false;
  
  /**
   * ct.
   * @throws Exception
   */
  public LineChart() throws Exception
  {
    super();
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
  
    int fontSize = Font.DEFAULT.getSWTFont().getFontData()[0].getHeight();

    // CUSTOMIZE THE PLOT
    Plot p = chart.getPlot();
    p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());
    p.getOutline().setVisible(false);
    String title = getTitle();
    if (title != null)
    {
      chart.getTitle().getLabel().getCaption().getFont().setSize(Font.H2.getSWTFont().getFontData()[0].getHeight());
      chart.getTitle().getLabel().getCaption().setValue(title);
    }
  
    // CUSTOMIZE THE LEGEND 
    Legend lg = chart.getLegend();
    lg.getText().getFont().setSize(fontSize);
    lg.getInsets().set(20, 5, 0, 0);
    lg.setAnchor(Anchor.NORTH_LITERAL);
  
    // CUSTOMIZE THE X-AXIS
    Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
    xAxisPrimary.setType(AxisType.TEXT_LITERAL);
    xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
    xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
    xAxisPrimary.getTitle().setVisible(false);
    xAxisPrimary.getLabel().getCaption().getFont().setSize(fontSize);
  
    // CUSTOMIZE THE Y-AXIS
    Axis yAxisPrimary = chart.getPrimaryOrthogonalAxis(xAxisPrimary);
    yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
    yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
    yAxisPrimary.getLabel().getCaption().getFont().setSize(fontSize);
  
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
        // Wir machen vorher nochmal ein Reset
        gi.begin();
        while (gi.hasNext())
        {
          Object o = gi.next();
          Object ovalue = BeanUtil.get(o,dataAttribute);
          Object olabel = BeanUtil.get(o,labelAttribute);
          
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
      AreaSeries bs1 = (AreaSeries) AreaSeriesImpl.create();
      bs1.setStacked(this.isStacked());

      SeriesDefinition sdX = SeriesDefinitionImpl.create();
      xAxisPrimary.getSeriesDefinitions().add(sdX);
      sdX.getSeries().add(seCategory);
    
      SeriesDefinition sdY = SeriesDefinitionImpl.create();
      yAxisPrimary.getSeriesDefinitions().add(sdY);

      if (label != null) bs1.setSeriesIdentifier(label);
      bs1.setDataSet(orthoValues1);
      bs1.getLabel().setVisible(false);
  

      int[] color = null;
      if (cd instanceof LineChartData)
        color = ((LineChartData)cd).getColor();

      if (color == null)
        color = ColorGenerator.create(ColorGenerator.PALETTE_OFFICE + i);
      
      ColorDefinition bg = ColorDefinitionImpl.create(color[0],color[1],color[2]);
      bg.setTransparency(120);

      sdY.setSeriesPalette(PaletteImpl.create(bg));
      EList colors = sdY.getSeriesPalette().getEntries();
      colors.add(bg);

      sdY.getSeriesPalette().update(bg);

      // Die Linie selbst machen wir etwas dunkler als die Hintergrundfarbe
      int r = color[0] - 90;
      int g = color[1] - 90;
      int b = color[2] - 90;
      if (r < 0) r = 0;
      if (g < 0) g = 0;
      if (b < 0) b = 0;
      bs1.getLineAttributes().setColor(ColorDefinitionImpl.create(r,g,b));
      bs1.getLineAttributes().setVisible(true);

      if (cd instanceof LineChartData)
      {
        LineChartData lcd = (LineChartData) cd;
        bs1.setCurve(lcd.getCurve());
      }

      sdY.getSeries().add(bs1);

    }
    return chart;
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
 * Revision 1.14  2010/08/12 10:13:41  willuhn
 * @R Der NULL-Check ist unnoetig
 *
 * Revision 1.13  2010-08-11 15:48:57  willuhn
 * @B Objekte mit fehlenden Attributen nicht ueberspringen sondern stattdessen NULL im Vector speichern. Sonst kann es zu einer "ChartException: Mismatch (x !=y) in dataset count found in stacked runtime series" kommen, wenn mehrere Linien gezeichnet werden und die eine unterschiedliche Anzahl von Elementen haben
 *
 * Revision 1.12  2009/08/27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich  eine Trendkurve an
 *
 * Revision 1.11  2009/08/26 10:29:44  willuhn
 * @N Kommentar fuer stacked line chart
 *
 * Revision 1.10  2009/08/24 23:55:04  willuhn
 * @N Bei der Office-Farbpalette beginnen
 *
 * Revision 1.9  2009/08/21 23:00:16  willuhn
 * @C Erzeugung der Farben in neue Klasse verschoben
 *
 * Revision 1.8  2008/08/29 14:30:19  willuhn
 * @C Java 1.4 Compatibility - wieso zur Hoelle sind die Fehler vorher nie aufgefallen? Ich compiliere immer gegen 1.4? Suspekt
 *
 * Revision 1.7  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.6  2007/04/19 18:10:12  willuhn
 * @B fehlendes Reset des Iterators vor der Verwendung
 *
 * Revision 1.5  2006/08/01 21:29:12  willuhn
 * @N Geaenderte LineCharts
 *
 * Revision 1.4  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
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