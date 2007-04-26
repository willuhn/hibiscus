/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KontoauszugControl.java,v $
 * $Revision: 1.16 $
 * $Date: 2007/04/26 15:02:19 $
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
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.io.Exporter;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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

  // Suche nach Konto/zeitraum
  private SelectInput kontoAuswahl     = null;
  private DateInput start              = null;
  private DateInput end                = null;

  // Suche nach Gegenkonto
  private DialogInput gegenkontoNummer = null;
  private TextInput gegenkontoName     = null;
  private TextInput gegenkontoBLZ      = null;

  private ReloadListener listener      = null;
  private UmsatzList umsatzlist        = null;

  private I18N i18n = null;

  /**
   * ct.
   * @param view
   */
  public KontoauszugControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.listener = new ReloadListener();
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
    this.kontoAuswahl.addListener(this.listener);
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
    Date dStart = HBCIProperties.startOfDay(cal.getTime());

    this.start = new DateInput(dStart, HBCI.DATEFORMAT);
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
  public DialogInput getGegenkontoNummer() throws RemoteException
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
  public Input getGegenkontoBLZ() throws RemoteException
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
  public Input getGegenkontoName() throws RemoteException
  {
    if (this.gegenkontoName != null)
      return this.gegenkontoName;
    this.gegenkontoName = new TextInput("",HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    this.gegenkontoName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    this.gegenkontoName.addListener(this.listener);
    return this.gegenkontoName;
  }

  /**
   * Liefert eine Liste mit den ausgewaehlten Umsaetzen.
   * @return Liste der Umsaetze.
   * @throws RemoteException
   */
  public TablePart getUmsatzList() throws RemoteException
  {
    if (this.umsatzlist == null)
    {
      this.umsatzlist = new UmsatzList(getUmsaetze(), new UmsatzDetail());
      this.umsatzlist.setFilterVisible(false);
    }
    return this.umsatzlist;
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
    {
      System.out.println("---------------");
      return;
    }
   
    try
    {
      System.out.println("+++++++++++++");
      TablePart part = getUmsatzList();
      part.removeAll();
      
      GenericIterator list = getUmsaetze();
      while (list.hasNext())
        part.addItem(list.next());
      
      // Zum Schluss Sortierung aktualisieren
      part.sort();
    }
    catch (RemoteException re)
    {
      Logger.error("error while reloading table",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Umsätze"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Startet den Export.
   */
  public synchronized void handlePrint()
  {
    // Vorher machen wir nochmal ein Reload.
    handleReload();

    try
    {
      // Wir laden die Umsaetze direkt aus der Tabelle.
      // Damit werden genau die ausgegeben, die gerade
      // angezeigt werden und wir sparen uns das erneute
      // Laden aus der Datenbank
      List list = getUmsatzList().getItems();

      // Wir sortieren jetzt nicht mehr um sondern uebernehmen die
      // Daten in der Reihenfolge, wie sie in der Tabelle angezeigt
      // werden
      // Collections.reverse(list);
        
      Umsatz[] u = (Umsatz[]) list.toArray(new Umsatz[list.size()]);
        
      if (u == null || u.length == 0)
      {
        GUI.getView().setErrorText(i18n.tr("Im gewählten Zeitraum wurden keine Umsätze gefunden"));
        return;
      }

      Date start = (Date) getStart().getValue();
      Date end   = (Date) getEnd().getValue();
      
      // Start- und End-Datum als Contextparameter an Exporter uebergeben
      if (start == null)
        Exporter.SESSION.remove("pdf.start");
      else
        Exporter.SESSION.put("pdf.start",start);
      
      if (end == null)
        Exporter.SESSION.remove("pdf.end");
      else
        Exporter.SESSION.put("pdf.end",end);

      new UmsatzExport().handleAction(u);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException re)
    {
      Logger.error("error while reloading table",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Umsätze"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Prueft, ob seit der letzten Aktion Eingaben geaendert wurden.
   * Ist das nicht der Fall, muss die Tabelle nicht neu geladen werden.
   * @return true, wenn sich wirklich was geaendert hat.
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
      return true;
    }
  }
  
  /**
   * Wird ausgeloest, wenn eines der Eingabe-Felder geaendert wurde.
   */
  private class ReloadListener implements Listener
  {
    public synchronized void handleEvent(Event event)
    {
      handleReload();
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

/*******************************************************************************
 * $Log: KontoauszugControl.java,v $
 * Revision 1.16  2007/04/26 15:02:19  willuhn
 * @N Zusaetzliche Suche nach Gegenkonto
 *
 * Revision 1.15  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.14  2007/04/10 23:28:03  willuhn
 * @N TablePart Redesign (removed dependencies from GenericIterator/GenericObject)
 *
 * Revision 1.13  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.12  2007/03/21 16:56:56  willuhn
 * @N Online-Hilfe aktualisiert
 * @N Bug 337 (Stichtag in Sparquote)
 * @C Refactoring in Sparquote
 *
 * Revision 1.11  2007/03/21 15:37:46  willuhn
 * @N Vorschau der Umsaetze in Auswertung "Kontoauszug"
 *
 * Revision 1.10  2007/01/09 13:07:13  willuhn
 * @B Sortierung der Umsaetze im Kontoauszug nun immer chronologisch
 *
 * Revision 1.9  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.8  2006/12/27 17:56:49  willuhn
 * @B Bug 341
 *
 * Revision 1.7  2006/10/16 12:51:32  willuhn
 * @B Uebernahme des originalen Datums aus dem Kontoauszug
 *
 * Revision 1.6  2006/08/25 10:13:43  willuhn
 * @B Fremdschluessel NICHT mittels PreparedStatement, da die sonst gequotet und von McKoi nicht gefunden werden. BUGZILLA 278
 *
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
