/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypChart.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/04/03 21:39:07 $
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
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.chart.ChartData;
import de.willuhn.jameica.hbci.gui.chart.ChartDataUmsatzTyp;
import de.willuhn.jameica.hbci.gui.chart.PieChart;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
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
  private int start   = HBCIProperties.UMSATZ_DEFAULT_DAYS;

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
      final LabelGroup group = new LabelGroup(parent,i18n.tr("Umsatz-Verteilung"),true);
      final ChartData eData = new ChartDataUmsatzTyp(true,start);
      final ChartData aData = new ChartDataUmsatzTyp(false,start);
      
      final PieChart einnahmen = new PieChart();
      einnahmen.setTitle(i18n.tr("Einnahmen ({0} Tage)",""+start));
      einnahmen.addData(eData);

      final PieChart ausgaben = new PieChart();
      ausgaben.setTitle(i18n.tr("Ausgaben ({0} Tage)",""+start));
      ausgaben.addData(aData);

      final UmsatzDaysInput i = new UmsatzDaysInput();
      i.addListener(new Listener()
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

            myEData = new ChartDataUmsatzTyp(true,newStart);
            myAData = new ChartDataUmsatzTyp(false,newStart);
            if (newStart < 0)
            {
              einnahmen.setTitle(i18n.tr("Einnahmen (alle Umsätze)"));
              ausgaben.setTitle(i18n.tr("Ausgaben (alle Umsätze)"));
            }
            else
            {
              einnahmen.setTitle(i18n.tr("Einnahmen ({0} Tage)",""+newStart));
              ausgaben.setTitle(i18n.tr("Ausgaben ({0} Tage)",""+newStart));
            }
            einnahmen.addData(myEData);
            ausgaben.addData(myAData);
            einnahmen.redraw();
            ausgaben.redraw();
          }
          catch (Throwable t)
          {
            Logger.error("unable to redraw chart",t);
            GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Aktualisieren des Saldo-Verlaufs"));
          }
        }
      });


      group.addLabelPair(i18n.tr("Anzahl der anzuzeigenden Tage"),i);
//      Label l = GUI.getStyleFactory().createLabel(parent,SWT.NONE);
//      l.setBackground(Color.BACKGROUND.getSWTColor());
//      l.setText(i18n.tr("Anzahl der anzuzeigenden Tage"));
//      i.paint(parent);
      
      Composite comp = new Composite(group.getComposite(),SWT.NONE);
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.horizontalSpan = 2;
      comp.setLayoutData(gridData);
      comp.setBackground(Color.BACKGROUND.getSWTColor());

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
 * Revision 1.1  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 *********************************************************************/