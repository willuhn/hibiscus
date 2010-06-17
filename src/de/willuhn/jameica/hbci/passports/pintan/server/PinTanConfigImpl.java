/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/server/PinTanConfigImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:38:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.pintan.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines in Hibiscus existierenden RDH-Schluessels.
 * @author willuhn
 */
public class PinTanConfigImpl extends UnicastRemoteObject implements PinTanConfig
{

  private Settings settings     = new Settings(PinTanConfig.class);
  private HBCIPassport passport = null;
  private File file             = null;
  
  private I18N i18n             = null;

  /**
   * ct.
   * @param passport
   * @param file
   * @throws RemoteException
   */
  public PinTanConfigImpl(HBCIPassport passport, File file) throws RemoteException
  {
    super();
    this.passport = passport;
    this.file = file;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attribute) throws RemoteException
  {
    if ("blz".equals(attribute))
      return getBLZ();
    if ("bank".equals(attribute))
      return HBCIUtils.getNameForBLZ(getBLZ());
    if ("url".equals(attribute))
      return getURL();
    if ("port".equals(attribute))
      return new Integer(getPort());
    if ("filtertype".equals(attribute))
      return getFilterType();
    if ("hbciversion".equals(attribute))
      return getHBCIVersion();
    if ("customerid".equals(attribute))
      return getCustomerId();
    if ("userid".equals(attribute))
      return getUserId();
    if ("bezeichnung".equals(attribute))
      return getBezeichnung();
    if ("savetan".equals(attribute))
      return new Boolean(getSaveUsedTan());
    if ("showtan".equals(attribute))
      return new Boolean(getShowTan());
    if ("secmech".equals(attribute))
      return getSecMech();
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"blz","bank","url","port","filtertype","hbciversion"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return PinTanConfigFactory.toRelativePath(getFilename());
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "url";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;

    if (this.getID() == null || other.getID() == null)
      return false;

    return getID().equals(other.getID());
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getHBCIVersion()
   */
  public String getHBCIVersion() throws RemoteException
  {
    return settings.getString(getID() + ".hbciversion",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setHBCIVersion(java.lang.String)
   */
  public void setHBCIVersion(String version) throws RemoteException
  {
    settings.setAttribute(getID() + ".hbciversion",version);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getURL()
   */
  public String getURL() throws RemoteException
  {
  	return passport.getHost();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setURL(java.lang.String)
   */
  public void setURL(String url) throws RemoteException
  {
  	if (url == null || url.length() == 0)
  	{
  		Logger.warn("no url entered");
			return;
  	}
  	if (url.startsWith("https://"))
    {
      Logger.warn("URL entered with https:// prefix, cutting");
  		url = url.substring(8);
    }
    Logger.info("saving URL " + url);
    passport.setHost(url);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getPort()
   */
  public int getPort() throws RemoteException
  {
    return passport.getPort().intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setPort(int)
   */
  public void setPort(int port) throws RemoteException
  {
    passport.setPort(new Integer(port));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getFilterType()
   */
  public String getFilterType() throws RemoteException
  {
    return passport.getFilterType();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setFilterType(java.lang.String)
   */
  public void setFilterType(String type) throws RemoteException
  {
    passport.setFilterType(type);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getBLZ()
   */
  public String getBLZ() throws RemoteException
  {
    return passport.getBLZ();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getCustomerId()
   */
  public String getCustomerId() throws RemoteException
  {
    return passport.getCustomerId();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setCustomerId(java.lang.String)
   */
  public void setCustomerId(String customer) throws RemoteException
  {
    passport.setCustomerId(customer);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getUserId()
   */
  public String getUserId() throws RemoteException
  {
    return passport.getUserId();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setUserId(java.lang.String)
   */
  public void setUserId(String user) throws RemoteException
  {
    passport.setUserId(user);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getFilename()
   */
  public String getFilename() throws RemoteException
  {
    return file.getAbsolutePath();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getPassport()
   */
  public HBCIPassport getPassport() throws RemoteException
  {
    return passport;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getBezeichnung()
   */
  public String getBezeichnung() throws RemoteException
  {
    return settings.getString(getID() + ".bezeichnung",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setBezeichnung(java.lang.String)
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    settings.setAttribute(getID() + ".bezeichnung",bezeichnung);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getKonten()
   */
  public Konto[] getKonten() throws RemoteException
  {
    /////////////////////////////////////////////////////////////////
    // BUGZILLA 314 Migration
    String id = settings.getString(getID() + ".konto",null);
    if (id != null && id.length() > 0)
    {
      settings.setAttribute(getID() + ".konto",(String) null); // Einzelwert loeschen
      settings.setAttribute(getID() + ".konto",new String[]{id}); // Als Array neu speichern
    }
    /////////////////////////////////////////////////////////////////

    // Und jetzt laden wir die Liste neu
    String[] ids = settings.getList(getID() + ".konto",null);
    if (ids == null || ids.length == 0)
      return null;
    
    ArrayList konten = new ArrayList();
    for (int i=0;i<ids.length;++i)
    {
      try
      {
        konten.add(de.willuhn.jameica.hbci.Settings.getDBService().createObject(Konto.class,ids[i]));
      }
      catch (ObjectNotFoundException noe)
      {
        Logger.warn("konto " + ids[i] + " does not exist, skipping");
      }
      catch (RemoteException re)
      {
        throw re;
      }
    }
    return (Konto[])konten.toArray(new Konto[konten.size()]);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setKonten(de.willuhn.jameica.hbci.rmi.Konto[])
   */
  public void setKonten(Konto[] k) throws RemoteException
  {
    if (k == null || k.length == 0)
    {
      settings.setAttribute(getID() + ".konto",(String[]) null);
      return;
    }
    
    String[] ids = new String[k.length];
    for (int i=0;i<k.length;++i)
    {
      ids[i] = k[i].getID();
    }
    settings.setAttribute(getID() + ".konto",ids);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getSaveUsedTan()
   */
  public boolean getSaveUsedTan() throws RemoteException
  {
    return settings.getBoolean(getID() + ".savetan",false);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setSaveUsedTan(boolean)
   */
  public void setSaveUsedTan(boolean save) throws RemoteException
  {
    settings.setAttribute(getID() + ".savetan",save);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getSecMech()
   */
  public String getSecMech() throws RemoteException
  {
    return settings.getString(getID() + ".secmech",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setSecMech(java.lang.String)
   */
  public void setSecMech(String s) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech",s);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getShowTan()
   */
  public boolean getShowTan() throws RemoteException
  {
    return settings.getBoolean(getID() + ".showtan",false);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setShowTan(boolean)
   */
  public void setShowTan(boolean show) throws RemoteException
  {
    settings.setAttribute(getID() + ".showtan",show);
  }
  
  

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#clearUsedTans()
   */
  public void clearUsedTans() throws RemoteException
  {
    try
    {
      Wallet wallet = de.willuhn.jameica.hbci.Settings.getWallet();
      wallet.deleteAll("tan."      + getID());
      wallet.deleteAll("tan.date." + getID());
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to delete TANs",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("TAN-Liste konnte nicht gelöscht werden. {0}",e.getLocalizedMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getTanUsed(java.lang.String)
   */
  public synchronized Date getTanUsed(String tan) throws RemoteException
  {
    try
    {
      return (Date) de.willuhn.jameica.hbci.Settings.getWallet().get("tan.date." + getID() + "." + tan);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to load TAN",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("TAN konnte nicht geladen werden. {0}",e.getLocalizedMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#saveUsedTan(java.lang.String)
   */
  public synchronized void saveUsedTan(String tan) throws RemoteException
  {
    if (!getSaveUsedTan())
      return;
    try
    {
      Wallet wallet = de.willuhn.jameica.hbci.Settings.getWallet();
      wallet.set("tan."      + getID() + "." + tan,tan);
      wallet.set("tan.date." + getID() + "." + tan,new Date());
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to store TAN",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("TAN konnte nicht gespeichert werden. {0}",e.getLocalizedMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getUsedTans()
   */
  public String[] getUsedTans() throws RemoteException
  {
    try
    {
      Wallet wallet = de.willuhn.jameica.hbci.Settings.getWallet();
      String[] keys = wallet.getAll("tan." + getID());
      String[] tans = new String[keys.length];
      for (int i=0;i<keys.length;++i)
      {
        tans[i] = (String) wallet.get(keys[i]);
      }
      return tans;
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to load TAN list",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Liste der verbrauchten TANs konnte nicht geladen werden. {0}",e.getLocalizedMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return new String[0];
  }
}

/*****************************************************************************
 * $Log: PinTanConfigImpl.java,v $
 * Revision 1.1  2010/06/17 11:38:16  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.20  2009/03/02 13:41:24  willuhn
 * @R alten Migrationscode (Custom-Filter) entfernt
 *
 * Revision 1.19  2007/12/12 09:54:28  willuhn
 * @C Bug 39 Migration
 *
 * Revision 1.18  2007/11/25 16:20:11  willuhn
 * @N Bug 276
 *
 * Revision 1.17  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 * Revision 1.16  2006/08/03 15:31:35  willuhn
 * @N Bug 62 completed
 *
 * Revision 1.15  2006/08/03 13:51:38  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.14  2006/08/03 11:27:36  willuhn
 * @N Erste Haelfte von BUG 62 (Speichern verbrauchter TANs)
 *
 * Revision 1.13  2006/02/26 22:27:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2006/01/10 22:34:07  willuhn
 * @B bug 173
 *
 * Revision 1.11  2005/07/18 12:53:30  web0
 * @B bug 96
 *
 * Revision 1.10  2005/06/23 21:52:49  web0
 * @B Bug 80
 *
 * Revision 1.9  2005/05/03 22:43:06  web0
 * @B bug39
 *
 * Revision 1.8  2005/04/27 00:30:12  web0
 * @N real test connection
 * @N all hbci versions are now shown in select box
 * @C userid and customerid are changable
 *
 * Revision 1.7  2005/03/11 02:43:59  web0
 * @N PIN/TAN works ;)
 *
 * Revision 1.6  2005/03/11 00:49:30  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/03/10 18:38:48  web0
 * @N more PinTan Code
 *
 * Revision 1.4  2005/03/09 17:24:40  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/08 18:44:57  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/07 17:17:30  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/07 14:31:00  web0
 * @N first classes
 *
*****************************************************************************/