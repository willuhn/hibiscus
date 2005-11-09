/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Overview.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/09 01:13:53 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige des Vermoegens-Ueberblicks.
 */
public class Overview extends AbstractBox implements Box
{
  private I18N i18n = null;
  private Input saldo       = null;
  private Input ausgaben    = null;
  private Input einnahmen   = null;
  private Input bilanz      = null;
  
  private Input start       = null;
  private Input end         = null;

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
    group.addLabelPair(i18n.tr("Saldo über alle Konten") + ":", getSaldo());
    group.addSeparator();
    group.addLabelPair(i18n.tr("Beginn des Zeitraumes") + ":", getStart());
    group.addLabelPair(i18n.tr("Ende des Zeitraumes") + ":", getEnd());
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
   * Liefert ein Anzeige-Feld mit dem Saldo ueber alle Konten.
   * @return Saldo ueber alle Konten.
   * @throws RemoteException
   */
  private Input getSaldo() throws RemoteException
  {
    if (this.saldo != null)
      return this.saldo;
    double d = 0d;
    DBIterator konten = Settings.getDBService().createList(Konto.class);
    while (konten.hasNext())
    {
      Konto k = (Konto) konten.next();
      d += k.getSaldo();
    }
    this.saldo = new LabelInput(HBCI.DECIMALFORMAT.format(d));
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
    Date begin = cal.getTime();

    CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    d.setTitle(i18n.tr("Start-Datum"));
    d.setDate(begin);
    d.setText(i18n.tr("Bitte wählen Sie das Start-Datum für die Berechnung"));
    d.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        try
        {
          Date start = (Date) event.data;
          ((DialogInput)getStart()).setText(HBCI.DATEFORMAT.format(start));
          getStart().setValue(start);
          calculate(start,(Date)getEnd().getValue());
        }
        catch (RemoteException e)
        {
          Logger.error("unable to calculate sum",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Berechnen der Summen"));
        }
      }
    });
    this.start = new DialogInput(HBCI.DATEFORMAT.format(begin),d);
    this.start.setValue(begin);
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
    Date d2 = cal.getTime();

    CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    d.setDate(d2);
    d.setTitle(i18n.tr("End-Datum"));
    d.setText(i18n.tr("Bitte wählen Sie das End-Datum für die Berechnung"));
    d.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        try
        {
          Date end = (Date) event.data;
          ((DialogInput)getEnd()).setText(HBCI.DATEFORMAT.format(end));
          getEnd().setValue(end);
          calculate((Date)getStart().getValue(),end);
        }
        catch (RemoteException e)
        {
          Logger.error("unable to calculate sum",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Berechnen der Summen"));
        }
      }
    });
    this.end = new DialogInput(HBCI.DATEFORMAT.format(d2),d);
    this.end.setValue(d2);
    ((DialogInput)this.end).disableClientControl();

    // Einmal am Anfang ausloesen
    try
    {
      calculate((Date)getStart().getValue(),d2);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to calculate sum",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Berechnen der Summen"));
    }
    return this.end;
  }

  /**
   * Hilfsfunktion zum Berechnen.
   * @param start
   * @param end
   * @throws RemoteException
   */
  private synchronized void calculate(Date start, Date end) throws RemoteException
  {
    if (start == null || end == null || start.after(end))
      return;

    double in = 0d;
    double out = 0d;
    DBIterator i = Settings.getDBService().createList(Konto.class);
    while (i.hasNext())
    {
      Konto k = (Konto) i.next();
      in += k.getEinnahmen(start,end);
      out += k.getAusgaben(start, end);
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
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/