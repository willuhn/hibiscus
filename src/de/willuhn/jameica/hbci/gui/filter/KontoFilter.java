/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/filter/KontoFilter.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/06/08 15:54:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.filter;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Mit diesem Filter koennen einzelne Konten bei der Suche
 * ausgefiltert werden. Das wird z.Bsp. genutzt, um bei
 * Auslandsueberweisungen nur jene Konten anzuzeigen, die
 * eine IBAN besitzen.
 */
public interface KontoFilter extends Filter<Konto>
{
  /**
   * @see de.willuhn.jameica.hbci.gui.filter.Filter#accept(java.lang.Object)
   */
  public boolean accept(Konto konto) throws RemoteException;
  
  /**
   * Filter, der alle Konten zulaesst.
   */
  public final static KontoFilter ALL = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      return true;
    }
  };
  
  /**
   * Filter, der nur aktive Konten zulaesst.
   */
  public final static KontoFilter ACTIVE = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null)
        return false;
      
      int flags = konto.getFlags();
      return ((flags & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED) &&
             ((flags & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE);
    }
  };

  /**
   * Filter, der nur aktive Konten zulaesst, die eine IBAN haben.
   */
  public final static KontoFilter FOREIGN = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null)
        return false;
      
      String iban = konto.getIban();

      int flags = konto.getFlags();
      return ((flags & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED) &&
             ((flags & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE) &&
             (iban != null && iban.length() > 0 && iban.length() <= HBCIProperties.HBCI_IBAN_MAXLENGTH);
    }
  }; 
}


/**********************************************************************
 * $Log: KontoFilter.java,v $
 * Revision 1.3  2010/06/08 15:54:45  willuhn
 * @B BUGZILLA 871 - nicht nur pruefen, ob die IBAN lang genug ist sondern auch, ob sie laenger als 0 Zeichen ist
 *
 * Revision 1.2  2010/04/22 15:43:06  willuhn
 * @B Debugging
 * @N Kontoliste aktualisieren
 *
 * Revision 1.1  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/
