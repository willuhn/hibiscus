/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/EinnahmeAusgabeControl.java,v $
 * $Revision: 1.21 $
 * $Date: 2012/04/23 21:03:41 $
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
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

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
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien-Auswertung
 */
public class EinnahmeAusgabeControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private SelectInput kontoAuswahl = null;
  private DateInput start          = null;
  private DateInput end            = null;

  private TablePart table          = null;

  /**
   * ct.
   * @param view
   */
  public EinnahmeAusgabeControl(AbstractView view)
  {
    super(view);
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

    this.kontoAuswahl = new KontoInput(null,KontoFilter.ALL);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoAuswahl.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          handleReload();
        }
        catch (RemoteException e)
        {
          Logger.error("error while reloading table",e);
        }
      }
    });
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

    this.start = new DateFromInput(null,"umsatzlist.filter.from");
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

    this.end = new DateToInput(null,"umsatzlist.filter.to");
    return this.end;
  }

  /**
   * Liefert eine Tabelle mit den Einnahmen/Ausgaben und Salden
   * @return Tabelle mit den Einnahmen/Ausgaben und Salden
   * @throws RemoteException
   */
  public TablePart getTable() throws RemoteException
  {
    if (this.table != null)
      return this.table;

    table = new TablePart(getWerte(), null);
    table.addColumn(i18n.tr("Konto"),        "text");
    table.addColumn(i18n.tr("Anfangssaldo"), "anfangssaldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Einnahmen"),    "einnahmen",   new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Ausgaben"),     "ausgaben",    new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Endsaldo"),     "endsaldo",    new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Plus/Minus"),   "plusminus",   new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Differenz"),    "differenz",   new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT));

    table.setFormatter(new TableFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TableItem item)
      {
        if (item == null)
          return;
        
        EinnahmeAusgabe ea = (EinnahmeAusgabe) item.getData();
        boolean summe = ea.isSumme();
        try
        {
          double plusminus = ea.getPlusminus();
          if (summe)
          {
            item.setForeground(Color.WIDGET_FG.getSWTColor());
          }
          else
          {
            item.setForeground(ColorUtil.getForeground(plusminus));
            item.setFont(ea.hasDiff() && !summe ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          }
          
        }
        catch (Exception e)
        {
          Logger.error("unable to format line", e);
        }
      }
    });

    table.setRememberColWidths(true);
    table.setSummary(false);
    return table;
  }

  /**
   * Ermittelt die Liste der Zeilen fuer die Tabelle.
   * @return Liste mit den Werten.
   * @throws RemoteException
   */
  private List<EinnahmeAusgabe> getWerte() throws RemoteException
  {
    List<EinnahmeAusgabe> list = new ArrayList<EinnahmeAusgabe>();

    Konto konto = (Konto) getKontoAuswahl().getValue();
    Date start  = (Date) this.getStart().getValue();
    Date end    = (Date) this.getEnd().getValue();
    
    // Uhrzeit zuruecksetzen, falls vorhanden
    if (start != null) start = DateUtil.startOfDay(start);
    if (end != null) end = DateUtil.startOfDay(end);

    // Wird nur ein Konto ausgewertet?
    if (konto != null)
    {
      list.add(new EinnahmeAusgabe(konto,start,end));
      return list;
    }
    
    // Alle Konten
    double summeAnfangssaldo = 0.0d;
    double summeEinnahmen    = 0.0d;
    double summeAusgaben     = 0.0d;
    double summeEndsaldo     = 0.0d;
    
    DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    while (it.hasNext())
    {
      EinnahmeAusgabe ea = new EinnahmeAusgabe((Konto) it.next(),start,end);
      
      // Zu den Summen hinzufuegen
      summeAnfangssaldo += ea.getAnfangssaldo();
      summeEinnahmen    += ea.getEinnahmen();
      summeAusgaben     += ea.getAusgaben();
      summeEndsaldo     += ea.getEndsaldo();
      list.add(ea);
    }
    
    // Summenzeile noch hinten dran haengen
    EinnahmeAusgabe summen = new EinnahmeAusgabe();
    summen.setIsSumme(true);
    summen.setText(i18n.tr("Summe"));
    summen.setAnfangssaldo(summeAnfangssaldo);
    summen.setAusgaben(summeAusgaben);
    summen.setEinnahmen(summeEinnahmen);
    summen.setEndsaldo(summeEndsaldo);
    summen.setEnddatum((Date) this.getStart().getValue());
    summen.setStartdatum((Date) this.getEnd().getValue());
    list.add(summen);
    
    return list;
  }

  /**
   * Aktualisiert die Tabelle.
   * @throws RemoteException
   */
  public void handleReload() throws RemoteException
  {
    TablePart table = this.getTable();
    table.removeAll();
    
    Date tStart = (Date) getStart().getValue();
    Date tEnd = (Date) getEnd().getValue();
    if (tStart != null && tEnd != null && tStart.after(tEnd))
    {
      GUI.getView().setErrorText(i18n.tr("Das Anfangsdatum muss vor dem Enddatum liegen"));
      return;
    }
    GUI.getView().setErrorText(""); // ggf. vorher angezeigten Fehler loeschen

    List<EinnahmeAusgabe> list = this.getWerte();
    for (EinnahmeAusgabe ea:list)
      table.addItem(ea);
  }
}

/*******************************************************************************
 * $Log: EinnahmeAusgabeControl.java,v $
 * Revision 1.21  2012/04/23 21:03:41  willuhn
 * @N BUGZILLA 1227
 *
 * Revision 1.20  2011/12/18 23:20:20  willuhn
 * @N GUI-Politur
 *
 * Revision 1.19  2011-08-05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.18  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.17  2010-08-24 17:38:04  willuhn
 * @N BUGZILLA 896
 *
 * Revision 1.16  2010/06/07 22:41:13  willuhn
 * @N BUGZILLA 844/852
 *
 * Revision 1.15  2010/04/09 09:31:03  willuhn
 * @B BUGZILLA 844
 *
 * Revision 1.14  2010/02/17 10:43:41  willuhn
 * @N Differenz in Einnahmen/Ausgaben anzeigen, Cleanup
 *
 * Revision 1.13  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.12  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.11  2009/04/05 21:16:22  willuhn
 * @B BUGZILLA 716
 *
 * Revision 1.10  2009/01/12 00:46:50  willuhn
 * @N Vereinheitlichtes KontoInput in den Auswertungen
 ******************************************************************************/
