/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/filter/KontoFilter.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/06 12:35:48 $
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
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;

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
  
  /**
   * Filter, der nur Konten zulaesst, fuer die Synchronisierungsoptionen aktiviert sind oder die prinzipiell
   * synchronisierbar sind.
   */
  public final static KontoFilter SYNCED = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_DISABLED))
        return false;

      // Wenn es ein Online-Konto ist, kann es prinzipiell gesynct werden
      if (!konto.hasFlag(Konto.FLAG_OFFLINE))
        return true;
      
      // Ist zwar ein Offline-Konto. Aber wir schauen mal, ob es
      // synchronisiert werden kann.
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine = service.get(SynchronizeEngine.class);
      return engine.supports(SynchronizeJobKontoauszug.class,konto);
    }
  }; 

}


/**********************************************************************
 * $Log: KontoFilter.java,v $
 * Revision 1.4  2011/05/06 12:35:48  willuhn
 * @N Neuer Konto-Auswahldialog mit Combobox statt Tabelle. Ist ergonomischer.
 *
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
