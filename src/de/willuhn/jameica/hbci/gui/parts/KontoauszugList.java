/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/KontoauszugList.java,v $
 * $Revision: 1.50 $
 * $Date: 2012/04/05 21:23:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.io.Exporter;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Vorkonfigurierte Liste fuer Kontoauszuege.
 */
public class KontoauszugList extends UmsatzList
{
  private final static Settings settings = new Settings(KontoauszugList.class);
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings syssettings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  
  // Konto/Zeitraum/Suchbegriff/nur geprueft
  private TextInput text               = null;
  private CheckboxInput unchecked      = null;
  private KontoInput kontoAuswahl      = null;
  private DateInput start              = null;
  private DateInput end                = null;
  private RangeInput range             = null;
  private UmsatzTypInput kategorie     = null;
  private CheckboxInput subKategorien  = null;

  // Gegenkonto/Betrag
  private DialogInput gegenkontoNummer = null;
  private TextInput gegenkontoName     = null;
  private TextInput gegenkontoBLZ      = null;
  private DecimalInput betragFrom      = null;
  private DecimalInput betragTo        = null;

  private Listener listener            = null;
  
  private boolean disposed             = false; // BUGZILLA 462
  private boolean changed              = false;
  private boolean hasFilter            = false;

  /**
   * ct.
   * @throws RemoteException
   */
  public KontoauszugList() throws RemoteException
  {
    super((GenericIterator)null,new UmsatzDetail());

    this.setFilterVisible(false);

    // bei Ausloesungen ueber SWT-Events verzoegern wir
    // das Reload, um schnell aufeinanderfolgende Updates
    // zu buendeln.
    this.listener = new DelayedListener(new Listener() {
      public void handleEvent(Event event)
      {
        handleReload(false);
      }
    });
  }

  /**
   * Ueberschrieben, um die Tabelle vorher noch mit Daten zu fuellen.
   * @see de.willuhn.jameica.hbci.gui.parts.UmsatzList#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    addColumn(new KontoColumn()); // BUGZILLA 723
    addColumn(i18n.tr("GK Konto"), "empfaenger_konto",new IbanFormatter());
    addColumn(i18n.tr("GK BLZ"),   "empfaenger_blz");
    addColumn(i18n.tr("Art"),      "art");

    /////////////////////////////////////////////////////////////////
    // Tab-Container
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    {
      TabGroup tab = new TabGroup(folder,i18n.tr("Konto/Zeitraum/Suchbegriff"));

      ColumnLayout columns = new ColumnLayout(tab.getComposite(),2);
      
      Container left = new SimpleContainer(columns.getComposite());
      left.addLabelPair(i18n.tr("Zweck/Notiz/Art enthält"), this.getText());
      left.addLabelPair(i18n.tr("Konto"),                   this.getKontoAuswahl());
      left.addLabelPair(i18n.tr("Kategorie"),               this.getKategorie());
      left.addCheckbox(this.getUnChecked(),i18n.tr("Nur ungeprüfte Umsätze"));
      
      Container right = new SimpleContainer(columns.getComposite());

      right.addInput(this.getRange());
      MultiInput range = new MultiInput(this.getStart(),this.getEnd());
      right.addInput(range);
      
      right.addCheckbox(this.getSubKategorien(),i18n.tr("Untergeordnete Kategorien einbeziehen"));
    }
    
    {
      TabGroup tab = new TabGroup(folder,i18n.tr("Gegenkonto/Betrag"));
      tab.addLabelPair(i18n.tr("IBAN oder Kontonummer enthält"), this.getGegenkontoNummer());

      ColumnLayout columns = new ColumnLayout(tab.getComposite(),2);
      Container left = new SimpleContainer(columns.getComposite());
      left.addLabelPair(i18n.tr("BIC oder BLZ enthält"),           this.getGegenkontoBLZ());
      left.addLabelPair(i18n.tr("Name des Kontoinhabers enthält"), this.getGegenkontoName());

      Container right = new SimpleContainer(columns.getComposite());
      right.addLabelPair(i18n.tr("Mindest-Betrag"), this.getMindestBetrag());
      right.addLabelPair(i18n.tr("Höchst-Betrag"),        this.getHoechstBetrag());
    }
    
    // Wir merken uns das aktive Tab.
    Integer tab = (Integer) cache.get("tab");
    if (tab != null) folder.setSelection(tab);
    folder.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        cache.put("tab",folder.getSelectionIndex());
      }
    });
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Exportieren..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handlePrint();
      }
    },null,false,"document-save.png");
    buttons.addButton(i18n.tr("Filter zurücksetzen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleReset();
      }
    },null,false,"edit-undo.png");
    buttons.addButton(i18n.tr("&Aktualisieren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleReload(true);
      }
    },null,true,"view-refresh.png");
    
    buttons.paint(parent);
    
    handleReload(true);
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        GUI.getView().setLogoText(""); // Hinweis-Test wieder ausblenden BUGZILLA 449
        disposed = true;
      }
    });
    super.paint(parent);

    // Machen wir explizit nochmal, weil wir die paint()-Methode ueberschrieben haben
    restoreState();
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    this.kontoAuswahl = new KontoInput(null,KontoFilter.ALL);
    this.kontoAuswahl.setRememberSelection("auswertungen.kontoauszug");
    this.kontoAuswahl.setSupportGroups(true);
    this.kontoAuswahl.setComment(null);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoAuswahl.addListener(this.listener);
    return this.kontoAuswahl;
  }

  /**
   * Liefert eine Checkbox, um nur die ungeprueften Umsaetze anzuzeigen.
   * @return Checkbox.
   */
  public CheckboxInput getUnChecked()
  {
    if (this.unchecked != null)
      return this.unchecked;
    
    this.unchecked = new CheckboxInput(settings.getBoolean("kontoauszug.list.unchecked",false));
    this.unchecked.addListener(this.listener);
    return this.unchecked;
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
    this.start.addListener(this.listener);
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
    this.end.setName(i18n.tr("bis"));
    this.end.setComment(null);
    this.end.addListener(this.listener);
    return this.end;
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
    this.range.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (range.getValue() != null)
          handleReload(true);
      }
    });
    
    return this.range;
  }
  
  /**
   * Liefert ein Auswahl-Feld fuer die Kategorie. 
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public UmsatzTypInput getKategorie() throws RemoteException
  {
    if (this.kategorie != null)
      return this.kategorie;
    
    UmsatzTyp preset = (UmsatzTyp) cache.get("kontoauszug.list.kategorie");
    if (preset == null || preset.getID() == null)
      preset = null; // wurde zwischenzeitlich geloescht
    this.kategorie = new UmsatzTypInput(preset,UmsatzTyp.TYP_EGAL);
    this.kategorie.setPleaseChoose(i18n.tr("<Alle Kategorien>"));
    this.kategorie.setComment(null);
    this.kategorie.addListener(this.listener);
    
    // Wenn in der Kategorie-Auswahl "<Alle Kategorien>" ausgewaehlt wurde, deaktivieren wir uns
    this.kategorie.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          getSubKategorien().setEnabled(kategorie.getValue() != null);
        }
        catch (Exception e)
        {
          Logger.error("unable to update checkbox",e);
        }
      }
    });
    
    return this.kategorie;
  }
  
  /**
   * Liefert eine Checkbox die angibt ob Unterkategorien ermittelt werden sollen.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getSubKategorien() throws RemoteException
  {
    if (this.subKategorien != null)
      return this.subKategorien;
    
    Boolean b = (Boolean) cache.get("kontoauszug.list.subkategorien");
    this.subKategorien = new CheckboxInput(b != null && b.booleanValue());
    this.subKategorien.addListener(this.listener);
    this.subKategorien.setEnabled(this.getKategorie().getValue() != null); // initial nur aktiviert, wenn eine Kategorie ausgewaehlt ist
    return this.subKategorien;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer die Kontonummer des Gegenkontos.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DialogInput getGegenkontoNummer() throws RemoteException
  {
    if (this.gegenkontoNummer != null)
      return this.gegenkontoNummer;

    AdresseAuswahlDialog d = new AdresseAuswahlDialog(AdresseAuswahlDialog.POSITION_MOUSE);
    d.addCloseListener(new AddressListener());
    this.gegenkontoNummer = new DialogInput((String) cache.get("kontoauszug.list.gegenkonto.nummer"),d);
    this.gegenkontoNummer.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
    this.gegenkontoNummer.addListener(this.listener);
    return this.gegenkontoNummer;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getGegenkontoBLZ() throws RemoteException
  {
    if (this.gegenkontoBLZ != null)
      return this.gegenkontoBLZ;
    
    this.gegenkontoBLZ = new BLZInput((String)cache.get("kontoauszug.list.gegenkonto.blz"));
    this.gegenkontoBLZ.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS); // nicht BIC sondern IBAN - dort sind auch die Kleinbuchstaben mit drin 
    this.gegenkontoBLZ.setComment(null);
    this.gegenkontoBLZ.addListener(this.listener);
    return this.gegenkontoBLZ;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Namen des Kontoinhabers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getGegenkontoName() throws RemoteException
  {
    if (this.gegenkontoName != null)
      return this.gegenkontoName;
    this.gegenkontoName = new TextInput((String)cache.get("kontoauszug.list.gegenkonto.name"),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    this.gegenkontoName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    this.gegenkontoName.addListener(this.listener);
    return this.gegenkontoName;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer Verwendungszweck/Kommentar.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getText() throws RemoteException
  {
    if (this.text != null)
      return this.text;
    this.text = new TextInput((String)cache.get("kontoauszug.list.text"),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    this.text.addListener(this.listener);
    return this.text;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Eingabe eines Mindestbetrages.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getMindestBetrag() throws RemoteException
  {
    if (this.betragFrom != null)
      return this.betragFrom;
    
    this.betragFrom = new DecimalInput((Double)cache.get("kontoauszug.list.betrag.from"), HBCI.DECIMALFORMAT);
    this.betragFrom.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.betragFrom.addListener(this.listener);
    this.betragFrom.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          // Nur bei Focus Out
          if (event.type == SWT.FocusOut)
          {
            // Wenn beim Hoechstbetrag noch nichts eingegeben ist, uebernehmen
            // wird dort automatisch den Mindestbetrag
            // Vorschlag von Roberto aus Mail vom 30.08.2008
            Input i = getHoechstBetrag();
            Double value = (Double) i.getValue();
            if (value == null || value.isNaN())
            {
              i.setValue(betragFrom.getValue());
              ((Text) i.getControl()).selectAll();
            }
          }
        }
        catch (Exception e)
        {
          Logger.error("error while auto applying max value",e);
        }
      }
    });
    return this.betragFrom;
  }

  /**
   * Liefert ein Eingabe-Feld fuer die Eingabe eines Hoechstbetrages.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getHoechstBetrag() throws RemoteException
  {
    if (this.betragTo != null)
      return this.betragTo;
    
    this.betragTo = new DecimalInput((Double)cache.get("kontoauszug.list.betrag.to"), HBCI.DECIMALFORMAT);
    this.betragTo.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.betragTo.addListener(this.listener);
    return this.betragTo;
  }

  /**
   * Liefert die Liste der Umsaetze basierend auf der aktuellen Auswahl.
   * @return Liste der Umsaetze.
   * @throws RemoteException
   */
  private synchronized List<Umsatz> getUmsaetze() throws RemoteException
  {
    Object o          = getKontoAuswahl().getValue();
    Date start        = (Date) getStart().getValue();
    Date end          = (Date) getEnd().getValue();
    String gkName     = (String) getGegenkontoName().getValue();
    String gkBLZ      = (String) getGegenkontoBLZ().getValue();
    String gkNummer   = (String) getGegenkontoNummer().getText();
    Double min        = (Double) getMindestBetrag().getValue();
    Double max        = (Double) getHoechstBetrag().getValue();
    String zk         = (String) getText().getValue();
    UmsatzTyp typ     = (UmsatzTyp) getKategorie().getValue();
    boolean unchecked = ((Boolean) getUnChecked().getValue()).booleanValue();
    boolean subKategorien = ((Boolean) getSubKategorien().getValue()).booleanValue();
    
    // Aktuelle Werte speichern
    cache.put("kontoauszug.list.gegenkonto.nummer",gkNummer);
    cache.put("kontoauszug.list.gegenkonto.blz",   gkBLZ);
    cache.put("kontoauszug.list.gegenkonto.name",  gkName);
    cache.put("kontoauszug.list.kategorie",        typ);
    cache.put("kontoauszug.list.text",             zk);
    cache.put("kontoauszug.list.betrag.from",      min);
    cache.put("kontoauszug.list.betrag.to",        max);
    cache.put("kontoauszug.list.subkategorien",    subKategorien);
    
    // geprueft/ungeprueft Flag speichern wir permanent
    settings.setAttribute("kontoauszug.list.unchecked",unchecked);

    DBIterator umsaetze = UmsatzUtil.getUmsaetzeBackwards();
    
    // BUGZILLA 449
    this.hasFilter = false;

    /////////////////////////////////////////////////////////////////
    // Zeitraum
    // Der Warnhinweis wird nicht fuer den Zeitraum angezeigt, da der
    // immer vorhanden ist
    if (start != null) umsaetze.addFilter("datum >= ?", new java.sql.Date(DateUtil.startOfDay(start).getTime()));
    if (end != null)   umsaetze.addFilter("datum <= ?", new java.sql.Date(DateUtil.endOfDay(end).getTime()));
    /////////////////////////////////////////////////////////////////
    // Gegenkonto
    if (gkBLZ    != null && gkBLZ.length() > 0)    {umsaetze.addFilter("LOWER(empfaenger_blz) like ?","%" + gkBLZ.toLowerCase() + "%");this.hasFilter = true;}
    if (gkNummer != null && gkNummer.length() > 0) {umsaetze.addFilter("LOWER(empfaenger_konto) like ?","%" + gkNummer.toLowerCase() + "%");this.hasFilter = true;}
    if (gkName   != null && gkName.length() > 0)   {umsaetze.addFilter("LOWER(empfaenger_name) like ?","%" + gkName.toLowerCase() + "%");hasFilter = true;}
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Konto oder Kontogruppe
    if (o != null && (o instanceof Konto))
      umsaetze.addFilter("konto_id = " + ((Konto) o).getID());
    else if (o != null && (o instanceof String))
      umsaetze.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) o);
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Betrag
    if (min != null && !(Double.isNaN(min.doubleValue())))
    {
      umsaetze.addFilter("betrag >= ?",min);
      this.hasFilter = true;
    }
    if (max != null && (!Double.isNaN(max.doubleValue())))
    {
      umsaetze.addFilter("betrag <= ?",max);
      this.hasFilter = true;
    }
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Zweck/Kommentar
    if (zk != null && zk.length() > 0)
    {
      zk = "%" + zk.toLowerCase() + "%";
      String zkStripped = zk;
      String q = "CONCAT(COALESCE(zweck,''),COALESCE(zweck2,''),COALESCE(zweck3,''))";
      if (syssettings.getBoolean("search.ignore.whitespace",true))
      {
        q = "REPLACE(REPLACE(REPLACE(" + q + ",' ',''),'\n',''),'\r','')";
        zkStripped = StringUtils.deleteWhitespace(zk);
      }
      umsaetze.addFilter("(LOWER(" + q + ") LIKE ? OR LOWER(kommentar) like ? OR LOWER(art) like ?)",zkStripped,zk,zk);
    }
    /////////////////////////////////////////////////////////////////


    GUI.getView().setLogoText(this.hasFilter ? i18n.tr("Hinweis: Aufgrund von Suchkriterien werden möglicherweise nicht alle Umsätze angezeigt") : "");
    
    List<Umsatz> result = new LinkedList<Umsatz>();
    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      if (typ != null && !matches(typ, u, subKategorien))
        continue;
      
      if (unchecked)
      {
        // Nur ungepruefte anzeigen
        if ((u.getFlags() & Umsatz.FLAG_CHECKED) != 0)
          continue;
      }
      result.add(u);
    }
    return result;
  }
  
  /**
   * Prueft, ob der Umsatz zur Kategorie passt.
   * @param typ die zu pruefende Kategorie.
   * @param u der zu pruefende Umsatz.
   * @param children true, wenn wenn der Umsatz auch dann passen soll, wenn er in
   * einer der Kind-Kategorien enthalten ist.
   * @return true, wenn der Umsatz zur Kategorie passt.
   * @throws RemoteException
   */
  private boolean matches(UmsatzTyp typ, Umsatz u, boolean children) throws RemoteException
  {
    // Keine rekursive Suche
    if (!children)
      return typ.matches(u);

    // wir suchen von unten nach oben, indem wir die Umsatzkategorien
    // des Umsatzes nach oben iterieren. Wenn wir dabei auf die gesuchte
    // Kategorie stossen, passts.
    UmsatzTyp t = u.getUmsatzTyp();
    
    if (t == null)
      return false; // nichts zum Suchen da
    
    for (int i=0;i<100;++i) // maximal 100 Iterationen - fuer den (eigentlich unmoeglichen Fall), dass eine Rekursion existiert
    {
      if (t == null)
        return false; // oben angekommen und nichts gefunden
      
      if (t.equals(typ))
        return true; // passt!
      
      t = (UmsatzTyp) t.getParent(); // weiter nach oben gehen
    }
    
    // nichts gefunden
    return false;
  }

  /**
   * Startet den Export.
   */
  private synchronized void handlePrint()
  {
    try
    {
      // Vorher machen wir nochmal ein UNVERZOEGERTES Reload,
      // denn es muss sichergestellt sein, dass die Tabelle
      // aktuell ist, wenn wir als naechstes getItems()
      // aufrufen
      handleReload(true);

      // Wir laden die Umsaetze direkt aus der Tabelle.
      // Damit werden genau die ausgegeben, die gerade
      // angezeigt werden und wir sparen uns das erneute
      // Laden aus der Datenbank
      List list = this.getItems();

      if (list == null || list.size() == 0)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine zu exportierenden Umsätze"), StatusBarMessage.TYPE_ERROR));
        return;
      }
      
      // Start- und End-Datum als Contextparameter an Exporter uebergeben
      Exporter.SESSION.put("filtered",this.hasFilter);
      Exporter.SESSION.put("pdf.start",getStart().getValue());
      Exporter.SESSION.put("pdf.end",getEnd().getValue());

      Umsatz[] u = (Umsatz[]) list.toArray(new Umsatz[list.size()]);
      new UmsatzExport().handleAction(u);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException re)
    {
      Logger.error("error while reloading table",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Exportieren der Umsätze"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Resettet alle Filter-Eingaben.
   */
  private synchronized void handleReset()
  {
    try
    {
      Range range = (Range) getRange().getValue();
      getStart().setValue(range != null ? range.getStart() : null);
      getEnd().setValue(range != null ? range.getEnd() : null);
      getMindestBetrag().setValue(Double.NaN);
      getHoechstBetrag().setValue(Double.NaN);
      getKontoAuswahl().setValue(null);
      getKategorie().setValue(null);
      getSubKategorien().setValue(Boolean.FALSE);
      getGegenkontoNummer().setText("");
      getGegenkontoBLZ().setValue(null);
      getGegenkontoName().setValue(null);
      getText().setValue(null);
      getUnChecked().setValue(Boolean.FALSE);
      this.changed = true;
      handleReload(true);
    }
    catch (Exception e)
    {
      Logger.error("unable to reset filters",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zurücksetzen der Filter"), StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Aktualisiert die Tabelle der angezeigten Umsaetze.
   * @param force true, wenn das Reload forciert werden soll - egal, ob sich was geaendert hat.
   * Andernfalls wird die Tabelle nur neu geladen, wenn wirklich Filter geaendert wurden.
   */
  private synchronized void handleReload(boolean force)
  {
    if (disposed)
      return;
    
    if (!force && !hasChanged())
      return;
    
    GUI.startSync(new Runnable() // Sanduhr einblenden
    {
      public void run()
      {
        try
        {
          removeAll();
          
          List<Umsatz> list = getUmsaetze();
          for(Umsatz u:list)
            addItem(u);

          
          // Zum Schluss Sortierung aktualisieren
          sort();
        }
        catch (Exception e)
        {
          Logger.error("error while reloading table",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Umsätze"), StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }
  
  /**
   * Prueft, ob seit der letzten Aktion Eingaben geaendert wurden.
   * Ist das nicht der Fall, muss die Tabelle nicht neu geladen werden.
   * @return true, wenn sich die Daten geaendert haben.
   */
  private boolean hasChanged()
  {
    try
    {
      boolean b = this.changed;
      this.changed = false;
      return b || getStart().hasChanged() ||
                getEnd().hasChanged() ||
                getUnChecked().hasChanged() ||
                getKontoAuswahl().hasChanged() ||
                getGegenkontoName().hasChanged() ||
                getGegenkontoNummer().hasChanged() ||
                getGegenkontoBLZ().hasChanged() ||
                getMindestBetrag().hasChanged() ||
                getHoechstBetrag().hasChanged() ||
                getKategorie().hasChanged() ||
                getSubKategorien().hasChanged() ||
                getText().hasChanged();
    }
    catch (Exception e)
    {
      Logger.error("unable to check change status",e);
      return false;
    }
  }
  
  /**
   * Listener, der bei Auswahl der Adresse die restlichen Daten vervollstaendigt.
   */
  private class AddressListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (event == null || event.data == null)
        return;

      Address address = (Address) event.data;
      try
      {
        getGegenkontoNummer().setText(address.getKontonummer());
        getGegenkontoBLZ().setValue(address.getBlz());
        getGegenkontoName().setValue(address.getName());
      }
      catch (RemoteException er)
      {
        Logger.error("error while choosing gegenkonto",er);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Auswahl des Gegenkontos"), StatusBarMessage.TYPE_ERROR));
      }
    }
  }

}
