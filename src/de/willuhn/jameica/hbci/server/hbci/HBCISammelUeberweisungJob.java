/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISammelUeberweisungJob.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/09/02 10:21:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Sammel-Ueberweisung".
 */
public class HBCISammelUeberweisungJob extends AbstractHBCISammelTransferJob
{

  /**
	 * ct.
   * @param ueberweisung die auszufuehrende Sammel-Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISammelUeberweisungJob(SammelUeberweisung ueberweisung) throws ApplicationException, RemoteException
	{
    super(ueberweisung);
    setJobParam("data",Converter.HibiscusSammelUeberweisung2DTAUS(ueberweisung).toString());
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "MultiUeb";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    String name = getSammelTransfer().getBezeichnung();
    return i18n.tr("Sammel-Überweisung {0}",name);
  }
}


/**********************************************************************
 * $Log: HBCISammelUeberweisungJob.java,v $
 * Revision 1.4  2010/09/02 10:21:06  willuhn
 * @N BUGZILLA 899
 *
 * Revision 1.3  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.2  2005/11/02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/