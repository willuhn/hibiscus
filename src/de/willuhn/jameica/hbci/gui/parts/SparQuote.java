/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.action.SparQuoteExport;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
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
  private final static Settings settings = new Settings(SparQuote.class);

  private static DateFormat DATEFORMAT = new SimpleDateFormat("MM.yyyy");

  private TablePart table              = null;
  private LineChart chart              = null;
  private KontoInput kontoauswahl      = null;
  private SpinnerInput tagAuswahl      = null;
  private SpinnerInput monatAuswahl    = null;
  private Input from                   = null;
  private Input to                     = null;
  private RangeInput range             = null;

  private List<UmsatzEntry> data       = new ArrayList<UmsatzEntry>();
  private List<UmsatzEntry> trend      = new ArrayList<UmsatzEntry>();

  private Listener listener            = null; // BUGZILLA 575

  /**
   * ct.
   */
  public SparQuote()
  {
    this.listener = new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          if (event != null && (event.type==SWT.FocusIn || event.type==SWT.FocusOut))
            return;
          
          if (load())
          {
            redraw();
            if (chart != null)
              chart.redraw();
          }
        }
        catch (Exception e)
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
   * Liefert die Konto-Auwahl.
   * @return die Konto-Auswahl.
   * @throws RemoteException
   */
  private SelectInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoauswahl != null)
      return this.kontoauswahl;

    this.kontoauswahl = new KontoInput(null,KontoFilter.ALL);
    this.kontoauswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoauswahl.setSupportGroups(true);
    this.kontoauswahl.setComment(null);
    this.kontoauswahl.setRememberSelection("auswertungen.spartquote");
    this.kontoauswahl.addListener(this.listener);
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
    this.tagAuswahl = new SpinnerInput(1,31,1);
    this.tagAuswahl.setComment(i18n.tr(". Tag des Monats"));
    this.tagAuswahl.setName(i18n.tr("Stichtag"));
    this.tagAuswahl.setValue(settings.getInt("stichtag",1));
    this.tagAuswahl.addListener(new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        settings.setAttribute("stichtag",(Integer) tagAuswahl.getValue());
      }
    });
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
    this.monatAuswahl = new SpinnerInput(1,12,1);
    this.monatAuswahl.setComment(i18n.tr("Anzahl der Monate pro Periode"));
    this.monatAuswahl.setName(i18n.tr("Monate"));
    this.monatAuswahl.setValue(settings.getInt("monate",1));
    this.monatAuswahl.addListener(new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        settings.setAttribute("monate",(Integer) monatAuswahl.getValue());
      }
    });
    this.monatAuswahl.addListener(new DelayedListener(500,this.listener));
    return this.monatAuswahl;
  }
  
  /**
   * Liefert das Eingabe-Datum fuer das Start-Datum.
   * @return Eingabe-Feld.
   */
  private synchronized Input getFrom()
  {
    if (this.from != null)
      return this.from;
    
    this.from = new DateFromInput(null,"auswertungen.spartquote.filter.from");
    this.from.setName(i18n.tr("Von"));
    this.from.setComment(null);
    this.from.addListener(this.listener);
    return this.from;
  }
  
  /**
   * Liefert das Eingabe-Datum fuer das End-Datum.
   * @return Eingabe-Feld.
   */
  public synchronized Input getTo()
  {
    if (this.to != null)
      return this.to;

    this.to = new DateToInput(null,"auswertungen.spartquote.filter.to");
    this.to.setName(i18n.tr("bis"));
    this.to.setComment(null);
    this.to.addListener(this.listener);
    return this.to;
  }
  
  /**
   * Liefert eine Auswahl mit Zeit-Presets.
   * @return eine Auswahl mit Zeit-Presets.
   */
  public RangeInput getRange()
  {
    if (this.range != null)
      return this.range;
    
    // Wir wollen hier nur die Zeitraume haben, die mindestens 2 Monate umfassen
    List<Range> ranges = new ArrayList<Range>();
    for (Range r:Range.getActiveRanges(Range.CATEGORY_AUSWERTUNG))
    {
      // Zeitraeume ohne Startdatum sind immer lang genug
      if (r.getStart() == null)
      {
        ranges.add(r);
      }
      else
      {
        // Wir ueberschlagen das nur grob
        long diff = r.getEnd().getTime() - r.getStart().getTime();
        if (diff > (2 * 30 * 24 * 60 * 60 * 1000L))
          ranges.add(r);
      }
    }
    
    this.range = new RangeInput(ranges,this.getFrom(),this.getTo(),"auswertungen.spartquote.filter.range");
    this.range.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (range.getValue() != null && range.hasChanged())
          listener.handleEvent(event);
      }
    });
    
    return this.range;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    load();

    {
      final TabFolder folder = new TabFolder(parent, SWT.NONE);
      folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

      ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);
      
      {
        Container left = new SimpleContainer(cols.getComposite());
        left.addInput(this.getKontoAuswahl());
        left.addInput(getTagAuswahl());
        left.addInput(getMonatAuswahl());
      }
      
      {
        Container right = new SimpleContainer(cols.getComposite());
        right.addInput(this.getRange());
        MultiInput range = new MultiInput(this.getFrom(),this.getTo());
        right.addInput(range);
      }
    }

    ButtonArea topButtons = new ButtonArea();
    topButtons.addButton(i18n.tr("Exportieren..."),new SparQuoteExport()
    {
      /**
       * @see de.willuhn.jameica.hbci.gui.action.SparQuoteExport#handleAction(java.lang.Object)
       */
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          super.handleAction(table.getItems()); // Wir exportieren pauschal alles
        }
        catch (RemoteException re)
        {
          Logger.error("unable to export data",re);
          throw new ApplicationException(i18n.tr("Export fehlgeschlagen: {0}",re.getMessage()));
        }
      }
    },null,false,"document-save.png");
    topButtons.addButton(i18n.tr("Aktualisieren"), new Action() {

      public void handleAction(Object context) throws ApplicationException
      {
        listener.handleEvent(null);
      }
    },null,true,"view-refresh.png");
    topButtons.paint(parent);

    // Wir initialisieren die Tabelle erstmal ohne Werte.
    this.table = new TablePart(data,null);
    this.table.addColumn(i18n.tr("Monat"), "start",new DateFormatter(DATEFORMAT));
    this.table.addColumn(i18n.tr("Einnahmen"), "einnahmen", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.table.addColumn(i18n.tr("Ausgaben"),  "ausgaben", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.table.addColumn(i18n.tr("Sparquote"), "sparquote", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.table.setRememberOrder(true);
    this.table.setRememberColWidths(true);
    
    final boolean bold = de.willuhn.jameica.hbci.Settings.getBoldValues();
    this.table.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        UmsatzEntry ue = (UmsatzEntry) item.getData();
        item.setForeground(ColorUtil.getForeground(ue.einnahmen - ue.ausgaben));
        
        if (bold)
          item.setFont(3,Font.BOLD.getSWTFont());
      }
    });

    TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    try
    {
      TabGroup tab2 = new TabGroup(folder,i18n.tr("Grafische Auswertung"),false,1);

      this.chart = new LineChart();
      this.chart.addData(new ChartDataSparQuote());
      this.chart.addData(new ChartDataTrend());
      this.chart.setTitle(i18n.tr("Sparquote im zeitlichen Verlauf"));
      this.chart.paint(tab2.getComposite());
    }
    catch (Exception e)
    {
      Logger.error("unable to create chart",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen des Diagramms"),StatusBarMessage.TYPE_ERROR));
    }

    TabGroup tab = new TabGroup(folder,i18n.tr("Tabellarische Auswertung"));
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

    List<UmsatzEntry> tableData = new ArrayList(this.data);
    if (tableData.size() > 1)
    {
      //Dummy-Eintrag für Tabellenansicht ignorieren
      tableData.remove(tableData.size() - 1);
    }
    for (UmsatzEntry e : tableData)
    {
      this.table.addItem(e);
    }
  }

  /**
   * Laedt die Sparquoten des Kontos
   * @return true, wenn das Chart neu gezeichnet werden kann.
   * @throws RemoteException
   */
  private boolean load() throws RemoteException
  {
    this.data.clear();

    final Date start   = (Date) this.getFrom().getValue();
    final Date end     = (Date) this.getTo().getValue();
    final int monate   = ((Integer) this.getMonatAuswahl().getValue()).intValue();
    final int stichtag = ((Integer) this.getTagAuswahl().getValue()).intValue();
    
    ////////////////////////////////////////////////////////////////////////////
    // Wir iterieren erstmal ueber den Zeitraum und erzeugen die passenden Time-Boxen
    Date from = start != null ? start : UmsatzUtil.getOldest(getKontoAuswahl().getValue());
    from          = DateUtil.startOfDay(from != null ? from : new Date());
    final Date to = DateUtil.endOfDay(end != null ? end : new Date());
    
    for (int i=0;i<1000;++i) // Wir machen keine Endlosschleife sondern hoeren bei maximal 1000 auf
    {
      // Wenn das Start-Datum in der Zukunft liegt, koennen wir aufhoeren
      if (from.after(to))
        break;

      UmsatzEntry e = new UmsatzEntry();
      e.start = from;
      e.text = DATEFORMAT.format(e.start);
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);

      // Anzahl der Monate fuer das Ende der Periode
      cal.add(Calendar.MONTH,monate);

      // Der Tag, ab dem die naechste Periode beginnt
      cal.set(Calendar.DATE, Math.min(stichtag, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));

      // Merken wir uns fuer den naechsten Durchlauf
      from = DateUtil.startOfDay(cal.getTime());
      
      // Das Ende des Vortages ist das Ende der aktuellen Periode
      cal.add(Calendar.DATE,-1);
      e.end = DateUtil.endOfDay(cal.getTime());

      // Zur Liste hinzufuegen
      this.data.add(e);
    }
    //Dummy-Eintrag für das nächste Intervall, da das letzte reguläre sonst
    //aufgrund der Normalisierung auf den ersten Tag des Intervalls
    //ggf. gar nicht angezeigt wird
    if (!this.data.isEmpty())
    {
      UmsatzEntry lastEntry = new UmsatzEntry();
      long time = this.data.get(this.data.size() - 1).getEnd().getTime();
      lastEntry.start = new Date(time + 1);
      lastEntry.end = lastEntry.start;
      this.data.add(lastEntry);
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    if (this.data.size() < 2)
    {
      //TODO diese Validierung hat auch angeschlagen, wenn man "letztes Jahr" und 12 Monate pro Periode gewählt hat
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie einen Zeitraum, der mindestens 2 Monate umfasst"),StatusBarMessage.TYPE_INFO));
      return false;
    }
    ////////////////////////////////////////////////////////////////////////////
    // Iterieren ueber die Umsaetze und einsortieren in die passende Timebox

    DBIterator umsaetze = UmsatzUtil.getUmsaetze();
    Object o = getKontoAuswahl().getValue();
    if (o != null && (o instanceof Konto))
      umsaetze.addFilter("konto_id = " + ((Konto) o).getID());
    else if (o != null && (o instanceof String))
      umsaetze.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) o);

    if (start != null)
      umsaetze.addFilter("datum >= ?", new java.sql.Date(start.getTime()));

    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      Date date = u.getDatum();
      if (date == null)
      {
        Logger.debug("no date found for umsatz, skipping record");
        continue;
      } else if (end != null && date.after(end))
      {
        //keine Umsätze nach dem gwünschten Zeitraum verarbeiten
        break;
      }
      
      UmsatzEntry e = this.getEntry(date);
      if (e == null)
      {
        Logger.debug("no matching entry found for umsatz, skipping record");
        continue;
      }
      
      double betrag = u.getBetrag();
      if (betrag > 0)
        e.einnahmen += betrag;
      else
        e.ausgaben -= betrag;
      //
      ////////////////////////////////////////////////////////////////////////////
    }
    
    // Trend ermitteln
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Glättungsverfahren
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Trend_und_Saisonkomponente

    this.trend.clear();
    for (int i=0;i<this.data.size();++i)
      this.trend.add(getDurchschnitt(this.data,i));
    
    return true;
  }

  /**
   * Liefert den passenden Zeitraum fuer das Datum.
   * @param date das Datum.
   * @return der passende Zeitraum oder NULL, wenn keiner existiert.
   */
  private UmsatzEntry getEntry(Date date)
  {
    if (date == null)
      return null;
    
    for (UmsatzEntry e:this.data)
    {
      if (!e.start.after(date) && !e.end.before(date))
        return e;
    }
    return null;
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
    UmsatzEntry ue = new UmsatzEntry();
    int found = 0;
    for (int i=-4;i<=4;++i)
    {
      try
      {
        UmsatzEntry current = (UmsatzEntry)list.get(pos + i);
        found++;
        ue.ausgaben  += current.ausgaben;
        ue.einnahmen += current.einnahmen;
        if (i == 0) // Als Monat verwenden wir genau den aus der Mitte
        {
          ue.start = current.start;
          ue.end = current.end;
          ue.text = current.text;
        }
      }
      catch (IndexOutOfBoundsException e)
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
    private Date start       = null;
    private Date end         = null;
    private String text      = null;

    /**
     * Liefert die Einnahmen.
     * @return die Einnahmen.
     */
    public double getEinnahmen()
    {
      return this.einnahmen;
    }
    
    /**
     * Liefert das Start-Datum.
     * @return das Start-Datum.
     */
    public Date getStart()
    {
      return start;
    }
    
    /**
     * Liefert das End-Datum.
     * @return das End-Datum.
     */
    public Date getEnd()
    {
      return end;
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
     * Liefert den Text.
     * Nicht entfernen! Wird fuer den Text im Velocity-Export gebraucht.
     * @return der Text.
     */
    public String getText()
    {
      return this.text;
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
    public List getData() throws RemoteException
    {
      return data;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
      Object o = getKontoAuswahl().getValue();
      if (o != null && (o instanceof String))
        return (String) o;
      else if (o != null && (o instanceof Konto))
        return ((Konto) o).getBezeichnung();
      return i18n.tr("Alle Konten");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
     */
    public String getDataAttribute() throws RemoteException
    {
      return "sparquote";
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
     */
    public String getLabelAttribute() throws RemoteException
    {
      return "start";
    }
    
    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getLineWidth()
     */
    @Override
    public int getLineWidth() throws RemoteException
    {
      return 1;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
     */
    public boolean getCurve()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
     */
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
    public List getData() throws RemoteException
    {
      return trend;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
      return i18n.tr("Trend");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
     */
    public boolean getCurve()
    {
      return true;
    }
  }
}
