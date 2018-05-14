/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.zip.CRC32;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;

/**
 * Implementierung eines Dauerauftrags.
 */
public class DauerauftragImpl extends AbstractBaseDauerauftragImpl implements Dauerauftrag
{
  /**
   * ct.
   * @throws RemoteException
   */
  public DauerauftragImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "dauerauftrag";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
		String ersteZahlung  = getErsteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getErsteZahlung());
		String letzteZahlung = getLetzteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getLetzteZahlung());
		String s = getTurnus().getChecksum() +
							 getBetrag() +
							 getTextSchluessel() +
							 getGegenkontoBLZ() +
							 getGegenkontoNummer() +
							 getGegenkontoName() +
							 getKonto().getChecksum() +
							 getZweck() +
							 getZweck2() +
							 ersteZahlung +
							 letzteZahlung;
		CRC32 crc = new CRC32();
		crc.update(s.getBytes());
		return crc.getValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getTextSchluessel()
   */
  public String getTextSchluessel() throws RemoteException
  {
    return (String) getAttribute("typ");
  }
}
