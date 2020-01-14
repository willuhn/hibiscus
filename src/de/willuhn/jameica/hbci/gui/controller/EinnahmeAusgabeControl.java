/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TreePart;
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
import de.willuhn.jameica.hbci.gui.parts.EinnahmenAusgabenVerlauf;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabeTreeNode;
import de.willuhn.jameica.hbci.server.KontoUtil;
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
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  private KontoInput kontoAuswahl  = null;
  private CheckboxInput onlyActive = null;
  private DateInput start          = null;
  private DateInput end            = null;
  private RangeInput range         = null;
  private SelectInput interval     = null;

  private List<EinnahmeAusgabeZeitraum> werte = null;
  private TreePart tree            = null;
  private EinnahmenAusgabenVerlauf chart = null;

  /**
   * Gruppierung der Einnahmen/Ausgaben nach Zeitraum.
   */
  private enum Interval
  {
    ALL(-1, -1, i18n.tr("Gesamtzeitraum")), 
    YEAR(Calendar.DAY_OF_YEAR, Calendar.YEAR,i18n.tr("Jahr")),
    MONTH(Calendar.DAY_OF_MONTH, Calendar.MONDAY, i18n.tr("Monat")),
    
    ;

    private String name;
    private int type;
    private int size;

    /**
     * ct.
     * @param type
     * @param size
     * @param name
     */
    private Interval(int type, int size, String name)
    {
      this.name = name;
      this.type = type;
      this.size = size;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
      return this.name;
    }
  }

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
   * Liefert die Checkbox, mit der eingestellt werden kann, ob nur aktive Konten angezeigt werden sollen.
   * @return Checkbox.
   */
  public CheckboxInput getActiveOnly()
  {
    if (this.onlyActive != null)
      return this.onlyActive;
    
    this.onlyActive = new CheckboxInput(settings.getBoolean("umsatzlist.filter.active",false));
    this.onlyActive.setName(i18n.tr("Nur aktive Konten"));
    return this.onlyActive;
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
   * Liefert ein Auswahl-Feld für die zeitliche Gruppierung.
   * @return Auswahl-Feld
   * */
  public SelectInput getInterval()
  {
    if(this.interval !=null)
      return this.interval;
    
    this.interval = new SelectInput(Interval.values(), Interval.MONTH);
    this.interval.setName(i18n.tr("Gruppierung nach"));
    return this.interval;
  }
  
  /**
   * Liefert ein Balkendiagramm bei dem Ausgaben und Einnahmen gegenübergestellt werden 
   * @return Balkendiagramm
   * @throws RemoteException 
   */
  public EinnahmenAusgabenVerlauf getChart() throws RemoteException
  {
    if(this.chart != null)
      return this.chart;
    
    this.chart = new EinnahmenAusgabenVerlauf(getWerte());
    return chart;
  }

  /**
   * Liefert eine Tabelle mit den Einnahmen/Ausgaben und Salden
   * @return Tabelle mit den Einnahmen/Ausgaben und Salden
   * @throws RemoteException
   */
  public TreePart getTree() throws RemoteException
  {
    if (this.tree != null)
      return this.tree;

    tree = new TreePart(getWerte(), null);
    tree.addColumn(i18n.tr("Konto"),        "text");
    tree.addColumn(i18n.tr("Anfangssaldo"), "anfangssaldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT), false, Column.ALIGN_RIGHT);
    tree.addColumn(i18n.tr("Einnahmen"),    "einnahmen",   new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT), false, Column.ALIGN_RIGHT);
    tree.addColumn(i18n.tr("Ausgaben"),     "ausgaben",    new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT), false, Column.ALIGN_RIGHT);
    tree.addColumn(i18n.tr("Endsaldo"),     "endsaldo",    new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT), false, Column.ALIGN_RIGHT);
    tree.addColumn(i18n.tr("Plus/Minus"),   "plusminus",   new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT), false, Column.ALIGN_RIGHT);
    tree.addColumn(i18n.tr("Differenz"),    "differenz",   new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE, HBCI.DECIMALFORMAT), false, Column.ALIGN_RIGHT);

    tree.setFormatter(new TreeFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TreeItem item)
      {
        if (item == null || item.getData() instanceof EinnahmeAusgabeTreeNode)
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
            Konto k = ea.getKonto();
            if (k != null && k.hasFlag(Konto.FLAG_DISABLED))
              item.setForeground(Color.COMMENT.getSWTColor());
            else
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

    tree.setRememberColWidths(true);
    return tree;
  }

  /**
   * Ermittelt die Liste der Knoten für den Baum. Wenn keine Aufschlüsselung gewünscht ist,
   * werden die Zeilen ohne Elternknoten angezeigt.
   * @return Liste mit den Werten.
   * @throws RemoteException
   */
  private List<EinnahmeAusgabeZeitraum> getWerte() throws RemoteException
  {
    if (this.werte != null)
      return this.werte;
    
    Date start  = (Date) this.getStart().getValue();
    Date end    = (Date) this.getEnd().getValue();

    Interval interval = (Interval) getInterval().getValue();
    this.werte = new ArrayList<EinnahmeAusgabeZeitraum>();
    
    // Sonderfall "alle". Es findet keine zeitliche Gruppierung statt
    if(Interval.ALL.equals(interval))
    {
      this.werte.addAll(this.getWerte(start, end));
      return this.werte;
    }
    
    EinnahmeAusgabeTreeNode node;
    if (start != null && end != null)
    {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(DateUtil.startOfDay(start));
      while (calendar.getTime().before(end))
      {
        calendar.set(interval.type, 1);
        Date nodeFrom = calendar.getTime();
        
        // ermittle den Zeipunkt unmittelbar vor dem nächsten Zeitraumstart
        calendar.add(interval.size,1);
        calendar.setTimeInMillis(calendar.getTime().getTime()-1);
        Date nodeTo = DateUtil.startOfDay(calendar.getTime());
        
        List<EinnahmeAusgabe> werte = this.getWerte(nodeFrom, nodeTo);
        node = new EinnahmeAusgabeTreeNode(nodeFrom, nodeTo, werte);
        this.werte.add(node);
        
        // ermittle den Start des nächsten Zeitraums
        calendar.setTime(nodeFrom);
        calendar.add(interval.size, 1);
      }
    }
    else
    {
      node = new EinnahmeAusgabeTreeNode(new Date(), new Date(), new ArrayList<EinnahmeAusgabe>());
      this.werte.add(node);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Kein Zeitraum ausgewählt"),StatusBarMessage.TYPE_INFO));
    }
    return this.werte;
  }

  /**
   * Liefert die Werte fuer den angegebenen Zeitraum.
   * @param start Startdatum.
   * @param end Enddatum.
   * @return die Liste der Werte.
   * @throws RemoteException
   */
  private List<EinnahmeAusgabe> getWerte(Date start, Date end) throws RemoteException
  {
    List<EinnahmeAusgabe> list = new ArrayList<EinnahmeAusgabe>();

    Object o = getKontoAuswahl().getValue();

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
    
    boolean onlyActive = ((Boolean)this.getActiveOnly().getValue()).booleanValue();
    settings.setAttribute("umsatzlist.filter.active",onlyActive);
    
    String group = o != null && (o instanceof String) ? (String) o : null;

    List<Konto> konten = KontoUtil.getKonten(onlyActive ? KontoFilter.ACTIVE : KontoFilter.ALL);
    for (Konto k:konten)
    {
      // Einschraenken auf gewaehlte Kontogruppe
      if (group != null && !ObjectUtils.equals(k.getKategorie(),group))
        continue;
      
      EinnahmeAusgabe ea = new EinnahmeAusgabe(k,start,end);
      
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
    summen.setAnfangssaldo(summeAnfangssaldo);
    summen.setAusgaben(summeAusgaben);
    summen.setEinnahmen(summeEinnahmen);
    summen.setEndsaldo(summeEndsaldo);
    summen.setEnddatum(start);
    summen.setStartdatum(end);
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
      TreePart tree = this.getTree();
      tree.removeAll();
      this.werte = null;
      
      Date tStart = (Date) getStart().getValue();
      Date tEnd = (Date) getEnd().getValue();
      if (tStart != null && tEnd != null && tStart.after(tEnd))
      {
        GUI.getView().setErrorText(i18n.tr("Das Anfangsdatum muss vor dem Enddatum liegen"));
        return;
      }
      if (tStart == null || tEnd == null)
      {
        // bei einem offenen Intervall ist keine Aufschlüsselung möglich
        getInterval().setValue(Interval.ALL);
      }

      tree.setList(this.getWerte());
      
      EinnahmenAusgabenVerlauf chart = getChart();
      chart.setList(this.getWerte());
    }
    catch (RemoteException re)
    {
      Logger.error("unable to redraw table",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren"), StatusBarMessage.TYPE_ERROR));
    }
    
  }
}
