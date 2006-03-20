/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Overview.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/03/20 16:59:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige des Vermoegens-Ueberblicks.
 */
public class Overview extends AbstractBox implements Box
{
  private I18N i18n = null;
  private SelectInput kontoAuswahl = null;
  private Input saldo              = null;
  private Input ausgaben           = null;
  private Input einnahmen          = null;
  private Input bilanz             = null;
  
  private Input start              = null;
  private Input end                = null;
  
  private Date dStart              = null;
  private Date dEnd                = null;
  private Konto konto              = null;

  /**
   * ct.
   */
  public Overview()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return i18n.tr("Finanz-Übersicht");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent,getName());
    group.addLabelPair(i18n.tr("Konto") + ":", getKontoAuswahl());
    group.addLabelPair(i18n.tr("Beginn des Zeitraumes") + ":", getStart());
    group.addLabelPair(i18n.tr("Ende des Zeitraumes") + ":", getEnd());
    group.addLabelPair(i18n.tr("Saldo") + ":", getSaldo());
    group.addLabelPair(i18n.tr("Einnahmen") + ":", getEinnahmen());
    group.addLabelPair(i18n.tr("Ausgaben") + ":", getAusgaben());
    group.addSeparator();
    group.addLabelPair(i18n.tr("Bilanz") + ":", getBilanz());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultEnabled()
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
    
    this.kontoAuswahl = new SelectInput(Settings.getDBService().createList(Konto.class),null);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    this.kontoAuswahl.setAttribute("longname");
    this.kontoAuswahl.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        konto = (Konto) kontoAuswahl.getValue();
        refresh();
      }
    });
    refresh();
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
    this.saldo.setComment(HBCIProperties.CURRENCY_DEFAULT_DE + " [" + HBCI.DATEFORMAT.format(new Date()) + "]");
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
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.DAY_OF_MONTH,1);
    this.dStart = cal.getTime();

    CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    d.setTitle(i18n.tr("Start-Datum"));
    d.setDate(dStart);
    d.setText(i18n.tr("Bitte wählen Sie das Start-Datum für die Berechnung"));
    d.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        dStart = (Date) event.data;
        refresh();
      }
    });
    this.start = new DialogInput(HBCI.DATEFORMAT.format(dStart),d);
    this.start.setValue(dStart);
    ((DialogInput)this.start).disableClientControl();
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
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    this.dEnd = cal.getTime();

    CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    d.setDate(dEnd);
    d.setTitle(i18n.tr("End-Datum"));
    d.setText(i18n.tr("Bitte wählen Sie das End-Datum für die Berechnung"));
    d.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        dEnd = (Date) event.data;
      }
    });
    this.end = new DialogInput(HBCI.DATEFORMAT.format(dEnd),d);
    this.end.setValue(dEnd);
    ((DialogInput)this.end).disableClientControl();

    return this.end;
  }

  /**
   * Aktualisiert die Salden.
   */
  private synchronized void refresh()
  {
    try
    {
      ////////////////////////////////////////////////////////////////////////////
      // Saldo ausrechnen
      double d = 0d;
      if (this.konto == null)
      {
        DBIterator konten = Settings.getDBService().createList(Konto.class);
        while (konten.hasNext())
        {
          Konto k = (Konto) konten.next();
          d += k.getSaldo();
        }
      }
      else
      {
        d = this.konto.getSaldo();
      }
      getSaldo().setValue(HBCI.DECIMALFORMAT.format(d));
      ////////////////////////////////////////////////////////////////////////////

      if (dStart == null || dEnd == null || dStart.after(dEnd))
        return;

      ((DialogInput)getStart()).setText(HBCI.DATEFORMAT.format(dStart));
      getStart().setValue(dStart);
    
      ((DialogInput)getEnd()).setText(HBCI.DATEFORMAT.format(dEnd));
      getEnd().setValue(dEnd);

      
      double in = 0d;
      double out = 0d;
      if (this.konto == null)
      {
        DBIterator i = Settings.getDBService().createList(Konto.class);
        while (i.hasNext())
        {
          Konto k = (Konto) i.next();
          in  += k.getEinnahmen(dStart,dEnd);
          out += k.getAusgaben(dStart, dEnd);
        }
      }
      else
      {
        in  = this.konto.getEinnahmen(dStart,dEnd);
        out = this.konto.getAusgaben(dStart,dEnd);
      }
      getAusgaben().setValue(HBCI.DECIMALFORMAT.format(out == 0d ? 0d : -out));
      getEinnahmen().setValue(HBCI.DECIMALFORMAT.format(in));

      double diff = in + out;
      getBilanz().setValue(HBCI.DECIMALFORMAT.format(diff));
      if (diff < 0)
        ((LabelInput)getBilanz()).setColor(Color.ERROR);
      else
        ((LabelInput)getBilanz()).setColor(Color.SUCCESS);
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
    return einnahmen;
  }
}


/*********************************************************************
 * $Log: Overview.java,v $
 * Revision 1.2  2006/03/20 16:59:01  willuhn
 * @N Overview ueber alle Konten
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/