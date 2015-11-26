/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
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
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.messaging.StatusBarMessage;
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

  private KontoInput kontoAuswahl  = null;
  private DateInput start          = null;
  private DateInput end            = null;
  private RangeInput range         = null;

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
    this.kontoAuswahl.setSupportGroups(true);
    this.kontoAuswahl.setComment(null);
    this.kontoAuswahl.setRememberSelection("auswertungen.einnahmeausgabe");
    
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
    this.start.setName(i18n.tr("Von"));
    this.start.setComment(null);
    return this.start;
  }
  
  /**
   * Liefert eine Auswahl mit Zeit-Presets.
   * @return eine Auswahl mit Zeit-Presets.
   */
  public RangeInput getRange()
  {
    if (this.range != null)
      return this.range;
    
    this.range = new RangeInput(this.getStart(),this.getEnd(),"umsatzlist.filter.range");
    return this.range;
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
    this.end.setName(i18n.tr("bis"));
    this.end.setComment(null);
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
            item.setForeground(Color.FOREGROUND.getSWTColor());
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

    Date start  = (Date) this.getStart().getValue();
    Date end    = (Date) this.getEnd().getValue();
    Object o    = getKontoAuswahl().getValue();

    // Uhrzeit zuruecksetzen, falls vorhanden
    if (start != null) start = DateUtil.startOfDay(start);
    if (end != null) end = DateUtil.startOfDay(end);

    // Wird nur ein Konto ausgewertet?
    if (o != null && (o instanceof Konto))
    {
      list.add(new EinnahmeAusgabe((Konto) o,start,end));
      return list;
    }
    
    // Alle Konten
    double summeAnfangssaldo = 0.0d;
    double summeEinnahmen    = 0.0d;
    double summeAusgaben     = 0.0d;
    double summeEndsaldo     = 0.0d;
    
    DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    // Einschraenken auf gewaehlte Kontogruppe
    if (o != null && (o instanceof String))
      it.addFilter("kategorie = ?", (String) o);
    it.setOrder("ORDER BY LOWER(kategorie), blz, kontonummer, bezeichnung");
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
   */
  public void handleReload()
  {
    try
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

      List<EinnahmeAusgabe> list = this.getWerte();
      for (EinnahmeAusgabe ea:list)
        table.addItem(ea);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to redraw table",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren"), StatusBarMessage.TYPE_ERROR));
    }
  }
}
