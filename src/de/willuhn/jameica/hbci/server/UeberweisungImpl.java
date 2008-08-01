/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UeberweisungImpl.java,v $
 * $Revision: 1.41 $
 * $Date: 2008/08/01 11:05:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Eine Ueberweisung.
 */
public class UeberweisungImpl extends AbstractBaseUeberweisungImpl
  implements Ueberweisung
{

  /**
   * @throws RemoteException
   */
  public UeberweisungImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "ueberweisung";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException {
    Ueberweisung u = (Ueberweisung) getService().createObject(Ueberweisung.class,null);
    u.setBetrag(getBetrag());
    u.setGegenkontoBLZ(getGegenkontoBLZ());
    u.setGegenkontoNummer(getGegenkontoNummer());
    u.setGegenkontoName(getGegenkontoName());
    u.setKonto(getKonto());
    u.setZweck(getZweck());
    u.setZweck2(getZweck2());
    u.setTextSchluessel(getTextSchluessel());
    
    u.setTermin(isTerminUeberweisung() ? getTermin() : new Date());
    u.setTerminUeberweisung(isTerminUeberweisung());
    return u;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#isTerminUeberweisung()
   */
  public boolean isTerminUeberweisung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("banktermin");
    return i != null && i.intValue() == 1;
    
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setTerminUeberweisung(boolean)
   */
  public void setTerminUeberweisung(boolean termin) throws RemoteException
  {
    setAttribute("banktermin",termin ? new Integer(1) : null);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractBaseUeberweisungImpl#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException
  {
    // BUGZILLA 370 Termin-Ueberweisungen gelten sofort als faellig
    if (isTerminUeberweisung())
      return !ausgefuehrt();
    
    return super.ueberfaellig();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getTransferTyp()
   */
  public int getTransferTyp() throws RemoteException
  {
    return Transfer.TYP_UEBERWEISUNG;
  }
}


/**********************************************************************
 * $Log: UeberweisungImpl.java,v $
 * Revision 1.41  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.40  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.39  2007/12/04 11:24:38  willuhn
 * @B Bug 509
 *
 * Revision 1.38  2007/11/30 18:37:08  willuhn
 * @B Bug 509
 *
 * Revision 1.37  2007/03/05 09:49:53  willuhn
 * @B Bug 370
 **********************************************************************/