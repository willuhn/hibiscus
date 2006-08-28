/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/UmsatzChart.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/08/28 22:03:26 $
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.ChartData;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Komponente, die den Umsatzverlauf eines Kontos grafisch anzeigt.
 * @author willuhn
 */
public class UmsatzChart implements Part
{
  
  private I18N i18n   = null;
  private Konto konto = null;
  
  private int start = UmsatzDaysInput.getDefaultDays();

  /**
   * ct.
   * @param konto das Konto.
   */
  public UmsatzChart(Konto konto)
  {
    this.konto = konto;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      final ChartData data = new ChartDataSaldoVerlauf(konto,start);
      final LineChart chart = new LineChart();
      chart.addData(data);
      chart.setTitle(i18n.tr("Saldo im Verlauf der letzten {0} Tage",""+start));
      
      final UmsatzDaysInput i = new UmsatzDaysInput();
      i.addListener(new Listener()
      {
        private ChartData myData = null;
        public void handleEvent(Event event)
        {
          try
          {
            int newStart = ((Integer)i.getValue()).intValue();
            if (newStart == start)
              return;

            start = newStart;
            
            if (myData != null)
              chart.removeData(myData);
            else
              chart.removeData(data);

            myData = new ChartDataSaldoVerlauf(konto,newStart);
            if (newStart < 0)
              chart.setTitle(i18n.tr("Saldo im Verlauf (alle Umsätze)"));
            else
              chart.setTitle(i18n.tr("Saldo im Verlauf der letzten {0} Tage",""+newStart));
            chart.addData(myData);
            chart.redraw();
          }
          catch (Throwable t)
          {
            Logger.error("unable to redraw chart",t);
            GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Aktualisieren des Saldo-Verlaufs"));
          }
        }
      });


      Label l = GUI.getStyleFactory().createLabel(parent,SWT.NONE);
      l.setBackground(Color.BACKGROUND.getSWTColor());
      l.setText(i18n.tr("Anzahl der anzuzeigenden Tage"));
      i.paint(parent);
      
      Composite comp = new Composite(parent,SWT.NONE);
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.horizontalSpan = 2;
      comp.setLayoutData(gridData);
      comp.setBackground(Color.BACKGROUND.getSWTColor());

      GridLayout layout = new GridLayout();
      layout.horizontalSpacing = 0;
      layout.verticalSpacing = 0;
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      comp.setLayout(layout);
      
      chart.paint(comp);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to paint chart",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Anzeigen des Saldo-Verlaufs"));
    }
  }

}


/*********************************************************************
 * $Log: UmsatzChart.java,v $
 * Revision 1.4  2006/08/28 22:03:26  willuhn
 * @B UmsatzChart - Anzahl der Default-Tage
 *
 * Revision 1.3  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.2  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.1  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 *********************************************************************/