/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AddressbookHibiscusImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/04/11 21:57:08 $
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
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
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
      result.addAll(findAddresses(list,text));
    }

    // 2) In den eigenen Konten suchen
    {
      DBIterator list = Settings.getDBService().createList(Konto.class);
      List<Konto> konten = findAddresses(list,text);
      for (Konto k:konten)
      {
        result.add(new KontoAddress(k));
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
   * Fuegt die Filterkriterien zum Iterator hinzu, fuehrt ihn aus und liefert das Ergebnis.
   * @param it der Iterator.
   * @param text der Suchbegriff. 
   * @return die Treffer-Liste.
   * @throws RemoteException
   */
  private List findAddresses(DBIterator it, String text) throws RemoteException
  {
    if (text != null && text.length() > 0)
    {
      // Gross-Kleinschreibung ignorieren wir
      text = "%" + text.toLowerCase() + "%";
      it.addFilter("(kontonummer LIKE ? OR " +
                   " blz LIKE ? OR " +
                   " LOWER(name) LIKE ? OR " +
                   " LOWER(kommentar) LIKE ?)",new Object[]{text,text,text,text});
    }
    it.setOrder("ORDER by LOWER(name)");
    return PseudoIterator.asList(it);
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
     * Liefert die BIC.
     * Steht zwar nicht im Interface - wird aber via Bean-Property (Reflection) auch angezeigt.
     * @return die BIC.
     * @throws RemoteException
     */
    public String getBic() throws RemoteException
    {
      return this.konto.getBic();
    }

    /**
     * Liefert die IBAN.
     * Steht zwar nicht im Interface - wird aber via Bean-Property (Reflection) auch angezeigt.
     * @return die IBAN.
     * @throws RemoteException
     */
    public String getIban() throws RemoteException
    {
      return this.konto.getIban();
    }


  }

}


/*********************************************************************
 * $Log: AddressbookHibiscusImpl.java,v $
 * Revision 1.7  2010/04/11 21:57:08  willuhn
 * @N Anzeige der eigenen Konten im Adressbuch als "virtuelle" Adressen. Basierend auf Ralfs Patch.
 *
 * Revision 1.6  2009/05/06 16:23:24  willuhn
 * @B NPE
 *
 * Revision 1.5  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.4  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.3  2007/04/24 17:52:17  willuhn
 * @N Bereits in den Umsatzdetails erkennen, ob die Adresse im Adressbuch ist
 * @C Gross-Kleinschreibung in Adressbuch-Suche
 *
 * Revision 1.2  2007/04/23 18:17:12  willuhn
 * @B Falsche Standardreihenfolge
 *
 * Revision 1.1  2007/04/23 18:07:15  willuhn
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