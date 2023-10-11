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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.ScaleInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoSumme;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoTrend;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.report.balance.AccountBalanceProvider;
import de.willuhn.jameica.hbci.report.balance.AccountBalanceService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Komponente, die den Saldoverlauf eines Kontos grafisch anzeigt.
 */
public class SaldoChart implements Part
{
  
  private static final String FORCE = "FORCE";

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  private Konto konto             = null;
  private boolean tiny            = false;
  
  private KontoInput kontoauswahl = null;
  private UmsatzDaysInput rangeTiny = null;
  private DateInput start          = null;
  private DateInput end            = null;
  private RangeInput range         = null;
  private CheckboxInput onlyActive = null;
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
   * @param konto das Konto. Optional. Wenn kein Konto angegeben ist, wir der Saldenverlauf ueber die Summe aller Konten berechnet.
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
    this.kontoauswahl.setSupportGroups(true);
    this.kontoauswahl.setRememberSelection("auswertungen.saldochart");
    this.kontoauswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoauswahl.setComment(null); // Keinen Kommentar anzeigen
    this.kontoauswahl.addListener(this.reloadListener);
    return this.kontoauswahl;
  }

  /**
   * Liefert eine Auswahl fuer den Zeitraum.
   * @return Auswahl fuer den Zeitraum.
   * @throws RemoteException
   */
  private ScaleInput getRangeTiny() throws RemoteException
  {
    if (this.rangeTiny != null)
      return this.rangeTiny;

    this.rangeTiny = new UmsatzDaysInput();
    this.rangeTiny.setRememberSelection("days.saldochart");
    this.rangeTiny.addListener(new DelayedListener(300,this.reloadListener));
    return this.rangeTiny;
  }

  /**
   * Liefert eine Auswahl mit Zeit-Presets.
   * @return eine Auswahl mit Zeit-Presets.
   */
  public RangeInput getRange()
  {
    if (this.range != null)
      return this.range;
    
    this.range = new RangeInput(this.getStart(),this.getEnd(),Range.CATEGORY_AUSWERTUNG, "auswertungen.saldochart.filter.range");
    this.range.addListener(this.reloadListener);
    return this.range;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * @return Auswahl-Feld.
   */
  public Input getStart()
  {
    if (this.start != null)
      return this.start;

    this.start = new DateFromInput(null, "auswertungen.saldochart.filter.from");
    this.start.setName(i18n.tr("Von"));
    this.start.setComment(null);
    this.start.addListener(new DelayedListener(300,this.reloadListener));
    return this.start;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * @return Auswahl-Feld.
   */
  public Input getEnd()
  {
    if (this.end != null)
      return this.end;

    this.end = new DateToInput(null, "auswertungen.saldochart.filter.to");
    this.end.setName(i18n.tr("bis"));
    this.end.setComment(null);
    this.end.addListener(new DelayedListener(300,this.reloadListener));
    return this.end;
  }

  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob nur aktive Konten angezeigt werden sollen.
   * @return Checkbox.
   */
  public CheckboxInput getActiveOnly()
  {
    if (this.onlyActive != null)
      return this.onlyActive;
    
    this.onlyActive = new CheckboxInput(settings.getBoolean("auswertungen.saldochart.filter.active",false));
    this.onlyActive.setName(i18n.tr("Nur aktive Konten"));
    this.onlyActive.addListener(this.reloadListener);
    this.onlyActive.addListener(new org.eclipse.swt.widgets.Listener() {

      @Override
      public void handleEvent(Event event)
      {
        settings.setAttribute("auswertungen.saldochart.filter.active", (Boolean) onlyActive.getValue());
      }
    });
    return this.onlyActive;
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
          right.addInput(this.getRangeTiny());
        }
        else
        {
          final TabFolder folder = new TabFolder(parent, SWT.NONE);
          folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
          TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));
          
          ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);
          
          Container left = new SimpleContainer(cols.getComposite());
          left.addInput(getKontoAuswahl());
          left.addInput(this.getActiveOnly());
          
          Container right = new SimpleContainer(cols.getComposite());
            
          right.addInput(getRange());
          MultiInput range = new MultiInput(getStart(),getEnd());
          right.addInput(range);

          ButtonArea buttons = new ButtonArea();
          buttons.addButton(i18n.tr("Aktualisieren"), new Action()
          {
          
            /**
             * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
             */
            public void handleAction(Object context) throws ApplicationException
            {
              Event event=new Event();
              event.data = FORCE;
              reloadListener.handleEvent(event);
            }
          },null,true,"view-refresh.png");
          
          buttons.paint(parent);
        }
      }
      else
      {
        Container container = new SimpleContainer(parent);
        container.addInput(this.getRangeTiny());
      }
      
      this.chart = new LineChart();
      
      this.reloadListener.handleEvent(null); // einmal initial ausloesen
      chart.paint(parent);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Throwable t)
    {
      Logger.error("unable to paint chart",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen des Saldo-Verlaufs"),StatusBarMessage.TYPE_ERROR));
    }
  }

  private Date getStartDate() throws RemoteException
  {
    Date date = null;
    if (tiny)
    {
      int start = ((Integer) getRangeTiny().getValue()).intValue();
      if (start >= 0)
      {
        long d = start * 24l * 60l * 60l * 1000l;
        date = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
      }
    } else if (getStart().getValue() != null)
    {
      date = (Date) getStart().getValue();
    }
    //keine Auswahl? erster Umsatz bzw. heute falls keine Umsätze
    if (date == null)
    {
      date = UmsatzUtil.getOldest(konto == null ? getKontoAuswahl().getValue() : konto);
    }
    if (date == null)
    {
      date = new Date();
    }
    return date;
  }

  private Date getEndDate()
  {
    if (tiny)
    {
      return null;
    } else
    {
      return (Date) getEnd().getValue();
    }
  }
  
  /**
   * Liefert die ausgewaehlten Konten.
   * @return
   * @throws RemoteException
   */
  private List<Konto> getSelectedAccounts() throws RemoteException
  {
    if (this.konto != null)
      return Arrays.asList(this.konto);
    
    final List<Konto> result = new ArrayList<>();
    final Object o = this.getKontoAuswahl().getValue();
    if (o instanceof Konto)
    {
      result.add((Konto) o);
    }
    else if (o == null || (o instanceof String))
    {
      boolean onlyActive = ((Boolean) this.getActiveOnly().getValue()).booleanValue();
      String group = o != null && (o instanceof String) ? (String) o : null;

      List<Konto> konten = KontoUtil.getKonten(onlyActive ? KontoFilter.ACTIVE : KontoFilter.ALL);
      for (Konto k : konten)
      {
        if (group == null || Objects.equals(group, k.getKategorie()))
        {
          result.add(k);
        }
      }
    }
    return result;
  }

  /**
   * Laedt den Chart neu.
   */
  private class ReloadListener implements Listener
  {
    private Object oPrev = null;
    private Date startPrev = new Date();
    private Date endPrev = new Date();
    
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (chart == null)
        return;
      
      try
      {

        Object o = konto;
        
        if (o == null) // Das ist der Fall, wenn das Kontoauswahlfeld verfuegbar ist
          o = getKontoAuswahl().getValue();
        
        final Date start = getStartDate();
        final Date end = getEndDate();

        final boolean changed = !Objects.equals(start, startPrev) ||
                                !Objects.equals(end, endPrev) ||
                                getActiveOnly().hasChanged() ||
                                o != oPrev;

        final boolean force = event != null && event.data == FORCE;
        
        if (!changed && !force)
          return;
          
        chart.removeAllData();
        
        String startString = start != null ? HBCI.DATEFORMAT.format(start) : "";
        String endString = end != null ? HBCI.DATEFORMAT.format(end) : "";
        chart.setTitle(i18n.tr("Saldo-Verlauf {0} - {1}", startString, endString));

        List<Konto> konten = getSelectedAccounts();
        
        final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
        final AccountBalanceService balanceService = bs.get(AccountBalanceService.class);       
        final ChartDataSaldoSumme sum = new ChartDataSaldoSumme();
        for (Konto konto : konten)
        {
          AccountBalanceProvider balanceProvider = balanceService.getBalanceProviderForAccount(konto);
          AbstractChartDataSaldo balance = balanceProvider.getBalanceChartData(konto, start, end);
          chart.addData(balance);
          sum.add(balance.getData());
        }
        
        // Mehr als 1 Konto. Dann zeigen wir auch eine Summe ueber alle an
        if (konten.size() > 1)
          chart.addData(sum);

        ChartDataSaldoTrend trend = new ChartDataSaldoTrend();
        trend.add(sum.getData());
        chart.addData(trend);
        
        if (event != null)
        {
          chart.redraw(); // nur neu laden, wenn via Select ausgeloest
        }
        
        oPrev = o;
        startPrev = start;
        endPrev = end;
      }
      catch (Exception e)
      {
        Logger.error("unable to redraw chart",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren des Saldo-Verlaufs"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
}
