/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KontoauszugControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/05/14 19:52:13 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.lowagie.text.DocumentException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.reports.KontoauszugReport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen.
 */
public class KontoauszugControl extends AbstractControl
{

  private SelectInput kontoAuswahl = null;

  private DialogInput start = null;

  private DialogInput end = null;

  private Date dStart = null;

  private Date dEnd = null;

  private Konto konto = null;

  private I18N i18n = null;

  /**
   * @param view
   */
  public KontoauszugControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources()
        .getI18N();
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * 
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    Konto k = (Konto) de.willuhn.jameica.hbci.Settings.getDBService()
        .createObject(Konto.class, null);
    DBIterator it = k.getList();
    it.setOrder("ORDER BY blz, kontonummer");
    this.kontoAuswahl = new SelectInput(it, null);
    this.kontoAuswahl.setAttribute("longname");
    this.kontoAuswahl.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        konto = (Konto) kontoAuswahl.getValue();
      }
    });

    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * 
   * @return Auswahl-Feld.
   */
  public Input getStart()
  {
    if (this.start != null)
      return this.start;

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.DAY_OF_MONTH, 1);
    this.dStart = cal.getTime();

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
        dStart = (Date) event.data;
        start.setValue(dStart);
        start.setText(HBCI.DATEFORMAT.format(dStart));
      }
    });
    this.start = new DialogInput(HBCI.DATEFORMAT.format(dStart), d);
    this.start.setValue(dStart);
    ((DialogInput) this.start).disableClientControl();
    return this.start;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * 
   * @return Auswahl-Feld.
   */
  public Input getEnd()
  {
    if (this.end != null)
      return this.end;

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    this.dEnd = cal.getTime();

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
        dEnd = (Date) event.data;
        end.setValue(dEnd);
        end.setText(HBCI.DATEFORMAT.format(dEnd));
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
   * @throws RemoteException
   */
  public void startReport()
  {
    GUI.getStatusBar().setSuccessText(i18n.tr("Report gestartet."));

    try
    {
      if (konto == null)
      {
        Konto k = (Konto) de.willuhn.jameica.hbci.Settings.getDBService()
            .createObject(Konto.class, null);
        DBIterator it = k.getList();
        it.setOrder("ORDER BY blz, kontonummer");
        konto = (Konto) it.next();
      }
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }

    try
    {
      KontoauszugReport rpt = new KontoauszugReport(HBCI.DATEFORMAT
          .format(dStart)
          + " - " + HBCI.DATEFORMAT.format(dEnd));
      rpt.open(System.getProperty("user.home") + "/bla.pdf");
      rpt.generate(this.konto.getUmsaetze(dStart, dEnd));
      rpt.close();
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (DocumentException e)
    {
      e.printStackTrace();
    }

    GUI.getStatusBar().setSuccessText(i18n.tr("Report erstellt."));
  }

}

/*******************************************************************************
 * $Log: KontoauszugControl.java,v $
 * Revision 1.1  2006/05/14 19:52:13  jost
 * Prerelease Kontoauszug-Report
 * 
 ******************************************************************************/
