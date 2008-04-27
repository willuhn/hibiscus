/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AddressbookHibiscusImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/04/27 22:22:56 $
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
import java.util.List;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
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
    
    DBIterator list = Settings.getDBService().createList(HibiscusAddress.class);
    list.addFilter("kontonummer like ?", new Object[]{"%" + address.getKontonummer()}); // Fuehrende Nullen ignorieren
    list.addFilter("blz=?",              new Object[]{address.getBLZ()});
    list.addFilter("LOWER(name)=?",      new Object[]{address.getName().toLowerCase()});
    if (list.hasNext())
      return (Address) list.next();
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#findAddresses(java.lang.String)
   */
  public List findAddresses(String text) throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(HibiscusAddress.class);
    if (text != null && text.length() > 0)
    {
      // Gross-Kleinschreibung ignorieren wir
      text = "%" + text.toLowerCase() + "%";
      list.addFilter("(kontonummer LIKE ? OR " +
                     " blz LIKE ? OR " +
                     " LOWER(name) LIKE ? OR " +
                     " LOWER(kommentar) LIKE ?)",new Object[]{text,text,text,text});
    }
    list.setOrder("ORDER by LOWER(name)");
    return PseudoIterator.asList(list);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Addressbook#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Hibiscus-Adressbuch");
  }

}


/*********************************************************************
 * $Log: AddressbookHibiscusImpl.java,v $
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