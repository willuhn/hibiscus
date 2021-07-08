/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIAccountProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service fuer den Zugriff auf die existierenden Accounts und die Erstellung
 * neuer Accounts. Dieser Service soll die direkte Verwendung der Passports abloesen,
 * da sie an das HBCIBackend gebunden sind, wir aber einen Assistenten zum Anlegen neuer
 * Bankzugaenge haben wollen, die unabhaengig vom Backend sind.
 */
@Lifecycle(Type.CONTEXT)
public class AccountService
{
  private List<AccountProvider> providers = null;

  /**
   * Der Primaer-Provider. Der steht immer oben.
   */
  private final static Class<? extends HBCIAccountProvider> PRIMARY = HBCIAccountProvider.class;

  /**
   * Liefert eine Liste der gefundenen Provider.
   * @return Liste der Provider.
   */
  public synchronized List<AccountProvider> getProviders()
  {
    if (this.providers != null)
      return this.providers;

    this.providers = new LinkedList<AccountProvider>();

    try
    {
      Logger.info("loading account providers");
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder().findImplementors(AccountProvider.class);
      for (Class<AccountProvider> c:found)
      {
        try
        {
          Logger.debug("  " + c.getName());
          this.providers.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load account provider " + c.getName() + ", skipping",e);
        }
      }

      // Wir sortieren die Provider so, dass der Primaer-Provider immer Vorrang hat
      Collections.sort(this.providers,new Comparator<AccountProvider>() {
        public int compare(AccountProvider o1, AccountProvider o2)
        {

          if (PRIMARY.isInstance(o1))
            return -1;
          if (PRIMARY.isInstance(o2))
            return 1;

          // Ansonsten alphabetisch nach Name
          return o1.getName().compareTo(o2.getName());
        }
      });
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no account providers found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching for account providers",e);
    }
    return this.providers;
  }
}
