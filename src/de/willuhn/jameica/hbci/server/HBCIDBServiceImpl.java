/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HBCIDBServiceImpl.java,v $
 * $Revision: 1.9 $
 * $Date: 2005/01/30 20:45:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.EmbeddedDBServiceImpl;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class HBCIDBServiceImpl extends EmbeddedDBServiceImpl implements HBCIDBService
{

  /**
   * @throws RemoteException
   */
  public HBCIDBServiceImpl() throws RemoteException
  {
    super(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/db/db.conf",
          "hibiscus","hibiscus");
    this.setClassFinder(Application.getClassLoader().getClassFinder());
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return i18n.tr("Datenbank-Service für Hibiscus");
  }

  /**
   * Ueberschreiben wir, um ein Login vorzuschalten, wenn die Anwendung als Client/Standalone laeuft.
   * @see de.willuhn.datasource.rmi.DBService#createList(java.lang.Class)
   */
  public DBIterator createList(Class arg0) throws RemoteException
  {
    return super.createList(arg0);
  }

  /**
   * Ueberschreiben wir, um ein Login vorzuschalten, wenn die Anwendung als Client/Standalone laeuft.
   * @see de.willuhn.datasource.rmi.DBService#createObject(java.lang.Class, java.lang.String)
   */
  public DBObject createObject(Class arg0, String arg1) throws RemoteException
  {
    return super.createObject(arg0, arg1);
  }

}


/*********************************************************************
 * $Log: HBCIDBServiceImpl.java,v $
 * Revision 1.9  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
 * Revision 1.7  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/03 18:42:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/09/15 22:31:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/08/31 18:13:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/23 16:23:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 **********************************************************************/