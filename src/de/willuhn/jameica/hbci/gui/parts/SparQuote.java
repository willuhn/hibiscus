/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SparQuote.java,v $
 * $Revision: 1.27 $
 * $Date: 2010/11/29 22:44:30 $
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
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
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
  
  private TablePart table          = null;
  private LineChart chart          = null;
  private SelectInput kontoauswahl = null;
  private DateInput startAuswahl   = null;
  private IntegerInput tagAuswahl  = null;
  
  private List<UmsatzEntry> data   = new ArrayList<UmsatzEntry>();
  private List<UmsatzEntry> trend  = new ArrayList<UmsatzEntry>();
  private Date start               = null;
  private int stichtag             = 1;
  
  private Listener listener        = null; // BUGZILLA 575

  /**
   * ct.
   */
  public SparQuote()
  {
    // Wir beginnen per Default mit dem 01.01. des letzten Jahres
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH,1);
    cal.set(Calendar.MONTH,Calendar.JANUARY);
    cal.add(Calendar.YEAR,-1);
    this.start = HBCIProperties.startOfDay(cal.getTime());
    
    this.listener = new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          Integer value = (Integer) getTagAuswahl().getValue();
          stichtag = value == null ? 1 : value.intValue();
          
          start = (Date) getStartAuswahl().getValue();
          if (start != null && !start.before(HBCIProperties.startOfDay(new Date())))
          {
            // Datum darf sich nicht in der Zukunft befinden. Wir resetten das Datum
            start = HBCIProperties.startOfDay(cal.getTime());
            getStartAuswahl().setValue(start);
          }

          load();
          redraw();
          if (chart != null)
            chart.redraw();
        }
        catch (Exception e)
        {
          // ignorieren wir. Durch den Delayed-Listener kann dieses
          // Event auch aufgerufen werden, wenn der Dialog schon verlassen wurde
          // Daher nur zu Debugging-Zwecken.
          Logger.write(Level.DEBUG,"unable to redraw data, it seems, the view was allready closed",e);
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
    this.kontoauswahl.addListener(new DelayedListener(500,this.listener));
    return this.kontoauswahl;
  }
  
  /**
   * Liefert ein Eingabefeld fuer den Stichtag.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  private IntegerInput getTagAuswahl() throws RemoteException
  {
    if (this.tagAuswahl != null)
      return this.tagAuswahl;

    // BUGZILLA 337
    this.tagAuswahl = new IntegerInput(this.stichtag);
    this.tagAuswahl.setComment(i18n.tr(". Tag des Monats"));
    this.tagAuswahl.setName(i18n.tr("Stichtag"));
    this.tagAuswahl.addListener(new DelayedListener(500,this.listener));
    return this.tagAuswahl;
  }
  
  /**
   * Liefert ein Eingabefeld fuer das Start-Datum.
   * @return
   * @throws RemoteException
   */
  private DateInput getStartAuswahl() throws RemoteException
  {
    if (this.startAuswahl != null)
      return this.startAuswahl;
    this.startAuswahl = new DateInput(this.start);
    this.startAuswahl.setName(i18n.tr("Start-Datum"));
    this.startAuswahl.setComment(i18n.tr("frühestes Valuta-Datum"));
    this.startAuswahl.addListener(new DelayedListener(500,this.listener));
    return this.startAuswahl;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    load();

    LabelGroup filter = new LabelGroup(parent,i18n.tr("Anzeige einschränken"));
    
    filter.addInput(getKontoAuswahl());
    filter.addInput(getStartAuswahl());
    filter.addInput(getTagAuswahl());

    ButtonArea topButtons = new ButtonArea(parent,2);
    topButtons.addButton(new Back(false));
    topButtons.addButton(i18n.tr("Aktualisieren"), new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        listener.handleEvent(null);
      }
    
    },null,true,"view-refresh.png");
    
    // Wir initialisieren die Tabelle erstmal ohne Werte.
    this.table = new TablePart(data,null);
    this.table.addColumn(i18n.tr("Monat"), "monat", new Formatter() {
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
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        UmsatzEntry ue = (UmsatzEntry) item.getData();
        if (ue.einnahmen - ue.ausgaben < 0)
          item.setForeground(Settings.getBuchungSollForeground());
        else
          item.setForeground(Settings.getBuchungHabenForeground());
      }
    });
    
    TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

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

    for (UmsatzEntry e:this.data)
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

    DBIterator umsaetze = UmsatzUtil.getUmsaetze();
    Konto konto = (Konto) getKontoAuswahl().getValue();
    if (konto != null)
      umsaetze.addFilter("konto_id = " + konto.getID());

    if (start != null)
      umsaetze.addFilter("valuta >= ?", new Object[] {new java.sql.Date(start.getTime())});

    UmsatzEntry currentEntry = null;
    Calendar cal             = Calendar.getInstance();
    Date currentLimit        = null;

    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      Date date = u.getDatum();
      if (date == null)
      {
        Logger.warn("no valuta found for umsatz, skipping record");
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
        cal.add(Calendar.MONTH,1);
        
        // BUGZILLA 691
        if (stichtag > cal.getActualMaximum(Calendar.DAY_OF_MONTH))
          cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        else
          cal.set(Calendar.DAY_OF_MONTH, stichtag);

        currentLimit = HBCIProperties.startOfDay(cal.getTime());
      }
      
      double betrag = u.getBetrag();
      if (betrag > 0)
        currentEntry.einnahmen += betrag;
      else
        currentEntry.ausgaben -= betrag;
    }
    
    // Trend ermitteln
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Glättungsverfahren
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
          ue.monat = current.monat;
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
    public List getData() throws RemoteException
    {
      return data;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
      Konto konto = (Konto) getKontoAuswahl().getValue();
      return konto == null ? i18n.tr("Alle Konten") : konto.getBezeichnung();
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
      return "monat";
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


/*********************************************************************
 * $Log: SparQuote.java,v $
 * Revision 1.27  2010/11/29 22:44:30  willuhn
 * @B getCurve() wurde falsch rum interpretiert ;)
 *
 * Revision 1.26  2010-11-24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.25  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.24  2010-08-11 14:53:19  willuhn
 * @B Kleiner Darstellungsfehler (unnoetig breiter rechter Rand wegen zweispaltigem Part)
 *
 * Revision 1.23  2009-10-30 10:05:05  willuhn
 * @B Datum statt Valuta verwenden - ein Umsatz landet sonst u.U. im falschen Monat, wenn Datum in einem, Valuta aber im anderen Monat ist
 *
 * Revision 1.22  2009/10/30 00:36:09  willuhn
 * @R Hinweistext "Daten werden geladen..." entfernt
 *
 * Revision 1.21  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.20  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.19  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.18  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.17  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.16  2009/01/20 09:33:15  willuhn
 * @B BUGZILLA 691
 *
 * Revision 1.15  2009/01/12 00:46:50  willuhn
 * @N Vereinheitlichtes KontoInput in den Auswertungen
 *
 * Revision 1.14  2008/04/06 23:21:43  willuhn
 * @C Bug 575
 * @N Der Vereinheitlichung wegen alle Buttons in den Auswertungen nach oben verschoben. Sie sind dann naeher an den Filter-Controls -> ergonomischer
 *
 * Revision 1.13  2008/02/26 01:12:30  willuhn
 * @R nicht mehr benoetigte Funktion entfernt
 *
 * Revision 1.12  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.11  2007/10/14 22:51:32  willuhn
 * @B Der 1. floss in den Vormonat
 *
 * Revision 1.10  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.9  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.8  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.7  2007/03/21 16:56:56  willuhn
 * @N Online-Hilfe aktualisiert
 * @N Bug 337 (Stichtag in Sparquote)
 * @C Refactoring in Sparquote
 *
 * Revision 1.6  2006/08/01 21:29:12  willuhn
 * @N Geaenderte LineCharts
 *
 * Revision 1.5  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
 * Revision 1.4  2006/07/13 23:28:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/07/13 23:09:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/07/13 22:34:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/07/13 00:21:15  willuhn
 * @N Neue Auswertung "Sparquote"
 *
 **********************************************************************/