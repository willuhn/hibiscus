/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/Attic/HBCISynchronizer.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/08/01 16:10:41 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Hilfsklasse zum Ausfuehren der Synchronisierung.
 */
public class HBCISynchronizer
{

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  /**
   * ct.
   */
  public HBCISynchronizer()
  {
  }
  
  /**
   * Startet die Synchronisierung.
   * @throws RemoteException 
   * @throws ApplicationException 
   */
  public void start() throws RemoteException, ApplicationException
  {
    Logger.info("Start synchronize");
    DBIterator list = Settings.getDBService().createList(Konto.class);
    while (list.hasNext())
    {
      Konto k = (Konto) list.next();
      if (k.getSynchronize())
        sync(k);
    }
  }
  
  private void sync(Konto k) throws RemoteException, ApplicationException
  {
    Logger.info("creating hbci factory");
    HBCIFactory factory = HBCIFactory.getInstance();

    Logger.info("adding umsatz job");
    factory.addJob(new HBCIUmsatzJob(k));
    
    Logger.info("adding saldo job");
    factory.addExclusiveJob(new HBCISaldoJob(k));

    if (settings.getBoolean("sync.ueb",false))
    {
      Logger.info("adding open transfers");
      DBIterator list = k.getUeberweisungen();
      while (list.hasNext())
      {
        Ueberweisung u = (Ueberweisung) list.next();
        if (u.ausgefuehrt())
          continue;
        if (u.ueberfaellig())
        {
          factory.addExclusiveJob(new HBCIUeberweisungJob(u));
        }
      }
    }
    factory.executeJobs(k,null);
    
  }

}


/*********************************************************************
 * $Log: HBCISynchronizer.java,v $
 * Revision 1.1  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 **********************************************************************/