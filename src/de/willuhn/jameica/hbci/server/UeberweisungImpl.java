/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UeberweisungImpl.java,v $
 * $Revision: 1.38 $
 * $Date: 2007/11/30 18:37:08 $
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
    
    // Als Termin nehmen wir aber das aktuelle Datum
    u.setTermin(new Date());
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
  
  


}


/**********************************************************************
 * $Log: UeberweisungImpl.java,v $
 * Revision 1.38  2007/11/30 18:37:08  willuhn
 * @B Bug 509
 *
 * Revision 1.37  2007/03/05 09:49:53  willuhn
 * @B Bug 370
 *
 * Revision 1.36  2005/11/30 23:21:06  willuhn
 * @B ObjectNotFoundException beim Abrufen der Dauerauftraege
 *
 * Revision 1.35  2005/11/14 13:08:11  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.34  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.33  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.32  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.31  2005/02/03 23:57:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.30  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 * Revision 1.28  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.24  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.22  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.21  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.20  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.19  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.18  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.16  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.15  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.11  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.10  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.8  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.7  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.6  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.3  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/