/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/AbstractChart.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/20 00:03:27 $
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
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung der Charts.
 */
public abstract class AbstractChart implements Chart, PaintListener
{

  private IDeviceRenderer idr = null;

  private org.eclipse.birt.chart.model.Chart chart = null;
  private String title        = null;
  private Vector data         = new Vector();
  
  private I18N i18n           = null;

  /**
   * ct.
   * @throws Exception
   */
  public AbstractChart() throws Exception
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
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#getTitle()
   */
  public String getTitle()
  {
    return this.title;
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
   * Liefert die anzuzeigenden Daten.
   * @return Anzuzeigende Daten.
   */
  Vector getData()
  {
    return this.data;
  }
  
  /**
   * Muss von den abgeleiteten Klassen implementiert werden, um dort den Chart zu erzeugen.
   * @return zu erzeugendes Chart.
   * @throws RemoteException
   */
  public abstract org.eclipse.birt.chart.model.Chart createChart() throws RemoteException;
  

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Canvas canvas = new Canvas(parent, SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    canvas.setLayoutData(gd);
    canvas.addPaintListener(this);
    this.chart = createChart();
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
 * $Log: AbstractChart.java,v $
 * Revision 1.1  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/