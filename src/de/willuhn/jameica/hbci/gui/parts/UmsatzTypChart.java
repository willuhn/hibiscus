/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypChart.java,v $
 * $Revision: 1.12 $
 * $Date: 2011/08/29 08:04:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.BarChart;
import de.willuhn.jameica.hbci.gui.chart.ChartData;
import de.willuhn.jameica.hbci.gui.chart.ChartDataUmsatzTyp;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Komponente, die die Umsatzverteilung grafisch anzeigt.
 * @author willuhn
 */
public class UmsatzTypChart implements Part
{
  
  private I18N i18n   = null;
  private int start   = UmsatzDaysInput.getDefaultDays();

  /**
   * ct.
   */
  public UmsatzTypChart()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      // TODO: Hier werden die benutzerdefinierten Farben von Kategorien noch nicht genutzt
      final Container group = new SimpleContainer(parent,true);
      final ChartData eData = new ChartDataUmsatzTyp(UmsatzTyp.TYP_EINNAHME,start);
      final ChartData aData = new ChartDataUmsatzTyp(UmsatzTyp.TYP_AUSGABE,start);
      
      final BarChart einnahmen = new BarChart();
      final BarChart ausgaben  = new BarChart();
      
      if (start < 0)
      {
        einnahmen.setTitle(i18n.tr("Einnahmen (alle Umsätze)"));
        ausgaben.setTitle(i18n.tr("Ausgaben (alle Umsätze)"));
      }
      else
      {
        einnahmen.setTitle(i18n.tr("Einnahmen ({0} Tage)",""+start));
        ausgaben.setTitle(i18n.tr("Ausgaben ({0} Tage)",""+start));
      }
      einnahmen.addData(eData);
      ausgaben.addData(aData);

      final UmsatzDaysInput i = new UmsatzDaysInput();
      i.addListener(new DelayedListener(300, new Listener()
      {
        private ChartData myEData = null;
        private ChartData myAData = null;
        public void handleEvent(Event event)
        {
          try
          {
            int newStart = ((Integer)i.getValue()).intValue();
            if (newStart == start)
              return;

            start = newStart;
            
            if (myEData != null) einnahmen.removeData(myEData);
            else                 einnahmen.removeData(eData);
            if (myAData != null) ausgaben.removeData(myAData);
            else                 ausgaben.removeData(aData);

            myEData = new ChartDataUmsatzTyp(UmsatzTyp.TYP_EINNAHME,newStart);
            myAData = new ChartDataUmsatzTyp(UmsatzTyp.TYP_AUSGABE,newStart);
            if (newStart < 0)
            {
              einnahmen.setTitle(i18n.tr("Einnahmen (alle Umsätze)"));
              ausgaben.setTitle(i18n.tr("Ausgaben (alle Umsätze)"));
            }
            else
            {
              einnahmen.setTitle(i18n.tr("Einnahmen ({0} Tage)",Integer.toString(newStart)));
              ausgaben.setTitle(i18n.tr("Ausgaben ({0} Tage)",Integer.toString(newStart)));
            }
            einnahmen.addData(myEData);
            ausgaben.addData(myAData);
            einnahmen.redraw();
            ausgaben.redraw();
          }
          catch (Throwable t)
          {
            Logger.error("unable to redraw chart",t);
            GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Aktualisieren der Umsatzverteilung"));
          }
        }
      }));


      group.addInput(i);
      
      final Composite comp = new Composite(group.getComposite(),SWT.NONE);
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.horizontalSpan = 2;
      comp.setLayoutData(gridData);

      GridLayout layout = new GridLayout(2,true);
      layout.horizontalSpacing = 0;
      layout.verticalSpacing = 0;
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      comp.setLayout(layout);
      
      einnahmen.paint(comp);
      ausgaben.paint(comp);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to paint chart",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Anzeigen der Umsatzverteilung"));
    }
  }
}


/*********************************************************************
 * $Log: UmsatzTypChart.java,v $
 * Revision 1.12  2011/08/29 08:04:58  willuhn
 * @B Dispose-Check fehlte
 * @N Umsatzverteilung verzoegert neu berechnen - der Schieberegler hakelt sonst
 *
 * Revision 1.11  2011-05-03 10:13:15  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.10  2010-11-24 16:27:18  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.9  2010-08-12 15:32:02  willuhn
 * @R Rahmen entfernt - spart Platz
 *
 * Revision 1.8  2010-08-11 16:06:05  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 * Revision 1.7  2009/08/27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich  eine Trendkurve an
 *
 * Revision 1.6  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.5  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.4  2007/01/16 12:35:43  willuhn
 * @B "-1 Tage"
 *
 * Revision 1.3  2006/10/31 23:04:48  willuhn
 * @B Wurde mit der falschen Anzahl Default-Tage initialisiert
 *
 * Revision 1.2  2006/08/05 22:00:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 *********************************************************************/