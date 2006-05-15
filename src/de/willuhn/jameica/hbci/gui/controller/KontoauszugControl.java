/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KontoauszugControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/05/15 20:12:38 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.reports.KontoauszugReport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Kontoauszug-Report
 */
public class KontoauszugControl extends AbstractControl
{

  private SelectInput kontoAuswahl = null;

  private DialogInput start = null;

  private DialogInput end = null;

  private Date dStart = null;

  private Date dEnd = null;

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

    DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(
        Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    this.kontoAuswahl = new SelectInput(it, null);
    this.kontoAuswahl.setAttribute("longname");
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
   */
  public void startReport()
  {
    try
    {
      Konto k = (Konto) getKontoAuswahl().getValue();
      if (k == null)
        Application.getMessagingFactory().sendMessage(
            new StatusBarMessage(i18n.tr("Bitte wählen Sie ein Konto aus"),
                StatusBarMessage.TYPE_ERROR));

      Settings settings = new Settings(this.getClass());
      String dir = settings.getString("lastdir", System
          .getProperty("user.home"));

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd
          .setText(i18n
              .tr("Bitte wählen Sie das Verzeichnis, in dem Sie die Auswertung speichern möchten"));
      fd.setFilterPath(dir);
      fd.setFileName(i18n.tr("konto_{0}_{1}-{2}.pdf", new String[] {
          k.getKontonummer(), HBCI.FASTDATEFORMAT.format(dStart),
          HBCI.FASTDATEFORMAT.format(dEnd) }));

      String file = fd.open();

      if (file == null)
      {
        // Dialog abgebrochen
        Logger.info("operation cancelled");
        return;
      }
      File f = new File(file);

      // Wir merken uns das letzte ausgewaehlte Verzeichnis
      settings.setAttribute("lastdir", f.getParent());
      if (f.exists())
      {
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        d.setTitle(i18n.tr("Datei existiert bereits"));
        d.setText(i18n.tr(
            "Die Datei \"{0}\" existiert bereits. Überschreiben?", f
                .getAbsolutePath()));
        if (!((Boolean) d.open()).booleanValue())
          return;
      }

      KontoauszugReport rpt = new KontoauszugReport(HBCI.DATEFORMAT
          .format(dStart)
          + " - " + HBCI.DATEFORMAT.format(dEnd));

      try
      {
        rpt.open(f.getAbsolutePath());
        rpt.generate(k, k.getUmsaetze(dStart, dEnd));
      }
      finally
      {
        rpt.close();
      }

      // Zugeordnetes Programm starten (PDF-Viewer)
      new Program().handleAction(f);
    }
    catch (Exception e)
    {
      Application.getMessagingFactory().sendMessage(
          new StatusBarMessage(i18n.tr("Fehler beim Erstellen der Auswertung"),
              StatusBarMessage.TYPE_ERROR));
      Logger.error("unable to create report", e);
    }
    Application.getMessagingFactory().sendMessage(
        new StatusBarMessage(i18n.tr("Auswertung erstellt"),
            StatusBarMessage.TYPE_SUCCESS));
  }

}

/*******************************************************************************
 * $Log: KontoauszugControl.java,v $
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
