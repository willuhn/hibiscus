/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KontoauszugControl.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/08/23 09:45:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Kontoauszug-Report
 */
public class KontoauszugControl extends AbstractControl
{

  private SelectInput kontoAuswahl = null;
  private DialogInput start        = null;
  private DialogInput end          = null;

  private I18N i18n = null;

  /**
   * ct.
   * @param view
   */
  public KontoauszugControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    this.kontoAuswahl = new SelectInput(it, null);
    this.kontoAuswahl.setAttribute("longname");
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    return this.kontoAuswahl;
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
    cal.set(Calendar.DAY_OF_MONTH, 1);
    Date dStart = cal.getTime();

    CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    d.setTitle(i18n.tr("Start-Datum"));
    d.setDate(dStart);
    d.setText(i18n.tr("Bitte wählen Sie das Start-Datum"));
    d.addCloseListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        Date d = (Date) event.data;
        start.setValue(d);
        start.setText(HBCI.DATEFORMAT.format(d));
      }
    });
    this.start = new DialogInput(HBCI.DATEFORMAT.format(dStart), d);
    this.start.setValue(dStart);
    ((DialogInput) this.start).disableClientControl();
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
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    Date dEnd = cal.getTime();

    CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    d.setDate(dEnd);
    d.setTitle(i18n.tr("End-Datum"));
    d.setText(i18n.tr("Bitte wählen Sie das End-Datum"));
    d.addCloseListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        Date d = (Date) event.data;
        end.setValue(d);
        end.setText(HBCI.DATEFORMAT.format(d));
      }
    });
    this.end = new DialogInput(HBCI.DATEFORMAT.format(dEnd), d);
    this.end.setValue(dEnd);
    ((DialogInput) this.end).disableClientControl();

    return this.end;
  }

  /**
   * Startet den Report
   * 
   */
  public synchronized void handleStart()
  {
    try
    {
      Konto k    = (Konto) getKontoAuswahl().getValue();
      Date start = (Date) getStart().getValue();
      Date end   = (Date) getEnd().getValue();

      DBIterator umsaetze = null;
      
      if (k == null)
      {
        // Alle Konten
        umsaetze = Settings.getDBService().createList(Umsatz.class);
        // TODO: Auf PreparedStatement umstellen
        if (start != null) umsaetze.addFilter("TONUMBER(valuta) >= " + start.getTime());
        if (end != null) umsaetze.addFilter("TONUMBER(valuta) <= " + end.getTime());
        umsaetze.setOrder("ORDER BY TONUMBER(valuta), id DESC");
      }
      else if (start == null || end == null)
      {
        umsaetze = k.getUmsaetze();
      }
      else
      {
        umsaetze = k.getUmsaetze(start,end);
      }
      

      ArrayList list = new ArrayList();
      while (umsaetze.hasNext())
      {
        list.add(umsaetze.next());
      }
      
      Umsatz[] u = (Umsatz[]) list.toArray(new Umsatz[list.size()]);
      
      if (u == null || u.length == 0)
        throw new ApplicationException(i18n.tr("Im gewählten Zeitraum wurden keine Umsätze gefunden"));
      
      new UmsatzExport().handleAction(u);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to create report", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erstellen der Auswertung"),StatusBarMessage.TYPE_ERROR));
    }
  }

}

/*******************************************************************************
 * $Log: KontoauszugControl.java,v $
 * Revision 1.5  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.4  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 * Revision 1.3  2006/05/15 20:12:38  jost
 * Zusätzlicher Parameter beim Aufruf des Kontoauszug-Reports
 * Kommentare
 * Revision 1.2 2006/05/15 12:05:22 willuhn
 * 
 * @N FileDialog zur Auswahl von Pfad und Datei beim Speichern
 * @N YesNoDialog falls Datei bereits existiert
 * @C KontoImpl#getUmsaetze mit tonumber() statt dateob()
 * 
 * Revision 1.1 2006/05/14 19:52:13 jost Prerelease Kontoauszug-Report
 * 
 ******************************************************************************/
