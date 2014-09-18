/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/

package de.willuhn.jameica.hbci.passports.pintan.server;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.passports.pintan.Detail;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines in Hibiscus existierenden RDH-Schluessels.
 * @author willuhn
 */
public class PinTanConfigImpl implements PinTanConfig
{

  private final static Settings settings = new Settings(PinTanConfig.class);

  private HBCIPassport passport = null;
  private File file             = null;
  
  /**
   * ct.
   * @param passport
   * @param file
   * @throws RemoteException
   */
  public PinTanConfigImpl(HBCIPassport passport, File file) throws RemoteException
  {
    this.passport = passport;
    this.file = file;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attribute) throws RemoteException
  {
    if ("blz".equals(attribute))
      return getBLZ();
    if ("bank".equals(attribute))
      return HBCIProperties.getNameForBank(getBLZ());
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
    if ("showtan".equals(attribute))
      return new Boolean(getShowTan());
    if ("secmech".equals(attribute))
      return getSecMech();
    if ("tanmedia".equals(attribute))
      return getTanMedia();
    if ("tanmedias".equals(attribute))
      return getTanMedias();
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#getDescription()
   */
  public String getDescription()
  {
    try
    {
      String name = this.getBezeichnung();
      String bank = HBCIProperties.getNameForBank(getBLZ());
      String url  = this.getURL();

      boolean haveName = (name != null && name.trim().length() > 0);
      boolean haveBank = (bank != null && bank.length() > 0);
      
      // wenn wir weder Name noch Bank haben, nehmen wir die URL
      if (!haveBank && !haveName)
        return url;
      
      // wenn wir Name und Bank haben, nehmen wir beides
      if (haveBank && haveName)
        return name + " - " + bank;

      // Ansonsten das, was da ist
      if (haveName)
        return name;
      
      return bank;
    }
    catch (Exception e)
    {
      Logger.error("unable to determine name",e);
      return passport.getHost();
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException
  {
    return Detail.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#delete()
   */
  public void delete() throws ApplicationException
  {
    PinTanConfigFactory.delete(this);
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
    return settings.getString(getID() + ".hbciversion",passport.getHBCIVersion());
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
    // Und jetzt laden wir die Liste neu
    String[] ids = settings.getList(getID() + ".konto",null);
    if (ids == null || ids.length == 0)
      return null;
    
    List<String> fixedIds = new ArrayList<String>();
    List<Konto> konten = new ArrayList<Konto>();
    for (int i=0;i<ids.length;++i)
    {
      try
      {
        konten.add((Konto) de.willuhn.jameica.hbci.Settings.getDBService().createObject(Konto.class,ids[i]));
        fixedIds.add(ids[i]); // Wenn das Konto geladen wurde, bleibt es erhalten
      }
      catch (ObjectNotFoundException noe)
      {
        Logger.warn("account " + ids[i] + " does not exist, removing from list");
      }
      catch (RemoteException re)
      {
        throw re;
      }
    }
    if (fixedIds.size() != ids.length)
    {
      Logger.info("fixing list of assigned accounts");
      settings.setAttribute(getID() + ".konto",fixedIds.toArray(new String[fixedIds.size()]));
    }
    return konten.toArray(new Konto[konten.size()]);
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
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getTanMedias()
   */
  public String[] getTanMedias() throws RemoteException
  {
    return settings.getList(getID() + ".tanmedias",new String[0]);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setTanMedias(java.lang.String[])
   */
  public void setTanMedias(String[] names) throws RemoteException
  {
    settings.setAttribute(getID() + ".tanmedias",names);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#addTanMedia(java.lang.String)
   */
  public void addTanMedia(String name) throws RemoteException
  {
    if (name == null || name.length() == 0)
      return;
    
    // Bisherige Werte
    String[] current = this.getTanMedias();
    List<String> list = new ArrayList<String>();

    for (String s:current)
    {
      // Wenn es schon in der Liste ist, nehmen wir 
      // es erstmal raus
      if (name.equals(s))
        continue;
      list.add(s);
    }
    
    // Am Anfang neu einfuegen.
    // Dann steht die letzte Auswahl immer vorn
    list.add(0,name);

    // Abspeichern
    this.setTanMedias(list.toArray(new String[list.size()]));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getTanMedia()
   */
  public String getTanMedia() throws RemoteException
  {
    return settings.getString(getID() + ".tanmedia",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setTanMedia(java.lang.String)
   */
  public void setTanMedia(String name) throws RemoteException
  {
    settings.setAttribute(getID() + ".tanmedia",name);
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
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getCustomProperty(java.lang.String)
   */
  public String getCustomProperty(String name) throws RemoteException
  {
    if (name == null)
      return null;
    return settings.getString(getID() + "." + name,null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setCustomProperty(java.lang.String, java.lang.String)
   */
  public void setCustomProperty(String name, String value) throws RemoteException
  {
    if (name == null)
      return;
    
    settings.setAttribute(getID() + "." + name,value);
  }
}
