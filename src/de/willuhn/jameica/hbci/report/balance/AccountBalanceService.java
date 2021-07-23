/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 * Class author: Fabian Aiteanu
 **********************************************************************/

package de.willuhn.jameica.hbci.report.balance;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service fuer den Zugriff auf Kontosalden, der die verschiedenen Provider fuer Salden verwaltet.
 */
@Lifecycle(Type.CONTEXT)
public class AccountBalanceService
{
  private List<AccountBalanceProvider> providers = null;
  
  /**
   * Der Standard-Provider. Der steht immer als letzter in der Liste.
   */
  private final static Class<? extends AccountBalanceProvider> DEFAULT = BookingAccountBalanceProvider.class;

  /**
   * Liefert eine Liste der gefundenen Provider.
   * @return Liste der Provider.
   */
  public synchronized List<AccountBalanceProvider> getProviders()
  {
    if (this.providers != null)
      return this.providers;
    
    this.providers = new LinkedList<AccountBalanceProvider>();
    
    try
    {
      Logger.info("loading account balance providers");
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder().findImplementors(AccountBalanceProvider.class);
      for (Class<AccountBalanceProvider> c:found)
      {
        try
        {
          Logger.debug("  " + c.getName());
          this.providers.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load account balance provider " + c.getName() + ", skipping",e);
        }
      }
      Logger.info("  found " + this.providers.size() + " provider(s)");
      
      // Wir sortieren die Provider so, dass der Standard-Provider immer als letzter an die Reihe kommt.
      Collections.sort(this.providers,new Comparator<AccountBalanceProvider>() {
        @Override
        public int compare(AccountBalanceProvider o1, AccountBalanceProvider o2)
        {
          
          if (DEFAULT.isInstance(o1))
            return 1;
          if (DEFAULT.isInstance(o2))
            return -1;
          
          // Ansonsten alphabetisch nach Name
          return o1.getName().compareTo(o2.getName());
        }
      });
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no account balance providers found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching for account balance providers",e);
    }
    return this.providers;
  }
  
  /**
   * Liefert den Provider, der Salden für ein Konto ermitteln kann. 
   * @param konto Das Konto, fuer welches Salden gesucht sind.
   * @return Einen speziellen Provider fuer beispielsweise Depots oder den DEFAULT-Provider.
   */
  public AccountBalanceProvider getBalanceProviderForAccount(Konto konto) {
    for(AccountBalanceProvider provider : getProviders()) {
      if(provider.supports(konto)) {
        return provider;
      }
    }
    
    return null; // dieser Fall kann nicht vorkommen, weil der Standard-Provider in der Liste der Provider vorkommen muss.
  }
}

