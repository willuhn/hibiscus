/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/HBCIDBService.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/04/19 18:12:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer den Datenbank-Service von Hibiscus.
 * @author willuhn
 */
public interface HBCIDBService extends DBService
{
  /**
   * Einstellungen fuer die DB-Services.
   */
  public final static Settings SETTINGS = new Settings(HBCIDBService.class);

  /**
   * Aktualisiert die Datenbank.
   * @param oldVersion vorherige Version.
   * @param newVersion neue Version.
   * @throws RemoteException Wenn beim Update ein Fehler auftrat.
   */
  public void update(double oldVersion, double newVersion) throws RemoteException;
  
  /**
   * Initialisiert/Erzeugt die Datenbank.
   * @throws RemoteException Wenn beim Initialisieren ein Fehler auftrat.
   */
  public void install() throws RemoteException;
  
  /**
   * Checkt die Konsistenz der Datenbank.
   * @throws RemoteException Wenn es beim Pruefen der Datenbank-Konsistenz zu einem Fehler kam.
   * @throws ApplicationException wenn die Datenbank-Konsistenz nicht gewaehrleistet ist.
   */
  public void checkConsistency() throws RemoteException, ApplicationException;
  
  /**
   * Liefert den Namen der SQL-Funktion, mit der die Datenbank aus einem DATE-Feld einen UNIX-Timestamp macht.
   * Bei MySQL ist das z.Bsp. "UNIX_TIMESTAMP" und bei McKoi schlicht "TONUMBER".
   * @param content der Feld-Name.
   * @return Name der SQL-Funktion samt Parameter. Also zum Beispiel "TONUMBER(datum)".
   * @throws RemoteException
   */
  public String getSQLTimestamp(String content) throws RemoteException;

}

/*****************************************************************************
 * $Log: HBCIDBService.java,v $
 * Revision 1.4  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.3  2006/12/27 11:52:36  willuhn
 * @C ResultsetExtractor moved into datasource
 *
 * Revision 1.2  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.1  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
*****************************************************************************/