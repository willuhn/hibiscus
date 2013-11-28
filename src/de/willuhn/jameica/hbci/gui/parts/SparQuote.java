/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SparQuote.java,v $
 * $Revision: 1.38 $
 * $Date: 2012/05/06 14:26:04 $
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Stellt die Einnahmen und Ausgaben eines Kontos gegenueber.
 */
public class SparQuote implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private static DateFormat DATEFORMAT = new SimpleDateFormat("MM.yyyy");

  private TablePart table              = null;
  private LineChart chart              = null;
  private KontoInput kontoauswahl      = null;
  private UmsatzDaysInput startAuswahl = null;
  private SpinnerInput tagAuswahl      = null;
  private SpinnerInput monatAuswahl    = null;

  private final List<UmsatzEntry> data       = new ArrayList<UmsatzEntry>();
  private final List<UmsatzEntry> trend      = new ArrayList<UmsatzEntry>();

  private Listener listener            = null; // BUGZILLA 575

  private Date start                   = null;
  private int stichtag                 = 1;
  private int monate                   = 1;

  /**
   * ct.
   */
  public SparQuote()
  {
    this.listener = new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          load();
          redraw();
          if (chart != null)
            chart.redraw();
        }
        catch (final Exception e)
        {
          // ignorieren wir. Durch den Delayed-Listener kann dieses
          // Event auch aufgerufen werden, wenn der Dialog schon verlassen wurde
          // Daher nur zu Debugging-Zwecken.
          Logger.write(Level.DEBUG,"unable to redraw data, it seems, the view was already closed",e);
        }
      }
    };
  }

  /**
   * Berechnet Start-Datum und Stichtag.
   * @throws RemoteException
   */
  private void calculateRange() throws RemoteException
  {
    // Stichtag
    Integer value = (Integer) getTagAuswahl().getValue();
    stichtag = value == null ? 1 : value.intValue();

    //Monate
    value = (Integer) getMonatAuswahl().getValue();
    monate = value == null ? 1 : value.intValue();

    // Anzahl der Tage
    final Integer days = (Integer) getStartAuswahl().getValue();
    if (days == null || days == -1)
    {
      start = null;
    }
    else
    {
      final long d = days * 24l * 60l * 60l * 1000l;
      start = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
    }
  }

  /**
   * Liefert die Konto-Auwahl.
   * @return die Konto-Auswahl.
   * @throws RemoteException
   */
  private SelectInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoauswahl != null)
      return this.kontoauswahl;

    this.kontoauswahl = new KontoInput(null,KontoFilter.ALL);
    if ( this.kontoauswahl.getList().size() != 1 ) {
      this.kontoauswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    }
    this.kontoauswahl.setSupportGroups(true);
    this.kontoauswahl.setRememberSelection("auswertungen.spartquote");
    this.kontoauswahl.addListener(new DelayedListener(500,this.listener));
    return this.kontoauswahl;
  }

  /**
   * Liefert ein Eingabefeld fuer den Stichtag.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  private SpinnerInput getTagAuswahl() throws RemoteException
  {
    if (this.tagAuswahl != null)
      return this.tagAuswahl;

    // BUGZILLA 337
    this.tagAuswahl = new SpinnerInput(1,31,this.stichtag);
    this.tagAuswahl.setComment(i18n.tr(". Tag des Monats"));
    this.tagAuswahl.setName(i18n.tr("Stichtag"));
    this.tagAuswahl.addListener(new DelayedListener(500,this.listener));
    return this.tagAuswahl;
  }

  /**
   * Liefert ein Eingabefeld fuer die Anzahl der Monate pro Periode.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  private SpinnerInput getMonatAuswahl() throws RemoteException
  {
    if (this.monatAuswahl != null)
      return this.monatAuswahl;

    // BUGZILLA 1233
    this.monatAuswahl = new SpinnerInput(1,12,this.monate);
    this.monatAuswahl.setComment(i18n.tr("Anzahl der Monate pro Periode"));
    this.monatAuswahl.setName(i18n.tr("Monate"));
    this.monatAuswahl.addListener(new DelayedListener(500,this.listener));
    return this.monatAuswahl;
  }

  /**
   * Liefert ein Eingabefeld fuer das Start-Datum.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  private UmsatzDaysInput getStartAuswahl() throws RemoteException
  {
    if (this.startAuswahl != null)
      return this.startAuswahl;
    this.startAuswahl = new UmsatzDaysInput();
    this.startAuswahl.addListener(new DelayedListener(500,this.listener));
    return this.startAuswahl;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    load();

    {
      final TabFolder folder = new TabFolder(parent, SWT.NONE);
      folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      final TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschr�nken"));

      tab.addInput(getKontoAuswahl());
      tab.addInput(getStartAuswahl());
      tab.addInput(getTagAuswahl());
      tab.addInput(getMonatAuswahl());
    }

    final ButtonArea topButtons = new ButtonArea();
    topButtons.addButton(i18n.tr("Aktualisieren"), new Action() {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        listener.handleEvent(null);
      }
    },null,true,"view-refresh.png");
    topButtons.paint(parent);

    // Wir initialisieren die Tabelle erstmal ohne Werte.
    this.table = new TablePart(data,null);
    this.table.addColumn(i18n.tr("Monat"), "monat", new Formatter() {
      @Override
      public String format(Object o)
      {
        if (o == null)
          return "";
        return DATEFORMAT.format((Date)o);
      }
    });
    this.table.addColumn(i18n.tr("Einnahmen"), "einnahmen", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.table.addColumn(i18n.tr("Ausgaben"),  "ausgaben", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.table.addColumn(i18n.tr("Sparquote"), "sparquote", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.table.setRememberOrder(true);
    this.table.setRememberColWidths(true);
    this.table.setFormatter(new TableFormatter() {
      @Override
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        final UmsatzEntry ue = (UmsatzEntry) item.getData();
        item.setForeground(ColorUtil.getForeground(ue.einnahmen - ue.ausgaben));
      }
    });

    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    try
    {
      final TabGroup tab2 = new TabGroup(folder,i18n.tr("Grafische Auswertung"),false,1);

      this.chart = new LineChart();
      this.chart.addData(new ChartDataSparQuote());
      this.chart.addData(new ChartDataTrend());
      this.chart.setTitle(i18n.tr("Sparquote im zeitlichen Verlauf"));
      this.chart.paint(tab2.getComposite());
    }
    catch (final Exception e)
    {
      Logger.error("unable to create chart",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen des Diagramms"),StatusBarMessage.TYPE_ERROR));
    }

    final TabGroup tab = new TabGroup(folder,i18n.tr("Tabellarische Auswertung"));
    this.table.paint(tab.getComposite());
  }

  /**
   * Aktualisiert die Anzeige fuer das Konto.
   * @throws RemoteException
   */
  private void redraw() throws RemoteException
  {
    if (this.data == null)
      return;

    this.table.removeAll();

    for (final UmsatzEntry e:this.data)
    {
      this.table.addItem(e);
    }
  }

  /**
   * Laedt die Sparquoten des Kontos
   * @throws RemoteException
   */
  private void load() throws RemoteException
  {
    this.data.clear();
    calculateRange();

    final DBIterator umsaetze = UmsatzUtil.getUmsaetze();
    final Object o = getKontoAuswahl().getValue();
    if (o != null && (o instanceof Konto))
      umsaetze.addFilter("konto_id = " + ((Konto) o).getID());
    else if (o != null && (o instanceof String))
      umsaetze.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) o);

    if (start != null)
      umsaetze.addFilter("datum >= ?", new Object[] {new java.sql.Date(start.getTime())});

    UmsatzEntry currentEntry = null;
    final Calendar cal             = Calendar.getInstance();
    Date currentLimit        = null;

    while (umsaetze.hasNext())
    {
      final Umsatz u = (Umsatz) umsaetze.next();
      final Date date = u.getDatum();
      if (date == null)
      {
        Logger.warn("no date found for umsatz, skipping record");
        continue;
      }

      if (currentLimit == null || date.after(currentLimit) || date.equals(currentLimit))
      {
        // Wir haben das Limit erreicht. Also beginnen wir einen neuen Block
        currentEntry = new UmsatzEntry();
        currentEntry.monat = date;
        this.data.add(currentEntry);

        // BUGZILLA 337
        // Neues Limit definieren
        cal.setTime(date);
        cal.add(Calendar.MONTH,this.monate);

        // BUGZILLA 691
        if (stichtag > cal.getActualMaximum(Calendar.DAY_OF_MONTH))
          cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        else
          cal.set(Calendar.DAY_OF_MONTH, stichtag);

        currentLimit = DateUtil.startOfDay(cal.getTime());
      }

      final double betrag = u.getBetrag();
      if (betrag > 0)
        currentEntry.einnahmen += betrag;
      else
        currentEntry.ausgaben -= betrag;
    }

    // Trend ermitteln
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Gl�ttungsverfahren
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Trend_und_Saisonkomponente

    this.trend.clear();
    for (int i=0;i<this.data.size();++i)
      this.trend.add(getDurchschnitt(this.data,i));
  }

  /**
   * Liefert einen synthetischen Umsatz-Entry basierend auf den
   * Daten der 4 links und rechts daneben liegenden Monaten als Durchschnitt.
   * @param list Liste der Umsaetze.
   * @param pos Position.
   * @return der Durchschnitt.
   */
  private UmsatzEntry getDurchschnitt(List<UmsatzEntry> list, int pos)
  {
    final UmsatzEntry ue = new UmsatzEntry();
    int found = 0;
    for (int i=-4;i<=4;++i)
    {
      try
      {
        final UmsatzEntry current = (UmsatzEntry)list.get(pos + i);
        found++;
        ue.ausgaben  += current.ausgaben;
        ue.einnahmen += current.einnahmen;
        if (i == 0) // Als Monat verwenden wir genau den aus der Mitte
          ue.monat = current.monat;
      }
      catch (final IndexOutOfBoundsException e)
      {
        // Ignore
      }
    }
    ue.einnahmen /= found;
    ue.ausgaben /= found;

    return ue;
  }

  /**
   * Hilfsobjekt fuer die einzelnen Monate.
   */
  public static class UmsatzEntry
  {
    private double einnahmen = 0d;
    private double ausgaben  = 0d;
    private Date monat       = null;

    /**
     * Liefert die Einnahmen.
     * @return die Einnahmen.
     */
    public double getEinnahmen()
    {
      return this.einnahmen;
    }

    /**
     * Liefert die Ausgaben.
     * @return die Ausgaben.
     */
    public double getAusgaben()
    {
      return this.ausgaben;
    }

    /**
     * Liefert den Monat.
     * @return der Monat.
     */
    public Date getMonat()
    {
      return this.monat;
    }

    /**
     * Liefert die Sparquote.
     * @return die Sparquote.
     */
    public double getSparquote()
    {
      return einnahmen - ausgaben;
    }
  }


  /**
   * Implementierung eines Datensatzes fuer die Darstellung der Sparquote.
   */
  private class ChartDataSparQuote implements LineChartData
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
     */
    @Override
    public List getData() throws RemoteException
    {
      return data;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    @Override
    public String getLabel() throws RemoteException
    {
      final Object o = getKontoAuswahl().getValue();
      if (o != null && (o instanceof String))
        return (String) o;
      else if (o != null && (o instanceof Konto))
        return ((Konto) o).getBezeichnung();
      return i18n.tr("Alle Konten");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
     */
    @Override
    public String getDataAttribute() throws RemoteException
    {
      return "sparquote";
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
     */
    @Override
    public String getLabelAttribute() throws RemoteException
    {
      return "monat";
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
     */
    @Override
    public boolean getCurve()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
     */
    @Override
    public int[] getColor() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#isFilled()
     */
    @Override
    public boolean isFilled() throws RemoteException
    {
      return true;
    }
  }

  /**
   * Implementiert die Datenspur fuer den Spar-Trend.
   */
  private class ChartDataTrend extends ChartDataSparQuote
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
     */
    @Override
    public List getData() throws RemoteException
    {
      return trend;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    @Override
    public String getLabel() throws RemoteException
    {
      return i18n.tr("Trend");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
     */
    @Override
    public boolean getCurve()
    {
      return true;
    }
  }
}
