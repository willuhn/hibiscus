/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/SynchronizeOptions.java,v $
 * $Revision: 1.10 $
 * $Date: 2010/09/02 12:25:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.io.Serializable;
import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;

/**
 * Container fuer die Synchronisierungsoptionen eines Kontos.
 */
public class SynchronizeOptions implements Serializable
{
  private String id = null;
  private boolean offline  = false;
  private boolean disabled = false;
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  /**
   * ct.
   * @param k das Konto.
   * @throws RemoteException
   */
  public SynchronizeOptions(Konto k) throws RemoteException
  {
    int flags = k.getFlags();
    this.offline  = ((flags & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE);
    this.disabled = ((flags & Konto.FLAG_DISABLED) == Konto.FLAG_DISABLED);
    this.id = k.getID();
  }
  
  /**
   * Prueft, ob irgendeine Synchronisierungsoption fuer das Konto aktiviert ist.
   * @return true, wenn irgendeine Option aktiv ist.
   */
  public boolean getSynchronize()
  {
    return getSyncSaldo() ||
           getSyncKontoauszuege() ||
           getSyncDauerauftraege() ||
           getSyncLastschriften() ||
           getSyncUeberweisungen() ||
           getSyncAuslandsUeberweisungen();
  }
  
  /**
   * Aendert den Synchronisierungsstatus aller Auftragstypen.
   * @param status neuer Status.
   */
  public void setAll(boolean status)
  {
    this.setSyncSaldo(status);
    this.setSyncKontoauszuege(status);
    this.setSyncDauerauftraege(status);
    this.setSyncLastschriften(status);
    this.setSyncUeberweisungen(status);
    this.setSyncAuslandsUeberweisungen(status);
  }

  /**
   * BUGZILLA 346
   * Prueft, ob die Kontoauszuege abgerufen werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncSaldo()
  {
    // Da das vorher mit ".kontoauszug" zusammengefasst
    // war, nehmen wir als Default-Wert auch den von dort.
    // Damit wird beim ersten mal der Vorwert uebernommen.
    // (Sanfte Migration)
    return !this.disabled && !this.offline && settings.getBoolean("sync.konto." + id + ".saldo",getSyncKontoauszuege());
  }

  /**
   * Prueft, ob die Kontoauszuege abgerufen werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncKontoauszuege()
  {
    return !this.disabled && !this.offline && settings.getBoolean("sync.konto." + id + ".kontoauszug",true);
  }
  
  /**
   * Prueft, ob offene und ueberfaellige Ueberweisungen abgesendet werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncUeberweisungen()
  {
    return !this.disabled && !this.offline && settings.getBoolean("sync.konto." + id + ".ueb",false);
  }

  /**
   * Prueft, ob offene und ueberfaellige Lastschriften eingereicht werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncLastschriften()
  {
    return !this.disabled && !this.offline && settings.getBoolean("sync.konto." + id + ".last",false);
  }

  /**
   * Prueft, ob die Dauerauftraege synchronisiert werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncDauerauftraege()
  {
    return !this.disabled && !this.offline && settings.getBoolean("sync.konto." + id + ".dauer",false);
  }
  
  /**
   * Prueft, ob offene und ueberfaellige Auslandsueberweisungen eingereicht werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncAuslandsUeberweisungen()
  {
    return !this.disabled && !this.offline && settings.getBoolean("sync.konto." + id + ".uebforeign",false);
  }

  /**
   * Prueft, ob in dem Konto automatisch passende Gegenbuchungen angelegt werden sollen,
   * wenn es ein Offline-Konto ist.
   * @return true, wenn automatische Offline-Synchronisierung stattfinden soll.
   */
  public boolean getSyncOffline()
  {
    return !this.disabled && settings.getBoolean("sync.konto." + id + ".offline",true);
  }
  
  /**
   * Legt fest, ob die Kontoauszuege abgerufen werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncKontoauszuege(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".kontoauszug",b);
  }
  
  /**
   * Legt fest, ob die Salden abgerufen werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncSaldo(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".saldo",b);
  }

  /**
   * Legt fest, ob offene und ueberfaellige Ueberweisungen abgesendet werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncUeberweisungen(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".ueb",b);
  }

  /**
   * Legt fest, ob offene und ueberfaellige Lastschriften eingereicht werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncLastschriften(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".last",b);
  }

  /**
   * Legt fest, ob die Dauerauftraege synchronisiert werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncDauerauftraege(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".dauer",b);
  }
  
  /**
   * Legt fest, ob offene und ueberfaellige Auslandsueberweisungen eingereicht werden sollen.
   * @param b true, wenn sie synchronisiert werden sollen.
   */
  public void setSyncAuslandsUeberweisungen(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".uebforeign",b);
  }

  /**
   * Legt fest, ob in dem Konto automatisch passende Gegenbuchungen angelegt werden sollen,
   * wenn es ein Offline-Konto ist.
   * @param b true, wenn automatische Offline-Synchronisierung stattfinden soll.
   */
  public void setSyncOffline(boolean b)
  {
    settings.setAttribute("sync.konto." + id + ".offline",b);
  }
}


/*********************************************************************
 * $Log: SynchronizeOptions.java,v $
 * Revision 1.10  2010/09/02 12:25:13  willuhn
 * @N BUGZILLA 900
 *
 * Revision 1.9  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.8  2010/02/26 15:42:23  willuhn
 * @N Alle Synchronisierungsoptionen auf einmal aktivieren/deaktivieren
 *
 * Revision 1.7  2009/09/15 00:23:35  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.6  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.5  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.4  2009/01/26 23:17:46  willuhn
 * @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
 *
 * Revision 1.3  2007/03/23 00:11:51  willuhn
 * @N Bug 346
 *
 * Revision 1.2  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
 **********************************************************************/