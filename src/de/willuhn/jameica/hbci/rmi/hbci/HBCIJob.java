/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/hbci/Attic/HBCIJob.java,v $
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
package de.willuhn.jameica.hbci.rmi.hbci;

import de.willuhn.jameica.hbci.rmi.Konto;


/**
 * Basis-Interface fuer alle HBCI-Jobs.
 */
public interface HBCIJob {

	/**
	 * HBCI4Java verwendet intern eindeutige Job-Namen.
	 * Diese Funktion liefert genau den Namen fuer genau den
	 * gewuenschten Job.
   * @return Job-Identifier.
   */
  public String getIdentifier();

	/**
	 * Diese Funktion wird von der HBCIFactory intern aufgerufen.
	 * Sie uebergibt hier den erzeugten HBCI-Job der Abfrage.
   * @param job der erzeugte Job.
   */
  public void setJob(org.kapott.hbci.GV.HBCIJob job);

	/**
	 * Liefert das Konto, fuer das der Job zustaendig ist.
   * @return das Konto.
   */
  public Konto getKonto();
}


/**********************************************************************
 * $Log: HBCIJob.java,v $
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/