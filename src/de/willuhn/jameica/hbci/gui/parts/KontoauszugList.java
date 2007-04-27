/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/KontoauszugList.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/27 15:30:44 $
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
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;


/**
 * Vorkonfigurierte Liste fuer Kontoauszuege.
 */
public class KontoauszugList extends UmsatzList
{
  // Suche nach Konto/zeitraum
  private SelectInput kontoAuswahl     = null;
  private DateInput start              = null;
  private DateInput end                = null;

  // Suche nach Gegenkonto
  private DialogInput gegenkontoNummer = null;
  private TextInput gegenkontoName     = null;
  private TextInput gegenkontoBLZ      = null;

  private Listener listener            = null;

  private de.willuhn.jameica.system.Settings mySettings = null;

  private I18N i18n = null;

  /**
   * ct.
   * @param action die auszufuehrende Aktion.
   * @throws RemoteException
   */
  public KontoauszugList(Action action) throws RemoteException
  {
    super((GenericIterator)null,action);

    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.mySettings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    this.setFilterVisible(false);

    // bei Ausloesungen ueber SWT-Events verzoegern wir
    // das Reload, um schnell aufeinanderfolgende Updates
    // zu buendeln.
    this.listener = new DelayedListener(new Listener() {
      public void handleEvent(Event event)
      {
        handleReload();
      }
    });
  }

  /**
   * Ueberschrieben, um die Tabelle vorher noch mit Daten zu fuellen.
   * @see de.willuhn.jameica.hbci.gui.parts.UmsatzList#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    /////////////////////////////////////////////////////////////////
    // Tab-Container
    TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup zeitraum = new TabGroup(folder,i18n.tr("Konto/Zeitraum"));
    zeitraum.addLabelPair(i18n.tr("Konto"), getKontoAuswahl());
    zeitraum.addLabelPair(i18n.tr("Start-Datum"), getStart());
    zeitraum.addLabelPair(i18n.tr("End-Datum"), getEnd());
    
    TabGroup gegenkonto = new TabGroup(folder,i18n.tr("Gegenkonto"));
    gegenkonto.addLabelPair(i18n.tr("Kontonummer enthält"),           getGegenkontoNummer());    
    gegenkonto.addLabelPair(i18n.tr("BLZ enthält"),                   getGegenkontoBLZ());    
    gegenkonto.addLabelPair(i18n.tr("Name des Kontoinhabers enthält"),getGegenkontoName());

    new Headline(parent,i18n.tr("Gefundene Umsätze"));

    removeAll();
    GenericIterator list = getUmsaetze();
    while (list.hasNext())
      addItem(list.next());
    
    super.paint(parent);
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        try
        {
          // Wir speichern hier alle eingegebenen Suchbegriffe fuer's naechste mal
          Date from = (Date) getStart().getValue();
          Date to   = (Date) getEnd().getValue();
          Konto k   = (Konto) getKontoAuswahl().getValue();
          mySettings.setAttribute("kontoauszug.list.from",from == null ? null : HBCI.DATEFORMAT.format(from));
          mySettings.setAttribute("kontoauszug.list.to",to == null ? null : HBCI.DATEFORMAT.format(to));
          mySettings.setAttribute("kontoauszug.list.konto",k == null ? null : k.getID());
        }
        catch (RemoteException re)
        {
          Logger.error("error while saving last filter settings",re);
        }
      }
    
    });

    // Zum Schluss Sortierung aktualisieren
    sort();
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  private Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    this.kontoAuswahl = new SelectInput(it, null);
    this.kontoAuswahl.setAttribute("longname");
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    this.kontoAuswahl.addListener(this.listener);
    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * @return Auswahl-Feld.
   */
  private Input getStart()
  {
    if (this.start != null)
      return this.start;

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.DAY_OF_MONTH, 1);
    Date dStart = HBCIProperties.startOfDay(cal.getTime());

    this.start = new DateInput(dStart, HBCI.DATEFORMAT);
    this.start.addListener(this.listener);
    return this.start;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * @return Auswahl-Feld.
   */
  private Input getEnd()
  {
    if (this.end != null)
      return this.end;

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    Date dEnd = HBCIProperties.endOfDay(cal.getTime());
    
    this.end = new DateInput(dEnd, HBCI.DATEFORMAT);
    this.end.addListener(this.listener);
    return this.end;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer die Kontonummer des Gegenkontos.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  private DialogInput getGegenkontoNummer() throws RemoteException
  {
    if (this.gegenkontoNummer != null)
      return this.gegenkontoNummer;

    AdresseAuswahlDialog d = new AdresseAuswahlDialog(AdresseAuswahlDialog.POSITION_MOUSE);
    d.addCloseListener(new AddressListener());
    this.gegenkontoNummer = new DialogInput("",d);
    this.gegenkontoNummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    this.gegenkontoNummer.addListener(this.listener);
    return this.gegenkontoNummer;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BLZ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  private Input getGegenkontoBLZ() throws RemoteException
  {
    if (this.gegenkontoBLZ != null)
      return this.gegenkontoBLZ;
    
    this.gegenkontoBLZ = new BLZInput("");
    this.gegenkontoBLZ.addListener(this.listener);
    return this.gegenkontoBLZ;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Namen des Kontoinhabers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  private Input getGegenkontoName() throws RemoteException
  {
    if (this.gegenkontoName != null)
      return this.gegenkontoName;
    this.gegenkontoName = new TextInput("",HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    this.gegenkontoName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    this.gegenkontoName.addListener(this.listener);
    return this.gegenkontoName;
  }

  /**
   * Liefert die Liste der Umsaetze basierend auf der aktuellen Auswahl.
   * @return Liste der Umsaetze.
   * @throws RemoteException
   */
  private synchronized GenericIterator getUmsaetze() throws RemoteException
  {
    Konto k         = (Konto) getKontoAuswahl().getValue();
    Date start      = (Date) getStart().getValue();
    Date end        = (Date) getEnd().getValue();
    String gkName   = (String) getGegenkontoName().getValue();
    String gkBLZ    = (String) getGegenkontoBLZ().getValue();
    String gkNummer = (String) getGegenkontoNummer().getText();
    
    HBCIDBService service = (HBCIDBService) Settings.getDBService();

    DBIterator umsaetze = Settings.getDBService().createList(Umsatz.class);

    /////////////////////////////////////////////////////////////////
    // Konto und Zeitraum
    if (k != null)     umsaetze.addFilter("konto_id = " + k.getID());
    if (start != null) umsaetze.addFilter("valuta >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(start).getTime())});
    if (end != null)   umsaetze.addFilter("valuta <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(end).getTime())});
    /////////////////////////////////////////////////////////////////
    // Gegenkonto
    if (gkBLZ    != null && gkBLZ.length() > 0)    umsaetze.addFilter("empfaenger_blz like ?",new Object[]{"%" + gkBLZ + "%"});
    if (gkNummer != null && gkNummer.length() > 0) umsaetze.addFilter("empfaenger_konto like ?",new Object[]{"%" + gkNummer + "%"});
    if (gkName   != null && gkName.length() > 0)   umsaetze.addFilter("LOWER(empfaenger_name) like ?",new Object[]{"%" + gkName.toLowerCase() + "%"});
    /////////////////////////////////////////////////////////////////
    
    umsaetze.setOrder("ORDER BY " + service.getSQLTimestamp("valuta") + " asc, id asc");
    return umsaetze;
  }

  /**
   * Aktualisiert die Tabelle der angezeigten Umsaetze.
   */
  public synchronized void handleReload()
  {
    if (!hasChanged())
      return;

    GUI.startSync(new Runnable() // Sanduhr einblenden
    {
      public void run()
      {
        try
        {
          removeAll();
          
          GenericIterator list = getUmsaetze();
          while (list.hasNext())
            addItem(list.next());
          
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
      return getStart().hasChanged() ||
                getEnd().hasChanged() ||
                getKontoAuswahl().hasChanged() ||
                getGegenkontoName().hasChanged() ||
                getGegenkontoNummer().hasChanged() ||
                getGegenkontoBLZ().hasChanged();
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
        getGegenkontoBLZ().setValue(address.getBLZ());
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


/*********************************************************************
 * $Log: KontoauszugList.java,v $
 * Revision 1.1  2007/04/27 15:30:44  willuhn
 * @N Kontoauszug-Liste in TablePart verschoben
 *
 **********************************************************************/