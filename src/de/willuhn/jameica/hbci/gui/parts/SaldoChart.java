/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SaldoChart.java,v $
 * $Revision: 1.9 $
 * $Date: 2012/04/05 21:27:41 $
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.ScaleInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoSumme;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoTrend;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Komponente, die den Saldoverlauf eines Kontos grafisch anzeigt.
 * @author willuhn
 */
public class SaldoChart implements Part
{
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Konto konto             = null;
  private boolean tiny            = false;
  
  private KontoInput kontoauswahl = null;
  private UmsatzDaysInput range   = null;
  private Listener reloadListener = new ReloadListener();
  
  private LineChart chart         = null;
  // private LineChart forecast      = null;

  /**
   * ct.
   * Konstruktor fuer die Anzeige des Saldo-Charts beliebiger Konten.
   * Bei Verwendung dieses Konstruktors wird eine Kontoauswahl eingeblendet.
   */
  public SaldoChart()
  {
    this(null);
  }
  
  /**
   * ct.
   * Konstruktor fuer die Anzeige des Saldo-Charts von genau einem Konto.
   * @param konto das Konto. Optional. Wenn kein konto ist, wir der Saldenverlauf ueber die Summe aller Konten berechnet.
   */
  public SaldoChart(Konto konto)
  {
    this.konto = konto;
  }
  
  /**
   * Aktiviert die platzsparende Anzeige der Controls.
   * @param b true, wenn die Anzeige platzsparend erfolgen soll.
   * Default: false.
   */
  public void setTinyView(boolean b)
  {
    this.tiny = b;
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
    this.kontoauswahl.setRememberSelection("auswertungen.saldochart");
    this.kontoauswahl.setSupportGroups(true);
    this.kontoauswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    if (tiny)
      this.kontoauswahl.setComment(null); // Keinen Kommentar anzeigen
    this.kontoauswahl.addListener(this.reloadListener);
    return this.kontoauswahl;
  }
  
  /**
   * Liefert eine Auswahl fuer den Zeitraum.
   * @return Auswahl fuer den Zeitraum.
   * @throws RemoteException
   */
  private ScaleInput getRange() throws RemoteException
  {
    if (this.range != null)
      return this.range;

    this.range = new UmsatzDaysInput();
    this.range.addListener(new DelayedListener(300,this.reloadListener));
    return this.range;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      if (this.konto == null)
      {
        if (tiny)
        {
          ColumnLayout layout = new ColumnLayout(parent,2);
          Container left = new SimpleContainer(layout.getComposite());
          left.addInput(this.getKontoAuswahl());
          Container right = new SimpleContainer(layout.getComposite());
          right.addInput(this.getRange());
        }
        else
        {
          final TabFolder folder = new TabFolder(parent, SWT.NONE);
          folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
          TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));
          
          tab.addInput(this.getKontoAuswahl());
          tab.addInput(this.getRange());

          ButtonArea buttons = new ButtonArea();
          buttons.addButton(i18n.tr("Aktualisieren"), new Action()
          {
          
            /**
             * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
             */
            public void handleAction(Object context) throws ApplicationException
            {
              reloadListener.handleEvent(new Event());
            }
          },null,true,"view-refresh.png");
          
          buttons.paint(parent);
        }
      }
      else
      {
        Container container = new SimpleContainer(parent);
        container.addInput(this.getRange());
      }
      
      this.chart = new LineChart();
      
//      if (this.tiny)
//      {
        this.reloadListener.handleEvent(null); // einmal initial ausloesen
        chart.paint(parent);
//      }
//      else
//      {
//        this.forecast = new LineChart();
//
//        final TabFolder folder = new TabFolder(parent, SWT.NONE);
//        folder.setLayoutData(new GridData(GridData.FILL_BOTH));
//        
//        TabGroup current = new TabGroup(folder,i18n.tr("Aktuell"),true,1);
//        TabGroup forecast = new TabGroup(folder,i18n.tr("Prognose"),true,1);
//
//        this.reloadListener.handleEvent(null); // einmal initial ausloesen
//        
//        this.chart.paint(current.getComposite());
//        this.forecast.paint(forecast.getComposite());
//      }
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to paint chart",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen des Saldo-Verlaufs"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Laedt den Chart neu.
   */
  private class ReloadListener implements Listener
  {
    private Object oPrev = null;
    private int startPrev = 0;
    
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (chart == null)
        return;
      
      try
      {
        int start = ((Integer)getRange().getValue()).intValue();

        Object o = konto;
        if (o == null) // Das ist der Fall, wenn das Kontoauswahlfeld verfuegbar ist
          o = getKontoAuswahl().getValue();
        
        if (start == startPrev && o == oPrev)
          return; // Auswahl nicht geaendert
          
        chart.removeAllData();
        
//        if (forecast != null)
//          forecast.removeAllData();

        Date date = null;

        if (start < 0)
        {
          // Keine Anzahl von Tagen angegeben. Dann nehmen wir den
          // aeltesten gefundenen Umsatz als Beginn
          String   query  = "select min(datum) from umsatz";
          Object[] params = null;
          if (o != null && (o instanceof Konto))
            query += " where konto_id = " + ((Konto) o).getID();
          else if (o != null && (o instanceof String))
          {
            query += " where konto_id in (select id from konto where kategorie = ?)";
            params = new String[]{(String) o};
          }
          
          date = (Date) Settings.getDBService().execute(query,params,new ResultSetExtractor() {
            public Object extract(ResultSet rs) throws RemoteException, SQLException
            {
              if (!rs.next())
                return null;
              return rs.getDate(1);
            }
          });
        }
        else
        {
          long d = start * 24l * 60l * 60l * 1000l;
          date = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
        }
        
        if (date == null)
        {
          Logger.info("no start date, no entries, skipping chart");
          return;
        }

        chart.setTitle(i18n.tr("Saldo-Verlauf seit {0}",HBCI.DATEFORMAT.format(date)));
        
//        if (forecast != null)
//        {
//          Date bis = null;
//          Calendar cal = Calendar.getInstance();
//          if (start < 0)
//          {
//            // Pauschal 3 Monate ab heute 
//            cal.add(Calendar.MONTH,3);
//          }
//          else
//          {
//            cal.add(Calendar.DATE,start);
//          }
//          bis = DateUtil.endOfDay(cal.getTime());
//          forecast.setTitle(i18n.tr("Saldo-Prognose bis {0}",HBCI.DATEFORMAT.format(bis)));
//          ChartDataSaldoForecast f = new ChartDataSaldoForecast(k,bis);
//          forecast.addData(f);
//        }
        
        if (o == null || !(o instanceof Konto)) // wir zeichnen einen Stacked-Graph ueber alle Konten 
        {
          DBIterator it = Settings.getDBService().createList(Konto.class);
          it.setOrder("ORDER BY LOWER(kategorie), blz, kontonummer, bezeichnung");
          if (o != null && (o instanceof String)) it.addFilter("kategorie = ?", (String) o);
          ChartDataSaldoSumme s = new ChartDataSaldoSumme();
          while (it.hasNext())
          {
            ChartDataSaldoVerlauf v = new ChartDataSaldoVerlauf((Konto)it.next(),date);
            chart.addData(v);
            s.add(v.getData());
          }

          ChartDataSaldoTrend t = new ChartDataSaldoTrend();
          t.add(s.getData());
          chart.addData(s);
          chart.addData(t);
        }
        else // Ansonsten nur fuer eine
        {
          ChartDataSaldoVerlauf s = new ChartDataSaldoVerlauf((Konto) o,date);
          ChartDataSaldoTrend   t = new ChartDataSaldoTrend();
          t.add(s.getData());
          chart.addData(s);
          chart.addData(t);
        }
        
        
        if (event != null)
        {
          chart.redraw(); // nur neu laden, wenn via Select ausgeloest
          
//          if (forecast != null)
//            forecast.redraw();
        }
        
        oPrev = o;
        startPrev = start;
      }
      catch (Exception e)
      {
        Logger.error("unable to redraw chart",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren des Saldo-Verlaufs"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
}
