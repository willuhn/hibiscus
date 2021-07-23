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

  @Override
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

  @Override
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
  
  @Override
  public Class getConfigDialog() throws RemoteException
  {
    return Detail.class;
  }

  @Override
  public void delete() throws ApplicationException
  {
    PinTanConfigFactory.delete(this);
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"blz","bank","url","port","filtertype","hbciversion"};
  }

  @Override
  public String getID() throws RemoteException
  {
    return PinTanConfigFactory.toRelativePath(getFilename());
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "url";
  }

  @Override
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;

    if (this.getID() == null || other.getID() == null)
      return false;

    return getID().equals(other.getID());
  }

  @Override
  public String getHBCIVersion() throws RemoteException
  {
    String ppVersion = StringUtils.trimToNull(this.getPassport().getHBCIVersion());
    return settings.getString(getID() + ".hbciversion",ppVersion != null ? ppVersion : HBCIVersion.HBCI_300.getId());
  }

  @Override
  public void setHBCIVersion(String version) throws RemoteException
  {
    settings.setAttribute(getID() + ".hbciversion",version);
  }

  @Override
  public String getURL() throws RemoteException
  {
    return this.getPassport().getHost();
  }

  @Override
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

  @Override
  public int getPort() throws RemoteException
  {
    return this.getPassport().getPort().intValue();
  }

  @Override
  public void setPort(int port) throws RemoteException
  {
    this.getPassport().setPort(new Integer(port));
  }

  @Override
  public String getFilterType() throws RemoteException
  {
    return this.getPassport().getFilterType();
  }

  @Override
  public void setFilterType(String type) throws RemoteException
  {
    this.getPassport().setFilterType(type);
  }

  @Override
  public String getBLZ() throws RemoteException
  {
    return this.getPassport().getBLZ();
  }

  @Override
  public String getCustomerId() throws RemoteException
  {
    return this.getPassport().getCustomerId();
  }

  @Override
  public void setCustomerId(String customer) throws RemoteException
  {
    this.getPassport().setCustomerId(customer);
  }

  @Override
  public String getUserId() throws RemoteException
  {
    return this.getPassport().getUserId();
  }

  @Override
  public void setUserId(String user) throws RemoteException
  {
    this.getPassport().setUserId(user);
  }

  @Override
  public String getFilename() throws RemoteException
  {
    return file.getAbsolutePath();
  }

  @Override
  public HBCIPassport getPassport() throws RemoteException
  {
    return this.loader.load();
  }

  @Override
  public String getBezeichnung() throws RemoteException
  {
    return settings.getString(getID() + ".bezeichnung",null);
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    settings.setAttribute(getID() + ".bezeichnung",bezeichnung);
  }

  @Override
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

  @Override
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
  
  @Override
  public PtSecMech getStoredSecMech() throws RemoteException
  {
    // Wir haben migriert. Vorher wurde nur die ID des Verfahrens gespeichert.
    // Jetzt zusaetzlich auch der Name, damit wir erkennen koennen, ob es chipTAN USB ist
    // Wenn wir den Namen nicht haben, forcieren wir, dass der User die Auswahl
    // neu taetigt. Wenn hier nur die ID enthalten ist, wird NULL zurueckgeliefert
    // und damit die Vorauswahl resettet.
    return PtSecMech.create(settings.getString(getID() + ".secmech",null));
  }

  @Override
  public void setStoredSecMech(PtSecMech mech) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech",mech != null ? mech.toString() : null);
  }
  
  @Override
  public PtSecMech getCurrentSecMech() throws RemoteException
  {
    // Checken, ob es ein aktuell ausgewaehltes gibt. Das hat Vorrang.
    PtSecMech mech = PtSecMech.create(settings.getString(getID() + ".secmech.current",null));
    
    // Wenn kein aktuelles vorhanden ist, nehmen wir das persistierte
    return mech != null ? mech : this.getStoredSecMech();
  }
  
  @Override
  public void setCurrentSecMech(PtSecMech mech) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech.current",mech != null ? mech.toString() : null);
  }
  
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
  
  @Override
  public void setAvailableSecMechs(String list) throws RemoteException
  {
    settings.setAttribute(getID() + ".secmech.list",list);
  }
  
  @Override
  public String getCardReader() throws RemoteException
  {
    return settings.getString(getID() + ".cardreader",null);
  }
  
  @Override
  public void setCardReader(String name) throws RemoteException
  {
    settings.setAttribute(getID() + ".cardreader",name);
  }
  
  @Override
  public Boolean isChipTANUSB() throws RemoteException
  {
    String s = StringUtils.trimToNull(settings.getString(getID() + ".chiptan.usb.enabled",null));
    return s != null ? Boolean.valueOf(s) : null;
  }
  
  @Override
  public void setChipTANUSB(Boolean b) throws RemoteException
  {
    settings.setAttribute(getID() + ".chiptan.usb.enabled",(String) (b != null ? b.toString() : null));
  }
  
  @Override
  public String[] getTanMedias() throws RemoteException
  {
    return settings.getList(getID() + ".tanmedias",new String[0]);
  }

  @Override
  public void setTanMedias(String[] names) throws RemoteException
  {
    settings.setAttribute(getID() + ".tanmedias",names);
  }
  
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
  
  @Override
  public void setAvailableTanMedias(String list) throws RemoteException
  {
    settings.setAttribute(getID() + ".tanmedias.list",list);
  }

  @Override
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

  @Override
  public String getTanMedia() throws RemoteException
  {
    return settings.getString(getID() + ".tanmedia",null);
  }

  @Override
  public void setTanMedia(String name) throws RemoteException
  {
    settings.setAttribute(getID() + ".tanmedia",name);
  }

  @Override
  public boolean getShowTan() throws RemoteException
  {
    return settings.getBoolean(getID() + ".showtan",true);
  }

  @Override
  public void setShowTan(boolean show) throws RemoteException
  {
    settings.setAttribute(getID() + ".showtan",show);
  }
  
  @Override
  public String getCustomProperty(String name) throws RemoteException
  {
    if (name == null)
      return null;
    return settings.getString(getID() + "." + name,null);
  }
  
  @Override
  public void setCustomProperty(String name, String value) throws RemoteException
  {
    if (name == null)
      return;
    
    settings.setAttribute(getID() + "." + name,value);
  }
  
  @Override
  public void reload() throws RemoteException
  {
    this.loader.reload();
  }
}
