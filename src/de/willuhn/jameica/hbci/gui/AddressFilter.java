/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/Attic/AddressFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/13 00:25:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;

/**
 * Mit diesem Filter koennen einzelne Adressen bei der Suche
 * ausgefiltert werden. Das wird z.Bsp. genutzt, um bei
 * Auslandsueberweisungen nur jene Adressen anzuzeigen, die
 * eine IBAN besitzen.
 */
public interface AddressFilter
{
  /**
   * Prueft, ob die Adresse angezeigt werden soll oder nicht.
   * @param address die zu pruefende Adresse.
   * @return true, wenn sie ok ist und angezeigt werden soll.
   * False, wenn sie uebersprungen werden soll.
   * @throws RemoteException
   */
  public boolean accept(Address address) throws RemoteException;
  
  /**
   * Adressfilter, der alle Adressen zulaesst.
   */
  public final static AddressFilter ALL = new AddressFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Address address) throws RemoteException
    {
      return true;
    }
  };
  
  /**
   * Adressfilter, der nur Adressen mit deutscher Bankverbindung zulaesst.
   */
  public final static AddressFilter INLAND = new AddressFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Address address) throws RemoteException
    {
      if (address == null)
        return false;
      String blz = address.getBlz();
      String kto = address.getKontonummer();
      return blz != null && kto != null &&
             blz.length() == HBCIProperties.HBCI_BLZ_LENGTH &&
             kto.length() <= HBCIProperties.HBCI_KTO_MAXLENGTH_HARD;
    }
  };
  
  /**
   * Adressfilter, der nur Adressen zulaesst, die eine IBAN haben.
   */
  public final static AddressFilter FOREIGN = new AddressFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Address address) throws RemoteException
    {
      if (address == null)
        return false;
      
      if (!(address instanceof HibiscusAddress))
        return false;
      
      HibiscusAddress a = (HibiscusAddress) address;
      String iban = a.getIban();
      return iban != null && 
             iban.length() <= HBCIProperties.HBCI_IBAN_MAXLENGTH;
    }
  }; 
}


/**********************************************************************
 * $Log: AddressFilter.java,v $
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/
