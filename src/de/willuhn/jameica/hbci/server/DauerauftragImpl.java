/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.zip.CRC32;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Dauerauftrags.
 */
public class DauerauftragImpl extends AbstractBaseDauerauftragImpl implements Dauerauftrag
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
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
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try {
      Date ersteZahlung = getErsteZahlung();
      Date letzteZahlung = getLetzteZahlung();
      
      // BUGZILLA 197
      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getTurnus() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Zahlungsturnus aus"));

      if (ersteZahlung == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie ein Datum für die erste Zahlung an"));

      // Und jetzt noch checken, dass sich das Datum der letzten Zahlung
      // nicht vor der ersten Zahlung befindet
      // BUGZILLA 371
      if (letzteZahlung != null && letzteZahlung.before(ersteZahlung))
        throw new ApplicationException(i18n.tr("Bei Angabe eines Datum für die letzte Zahlung ({0}) muss dieses nach der ersten Zahlung ({1}) liegen", new String[]{HBCI.DATEFORMAT.format(letzteZahlung), HBCI.DATEFORMAT.format(ersteZahlung)}));
    }
    catch (RemoteException e)
    {
      Logger.error("error while insert check in DauerAuftrag",e);
      throw new ApplicationException(i18n.tr("Fehler bei der Prüfung des Dauerauftrags"));
    }
    super.insertCheck();
  }


  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getTextSchluessel()
   */
  public String getTextSchluessel() throws RemoteException
  {
    return (String) getAttribute("typ");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setTextSchluessel(java.lang.String)
   */
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    setAttribute("typ",schluessel);
  }
}
