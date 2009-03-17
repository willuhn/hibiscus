/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/SynchronizeOptions.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/03/17 23:44:15 $
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
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  /**
   * ct.
   * @param k das Konto.
   * @throws RemoteException
   */
  public SynchronizeOptions(Konto k) throws RemoteException
  {
    super();
    this.id = k == null ? null : k.getID();
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
    return settings.getBoolean("sync.konto." + id + ".saldo",getSyncKontoauszuege());
  }

  /**
   * Prueft, ob die Kontoauszuege abgerufen werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncKontoauszuege()
  {
    return settings.getBoolean("sync.konto." + id + ".kontoauszug",true);
  }
  
  /**
   * Prueft, ob offene und ueberfaellige Ueberweisungen abgesendet werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncUeberweisungen()
  {
    return settings.getBoolean("sync.konto." + id + ".ueb",false);
  }

  /**
   * Prueft, ob offene und ueberfaellige Lastschriften eingereicht werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncLastschriften()
  {
    return settings.getBoolean("sync.konto." + id + ".last",false);
  }

  /**
   * Prueft, ob die Dauerauftraege synchronisiert werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncDauerauftraege()
  {
    return settings.getBoolean("sync.konto." + id + ".dauer",false);
  }
  
  /**
   * Prueft, ob offene und ueberfaellige Auslandsueberweisungen eingereicht werden sollen.
   * @return true, wenn sie synchronisiert werden sollen.
   */
  public boolean getSyncAuslandsUeberweisungen()
  {
    return settings.getBoolean("sync.konto." + id + ".uebforeign",false);
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

}


/*********************************************************************
 * $Log: SynchronizeOptions.java,v $
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