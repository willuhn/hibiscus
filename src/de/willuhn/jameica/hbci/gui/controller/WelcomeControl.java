/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/WelcomeControl.java,v $
 * $Revision: 1.23 $
 * $Date: 2005/11/07 18:51:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCISynchronizer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Startseite.
 */
public class WelcomeControl extends AbstractControl {

	private I18N i18n 											= null;

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  private CheckboxInput syncDauer = null;
  private CheckboxInput syncUeb   = null;
  private CheckboxInput syncLast  = null;
  
  private Input saldo       = null;
  private Input ausgaben    = null;
  private Input einnahmen   = null;
  private Input bilanz      = null;
  
  private Input start       = null;
  private Input end         = null;

  /**
   * @param view
   */
  public WelcomeControl(AbstractView view) {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * Liefert ein Anzeige-Feld mit dem Saldo ueber alle Konten.
   * @return Saldo ueber alle Konten.
   * @throws RemoteException
   */
  public Input getSaldo() throws RemoteException
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
  public Input getStart()
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
  public Input getEnd()
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
  public Input getBilanz()
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
  public Input getAusgaben()
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
  public Input getEinnahmen()
  {
    if (this.einnahmen != null)
      return this.einnahmen;
    einnahmen = new LabelInput("");
    einnahmen.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    return einnahmen;
  }

  /**
   * Liefert eine Liste der zu synchronisierenden Konten.
   * @return Liste der zu synchroinisierenden Konten.
   * @throws RemoteException
   */
  public Part getKontoList() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Konto.class);
    list.addFilter("synchronize = 1 or synchronize is null");
    KontoList l = new KontoList(list,new KontoNew());
    // BUGZILLA 108 http://www.willuhn.de/bugzilla/show_bug.cgi?id=108
    l.addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
    l.setSummary(false);
    
    return l;
  }
  
  /**
   * Liefert eine Checkbox zur Aktivierung der Synchronisierung der Dauerauftraege.
   * @return Checkbox.
   */
  public CheckboxInput getSyncDauer()
  {
    if (this.syncDauer != null)
      return this.syncDauer;
    this.syncDauer = new CheckboxInput(settings.getBoolean("sync.dauer",true));
    return this.syncDauer;
  }

  /**
   * Liefert eine Checkbox zur Aktivierung der Synchronisierung der Ueberweisungen.
   * @return Checkbox.
   */
  public CheckboxInput getSyncUeb()
  {
    if (this.syncUeb != null)
      return this.syncUeb;
    this.syncUeb = new CheckboxInput(settings.getBoolean("sync.ueb",true));
    return this.syncUeb;
  }
  
  /**
   * Liefert eine Checkbox zur Aktivierung der Synchronisierung der Lastschriften.
   * @return Checkbox.
   */
  public CheckboxInput getSyncLast()
  {
    if (this.syncLast != null)
      return this.syncLast;
    this.syncLast = new CheckboxInput(settings.getBoolean("sync.last",true));
    return this.syncLast;
  }

  /**
   * Startet die Synchronisierung der Konten.
   */
  public void handleStart()
  {
    try
    {
      Logger.info("Start synchronize");
      boolean dauer = ((Boolean)getSyncDauer().getValue()).booleanValue();
      boolean last  = ((Boolean)getSyncLast().getValue()).booleanValue();
      boolean ueb   = ((Boolean)getSyncUeb().getValue()).booleanValue();
      settings.setAttribute("sync.dauer",dauer);
      settings.setAttribute("sync.last",last);
      settings.setAttribute("sync.ueb",ueb);
      
      HBCISynchronizer sync = new HBCISynchronizer();
      sync.start();
    }
    catch (Throwable t)
    {
      Logger.error("error while synchronizing",t);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Synchronisieren der Konten"));
    }
    finally
    {
      Logger.info("Synchronize finished");
    }
  }

}


/**********************************************************************
 * $Log: WelcomeControl.java,v $
 * Revision 1.23  2005/11/07 18:51:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2005/10/17 13:44:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2005/10/17 13:01:59  willuhn
 * @N Synchronize auf Start-Seite verschoben
 * @N Gesamt-Vermoegensuebersicht auf Start-Seite
 *
 * Revision 1.20  2005/06/21 20:25:10  web0
 * *** empty log message ***
 *
 * Revision 1.19  2005/06/17 16:12:55  web0
 * *** empty log message ***
 *
 * Revision 1.18  2005/06/17 16:09:57  web0
 * *** empty log message ***
 *
 * Revision 1.17  2005/03/31 23:05:46  web0
 * @N geaenderte Startseite
 * @N klickbare Links
 *
 * Revision 1.16  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.15  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.13  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.11  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.9  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.8  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/20 23:31:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.5  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/19 22:53:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.1  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 **********************************************************************/