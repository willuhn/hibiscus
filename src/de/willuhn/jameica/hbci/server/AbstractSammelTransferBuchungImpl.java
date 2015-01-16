/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractSammelTransferBuchungImpl.java,v $
 * $Revision: 1.22 $
 * $Date: 2011/10/18 09:28:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
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
public abstract class AbstractSammelTransferBuchungImpl extends AbstractHibiscusDBObject implements SammelTransferBuchung, Duplicatable
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @throws java.rmi.RemoteException
   */
  public AbstractSammelTransferBuchungImpl() throws RemoteException
  {
    super();
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
      if (getSammelTransfer() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie den zugehörigen Sammel-Auftrag aus."));

      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Kontonummer des Gegenkontos ein"));
      
      if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die BLZ des Gegenkontos ein"));

      // BUGZILLA 280
      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkChars(getGegenkontoBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);

      if (StringUtils.trimToNull(getGegenkontoName()) == null)
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
      
      VerwendungszweckUtil.checkMaxUsage(this);
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
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_nr");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
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
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
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
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("betrag");
    if (d == null)
      return 0;
    return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
   */
  public String getZweck2() throws RemoteException
  {
    return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException
  {
    setAttribute("zweck",zweck);
  }

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
    b.setWeitereVerwendungszwecke(getWeitereVerwendungszwecke());
    b.setTextSchluessel(getTextSchluessel());
    return (Duplicatable) b;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getTextSchluessel()
   */
  public String getTextSchluessel() throws RemoteException
  {
    return (String) getAttribute("typ");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setTextSchluessel(java.lang.String)
   */
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    setAttribute("typ",schluessel);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getWeitereVerwendungszwecke()
   */
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    return VerwendungszweckUtil.split((String)this.getAttribute("zweck3"));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setWeitereVerwendungszwecke(java.lang.String[])
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    setAttribute("zweck3",VerwendungszweckUtil.merge(list));
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getWarnung()
   */
  public String getWarnung() throws RemoteException
  {
    return (String) getAttribute("warnung");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setWarnung(java.lang.String)
   */
  public void setWarnung(String warnung) throws RemoteException
  {
    setAttribute("warnung",warnung);
  }
}
