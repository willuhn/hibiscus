/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SaldoChart.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/08/11 16:06:05 $
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoTrend;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Komponente, die den Saldoverlauf eines Kontos grafisch anzeigt.
 * @author willuhn
 */
public class SaldoChart implements Part
{
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Konto konto = null;
  
  private KontoInput kontoauswahl = null;
  private UmsatzDaysInput range   = null;
  private Listener listener       = null;
  
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
   * @param konto das Konto.
   */
  public SaldoChart(Konto konto)
  {
    this.konto = konto;
    this.listener = new ReloadListener();
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
    this.kontoauswahl.addListener(this.listener);
    return this.kontoauswahl;
  }
  
  /**
   * Liefert eine Auswahl fuer den Zeitraum.
   * @return Auswahl fuer den Zeitraum.
   * @throws RemoteException
   */
  private UmsatzDaysInput getRange() throws RemoteException
  {
    if (this.range != null)
      return this.range;

    this.range = new UmsatzDaysInput();
    this.range.addListener(this.listener);
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
        ColumnLayout layout = new ColumnLayout(parent,2);
        Container left = new SimpleContainer(layout.getComposite());
        left.addInput(this.getKontoAuswahl());
        Container right = new SimpleContainer(layout.getComposite());
        right.addInput(this.getRange());
      }
      else
      {
        Container container = new SimpleContainer(parent);
        container.addInput(this.getRange());
      }
      
      this.chart = new LineChart();
      this.listener.handleEvent(null); // einmal initial ausloesen
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

        if (start < 0)
          chart.setTitle(i18n.tr("Saldo im Verlauf (alle Umsätze)"));
        else
          chart.setTitle(i18n.tr("Saldo im Verlauf der letzten {0} Tage",Integer.toString(start)));

        chart.addData(new ChartDataSaldoVerlauf(k,start));
        chart.addData(new ChartDataSaldoTrend(k,start));
        
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
 * Revision 1.1  2010/08/11 16:06:05  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 * Revision 1.6  2009/08/27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich  eine Trendkurve an
 *********************************************************************/