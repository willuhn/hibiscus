/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SaldoChart.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/05/19 08:41:53 $
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
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
          LabelGroup group = new LabelGroup(parent, i18n.tr("Anzeige einschränken"));
          group.addInput(this.getKontoAuswahl());
          group.addInput(this.getRange());

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
      this.reloadListener.handleEvent(null); // einmal initial ausloesen
      chart.paint(parent);
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
    private Konto kPrev = null;
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

        Konto k = konto;
        if (k == null) // Das ist der Fall, wenn das Kontoauswahlfeld verfuegbar ist
          k = (Konto) getKontoAuswahl().getValue();
        
        if (start == startPrev && k == kPrev)
          return; // Auswahl nicht geaendert
          
        chart.removeAllData();

        Date date = null;

        if (start < 0)
        {
          // Keine Anzahl von Tagen angegeben. Dann nehmen wir den
          // aeltesten gefundenen Umsatz als Beginn
          String query = "select min(valuta) from umsatz";
          if (k != null)
            query += " where konto_id = " + k.getID();
          
          date = (Date) Settings.getDBService().execute(query,null,new ResultSetExtractor() {
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
        
        if (k == null) // wir zeichnen einen Stacked-Graph ueber alle Konten 
        {
          DBIterator it = Settings.getDBService().createList(Konto.class);
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
          ChartDataSaldoVerlauf s = new ChartDataSaldoVerlauf(k,date);
          ChartDataSaldoTrend   t = new ChartDataSaldoTrend();
          t.add(s.getData());
          chart.addData(s);
          chart.addData(t);
        }
        
        
        if (event != null)
          chart.redraw(); // nur neu laden, wenn via Select ausgeloest
        
        kPrev = k;
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


/*********************************************************************
 * $Log: SaldoChart.java,v $
 * Revision 1.7  2011/05/19 08:41:53  willuhn
 * @N BUGZILLA 1038 - generische Loesung
 *
 * Revision 1.6  2011-05-04 12:04:40  willuhn
 * @N Zeitraum in Umsatzliste und Saldo-Chart kann jetzt freier und bequemer ueber einen Schieberegler eingestellt werden
 * @B Dispose-Checks in Umsatzliste
 *
 * Revision 1.5  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.4  2011-04-08 09:28:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.2  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.1  2010-08-11 16:06:05  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 * Revision 1.6  2009/08/27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich  eine Trendkurve an
 *********************************************************************/