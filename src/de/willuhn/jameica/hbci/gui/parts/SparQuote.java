/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SparQuote.java,v $
 * $Revision: 1.10 $
 * $Date: 2007/08/07 23:54:15 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Stellt die Einnahmen und Ausgaben eines Kontos gegenueber.
 */
public class SparQuote implements Part
{
  private static DateFormat DATEFORMAT = new SimpleDateFormat("MM.yyyy");
  
  private TablePart table       = null;
  private LineChart chart       = null;
  
  private Konto konto           = null;
  private int stichtag          = 1;
  private GenericIterator data  = null;
  private GenericIterator trend = null;

  private I18N i18n             = null;
  
  /**
   * ct.
   */
  public SparQuote()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Container container = new SimpleContainer(parent);
    
    // Wir nehmen standardmaessig das erste Konto
    DBIterator konten = Settings.getDBService().createList(Konto.class);
    if (konten.hasNext())
    {
      this.konto = (Konto) konten.next();
      konten.begin();
      load();
    }

    final SelectInput auswahl = new SelectInput(konten,konto);
    auswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    auswahl.setName(i18n.tr("Konto"));
    auswahl.setAttribute("longname");
    container.addInput(auswahl);
    
    // BUGZILLA 337
    final IntegerInput si = new IntegerInput(this.stichtag);
    si.setComment(i18n.tr(". Tag des Monats"));
    si.setName(i18n.tr("Stichtag"));
    container.addInput(si);
    
    // Wir initialisieren die Tabelle erstmal ohne Werte.
    this.table = new TablePart(data == null ? PseudoIterator.fromArray(new UmsatzEntry[]{new UmsatzEntry()}) : data,null);
    this.table.addColumn(i18n.tr("Monat"),     "monat", new Formatter() {
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
      TabGroup tab2 = new TabGroup(folder,i18n.tr("Grafische Auswertung"));

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

    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Zurück"),new Back());
    buttons.addButton(i18n.tr("Aktualisieren"), new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        konto = (Konto) auswahl.getValue();
        Integer value = (Integer) si.getValue();
        stichtag = value == null ? 1 : value.intValue();
        try
        {
          load();
          redraw();
          if (chart != null)
            chart.redraw();
        }
        catch (RemoteException re)
        {
          Logger.error("error while calculating values",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ermitteln der Sparquote"),StatusBarMessage.TYPE_ERROR));
        }
      }
    
    },null,true);
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

    this.data.begin();
    while (this.data.hasNext())
    {
      this.table.addItem(this.data.next());
    }
    this.data.begin();
  }

  /**
   * Laedt die Sparquoten des Kontos
   * @throws RemoteException
   */
  private void load() throws RemoteException
  {
    DBIterator umsaetze = null;
    
    umsaetze = UmsatzUtil.getUmsaetze();
    if (konto != null)
      umsaetze.addFilter("konto_id = " + konto.getID());

    ArrayList list           = new ArrayList();
    UmsatzEntry currentEntry = null;
    Calendar cal             = Calendar.getInstance();
    Date currentLimit        = null;

    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      Date valuta = u.getValuta();
      if (valuta == null)
      {
        Logger.warn("no valuta found for umsatz, skipping record");
        continue;
      }

      if (currentLimit == null || valuta.after(currentLimit))
      {
        // Wir haben das Limit erreicht. Also beginnen wir einen neuen Block
        currentEntry = new UmsatzEntry();
        currentEntry.monat = valuta;
        list.add(currentEntry);

        // BUGZILLA 337
        // Neues Limit definieren
        cal.setTime(valuta);
        cal.add(Calendar.MONTH,1);
        cal.set(Calendar.DAY_OF_MONTH,stichtag);
        currentLimit = cal.getTime();
      }
      
      double betrag = u.getBetrag();
      if (betrag > 0)
        currentEntry.einnahmen += betrag;
      else
        currentEntry.ausgaben -= betrag;
    }
    this.data  = PseudoIterator.fromArray((UmsatzEntry[])list.toArray(new UmsatzEntry[list.size()]));
    
    // Trend ermitteln
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Glättungsverfahren
    // http://de.wikibooks.org/wiki/Mathematik:_Statistik:_Trend_und_Saisonkomponente
    
    ArrayList trendList = new ArrayList();
    for (int i=0;i<list.size();++i)
      trendList.add(getDurchschnitt(list,i));
    this.trend = PseudoIterator.fromArray((UmsatzEntry[])trendList.toArray(new UmsatzEntry[trendList.size()]));
  }
  
  /**
   * Liefert einen synthetischen Umsatz-Entry basierend auf den
   * Daten der 4 links und rechts daneben liegenden Monaten als Durchschnitt.
   * @param list Liste der Umsaetze.
   * @param pos Position.
   * @return der Durchschnitt.
   */
  private UmsatzEntry getDurchschnitt(ArrayList list, int pos)
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
  private class UmsatzEntry implements GenericObject
  {
    private double einnahmen = 0d;
    private double ausgaben  = 0d;
    private Date monat       = null;

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if ("einnahmen".equals(arg0))
        return new Double(einnahmen);
      if ("ausgaben".equals(arg0))
        return new Double(ausgaben);
      if ("monat".equals(arg0))
        return monat;
      if ("sparquote".equals(arg0))
        return new Double(einnahmen - ausgaben);
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"einnahmen","ausgaben","monat","sparquote"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      if (monat == null)
        return "foo";
      return monat.toString();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "sparquote";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null || arg0.getID() == null)
        return false;
      return arg0.getID().equals(this.getID());
    }
  }
  
  
  /**
   * Implementierung eines Datensatzes fuer die Darstellung der Sparquote.
   */
  private class ChartDataSparQuote implements LineChartData
  {

    private Formatter formatter = null;
    
    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
     */
    public GenericIterator getData() throws RemoteException
    {
      return data == null ? PseudoIterator.fromArray(new UmsatzEntry[0]) : data;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
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
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelFormatter()
     */
    public Formatter getLabelFormatter() throws RemoteException
    {
      if (this.formatter != null)
        return this.formatter;
      
      return new Formatter() {

        /**
         * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
         */
        public String format(Object o)
        {
          if (o == null)
            return "";
          if (!(o instanceof Date))
            return o.toString();
          return DATEFORMAT.format((Date)o);
        }
      };
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
     */
    public boolean getCurve()
    {
      return true;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getShowMarker()
     */
    public boolean getShowMarker()
    {
      return false;
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
    public GenericIterator getData() throws RemoteException
    {
      return trend == null ? PseudoIterator.fromArray(new UmsatzEntry[0]) : trend;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
      return i18n.tr("Trend");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getShowMarker()
     */
    public boolean getShowMarker()
    {
      return false;
    }
  }

}


/*********************************************************************
 * $Log: SparQuote.java,v $
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