/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/HBCIDBService.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/04/25 23:25:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBService;

/**
 * Interface fuer den Datenbank-Service von Hibiscus.
 * @author willuhn
 */
public interface HBCIDBService extends DBService
{
  /**
   * Fuehrt ein SQL-Statement aus und uebergibt das Resultset an den Extractor.
   * @param sql das Statement.
   * @param params die Parameter zur Erzeugung des PreparedStatements.
   * @param extractor der Extractor.
   * @return die vom ResultSetExtractor zurueckgelieferten Daten.
   * @throws RemoteException
   */
  public Object execute(String sql, Object[] params, ResultSetExtractor extractor) throws RemoteException;

}

/*****************************************************************************
 * $Log: HBCIDBService.java,v $
 * Revision 1.2  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.1  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
*****************************************************************************/