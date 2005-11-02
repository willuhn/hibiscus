/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISammelUeberweisungJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/11/02 17:33:31 $
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
}


/**********************************************************************
 * $Log: HBCISammelUeberweisungJob.java,v $
 * Revision 1.2  2005/11/02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/