/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISammelLastschriftJob.java,v $
 * $Revision: 1.3 $
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

import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Sammel-Lastschrift".
 */
public class HBCISammelLastschriftJob extends AbstractHBCISammelTransferJob
{

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Sammel-Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISammelLastschriftJob(SammelLastschrift lastschrift) throws ApplicationException, RemoteException
	{
    super(lastschrift);
    setJobParam("data",Converter.HibiscusSammelLastschrift2DTAUS(lastschrift).toString());
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "MultiLast";
  }
  
}


/**********************************************************************
 * $Log: HBCISammelLastschriftJob.java,v $
 * Revision 1.3  2005/11/02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.2  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.1  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 **********************************************************************/