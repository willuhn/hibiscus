/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/EinnahmeAusgabeControl.java,v $
 * $Revision: 1.11 $
 * $Date: 2009/04/05 21:16:22 $
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
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.io.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien-Auswertung
 */
public class EinnahmeAusgabeControl extends AbstractControl
{

  private SelectInput kontoAuswahl = null;

  private DateInput start = null;

  private DateInput end = null;

  private I18N i18n = null;

  private TablePart table = null;

  private double summeAnfangssaldo = 0.0d;

  private double summeEinnahmen = 0.0d;

  private double summeAusgaben = 0.0d;

  private double summeEndsaldo = 0.0d;

  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(
      EinnahmeAusgabeControl.class);

  static
  {
    settings.setStoreWhenRead(true);
  }

  /**
   * ct.
   * 
   * @param view
   */
  public EinnahmeAusgabeControl(AbstractView view)
  {
    super(view);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();
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

    this.kontoAuswahl = new KontoInput(null);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoAuswahl.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event.type == SWT.Selection)
        {
          try
          {
            handleReload();
          }
          catch (RemoteException e)
          {
            //
          }
        }
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

    // Standardmaessig verwenden wir das aktuelle Jahr als Bemessungszeitraum
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DATE, 1);

    Date d = HBCIProperties.startOfDay(cal.getTime());
    try
    {
      String s = settings.getString("laststart", null);
      if (s != null && s.length() > 0)
      {
        d = HBCI.DATEFORMAT.parse(s);
      }
    }
    catch (Exception e)
    {
      // ignore
    }
    this.start = new DateInput(d, HBCI.DATEFORMAT);
    this.start.setComment(i18n.tr("Frühestes Valuta-Datum"));
    this.start.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event.type == SWT.FocusOut)
        {
          try
          {
            settings.setAttribute("laststart", HBCI.DATEFORMAT
                .format((Date) start.getValue()));
            handleReload();
          }
          catch (RemoteException e)
          {
          }
        }
      }
    });
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
    // cal.set(Calendar.MONTH,Calendar.DECEMBER);
    // cal.set(Calendar.DATE,31);

    Date d = HBCIProperties.endOfDay(cal.getTime());
    try
    {
      String s = settings.getString("lastend", null);
      if (s != null && s.length() > 0)
        d = HBCI.DATEFORMAT.parse(s);
    }
    catch (Exception e)
    {
      // ignore
    }
    this.end = new DateInput(d, HBCI.DATEFORMAT);
    this.end.setComment(i18n.tr("Spätestes Valuta-Datum"));
    this.end.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (event.type == SWT.FocusOut)
        {
          try
          {
            settings.setAttribute("lastend", HBCI.DATEFORMAT.format((Date) end
                .getValue()));
            handleReload();
          }
          catch (RemoteException e)
          {
          }
        }
      }
    });

    return this.end;
  }

  /**
   * Liefert eine Tabelle mit den Einnahmen/Ausgaben und Salden
   * 
   * @return Tabelle mit den Einnahmen/Ausgaben und Salden
   * @throws RemoteException
   */
  public TablePart getTable() throws RemoteException
  {
    if (this.table != null)
      return this.table;

    GenericIterator eintr = PseudoIterator
        .fromArray((GenericObject[]) getWerte());
    table = new TablePart(eintr, null);
    table.addColumn(i18n.tr("Text"), "text");
    table.addColumn(i18n.tr("Anfangssaldo"), "anfangssaldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Einnahmen"), "einnahme", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Ausgaben"), "ausgabe", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Endsaldo"), "endsaldo", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Bemerkung"), "bemerkung");

    table.setFormatter(new TableFormatter()
    {

      public void format(TableItem item)
      {
        if (item == null)
          return;
        EinnahmeAusgabe ea = (EinnahmeAusgabe) item.getData();
        try
        {
          Double einnahmen = (Double) ea.getAttribute("einnahme");
          Double ausgaben = (Double) ea.getAttribute("ausgabe");
          if (einnahmen == null || ausgaben == null)
            return;
          if (einnahmen.doubleValue() > ausgaben.doubleValue())
            item.setForeground(Settings.getBuchungHabenForeground());
          else if (einnahmen.doubleValue() < ausgaben.doubleValue())
            item.setForeground(Settings.getBuchungSollForeground());
          else
            item.setForeground(Color.WIDGET_FG.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("unable to format line", e);
        }
      }

    });

    table.setRememberColWidths(true);
    return table;
  }

  /**
   * Werte ermitteln.
   * 
   * @return Array mit Werten.
   * @throws RemoteException
   */
  public EinnahmeAusgabe[] getWerte() throws RemoteException
  {
    summeAnfangssaldo = 0.0d;
    summeEinnahmen = 0.0d;
    summeAusgaben = 0.0d;
    summeEndsaldo = 0.0d;

    EinnahmeAusgabe[] eae;

    Konto konto = (Konto) getKontoAuswahl().getValue();

    // Wird nur ein Konto ausgewertet?
    if (konto != null)
    {
      eae = new EinnahmeAusgabe[1];
      ermittelnWerte(konto, eae, 0);
    }
    else
    {
      DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
      it.setOrder("ORDER BY blz, kontonummer");
      int index = 0;
      eae = new EinnahmeAusgabe[it.size() + 1];
      while (it.hasNext())
      {
        konto = (Konto) it.next();
        ermittelnWerte(konto, eae, index);
        index++;
      }
      eae[it.size()] = new EinnahmeAusgabe("Summe", (Date) this.start.getValue(), summeAnfangssaldo, summeEinnahmen, summeAusgaben,(Date) this.end.getValue(), summeEndsaldo);
    }
    return eae;
  }

  private void ermittelnWerte(Konto konto, EinnahmeAusgabe[] eae, int index)
      throws RemoteException
  {
    double anfangssaldo = konto.getAnfangsSaldo((Date) this.getStart().getValue());
    summeAnfangssaldo += anfangssaldo;

    double einnahmen = konto.getEinnahmen((Date) start.getValue(), (Date) end.getValue());
    summeEinnahmen += einnahmen;

    double ausgaben = konto.getAusgaben((Date) start.getValue(), (Date) end.getValue());
    summeAusgaben += ausgaben;

    double endsaldo = konto.getEndSaldo((Date) end.getValue());
    summeEndsaldo += endsaldo;

    eae[index] = new EinnahmeAusgabe(konto.getLongName(), (Date) this.start
        .getValue(), anfangssaldo, einnahmen, ausgaben, (Date) this.getEnd()
        .getValue(), endsaldo);
  }

  /**
   * Aktualisiert die Tabelle.
   * @throws RemoteException
   */
  public void handleReload() throws RemoteException
  {
    this.table.removeAll();
    Date t_start = (Date) start.getValue();
    Date t_end = (Date) end.getValue();
    if (t_start != null && t_end != null && t_start.after(t_end))
    {
      GUI.getView().setErrorText(i18n.tr("Das Anfangsdatum muss vor dem Enddatum liegen"));
      return;
    }
    GUI.getView().setErrorText(""); // ggf. vorher angezeigten Fehler loeschen

    EinnahmeAusgabe[] eae = this.getWerte();
    for (int i = 0; i < eae.length; i++)
    {
      this.table.addItem((EinnahmeAusgabe) eae[i]);
    }
  }
}

/*******************************************************************************
 * $Log: EinnahmeAusgabeControl.java,v $
 * Revision 1.11  2009/04/05 21:16:22  willuhn
 * @B BUGZILLA 716
 *
 * Revision 1.10  2009/01/12 00:46:50  willuhn
 * @N Vereinheitlichtes KontoInput in den Auswertungen
 *
 * Revision 1.9  2007/08/12 22:02:10  willuhn
 * @C BUGZILLA 394 - restliche Umstellungen von Valuta auf Buchungsdatum
 *
 * Revision 1.8  2007/08/07 12:04:43  willuhn
 * @C Spalte "Bemerkung" wieder eingefuegt
 *
 * Revision 1.7  2007/07/17 12:36:44  willuhn
 * @C Spalte "Bemerkung" fuer RELEASE auskommentiert. Deren Bedeutung verstehen die User irgendwie nicht - hatte schon mehrere Anfragen von Usern erhalten. Daher entferne ich sie fuer das Release und nehm sie danach wieder rein ;)
 *
 * Revision 1.6  2007/07/16 12:01:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2007/06/19 17:55:09  jost
 * Sichtbarkeit der Members eingeschrÃ¤nkt.
 *
 * Revision 1.4  2007/06/05 08:40:06  willuhn
 * @C Fehlermeldung via Dialog gegen Hinweistext in Title ersetzt
 *
 * Revision 1.3  2007/06/04 18:35:28  jost
 * ZusÃ¤tzliche Plausi: Startdatum muÃŸ vor Endedatum liegen.
 * Revision 1.2 2007/06/04 17:37:00
 * willuhn
 * 
 * @D javadoc
 * @C java 1.4 compatibility
 * @N table colorized
 * 
 * Revision 1.1 2007/06/04 15:58:00 jost Neue Auswertung: Einnahmen/Ausgaben
 * 
 ******************************************************************************/
