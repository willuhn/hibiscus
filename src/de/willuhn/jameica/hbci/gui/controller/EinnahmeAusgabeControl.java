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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
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
import de.willuhn.jameica.hbci.report.balance.AccountBalanceProvider;
import de.willuhn.jameica.hbci.report.balance.AccountBalanceService;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabeTreeNode;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
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
    YEAR(Calendar.DAY_OF_YEAR, Calendar.YEAR, i18n.tr("Jahr")),
    MONTH(Calendar.DAY_OF_MONTH, Calendar.MONTH, i18n.tr("Monat")),
    
    ;

    private String name;
    /**
     * Typ des Tages zur Adressierung innerhalb des Intervalls
     */
    private int type;
    /**
     * Intervalldauer
     */
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

    this.start = new DateFromInput(null, "auswertungen.einnahmeausgabe.filter.from");
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
    
    this.onlyActive = new CheckboxInput(settings.getBoolean("auswertungen.einnahmeausgabe.filter.active",false));
    this.onlyActive.setName(i18n.tr("Nur aktive Konten"));
    this.onlyActive.addListener(new org.eclipse.swt.widgets.Listener() {

      @Override
      public void handleEvent(Event event)
      {
        settings.setAttribute("auswertungen.einnahmeausgabe.filter.active", (Boolean) onlyActive.getValue());
      }
    });
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
    
    this.range = new RangeInput(this.getStart(), this.getEnd(), Range.CATEGORY_AUSWERTUNG, "auswertungen.einnahmeausgabe.filter.range");
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

    this.end = new DateToInput(null, "auswertungen.einnahmeausgabe.filter.to");
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
    if (this.interval != null)
      return this.interval;

    this.interval = new SelectInput(Interval.values(), Interval.valueOf(settings.getString("auswertungen.einnahmeausgabe.filter.interval", "MONTH")));
    this.interval.setName(i18n.tr("Gruppierung nach"));
    this.interval.addListener(new Listener() {

      @Override
      public void handleEvent(Event event)
      {
        Interval value = (Interval) interval.getValue();
        settings.setAttribute("auswertungen.einnahmeausgabe.filter.interval", value.name());
      }

    });
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
    {
      return this.werte;
    }

    Date start = (Date) this.getStart().getValue();
    Date end = (Date) this.getEnd().getValue();

    List<Konto> konten = getSelectedAccounts();
    List<Umsatz> umsatzList = getUmsaetze(konten, start, end);
    if (!umsatzList.isEmpty())
    // bei offenen Zeiträumen können wir den ersten und letzten Umsatztag ermitteln
    {
      if (start == null)
      {
        start = umsatzList.get(0).getDatum();
      }
      if (end == null)
      {
        end = umsatzList.get(umsatzList.size() - 1).getDatum();
      }
    }
    Map<String, List<Value>> saldenProKonto = getSaldenProKonto(konten, start, end);
    
    // wenn die Umsatzliste leer ist, erfolgt keine Gruppierung, es wird nur der Gesamtzeitraum
    // ausgewertet und da keine Umsätze zugeordnet werden müssen, spielen fehlende Datumsangaben keine Rolle
    Interval interval = umsatzList.isEmpty() ? Interval.ALL : (Interval) getInterval().getValue();
    List<EinnahmeAusgabeTreeNode> nodes = createEmptyNodes(start, end, konten, interval);

    this.werte = new ArrayList<EinnahmeAusgabeZeitraum>();
    if (nodes.isEmpty())
    {
      Logger.warn("no nodes created between range starts on " + start + " and range ends on " + end);
      return this.werte;
    }
    addData(nodes, umsatzList, saldenProKonto, start, end);

    if (interval == Interval.ALL)
    {
      // Es gibt nur einen Zweig - da reichen uns die darunterliegenden Elemente
      this.werte.addAll(getChildren(nodes.get(0)));
    } else
    {
      this.werte.addAll(nodes);
    }
    return this.werte;
  }

  private List<Umsatz> getUmsaetze(List<Konto> konten, Date start, Date end) throws RemoteException
  {
    final DBIterator umsaetze = UmsatzUtil.getUmsaetze();
    if (start != null)
    {
      umsaetze.addFilter("datum >= ?", new java.sql.Date(DateUtil.startOfDay(start).getTime()));
    }
    if (end != null)
    {
      umsaetze.addFilter("datum <= ?", new java.sql.Date(DateUtil.endOfDay(end).getTime()));
    }

    if (konten != null && !konten.isEmpty())
    {
      final List<String> kontoIds = new ArrayList<String>();
      for (Konto konto : konten)
      {
        kontoIds.add(konto.getID());
      }
      umsaetze.addFilter("konto_id in (" + StringUtils.join(kontoIds,",") + ")");
    }

    final List<Umsatz> umsatzList = new ArrayList<Umsatz>();
    while (umsaetze.hasNext())
    {
      final Umsatz u = (Umsatz) umsaetze.next();
      if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
        continue;

      umsatzList.add(u);
    }
    return umsatzList;
  }

  /**
   * Liefert die Salden pro Konto für den angegebenen Zeitraum.
   * Zu beachten ist, dass ein Tagessaldo immer am Ende eines Tages berechnet wird. 
   * Da für Auswertungen ein Anfangssaldo angezeigt werden soll, welcher der Endsaldo des vorhergehenden Tages ist,
   * wird als erstes Element der Liste ein zusätzlicher Tag eingefügt.
   * 
   * Beispiel: start=7.11., end=8.11. -> Liste enthält 6.11., 7.11., 8.11.
   * @param konten
   * @param start
   * @param end
   * @return
   * @throws RemoteException
   */
  private Map<String, List<Value>> getSaldenProKonto(List<Konto> konten, Date start, Date end) throws RemoteException
  {
    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final AccountBalanceService balanceService = bs.get(AccountBalanceService.class);
    Map<String, List<Value>> saldenProKonto = new HashMap<String, List<Value>>();
    if ((start == null) || (end == null))
    {
      return saldenProKonto;
    }
    
    final Calendar cal = Calendar.getInstance();
    cal.setTime(start);
    cal.add(Calendar.DAY_OF_MONTH, -1); // Salden um einen Tag nach vorne verlängern, weil die Salden immer nur für das Ende eines Tages berechnet werden
    Date saldoStart = cal.getTime();
    for (Konto konto : konten)
    {
      AccountBalanceProvider balanceProvider = balanceService.getBalanceProviderForAccount(konto);
      List<Value> balance = balanceProvider.getBalanceData(konto, saldoStart, end);
      saldenProKonto.put(konto.getID(), balance);
    }
    return saldenProKonto;
  }
  
  private void addData(List<EinnahmeAusgabeTreeNode> nodes, List<Umsatz> umsatzList, Map<String, List<Value>> saldoProKonto, Date start, Date end) throws RemoteException
  {
    int index = 0;
    EinnahmeAusgabeTreeNode currentNode = null;
    // Map der Daten für eine Konto-ID für schnelles Zuweisen der Umsätze
    Map<String, EinnahmeAusgabe> kontoData = null;
    for (Umsatz umsatz : umsatzList)
    {
      // Daten für das nächste relevante Intervall vorbereiten; 'while' da es möglich wäre, dass es für einen Zeitraum in der Mitte gar keine Umsätze gab
      while (currentNode == null || umsatz.getDatum().after(currentNode.getEnddatum()))
      {
        // Wenn der Filterzeitraum identisches Start- und Enddatum hat, es an diesem Tag
        // aber einen Umsatz auf dem gewählten Konto gibt, so bekam man eine leere Liste nodes.
        // Ggf. gibt es weitere Fälle, die eine IndexOutOfBoundsException auslösen können.
        // Daher ist Prüfung des index erforderlich.
        if (index >= nodes.size())
        {
          Date endInterval = (currentNode != null ? currentNode.getEnddatum() : null);
          Logger.warn("found umsatz at date " + umsatz.getDatum() + " outside last interval ending at " + endInterval);
          return;
        }
        
        currentNode = nodes.get(index++);
        kontoData = getKontoDataMap(currentNode);
      }

      EinnahmeAusgabe ea = kontoData.get(umsatz.getKonto().getID());
      ea.addUmsatz(umsatz);
    }
    
    // Salden eintragen
    int tagStart = 0; // Tag in der Liste der Salden
    for(EinnahmeAusgabeTreeNode node : nodes)
    {
      Map<String, EinnahmeAusgabe> kontoMap = getKontoDataMap(node);
      
      final Date startDatum = (start != null && tagStart == 0 ? start : node.getStartdatum());
      final Date endDatum = (end != null && end.before(node.getEnddatum()) ? end : node.getEnddatum());
      int tagEnde = tagStart + (int) getDifferenceDays(startDatum, endDatum) + 1;
      for (Entry<String, EinnahmeAusgabe> kontoEntry : kontoMap.entrySet())
      {
        EinnahmeAusgabe ea = kontoEntry.getValue();
        List<Value> saldo = saldoProKonto.get(ea.getKonto().getID());
        if ((saldo == null) || saldo.isEmpty())
        {
          // sollte nicht passieren, aber sonst wird tagEnde im Folgenden negativ
          continue;
        }
        if (tagEnde >= saldo.size())
        {
          Logger.warn("Unexpected access to saldo, should pick day at index " + tagEnde + " but saldo has only " + saldo.size() + " days. Using last available day instead");
          tagEnde = saldo.size() - 1;
        }
          
        ea.setAnfangssaldo(saldo.get(tagStart).getValue());
        ea.setEndsaldo(saldo.get(tagEnde).getValue());
      }
      
      tagStart = tagEnde;
    }
    calculateSums(nodes);
  }

  private Map<String, EinnahmeAusgabe> getKontoDataMap(EinnahmeAusgabeTreeNode node) throws RemoteException
  {
    Map<String, EinnahmeAusgabe> kontoData = new HashMap<>();
    List<EinnahmeAusgabe> eaList = getChildren(node);
    for (EinnahmeAusgabe ea : eaList)
    {
      if (ea.getKonto() != null)
      {
        kontoData.put(ea.getKonto().getID(), ea);
      }
    }
    return kontoData;
  }
  
  private void calculateSums(List<EinnahmeAusgabeTreeNode> nodes) throws RemoteException
  {
    for (EinnahmeAusgabeTreeNode node : nodes)
    {
      List<EinnahmeAusgabe> list = getChildren(node);
      // Alle Konten
      double summeAnfangssaldo = 0.0d;
      double summeEinnahmen = 0.0d;
      double summeAusgaben = 0.0d;
      double summeEndsaldo = 0.0d;
      EinnahmeAusgabe sumElement = null;
      for (EinnahmeAusgabe ea : list)
      {
        if (!ea.isSumme())
        {
          summeAnfangssaldo += ea.getAnfangssaldo();
          summeEinnahmen += ea.getEinnahmen();
          summeAusgaben += ea.getAusgaben();
          summeEndsaldo += ea.getEndsaldo();
        } else if (sumElement != null)
        {
          throw new IllegalStateException("implementation error - there must be only one sum element");
        } else
        {
          sumElement = ea;
        }
      }
      if (sumElement != null)
      {
        sumElement.setAnfangssaldo(summeAnfangssaldo);
        sumElement.setEndsaldo(summeEndsaldo);
        sumElement.setEinnahmen(summeEinnahmen);
        sumElement.setAusgaben(summeAusgaben);
      }
    }
  }

  private List<EinnahmeAusgabe> getChildren(EinnahmeAusgabeTreeNode treeNode) throws RemoteException
  {
    List<EinnahmeAusgabe> result = new ArrayList<>();
    GenericIterator iterator = treeNode.getChildren();
    while (iterator.hasNext())
    {
      result.add((EinnahmeAusgabe) iterator.next());
    }
    return result;
  }

  private List<Konto> getSelectedAccounts() throws RemoteException
  {
    List<Konto> result = new ArrayList<>();
    Object o = getKontoAuswahl().getValue();
    if (o instanceof Konto)
    {
      result.add((Konto) o);
    } else if (o == null || (o instanceof String))
    {
      boolean onlyActive = ((Boolean) this.getActiveOnly().getValue()).booleanValue();
      String group = o != null && (o instanceof String) ? (String) o : null;

      List<Konto> konten = KontoUtil.getKonten(onlyActive ? KontoFilter.ACTIVE : KontoFilter.ALL);
      for (Konto k : konten)
      {
        if (group == null || Objects.equals(group, k.getKategorie()))
        {
          result.add(k);
        }
      }
    }
    return result;
  }

  /**
   * Erstelle die finale Struktur - nur ohne Beträge und Salden
   */
  private List<EinnahmeAusgabeTreeNode> createEmptyNodes(Date start, Date end, List<Konto> konten, Interval interval) throws RemoteException
  {
    List<EinnahmeAusgabeTreeNode> result = new ArrayList<>();
    if (interval == Interval.ALL)
    {
      List<EinnahmeAusgabe> kontoNodes = getEmptyNodes(start, end, konten);
      EinnahmeAusgabeTreeNode node = new EinnahmeAusgabeTreeNode(start, end, kontoNodes);
      result.add(node);
    } else if (start == null || end == null)
    {
      throw new IllegalStateException("programming error - if there is grouping, there must be transactions and hence both dates are set");
    } else
    {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(DateUtil.startOfDay(start));
      // Prüfe auf time <= end mit !after(), damit bei start==end auch ein Intervallknoten bestimmt wird.
      while (!calendar.getTime().after(end))
      {
        // Tag auf den ersten im Intervall setzen
        calendar.set(interval.type, 1);
        Date nodeFrom = calendar.getTime();

        // ermittle den Zeitpunkt unmittelbar vor dem nächsten Zeitraumstart
        calendar.add(interval.size, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Date nodeTo = DateUtil.startOfDay(calendar.getTime());

        List<EinnahmeAusgabe> werte = getEmptyNodes(nodeFrom, nodeTo, konten);
        result.add(new EinnahmeAusgabeTreeNode(nodeFrom, nodeTo, werte));
        // ermittle den Start des nächsten Zeitraums
        calendar.setTime(nodeFrom);
        // eine Intervalldauer in die Zukunft springen
        calendar.add(interval.size, 1);
      }
    }
    return result;
  }

  private List<EinnahmeAusgabe> getEmptyNodes(Date start, Date end, List<Konto> konten) throws RemoteException
  {
    List<EinnahmeAusgabe> result = new ArrayList<>();
    for (Konto konto : konten)
    {
      EinnahmeAusgabe ea = new EinnahmeAusgabe(konto);
      ea.setStartdatum(start);
      ea.setEnddatum(end);
      result.add(ea);
    }
    if (konten.size() > 1)
    {
      EinnahmeAusgabe summe = new EinnahmeAusgabe();
      summe.setStartdatum(start);
      summe.setEnddatum(end);
      summe.setIsSumme(true);
      result.add(summe);
    }
    return result;
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
  
  /**
   * Berechnet die Anzahl an Tagen zwischen zwei Daten. 
   * (Sollte besser in eine andere Klasse verschoben werden, zb jameica.util.DateUtil)
   * @param d1
   * @param d2
   * @return
   */
  private long getDifferenceDays(Date d1, Date d2) {
    java.time.LocalDate date1 = DateUtil.startOfDay(toUtilDate(d1)).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    java.time.LocalDate date2 = DateUtil.endOfDay(toUtilDate(d2)).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    return java.time.temporal.ChronoUnit.DAYS.between(date1, date2);
  }
  
  /**
   * Stellt sicher, dass ein java.util.Date zurueckgeliefert wird.
   * Unter Umstaenden kann hier ein java.sql.Date ankommen. In dem ist aber
   * "toInstant" nicht implementiert und wirft eine NoSuchOperationException.
   * @param d das garantierte java.util.Date.
   * @return das potentielle java.sql.Date
   */
  private Date toUtilDate(Date d)
  {
    if (d == null)
      return new Date();
    
    if (!(d instanceof java.sql.Date))
      return d;
    
    java.sql.Date sql = (java.sql.Date) d;
    return new Date(sql.getTime());
  }
}
