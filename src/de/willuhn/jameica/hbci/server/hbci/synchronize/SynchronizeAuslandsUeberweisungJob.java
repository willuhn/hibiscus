/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeAuslandsUeberweisungJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/13 00:25:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIAuslandsUeberweisungJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen AuslandsUeberweisung.
 */
public class SynchronizeAuslandsUeberweisungJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param ueb
   */
  public SynchronizeAuslandsUeberweisungJob(AuslandsUeberweisung ueb)
  {
    super(ueb);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCIAuslandsUeberweisungJob((AuslandsUeberweisung)getContext())};
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    AuslandsUeberweisung ueb = (AuslandsUeberweisung) getContext();
    Konto k = ueb.getKonto();
    String[] params = new String[] {
        k.getLongName(),
        ueb.getZweck(),
        HBCI.DECIMALFORMAT.format(ueb.getBetrag()),
        k.getWaehrung(),
        ueb.getGegenkontoName()
       };
    return i18n.tr("{0}: ({1}) {2} {3} an {4} überweisen",params);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new AuslandsUeberweisungNew().handleAction(getContext());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring() throws RemoteException
  {
    return false;
  }
}


/*********************************************************************
 * $Log: SynchronizeAuslandsUeberweisungJob.java,v $
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/