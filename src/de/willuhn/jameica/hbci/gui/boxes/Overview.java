/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige des Vermoegens-Ueberblicks.
 */
public class Overview extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  // BUGZILLA 993
  private static Object konto      = null;
  private static Date startDate    = null;
  private static Date endDate      = null;

  private KontoInput kontoAuswahl  = null;
  private Input saldo              = null;
  private Input ausgaben           = null;
  private Input einnahmen          = null;
  private Input bilanz             = null;
  
  private DateInput start          = null;
  private DateInput end            = null;
  
  private MessageConsumer mc = new SaldoMessageConsumer();
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Finanz-Übersicht");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Container group = new SimpleContainer(parent);
    group.addLabelPair(i18n.tr("Konto") + ":", getKontoAuswahl());
    group.addLabelPair(i18n.tr("Beginn des Zeitraumes") + ":", getStart());
    group.addLabelPair(i18n.tr("Ende des Zeitraumes") + ":", getEnd());
    group.addLabelPair(i18n.tr("Saldo") + ":", getSaldo());
    group.addLabelPair(i18n.tr("Einnahmen im Zeitraum") + ":", getEinnahmen());
    group.addLabelPair(i18n.tr("Ausgaben im Zeitraum") + ":", getAusgaben());
    group.addSeparator();
    group.addLabelPair(i18n.tr("Bilanz") + ":", getBilanz());
    refresh();
    
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }
  
  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  private Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    this.kontoAuswahl = new KontoInput(konto != null && (konto instanceof Konto) && ((Konto) konto).getID() != null ? ((Konto) konto) : null,KontoFilter.ACTIVE);
    this.kontoAuswahl.setSupportGroups(true);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    this.kontoAuswahl.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        refresh();
      }
    });
    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Anzeige-Feld mit dem Saldo ueber alle Konten.
   * @return Saldo ueber alle Konten.
   */
  private Input getSaldo()
  {
    if (this.saldo != null)
      return this.saldo;
    this.saldo = new LabelInput("");
    this.saldo.setComment("");
    return this.saldo;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * @return Auswahl-Feld.
   */
  private Input getStart()
  {
    if (this.start != null)
      return this.start;
    
    if (startDate == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(Calendar.DAY_OF_MONTH,1);
      startDate = cal.getTime();
    }
    
    this.start = new DateInput(DateUtil.startOfDay(startDate),HBCI.DATEFORMAT);
    this.start.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        refresh();
      }
    });
    return this.start;
  }
  
  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * @return Auswahl-Feld.
   */
  private Input getEnd()
  {
    if (this.end != null)
      return this.end;

    if (endDate == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      endDate = cal.getTime();
    }

    this.end = new DateInput(DateUtil.endOfDay(endDate),HBCI.DATEFORMAT);
    this.end.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        refresh();
      }
    });
    return this.end;
  }

  /**
   * Aktualisiert die Salden.
   */
  private synchronized void refresh()
  {
    try
    {
      konto     = this.getKontoAuswahl().getValue();
      startDate = (Date) getStart().getValue();
      endDate   = (Date) getEnd().getValue();
      Date saldoDate = null;

      ////////////////////////////////////////////////////////////////////////////
      // Saldo ausrechnen
      double d = 0d;
      if (konto == null || !(konto instanceof Konto))
      {
        DBIterator konten = Settings.getDBService().createList(Konto.class);
        if (konto != null && (konto instanceof String))
          konten.addFilter("kategorie = ?", (String) konto);
        while (konten.hasNext())
        {
          Konto k = (Konto) konten.next();
          d += k.getSaldo();
        }
      }
      else
      {
        d = ((Konto) konto).getSaldo();
        saldoDate = ((Konto) konto).getSaldoDatum();
      }
      
      LabelInput saldo = (LabelInput) this.getSaldo();
      saldo.setValue(HBCI.DECIMALFORMAT.format(d));
      String comment = HBCIProperties.CURRENCY_DEFAULT_DE;
      if (saldoDate != null)
        comment += " [" + HBCI.DATEFORMAT.format(saldoDate) + "]";
      saldo.setComment(comment);
      saldo.setColor(ColorUtil.getColor(d,Color.ERROR,Color.SUCCESS,Color.FOREGROUND));
      ////////////////////////////////////////////////////////////////////////////

      
      if (startDate == null || endDate == null || startDate.after(endDate))
        return;

      double in = 0d;
      double out = 0d;
      if (konto == null || !(konto instanceof Konto))
      {
        DBIterator i = Settings.getDBService().createList(Konto.class);
        if (konto != null && (konto instanceof String))
          i.addFilter("kategorie = ?", (String) konto);
        while (i.hasNext())
        {
          Konto k = (Konto) i.next();
          in  += KontoUtil.getEinnahmen(k,startDate,endDate);
          out += KontoUtil.getAusgaben(k,startDate,endDate);
        }
      }
      else
      {
        in  = KontoUtil.getEinnahmen((Konto) konto,startDate,endDate);
        out = KontoUtil.getAusgaben((Konto) konto,startDate,endDate);
      }
      out = Math.abs(out); // BUGZILLA 405
      getAusgaben().setValue(HBCI.DECIMALFORMAT.format(out));
      getEinnahmen().setValue(HBCI.DECIMALFORMAT.format(in));

      double diff = in - out;
      getBilanz().setValue(HBCI.DECIMALFORMAT.format(diff));
      ((LabelInput)getBilanz()).setColor(ColorUtil.getColor(diff,Color.ERROR,Color.SUCCESS,Color.FOREGROUND));
    }
    catch (RemoteException e)
    {
      Logger.error("unable to calculate sum",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Berechnen der Bilanz"),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Liefert ein Anzeige-Feld fuer die Bilanz.
   * @return Anzeige-Feld.
   */
  private Input getBilanz()
  {
    if (this.bilanz != null)
      return this.bilanz;
    bilanz = new LabelInput("");
    bilanz.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    return bilanz;
  }
  
  /**
   * Liefert ein Anzeige-Feld fuer die Ausgaben.
   * @return Anzeige-Feld.
   */
  private Input getAusgaben()
  {
    if (this.ausgaben != null)
      return this.ausgaben;
    ausgaben = new LabelInput("");
    ausgaben.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    ((LabelInput)ausgaben).setColor(Color.ERROR);
    return ausgaben;
  }

  /**
   * Liefert ein Anzeige-Feld fuer die Einnahmen.
   * @return Anzeige-Feld.
   */
  private Input getEinnahmen()
  {
    if (this.einnahmen != null)
      return this.einnahmen;
    einnahmen = new LabelInput("");
    einnahmen.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    ((LabelInput)einnahmen).setColor(Color.SUCCESS);
    return einnahmen;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }
  
  /**
   * Wird ueber Saldo-Aenderungen benachrichtigt.
   */
  private class SaldoMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SaldoMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          refresh();
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }

}
