package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesLabel;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.ext.InteractiveChart;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabeTreeNode;
import de.willuhn.util.ColorGenerator;

/**
 * Chart zum Anzeigen von 2 Serien: Einnahmen und Ausgaben.
 * 
 * @author Tobias Amon
 *
 */
public class VergleichBarChart extends AbstractChart
{
  private Composite comp = null;
  private List data = null;

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
      title.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      title.setFont(Font.BOLD.getSWTFont());
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Layout der Achsen
    Color gray = getColor(new RGB(230,230,230));
    
    // X-Achse
    {
      IAxis axis = getChart().getAxisSet().getXAxis(0);
      axis.getTitle().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // wenn wir den auch ausblenden, geht die initiale Skalierung kaputt. Scheint ein Bug zu sein

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);

      axis.getTick().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      axis.getTick().setFormat(HBCI.DATEFORMAT);
      
      String[] categoryNames = getCategoryNames();
      axis.setCategorySeries(categoryNames);
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
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    erstelleEinnahmenSerie();
    erstelleAusgabenSerie();
    
    getChart().getAxisSet().adjustRange();
    this.comp.layout(true);
  }

  private void erstelleEinnahmenSerie() throws RemoteException
  {
    IBarSeries barSeries = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR,"einnahmen");
    double[] einnahmenSerie = getIncomeSeries();
    barSeries.setYSeries(einnahmenSerie);
    barSeries.setDescription("Einnahmen");
    
    int[] cValues = ColorGenerator.create(ColorGenerator.PALETTE_OFFICE);
    barSeries.setBarColor(getColor(new RGB(cValues[0],cValues[1],cValues[2])));
    
    ISeriesLabel label = barSeries.getLabel();
    label.setFont(Font.SMALL.getSWTFont());
    label.setFormat(HBCI.DECIMALFORMAT.toPattern()); // BUGZILLA 1123
    label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    label.setVisible(true);
  }

  private void erstelleAusgabenSerie() throws RemoteException
  {
    ISeriesLabel label;
    IBarSeries barSeries2 = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR,"ausgaben");
    double[] ausgabenSerie = getExpensesSeries();
    barSeries2.setYSeries(ausgabenSerie);
    barSeries2.setDescription("Ausgaben");
    
    int[] cValues2 = ColorGenerator.create(ColorGenerator.PALETTE_RICH);
    barSeries2.setBarColor(getColor(new RGB(cValues2[0],cValues2[1],cValues2[2])));
    
    label = barSeries2.getLabel();
    label.setFont(Font.SMALL.getSWTFont());
    label.setFormat(HBCI.DECIMALFORMAT.toPattern()); // BUGZILLA 1123
    label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    label.setVisible(true);
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
   * 
   * @param items List von EinnahmeAusgabeTreeNode Elementen
   * @throws RemoteException 
   */
  public void setData(List items) throws RemoteException
  {
    this.data = items;
  }

  private String[] getCategoryNames()
  {
    if(data == null)
      return new String[] {"No Data"};
    
    List<EinnahmeAusgabeTreeNode> nodes = (List<EinnahmeAusgabeTreeNode>) this.data;
    List<String> categories = new ArrayList<>();
    for (EinnahmeAusgabeTreeNode einnahmeAusgabeTreeNode : nodes)
    {
      categories.add(getLabel(einnahmeAusgabeTreeNode));
    }
    return categories.toArray(new String[categories.size()]);
  }
  
  private String getLabel(EinnahmeAusgabeTreeNode node)
  {
    Calendar startCal = new GregorianCalendar();
    startCal.setTime(node.getStartdatum());
    int startMonth = startCal.get(Calendar.MONTH);
    int startYear = startCal.get(Calendar.YEAR);
    
    Calendar endCal = new GregorianCalendar();
    endCal.setTime(node.getEnddatum());
    int endMonth = endCal.get(Calendar.MONTH);
    int endYear = endCal.get(Calendar.YEAR);
    
    if(startMonth == endMonth) // Gleicher Monat, also Monatsweise Anzeige
      return getMonthForInt(startMonth);
    else if(startYear == endYear) // Anderer Monat, aber gleiches Jahr, also jahresweise Anzeige
      return String.valueOf(startYear);
    else
      return "-";
  }
  
  private String getMonthForInt(int num) 
  {
    String month = "-";
    DateFormatSymbols dfs = new DateFormatSymbols();
    String[] months = dfs.getMonths();
    if (num >= 0 && num <= 11 ) {
        month = months[num];
    }
    return month;
  }

  private double[] getIncomeSeries() throws RemoteException
  {
    if(data == null)
      return new double[] {0.0};
    
    List<EinnahmeAusgabeTreeNode> nodes = (List<EinnahmeAusgabeTreeNode>) this.data;
    double[] serie = new double[nodes.size()];
    for(int i = 0; i < nodes.size(); i++) {
      serie[i] = getEinnahmen(nodes.get(i).getChildren());
    }
    return serie;
  }

  private double getEinnahmen(GenericIterator children) throws RemoteException
  {
    if(!children.hasNext())
      return 0.0;
    
    if(children.size() > 1) // Summenzeile, wenn mehrere Konten geählt sind
    {
      while(children.hasNext())
      {
        EinnahmeAusgabe ea = (EinnahmeAusgabe) children.next();
        if(ea.isSumme())
        {
          return ea.getEinnahmen();
        }
      }
    }
    
    if(children.size() == 1) // Für einzelne Konten
    {
      EinnahmeAusgabe ea = (EinnahmeAusgabe) children.next();
      return ea.getEinnahmen();
    }
    return 0.0;
  }

  private double[] getExpensesSeries() throws RemoteException
  {
    if(data == null)
      return new double[] {0.0};
    
    List<EinnahmeAusgabeTreeNode> nodes = (List<EinnahmeAusgabeTreeNode>) this.data;
    double[] serie = new double[nodes.size()];
    for(int i = 0; i < nodes.size(); i++) {
      serie[i] = getExpenses(nodes.get(i).getChildren());
    }
    return serie;
  }

  private double getExpenses(GenericIterator children) throws RemoteException
  {
    if(!children.hasNext())
      return 0.0;
    
    if(children.size() > 1) // Summenzeile, wenn mehrere Konten geählt sind
    {
      while(children.hasNext())
      {
        EinnahmeAusgabe ea = (EinnahmeAusgabe) children.next();
        if(ea.isSumme())
        {
          return ea.getAusgaben();
        }
      }
    }
    
    if(children.size() == 1) // Für einzelne Konten
    {
      EinnahmeAusgabe ea = (EinnahmeAusgabe) children.next();
      return ea.getAusgaben();
    }
    return 0.0;
  }
  
}
