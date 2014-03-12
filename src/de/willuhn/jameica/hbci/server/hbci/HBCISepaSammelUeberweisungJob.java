/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Sammelueberweisung".
 */
public class HBCISepaSammelUeberweisungJob extends AbstractHBCISepaSammelTransferJob<SepaSammelUeberweisung>
{
  /**
	 * ct.
   * @param u die auszufuehrende Sammel-Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISepaSammelUeberweisungJob(SepaSammelUeberweisung u) throws ApplicationException, RemoteException
	{
    super(u);
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    return "MultiUebSEPA";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("SEPA-Sammelüberweisung {0}",getSammelTransfer().getBezeichnung());
  }
}
