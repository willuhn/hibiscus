/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChart.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/12 15:46:55 $
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

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
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
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Linien-Diagramms.
 */
public class LineChart implements Chart, PaintListener
{

  private IDeviceRenderer idr = null;
  private ChartWithAxes chart = null;

  private String title        = null;
  private Vector data         = new Vector();
  
  private I18N i18n           = null;

  /**
   * ct.
   * @throws Exception
   */
  public LineChart() throws Exception
  {
    // Muessen wir setzen, damit die CHart-Engine auch ausserhalb von Birt laeuft.
    System.setProperty("STANDALONE", "");
    
    final PluginSettings ps = PluginSettings.instance();
    this.idr = ps.getDevice("dv.SWT");
    
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#setTitle(java.lang.String)
   */
  public void setTitle(String title)
  {
    this.title = title;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#addData(de.willuhn.jameica.hbci.gui.chart.ChartData)
   */
  public void addData(ChartData data)
  {
    if (data != null)
      this.data.add(data);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Canvas canvas = new Canvas(parent, SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    canvas.setLayoutData(gd);
    canvas.addPaintListener(this);

    // Wir erzeugen ein Chart mit Axen.
    this.chart = ChartWithAxesImpl.create();
    this.chart.getBlock().setBackground(ColorDefinitionImpl.WHITE()); // Hintergrundfarbe
    this.chart.getBlock().getOutline().setVisible(true); // Rahmen um alles
    this.chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL); // Kein 3D
  
    // CUSTOMIZE THE PLOT
    Plot p = this.chart.getPlot();
    p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());
    p.getOutline().setVisible(false);
    if (this.title != null)
    {
      this.chart.getTitle().getLabel().getCaption().getFont().setSize(11);
      this.chart.getTitle().getLabel().getCaption().setValue(this.title);
    }
  
    // CUSTOMIZE THE LEGEND 
    Legend lg = this.chart.getLegend();
    lg.getText().getFont().setSize(10);
    lg.getInsets().set(10, 5, 0, 0);
    lg.setAnchor(Anchor.NORTH_LITERAL);
  
    // CUSTOMIZE THE X-AXIS
    Axis xAxisPrimary = this.chart.getPrimaryBaseAxes()[0];
    xAxisPrimary.setType(AxisType.TEXT_LITERAL);
    xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
    xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
    xAxisPrimary.getTitle().setVisible(false);
  
    // CUSTOMIZE THE Y-AXIS
    Axis yAxisPrimary = this.chart.getPrimaryOrthogonalAxis(xAxisPrimary);
    yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
    yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
  
    for (int i=0;i<this.data.size();++i)
    {
      final Vector labelLine = new Vector();
      final Vector dataLine  = new Vector();
      
      ChartData cd          = (ChartData) this.data.get(i);
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
      bs1.getMarker().setVisible(false);
      bs1.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
    
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
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent pe)
  {
    // Wir geben den Context an, auf dem gezeichnet werden soll.
    idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, pe.gc);
    
    // Wir holen uns das Composite dazu.
    Composite co = (Composite) pe.getSource();
    Rectangle re = co.getClientArea();
    Bounds bo = BoundsImpl.create(re.x, re.y,re.width, re.height);

    // Skalieren das Teil auf Bildschirm-Format (72 DPI).
    bo.scale(72d / idr.getDisplayServer().getDpiResolution());
        
    // Erzeugen einen Generator
    Generator gr = Generator.instance();
    
    try {
      gr.render(idr,gr.build(idr.getDisplayServer(),this.chart, null,bo,null));
    }
    catch (ChartException gex)
    {
      Logger.error("unable to paint chart",gex);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Zeichnen des Diagramms"));
    }
  }

}


/*********************************************************************
 * $Log: LineChart.java,v $
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/