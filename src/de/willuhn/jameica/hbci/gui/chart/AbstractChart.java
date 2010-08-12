/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/AbstractChart.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/08/12 17:12:31 $
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
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung der Charts.
 */
public abstract class AbstractChart implements Chart
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private IDeviceRenderer idr = null;

  private org.eclipse.birt.chart.model.Chart chart = null;
  private String title      = null;
  private Vector data       = new Vector();

  private Composite parent  = null;
  private Canvas canvas     = null;
  
  private RedrawListener listener = null;

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
    this.listener = new RedrawListener();
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
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeData(de.willuhn.jameica.hbci.gui.chart.ChartData)
   */
  public void removeData(ChartData data)
  {
    if (data != null)
      this.data.remove(data);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeAllData()
   */
  public void removeAllData()
  {
    if (this.data != null)
      this.data.clear();
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
    this.parent = parent;
    redraw();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#redraw()
   */
  public void redraw() throws RemoteException
  {
    if (this.parent == null || this.parent.isDisposed())
    {
      Logger.warn("unable to redraw chart - no parent composite defined or parent disposed");
      return;
    }

    if (this.canvas == null)
    {
      this.canvas = new Canvas(parent, SWT.NONE);
      this.canvas.addPaintListener(this.listener);
    }
    else
    {
      Logger.debug("dispose old chart");
      SWTUtil.disposeChildren(this.canvas);
    }
    
    GridData gd = new GridData(GridData.FILL_BOTH);
    this.canvas.setLayoutData(gd);
    
    this.chart = null; // forciert das Neuladen der Daten
    this.canvas.redraw();
  }

  /**
   * Zeichnet den bei Paint-Events neu.
   */
  private class RedrawListener implements PaintListener
  {
    /**
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent event)
    {
      try
      {
        GC gc = event.gc;
  
        // Wir geben den Context an, auf dem gezeichnet werden soll.
        idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
  
        Rectangle re = canvas.getClientArea();
        Bounds bo = BoundsImpl.create(re.x, re.y, re.width, re.height);
  
        // Skalieren das Teil auf Bildschirm-Format (72 DPI).
        bo.scale(72d / idr.getDisplayServer().getDpiResolution());
  
        // Erzeugen einen Generator
        Generator gr = Generator.instance();
        
  
        if (chart == null)
          chart = createChart();
        
        gr.render(idr, gr.build(idr.getDisplayServer(), chart, bo, null, null));
      }
      catch (SWTException se)
      {
        // Windows 2000-Behaviour
        String text = se.getMessage();
        if (text != null && text.indexOf("GDI+") != -1)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler: Bitte installieren Sie GDI+"),StatusBarMessage.TYPE_ERROR));
        }
        else
        {
          Logger.error("unable to paint chart", se);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zeichnen des Diagramms"),StatusBarMessage.TYPE_ERROR));
        }
      }
      catch (Exception ex)
      {
        Logger.error("unable to paint chart", ex);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zeichnen des Diagramms"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
}

/*********************************************************************
 * $Log: AbstractChart.java,v $
 * Revision 1.7  2010/08/12 17:12:31  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 * Revision 1.6 2010-08-11 16:06:05 willuhn
 * 
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 * 
 *    Revision 1.5 2008/03/20 10:20:09 willuhn
 * @C Fehler fangen, wenn GDI+ nicht installiert ist (nur Windows 2000)
 * 
 *    Revision 1.4 2008/02/26 01:01:16 willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp.
 *    die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet
 *    werden muss
 * 
 *    Revision 1.3 2006/04/20 08:44:21 willuhn
 * @C s/Childs/Children/
 * 
 *    Revision 1.2 2006/03/09 18:24:05 willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 * 
 *    Revision 1.1 2005/12/20 00:03:27 willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 * 
 *    Revision 1.1 2005/12/12 15:46:55 willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 * 
 **********************************************************************/
