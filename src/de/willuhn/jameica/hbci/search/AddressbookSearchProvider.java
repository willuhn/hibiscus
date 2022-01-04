/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.search;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Searchproviders fuer das Hibiscus-Adressbuch.
 */
public class AddressbookSearchProvider implements SearchProvider
{
  @Override
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Adressbuch");
  }

  @Override
  public List search(String search) throws RemoteException,
      ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;

    try
    {
      AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      List result = service.findAddresses(search);
      if (result == null)
        return null;
      
      ArrayList al = new ArrayList();
      for (int i=0;i<result.size();++i)
      {
        al.add(new MyResult((Address) result.get(i)));
      }
      return al;
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to search in addressbook",e);
    }
    return null;
  }

  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Address address = null;
    
    /**
     * ct.
     * @param a
     */
    private MyResult(Address a)
    {
      this.address = a;
    }

    @Override
    public void execute() throws RemoteException, ApplicationException
    {
      new EmpfaengerNew().handleAction(this.address);
    }

    @Override
    public String getName()
    {
      try
      {
        String comment = this.address.getKommentar();
        if (comment != null && comment.length() > 0)
          return this.address.getName() + " (" + comment + ")";
        return this.address.getName();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
    
  }

}


/*********************************************************************
 * $Log: AddressbookSearchProvider.java,v $
 * Revision 1.1  2008/09/03 11:13:51  willuhn
 * @N Mehr Suchprovider
 *
 **********************************************************************/