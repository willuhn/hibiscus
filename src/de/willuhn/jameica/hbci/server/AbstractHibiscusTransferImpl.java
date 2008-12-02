/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractHibiscusTransferImpl.java,v $
 * $Revision: 1.10 $
 * $Date: 2008/12/02 10:52:23 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung von Geld-Transfers zwischen Konten.
 */
public abstract class AbstractHibiscusTransferImpl extends AbstractDBObject implements HibiscusTransfer
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private transient String[] verwendungszwecke = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public AbstractHibiscusTransferImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  	try {
			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));
			if (getKonto().isNewObject())
				throw new ApplicationException(i18n.tr("Bitte speichern Sie zunächst das Konto"));

			if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie die Kontonummer des Gegenkontos ein"));
			
			if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie die BLZ des Gegenkontos ein"));

      // BUGZILLA 280
      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkChars(getGegenkontoBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);

      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getGegenkontoName() == null || getGegenkontoName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));

      int blzLen = getGegenkontoBLZ().length();
      if (blzLen != HBCIProperties.HBCI_BLZ_LENGTH)
        throw new ApplicationException(i18n.tr("Ungültige BLZ \"{0}\". Muss {1} Stellen lang sein.", new String[]{getGegenkontoBLZ(),""+HBCIProperties.HBCI_BLZ_LENGTH}));
      
      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);

      // BUGZILLA 163
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);

      if (!HBCIProperties.checkAccountCRC(getGegenkontoBLZ(),getGegenkontoNummer()))
				throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));
				
			if (getZweck() == null || "".equals(getZweck()))
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Verwendungszweck ein"));

      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkLength(getZweck2(), HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);

			HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      HBCIProperties.checkChars(getZweck2(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      
      AccountUtil.checkMaxUsage(this);
  	}
  	catch (RemoteException e)
  	{
  		Logger.error("error while checking ueberweisung",e);
  		throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Überweisung."));
  	}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
		if ("konto_id".equals(field))
			return Konto.class;
    return super.getForeignObject(field);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
   */
  public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
		setAttribute("konto_id",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException {
		setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException {
		return (String) getAttribute("empfaenger_blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException {
		return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoNummer(java.lang.String)
   */
  public void setGegenkontoNummer(String konto) throws RemoteException {
		setAttribute("empfaenger_konto",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException {
		setAttribute("empfaenger_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    try
    {
      this.transactionBegin();

      Konto k = this.getKonto();
      
      VerwendungszweckUtil.delete(this); // Loescht die erweiterten Verwendungszwecke
      super.delete();
      
      if (k != null)
      {
        String[] params = new String[] {
          getGegenkontoName(),
          getGegenkontoNummer(),
          getGegenkontoBLZ(),
          k.getWaehrung(),
          HBCI.DECIMALFORMAT.format(getBetrag())
        };
        k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}, BLZ {2}] {3} {4} gelöscht",params),Protokoll.TYP_SUCCESS);
      }
      
      this.transactionCommit();
    }
    catch (RemoteException re)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw re;
    }
    catch (ApplicationException ae)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw ae;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkonto(de.willuhn.jameica.hbci.rmi.Address)
   */
  public void setGegenkonto(Address e) throws RemoteException
  {
  	if (e == null)
  		return;
    setGegenkontoBLZ(e.getBlz());
  	setGegenkontoNummer(e.getKontonummer());
  	setGegenkontoName(e.getName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getWeitereVerwendungszwecke()
   */
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    if (this.verwendungszwecke == null)
      this.verwendungszwecke = VerwendungszweckUtil.get(this);
    return this.verwendungszwecke;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setWeitereVerwendungszwecke(java.lang.String[])
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    this.verwendungszwecke = list;
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    try
    {
      this.transactionBegin();
      super.store();
      
      if (this.verwendungszwecke != null)
        VerwendungszweckUtil.store(this,this.verwendungszwecke);

      Konto k = this.getKonto();
      String[] params = new String[]
      {
        getGegenkontoName(),
        getGegenkontoNummer(),
        getGegenkontoBLZ(),
        k.getWaehrung(),
        HBCI.DECIMALFORMAT.format(getBetrag())
      };
      k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}, BLZ {2}] {3} {4} gespeichert",params),Protokoll.TYP_SUCCESS);
      
      this.transactionCommit();
    }
    catch (RemoteException re)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw re;
    }
    catch (ApplicationException ae)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw ae;
    }
  }
}


/**********************************************************************
 * $Log: AbstractHibiscusTransferImpl.java,v $
 * Revision 1.10  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.9  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.8  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.7  2008/09/17 23:44:29  willuhn
 * @B SQL-Query fuer MaxUsage-Abfrage korrigiert
 *
 * Revision 1.6  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.5  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.4  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 **********************************************************************/