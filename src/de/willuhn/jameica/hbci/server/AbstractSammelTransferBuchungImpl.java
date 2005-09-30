/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractSammelTransferBuchungImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer einzelnen Buchung eines Sammel-Auftrages.
 * @author willuhn
 */
public abstract class AbstractSammelTransferBuchungImpl extends AbstractDBObject implements SammelTransferBuchung, Duplicatable
{

  private I18N i18n;

  /**
   * @throws java.rmi.RemoteException
   */
  public AbstractSammelTransferBuchungImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try {
      if (getBetrag() == 0.0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Kontonummer des Gegenkontos ein"));
      
      if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die BLZ des Gegenkontos ein"));

      if (getGegenkontoName() == null || getGegenkontoName().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));

			if (getGegenkontoName().length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
				throw new ApplicationException(i18n.tr("Bitte geben Sie maximal {0} Zeichen für den Namen des Kontoinhabers ein",""+HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));

      if (!HBCIProperties.checkAccountCRC(getGegenkontoBLZ(),getGegenkontoNummer()))
        throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));
        
      if (getZweck() == null || "".equals(getZweck()))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Verwendungszweck ein"));

      if (getZweck().length() > HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH)
        throw new ApplicationException(i18n.tr("Bitten geben Sie als Verwendungszweck maximal {0} Zeichen an",""+HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH));
        
      if (getZweck2() != null && getZweck2().length() > HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH)
        throw new ApplicationException(i18n.tr("Bitten geben Sie als weiteren Verwendungszweck maximal {0} Zeichen an",""+HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH));

      HBCIProperties.checkChars(getZweck());
      HBCIProperties.checkChars(getZweck2());
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammeltransferbuchung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Buchung."));
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_nr");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_blz");
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("this".equals(arg0))
      return this;
    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkontoNummer(java.lang.String)
   */
  public void setGegenkontoNummer(String kontonummer) throws RemoteException
  {
    setAttribute("gegenkonto_nr",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    setAttribute("gegenkonto_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException
  {
    setAttribute("gegenkonto_name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkonto(de.willuhn.jameica.hbci.rmi.Adresse)
   */
  public void setGegenkonto(Adresse gegenkonto) throws RemoteException
  {
    setGegenkontoBLZ(gegenkonto == null ? null : gegenkonto.getBLZ());
    setGegenkontoName(gegenkonto == null ? null : gegenkonto.getName());
    setGegenkontoNummer(gegenkonto == null ? null : gegenkonto.getKontonummer());
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getGegenkonto()
	 */
	public Adresse getGegenkonto() throws RemoteException
	{
		// Wir schauen erstmal, ob wir diese Adresse in der Datenbank schon haben.
		Logger.debug("checking if we allready have this address in the database");
		DBIterator list = getService().createList(Adresse.class);
		list.addFilter("kontonummer='"+getGegenkontoNummer()+"'");
		list.addFilter("blz='"+getGegenkontoBLZ()+"'");
		list.addFilter("name='"+getGegenkontoName()+"'");
		if (list.hasNext())
		{
			Logger.debug("found one, returning this one");
			return (Adresse) list.next();
		}

		// ne, nix gefunden
		Logger.debug("no address found, creating a new one");
		Adresse a = (Adresse) getService().createObject(Adresse.class,null);
		a.setBLZ(getGegenkontoBLZ());
		a.setName(getGegenkontoName());
		a.setKontonummer(getGegenkontoNummer());
		return a;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getBetrag()
   */
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getBetrag()
   */
  public double getBetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("betrag");
    if (d == null)
      return 0;
    return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getZweck()
   */
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getZweck()
   */
  public String getZweck() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getZweck2()
   */
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getZweck2()
   */
  public String getZweck2() throws RemoteException
  {
    return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setBetrag(double)
   */
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setZweck(java.lang.String)
   */
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException
  {
    setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setZweck2(java.lang.String)
   */
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException
  {
    setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException
  {
    SammelTransferBuchung b = (SammelTransferBuchung) getService().createObject(this.getClass(),null);
    b.setBetrag(getBetrag());
    b.setGegenkontoBLZ(getGegenkontoBLZ());
    b.setGegenkontoNummer(getGegenkontoNummer());
    b.setGegenkontoName(getGegenkontoName());
    b.setSammelTransfer(getSammelTransfer());
    b.setZweck(getZweck());
    b.setZweck2(getZweck2());
    return (Duplicatable) b;
  }
}

/*****************************************************************************
 * $Log: AbstractSammelTransferBuchungImpl.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.5  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.4  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/