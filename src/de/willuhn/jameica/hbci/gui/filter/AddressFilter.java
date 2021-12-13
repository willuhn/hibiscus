/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.filter;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;

/**
 * Mit diesem Filter koennen einzelne Adressen bei der Suche
 * ausgefiltert werden. Das wird z.Bsp. genutzt, um bei
 * Auslandsueberweisungen nur jene Adressen anzuzeigen, die
 * eine IBAN besitzen.
 */
public interface AddressFilter extends Filter<Address>
{
  @Override
  public boolean accept(Address address) throws RemoteException;
  
  /**
   * Adressfilter, der alle Adressen zulaesst.
   */
  public final static AddressFilter ALL = new AddressFilter()
  {
    @Override
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
    @Override
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
    @Override
    public boolean accept(Address address) throws RemoteException
    {
      if (address == null)
        return false;

      String iban = address.getIban();
      return iban != null && iban.length() > 0 &&
             iban.length() <= HBCIProperties.HBCI_IBAN_MAXLENGTH;
    }
  }; 
}


/**********************************************************************
 * $Log: AddressFilter.java,v $
 * Revision 1.3  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.2  2010/04/11 22:05:40  willuhn
 * @N virtuelle Konto-Adressen in SEPA-Auftraegen beruecksichtigen
 *
 * Revision 1.1  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/
