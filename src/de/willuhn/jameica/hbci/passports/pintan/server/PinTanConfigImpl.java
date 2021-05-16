/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan.server;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportLoader;
import de.willuhn.jameica.hbci.passports.pintan.Detail;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMech;
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

  private PassportLoader loader = null;
  private File file             = null;
  
  /**
   * ct.
   * @param loader
   * @param file
   * @throws RemoteException
   */
  public PinTanConfigImpl(PassportLoader loader, File file) throws RemoteException
  {
    this.loader = loader;
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
      try
      {
        return this.getPassport().getHost();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine host",re);
        return "<unknown> (" + re.getMessage() + ")";
      }
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
    String ppVersion = StringUtils.trimToNull(this.getPassport().getHBCIVersion());
    return settings.getString(getID() + ".hbciversion",ppVersion != null ? ppVersion : HBCIVersion.HBCI_300.getId());
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
    return this.getPassport().getHost();
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
    this.getPassport().setHost(url);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getPort()
   */
  public int getPort() throws RemoteException
  {
    return this.getPassport().getPort().intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setPort(int)
   */
  public void setPort(int port) throws RemoteException
  {
    this.getPassport().setPort(new Integer(port));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getFilterType()
   */
  public String getFilterType() throws RemoteException
  {
    return this.getPassport().getFilterType();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setFilterType(java.lang.String)
   */
  public void setFilterType(String type) throws RemoteException
  {
    this.getPassport().setFilterType(type);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getBLZ()
   */
  public String getBLZ() throws RemoteException
  {
    return this.getPassport().getBLZ();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getCustomerId()
   */
  public String getCustomerId() throws RemoteException
  {
    return this.getPassport().getCustomerId();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setCustomerId(java.lang.String)
   */
  public void setCustomerId(String customer) throws RemoteException
  {
    this.getPassport().setCustomerId(customer);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getUserId()
   */
  public String getUserId() throws RemoteException
  {
    return this.getPassport().getUserId();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setUserId(java.lang.String)
   */
  public void setUserId(String user) throws RemoteException
  {
    this.getPassport().setUserId(user);
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
    return this.loader.load();
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
    for (String id : ids)
    {
      try
      {
        konten.add((Konto) de.willuhn.jameica.hbci.Settings.getDBService().createObject(Konto.class, id));
        fixedIds.add(id); // Wenn das Konto geladen wurde, bleibt es erhalten
      }
      catch (ObjectNotFoundException noe)
      {
        Logger.warn("account " + id + " does not exist, removing from list");
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
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getStoredSecMech()
   */
  public PtSecMech getStoredSecMech() throws RemoteException
  {
    // Wir haben migriert. Vorher wurde nur die ID des Verfahrens gespeichert.
    // Jetzt zusaetzlich auch der Name, damit wir erkennen koennen, ob es chipTAN USB ist
    // Wenn wir den Namen nicht haben, forcieren wir, dass der User die Auswahl
    // neu taetigt. Wenn hier nur die ID enthalten ist, wird NULL zurueckgeliefert
    // und damit die Vorauswahl resettet.
    return PtSecMech.create(settings.getString(getID() + ".secmech",null));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setStoredSecMech(de.willuhn.jameica.hbci.passports.pintan.PtSecMech)
   */
  public void setStoredSecMech(PtSecMech mech) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech",mech != null ? mech.toString() : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getCurrentSecMech()
   */
  @Override
  public PtSecMech getCurrentSecMech() throws RemoteException
  {
    // Checken, ob es ein aktuell ausgewaehltes gibt. Das hat Vorrang.
    PtSecMech mech = PtSecMech.create(settings.getString(getID() + ".secmech.current",null));
    
    // Wenn kein aktuelles vorhanden ist, nehmen wir das persistierte
    return mech != null ? mech : this.getStoredSecMech();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setCurrentSecMech(de.willuhn.jameica.hbci.passports.pintan.PtSecMech)
   */
  @Override
  public void setCurrentSecMech(PtSecMech mech) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech.current",mech != null ? mech.toString() : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getAvailableSecMechs()
   */
  @Override
  public List<PtSecMech> getAvailableSecMechs() throws RemoteException
  {
    List<PtSecMech> result = new ArrayList<PtSecMech>();
    final String s = settings.getString(getID() + ".secmech.list",null);
    if (s == null)
      return result;
    
    try
    {
      result.addAll(PtSecMech.parse(s));
    }
    catch (Exception e)
    {
      Logger.error("unparsable secmech list",e);
    }
    return result;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setAvailableSecMechs(java.lang.String)
   */
  @Override
  public void setAvailableSecMechs(String list) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech.list",list);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getCardReader()
   */
  @Override
  public String getCardReader() throws RemoteException
  {
    return settings.getString(getID() + ".cardreader",null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setCardReader(java.lang.String)
   */
  @Override
  public void setCardReader(String name) throws RemoteException
  {
    settings.setAttribute(getID() + ".cardreader",name);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#isChipTANUSB()
   */
  @Override
  public Boolean isChipTANUSB() throws RemoteException
  {
    String s = StringUtils.trimToNull(settings.getString(getID() + ".chiptan.usb.enabled",null));
    return s != null ? Boolean.valueOf(s) : null;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setChipTANUSB(java.lang.Boolean)
   */
  @Override
  public void setChipTANUSB(Boolean b) throws RemoteException
  {
    settings.setAttribute(getID() + ".chiptan.usb.enabled",(String) (b != null ? b.toString() : null));
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
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#getAvailableTanMedias()
   */
  @Override
  public List<String> getAvailableTanMedias() throws RemoteException
  {
    List<String> result = new ArrayList<String>();
    final String s = settings.getString(getID() + ".tanmedias.list",null);
    if (s == null)
      return result;
    
    try
    {
      result.addAll(Arrays.asList(s.split("\\|")));
    }
    catch (Exception e)
    {
      Logger.error("unparsable tan media list",e);
    }
    return result;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#setAvailableTanMedias(java.lang.String)
   */
  @Override
  public void setAvailableTanMedias(String list) throws RemoteException
  {
    settings.setAttribute(getID() + ".tanmedias.list",list);
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
    return settings.getBoolean(getID() + ".showtan",true);
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
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig#reload()
   */
  @Override
  public void reload() throws RemoteException
  {
    this.loader.reload();
  }
}
