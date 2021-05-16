/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.I18N;

/**
 * Implementierung des Adressbuch-Services.
 */
public class AddressbookServiceImpl extends UnicastRemoteObject implements AddressbookService
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private boolean started = false;
  private Addressbook[] books = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public AddressbookServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#findAddresses(java.lang.String)
   */
  public List findAddresses(String text) throws RemoteException
  {
    Addressbook[] books = getAddressbooks();

    ArrayList<Address> result = new ArrayList<>();
    for (Addressbook book : books)
    {
      if (book.getClass().equals(this.getClass()))
        continue; // WICHTIG: Uns selbst ueberspringen wir, um eine Rekursion zu vermeiden
      List<Address> list = book.findAddresses(text);

      if (list == null)
        continue;

      result.addAll(list);
    }
    return result;
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
    Addressbook[] books = getAddressbooks();

    for (Addressbook book : books)
    {
      if (book.getClass().equals(this.getClass()))
        continue; // WICHTIG: Uns selbst ueberspringen wir, um eine Rekursion zu vermeiden
      Address found = book.contains(address);
      if (found != null)
        return found;
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AddressbookService#getAddressbooks()
   */
  public synchronized Addressbook[] getAddressbooks() throws RemoteException
  {
    if (this.books == null)
    {
      try
      {
        Logger.info("loading addressbooks");
        
        BeanService service = Application.getBootLoader().getBootable(BeanService.class);
        ClassFinder finder = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder();
        Class[] found = finder.findImplementors(Addressbook.class);
        ArrayList<Addressbook> list = new ArrayList<>();

        // Uns selbst tun wir immer zuerst rein.
        // Damit stehen wir immer oben in der Liste
        list.add(this);

        for (Class book : found)
        {
          if (book.equals(this.getClass()))
            continue; // Das sind wir selbst
          try
          {
            Addressbook a = (Addressbook) service.get(book);
            Logger.info("  " + a.getName());
            list.add(a);
          }
          catch (Throwable t)
          {
            Logger.error("unable to load addressbook " + book + ", skipping");
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
        this.books = new Addressbook[0];
      }

    }
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
    Addressbook[] books = getAddressbooks();
    return books != null && books.length > 2;
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
    return this.started;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (isStarted())
    {
      Logger.warn("service already started, skipping request");
      return;
    }
    this.started = true;
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
    this.started = false;
  }
}


/*********************************************************************
 * $Log: AddressbookServiceImpl.java,v $
 * Revision 1.5  2008/10/20 09:18:56  willuhn
 * @B BUGZILLA 641
 *
 * Revision 1.4  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.3  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.2  2007/04/20 14:55:31  willuhn
 * @C s/findAddress/findAddresses/
 *
 * Revision 1.1  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 **********************************************************************/