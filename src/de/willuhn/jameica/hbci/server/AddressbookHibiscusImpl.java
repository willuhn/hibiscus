/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AddressbookHibiscusImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/04/14 17:44:10 $
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
import java.util.List;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Hibiscus-Adressbuches.
 */
public class AddressbookHibiscusImpl extends UnicastRemoteObject implements Addressbook
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public AddressbookHibiscusImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#contains(de.willuhn.jameica.hbci.rmi.Address)
   */
  public Address contains(Address address) throws RemoteException
  {
    if (address == null)
      return null;
    
    // 1) Im Adressbuch suchen
    {
      DBIterator list = Settings.getDBService().createList(HibiscusAddress.class);
      Address a = (Address) contains(list,address);
      if (a != null)
        return a;
    }

    // 2) In den eigenen Konten suchen
    {
      DBIterator list = Settings.getDBService().createList(Konto.class);
      Konto k = (Konto) contains(list,address);
      if (k != null)
        return new KontoAddress(k);
    }
    
    return null;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#findAddresses(java.lang.String)
   */
  public List findAddresses(String text) throws RemoteException
  {
    List<Address> result = new ArrayList<Address>();

    // 1) Im Adressbuch suchen
    {
      DBIterator list = Settings.getDBService().createList(HibiscusAddress.class);
      if (text != null && text.length() > 0)
      {
        // Gross-Kleinschreibung ignorieren wir
        String s = "%" + text.toLowerCase() + "%";
        list.addFilter("(kontonummer LIKE ? OR " +
                     " blz LIKE ? OR " +
                     " LOWER(kategorie) LIKE ? OR " +
                     " LOWER(name) LIKE ? OR " +
                     " LOWER(kommentar) LIKE ?)",new Object[]{s,s,s,s,s});
      }
      list.setOrder("ORDER by LOWER(name)");
      
      // Iterieren ueber die Adressen um BIC/IBAN zu vervollstaendigen
      while (list.hasNext())
      {
        HibiscusAddress a = (HibiscusAddress) list.next();
        HBCIProperties.completeIBAN(a);
        result.add(a);
      }
    }

    // 2) In den eigenen Konten suchen
    {
      DBIterator list = Settings.getDBService().createList(Konto.class);
      if (text != null && text.length() > 0)
      {
        // Gross-Kleinschreibung ignorieren wir
        String s = "%" + text.toLowerCase() + "%";
        list.addFilter("(kontonummer LIKE ? OR " +
                     " blz LIKE ? OR " +
                     " LOWER(name) LIKE ? OR " +
                     " LOWER(kommentar) LIKE ?)",new Object[]{s,s,s,s});
      }
      while (list.hasNext())
      {
        result.add(new KontoAddress((Konto) list.next()));
      }
    }
    return result;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Hibiscus-Adressbuch");
  }

  /**
   * Fuegt die Filterkriterien zum Iterator hinzu, fuehrt ihn aus und liefert den Treffer.
   * @param it der Iterator.
   * @param a die gesuchte Adresse.
   * @return die gefundene Adresse oder NULL.
   * @throws RemoteException
   */
  private GenericObject contains(DBIterator it, Address a) throws RemoteException
  {
    it.addFilter("kontonummer like ?", new Object[] {"%" + a.getKontonummer()});
    it.addFilter("blz=?",              new Object[] {a.getBlz()});

    String name = a.getName();
    if (name != null)
      it.addFilter("LOWER(name)=?", new Object[] {name.toLowerCase()});
    
    return it.hasNext() ? it.next() : null;
  }

  /**
   * Hilfsklasse, um ein Konto in ein Address-Interface zu packen
   */
  public class KontoAddress implements Address
  {

    private Konto konto = null;

    /**
     * Der Konstruktor erwartet ein Konto-Objekt. Dieses wird dann als Adresse bereitgestellt.
     * @param konto
     */
    private KontoAddress(Konto konto)
    {
      this.konto = konto;
    }

    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getBlz()
     */
    public String getBlz() throws RemoteException
    {
      return this.konto.getBLZ();
    }

    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getKommentar()
     */
    public String getKommentar() throws RemoteException
    {
      return this.konto.getKommentar();
    }

    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getKontonummer()
     */
    public String getKontonummer() throws RemoteException
    {
      return this.konto.getKontonummer();
    }

    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getName()
     */
    public String getName() throws RemoteException
    {
      return this.konto.getName();
    }
    
    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getBic()
     */
    public String getBic() throws RemoteException
    {
      return this.konto.getBic();
    }

    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getIban()
     */
    public String getIban() throws RemoteException
    {
      return this.konto.getIban();
    }

    /**
     * @see de.willuhn.jameica.hbci.rmi.Address#getKategorie()
     */
    public String getKategorie() throws RemoteException
    {
      return i18n.tr("Eigenes Konto");
    }


  }

}
