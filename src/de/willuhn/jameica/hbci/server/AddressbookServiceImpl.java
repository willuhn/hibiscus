/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AddressbookServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/20 14:55:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des Adressbuch-Services.
 */
public class AddressbookServiceImpl extends UnicastRemoteObject implements AddressbookService
{
  private transient I18N i18n = null;
  private Addressbook[] books = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public AddressbookServiceImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#findAddresses(java.lang.String)
   */
  public GenericIterator findAddresses(String text) throws RemoteException
  {
    ArrayList result = new ArrayList();
    for (int i=0;i<this.books.length;++i)
    {
      if (books[i].getClass().equals(this.getClass()))
        continue; // WICHTIG: Uns selbst ueberspringen wir, um eine Rekursion zu vermeiden
      GenericIterator list = this.books[i].findAddresses(text);
      if (list == null || list.size() == 0)
        continue;
      result.addAll(PseudoIterator.asList(list));
    }
    return PseudoIterator.fromArray((GenericObject[])result.toArray(new GenericObject[result.size()]));
  }

  /**
   * Die Funktion liefert die erste gefundene Adresse aus den Adressbuechern.
   * Falls die Adresse in mehreren Adressbuechern existiert, aus welchem
   * der Adressbuecher die Adresse verwendet wird. Die Funktion sollte daher
   * nur verwendet werden, um <b>ueberhaupt</b> festzustellen, ob die Adresse existiert.
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#contains(de.willuhn.jameica.hbci.rmi.Address)
   */
  public Address contains(Address address) throws RemoteException
  {
    for (int i=0;i<this.books.length;++i)
    {
      if (books[i].getClass().equals(this.getClass()))
        continue; // WICHTIG: Uns selbst ueberspringen wir, um eine Rekursion zu vermeiden
      Address found = books[i].contains(address);
      if (found != null)
        return found;
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AddressbookService#getAddressbooks()
   */
  public Addressbook[] getAddressbooks() throws RemoteException
  {
    return this.books;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AddressbookService#hasExternalAddressbooks()
   */
  public boolean hasExternalAddressbooks() throws RemoteException
  {
    // Adressbuch 1 sind wir selbst
    // Adressbuch 2 ist das Hibiscus-Adressbuch
    // --> Diese beiden existieren immer in Hibiscus
    // Existiert noch mindestens eins mehr, dann haben wir externe 
    return this.books != null && this.books.length > 2;
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Alle Adressbücher");
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.books != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    
    try
    {
      Logger.info("loading addressbooks");
      Class[] found = Application.getClassLoader().getClassFinder().findImplementors(Addressbook.class);
      ArrayList list = new ArrayList();
      
      // Uns selbst tun wir immer zuerst rein.
      // Damit stehen wir immer oben in der Liste
      list.add(this);
      
      for(int i=0;i<found.length;++i)
      {
        if (found[i].equals(this.getClass()))
          continue; // Das sind wir selbst
        try
        {
          Addressbook a = (Addressbook) found[i].newInstance();
          Logger.info("  " + a.getName());
          list.add(a);
        }
        catch (Throwable t)
        {
          Logger.error("unable to load addressbook " + found[i] + ", skipping");
        }
      }
      this.books = (Addressbook[]) list.toArray(new Addressbook[list.size()]);
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no addressbooks found, suspekt!");
    }
    
    // Sollte eigentlich nie passieren. Daher nur zur Sicherheit
    if (this.books == null || this.books.length == 0)
    {
      Logger.error("no addressbooks found, suspekt!");
      // Dann tun wir uns selbst rein ;)
      // Dort werden dann zwar nie Adressen gefunden. Aber wenigstens existiert eins.
      this.books = new Addressbook[]{this};
    }
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    this.books = null;
  }
}


/*********************************************************************
 * $Log: AddressbookServiceImpl.java,v $
 * Revision 1.2  2007/04/20 14:55:31  willuhn
 * @C s/findAddress/findAddresses/
 *
 * Revision 1.1  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 **********************************************************************/