/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/AbstractHBCIJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/19 22:05:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import org.kapott.hbci.GV_Result.HBCIJobResult;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.hbci.HBCIJob;

/**
 * Basis-Klasse fuer die HBCI-Jobs.
 */
public abstract class AbstractHBCIJob implements HBCIJob {

	private org.kapott.hbci.GV.HBCIJob job = null;
	private Konto konto = null;

	/**
	 * ct.
   * @param konto
   */
  public AbstractHBCIJob(Konto konto)
	{
		super();
		this.konto = konto;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public abstract String getIdentifier();

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#setJob(org.kapott.hbci.GV.HBCIJob)
   */
  public void setJob(org.kapott.hbci.GV.HBCIJob job) {
  	this.job = job;
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getKonto()
	 */
	public Konto getKonto() {
		return konto;
	}

  /**
   * Liefert das Job-Resultat.
   * @return Job-Resultat.
   */
  HBCIJobResult getJobResult()
	{
		return job.getJobResult();
	}
	
	/**
	 * Liefert den Status-Text.
   * @return
   */
  String getStatusText()
	{
		return getJobResult().getJobStatus().getRetVals()[0].text;
	}

}


/**********************************************************************
 * $Log: AbstractHBCIJob.java,v $
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/