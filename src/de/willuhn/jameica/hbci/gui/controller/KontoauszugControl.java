/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KontoauszugControl.java,v $
 * $Revision: 1.18 $
 * $Date: 2007/04/27 15:30:44 $
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
import java.util.List;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.parts.KontoauszugList;
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

  private KontoauszugList list = null;
  private I18N i18n = null;

  /**
   * ct.
   * @param view
   */
  public KontoauszugControl(AbstractView view)
  {
    super(view);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert eine Liste mit den ausgewaehlten Umsaetzen.
   * @return Liste der Umsaetze.
   * @throws RemoteException
   */
  public KontoauszugList getKontoauszugList() throws RemoteException
  {
    if (this.list == null)
      this.list = new KontoauszugList(new UmsatzDetail());
    return this.list;
  }
  
  
  /**
   * Startet den Export.
   */
  public synchronized void handlePrint()
  {
    try
    {
      KontoauszugList auszug = getKontoauszugList();

      // Vorher machen wir nochmal ein UNVERZOEGERTES Reload,
      // denn es muss sichergestellt sein, dass die Tabelle
      // aktuell ist, wenn wir als naechstes getItems()
      // aufrufen
      auszug.handleReload();

      // Wir laden die Umsaetze direkt aus der Tabelle.
      // Damit werden genau die ausgegeben, die gerade
      // angezeigt werden und wir sparen uns das erneute
      // Laden aus der Datenbank
      List list = auszug.getItems();

      if (list == null || list.size() == 0)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine zu exportierenden Umsätze"), StatusBarMessage.TYPE_ERROR));
        return;
      }

      // Start- und End-Datum als Contextparameter an Exporter uebergeben
      // TODO Parameter uebergeben
//      Exporter.SESSION.put("pdf.start",getStart().getValue());
//      Exporter.SESSION.put("pdf.end",getEnd().getValue());

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
}

/*******************************************************************************
 * $Log: KontoauszugControl.java,v $
 * Revision 1.18  2007/04/27 15:30:44  willuhn
 * @N Kontoauszug-Liste in TablePart verschoben
 *
 * Revision 1.17  2007/04/26 18:28:30  willuhn
 * @N Ausfuehren der Reloads via DelayedListener
 *
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
