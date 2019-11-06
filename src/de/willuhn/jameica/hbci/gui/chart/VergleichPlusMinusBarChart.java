/**********************************************************************
 *
 * Copyright (c) 2019 Tobias Amon
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.IGrid;
import org.swtchart.ILegend;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesLabel;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.ext.InteractiveChart;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;

/**
 * Chart zum Anzeigen der Differenz aus Einnahmen und Ausgaben.
 */
public class VergleichPlusMinusBarChart extends AbstractChart
{
  private Composite comp = null;
  private List<EinnahmeAusgabeZeitraum> data = null;

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#redraw()
   */
  @Override
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
    getChart().setOrientation(SWT.HORIZONTAL);

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
      title.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
      title.setFont(Font.BOLD.getSWTFont());
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Legende
    {
      ILegend legend = getChart().getLegend();
      legend.setFont(Font.SMALL.getSWTFont());
      legend.setVisible(true);
      legend.setPosition(SWT.RIGHT);
      legend.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Layout der Achsen
    Color gray = getColor(new RGB(234,234,234));
    
    // X-Achse
    {
      IAxis axis = getChart().getAxisSet().getXAxis(0);
      axis.getTitle().setFont(Font.SMALL.getSWTFont());
      axis.getTitle().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // wenn wir den auch ausblenden, geht die initiale Skalierung kaputt. Scheint ein Bug zu sein

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);

      IAxisTick tick = axis.getTick();
      tick.setFormat(HBCI.DATEFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
      
      axis.setCategorySeries(this.getCategoryNames());
      axis.enableCategory(true);
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
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    {
      IBarSeries barSeries = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR,"plus");
      barSeries.setYSeries(getPlusMinusSeries(true));
      barSeries.setDescription(i18n.tr("Plus"));
      barSeries.setBarColor(Settings.getBuchungHabenForeground());
      ISeriesLabel label = barSeries.getLabel();
      label.setFont(Font.SMALL.getSWTFont());
      label.setFormat(HBCI.DECIMALFORMAT.toPattern());
      label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      label.setVisible(true);
    }
    
    {
      IBarSeries barSeries = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR,"minus");
      barSeries.setYSeries(getPlusMinusSeries(false));
      barSeries.setDescription(i18n.tr("Minus"));
      barSeries.setBarColor(Settings.getBuchungSollForeground());
      ISeriesLabel label = barSeries.getLabel();
      label.setFont(Font.SMALL.getSWTFont());
      label.setFormat(HBCI.DECIMALFORMAT.toPattern());
      label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      label.setVisible(true);
    }

    getChart().getAxisSet().adjustRange();
    this.comp.layout(true);
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
   * Setter für die anzuzeigenden Daten
   * @param items Liste mit den Daten.
   * @throws RemoteException 
   */
  public void setData(List<EinnahmeAusgabeZeitraum> items) throws RemoteException
  {
    this.data = items;
  }

  /**
   * Liefert die Namen der Kategorien.
   * @return die Namen der Kategorien.
   */
  private String[] getCategoryNames()
  {
    if (data == null)
      return new String[] {i18n.tr("Keine Daten")};
    
    final List<String> result = new ArrayList<String>();
    for (EinnahmeAusgabeZeitraum n : this.data)
    {
      result.add(this.getLabel(n));
    }
    return result.toArray(new String[result.size()]);
  }
  
  /**
   * Liefert das Label fuer das Element.
   * @param node das Element.
   * @return das Label.
   */
  private String getLabel(EinnahmeAusgabeZeitraum node)
  {
    if (node instanceof EinnahmeAusgabe)
      return node.getText(); // Gesamtzeitraum
    
    final Calendar cal = Calendar.getInstance();
    cal.setTime(node.getStartdatum());
    final int sm = cal.get(Calendar.MONTH);
    final int sy = cal.get(Calendar.YEAR);
    
    cal.setTime(node.getEnddatum());
    final int em = cal.get(Calendar.MONTH);
    final int ey = cal.get(Calendar.YEAR);

    // Gruppiert nach Monat
    if (sm == em && sy == ey)
      return String.format("%02d",sm+1) + "/" + sy; // Monat beginnt im Calendar bei 0
    
    // Gruppiert nach Jahr
    if (sy == ey)
      return Integer.toString(sy);

    // Gruppierung unbekannt
    return node.getText();
  }
  
  /**
   * Liefert die Datenreihe mit der Differenz aus Einnahmen und Ausgaben.
   * @return Datenreihe mit der Differenz aus Einnahmen und Ausgaben.
   * @throws RemoteException
   */
  private double[] getPlusMinusSeries(boolean plus) throws RemoteException
  {
    if (data == null)
      return new double[] {0.0};
    
    double[] serie = new double[this.data.size()];
    for (int i = 0; i < this.data.size(); i++)
    {
      EinnahmeAusgabeZeitraum e = this.data.get(i);
      //TODO Bug hier und in VergleichBarChart
      //Wenn genau ein Konto ausgewählt ist und eine Gruppierung nach Jahr oder Monat aktiv ist
      //werden keine Daten angezeigt
      //Ursache ist, dass dann im Baum zwar eine Unterteilung erfolgt, aber keine Summenzeile existiert
      //dadurch ist e.getEinnahme/getAusgabe immer 0
      double diff=e.getEinnahmen()-e.getAusgaben();
      serie[i] = 0.0;
      if(plus&&diff>0 ||!plus &&diff<0){
        serie[i] = diff;
      }
    }
    return serie;
  }

}
