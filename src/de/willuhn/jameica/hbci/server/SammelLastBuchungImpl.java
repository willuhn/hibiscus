/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelLastBuchungImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/28 16:28:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer einzelnen Buchung einer Sammellastschrift.
 * @author willuhn
 */
public class SammelLastBuchungImpl extends AbstractDBObject implements SammelLastBuchung
{

  private I18N i18n;
  /**
   * @throws java.rmi.RemoteException
   */
  public SammelLastBuchungImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "slastbuchung";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
    // kann geloescht werden
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

      if (!HBCIUtils.checkAccountCRC(getGegenkontoBLZ(),getGegenkontoNummer()))
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
      Logger.error("error while checking sammellastbuchung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Lastschrift."));
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
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("slastschrift_id".equals(arg0))
      return SammelLastschrift.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getSammelLastschrift()
   */
  public SammelLastschrift getSammelLastschrift() throws RemoteException
  {
    return (SammelLastschrift) getAttribute("slastschrift_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setSammelLastschrift(de.willuhn.jameica.hbci.rmi.SammelLastschrift)
   */
  public void setSammelLastschrift(SammelLastschrift s) throws RemoteException
  {
    setAttribute("slastschrift_id",s);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_nr");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setGegenkontoNummer(java.lang.String)
   */
  public void setGegenkontoNummer(String kontonummer) throws RemoteException
  {
    setAttribute("gegenkonto_nr",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    setAttribute("gegenkonto_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException
  {
    setAttribute("gegenkonto_name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setGegenkonto(de.willuhn.jameica.hbci.rmi.Adresse)
   */
  public void setGegenkonto(Adresse gegenkonto) throws RemoteException
  {
    setGegenkontoBLZ(gegenkonto == null ? null : gegenkonto.getBLZ());
    setGegenkontoName(gegenkonto == null ? null : gegenkonto.getName());
    setGegenkontoNummer(gegenkonto == null ? null : gegenkonto.getKontonummer());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getBetrag()
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
  public String getZweck() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#getZweck2()
   */
  public String getZweck2() throws RemoteException
  {
    return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException
  {
    setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException
  {
    setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastBuchung#duplicate()
   */
  public SammelLastBuchung duplicate() throws RemoteException
  {
    SammelLastBuchung b = (SammelLastBuchung) getService().createObject(SammelLastBuchung.class,null);
    b.setBetrag(getBetrag());
    b.setGegenkontoBLZ(getGegenkontoBLZ());
    b.setGegenkontoNummer(getGegenkontoNummer());
    b.setGegenkontoName(getGegenkontoName());
    b.setSammelLastschrift(getSammelLastschrift());
    b.setZweck(getZweck());
    b.setZweck2(getZweck2());
    return b;
  }
}

/*****************************************************************************
 * $Log: SammelLastBuchungImpl.java,v $
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/