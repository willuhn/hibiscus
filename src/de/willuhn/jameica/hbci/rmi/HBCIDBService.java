/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/HBCIDBService.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/11/02 11:32:09 $
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
   * Initialisiert/erzeugt die Datenbank.
   * @throws RemoteException Wenn beim Initialisieren ein Fehler auftrat.
   */
  public void install() throws RemoteException;
  
  /**
   * Checkt die Konsistenz der Datenbank und fuehrt bei Bedarf Updates durch.
   * @throws RemoteException Wenn es beim Pruefen der Datenbank-Konsistenz zu einem Fehler kam.
   * @throws ApplicationException wenn die Datenbank-Konsistenz nicht gewaehrleistet ist.
   */
  public void checkConsistency() throws RemoteException, ApplicationException;
  
  /**
   * Liefert den verwendeten Treiber.
   * @return der Treiber.
   * @throws RemoteException
   */
  public DBSupport getDriver() throws RemoteException;
  
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
 * Revision 1.7  2010/11/02 11:32:09  willuhn
 * @R Alten SQL-Update-Mechanismus komplett entfernt. Wir haben das jetzt seit Hibiscus 1.8 (2008) aus Migrationsgruenden mit uns herumgetragen. Das ist jetzt lange genug her. User, die noch Hibiscus < 1.8 nutzen, muessen jetzt erst auf 1.8 updaten, damit noch die letzten sql/update_x.y-x.y.sql ausgefuehrt werden und dann erst auf die aktuelle Version
 *
 * Revision 1.6  2008/12/30 15:21:40  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.5  2008/05/06 10:10:56  willuhn
 * @N Diagnose-Dialog, mit dem man die JDBC-Verbindungsdaten (u.a. auch das JDBC-Passwort) ausgeben kann
 *
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