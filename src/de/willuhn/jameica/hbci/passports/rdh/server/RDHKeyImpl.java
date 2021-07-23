/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.server;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.rdh.Detail;
import de.willuhn.jameica.hbci.passports.rdh.RDHKeyFactory;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.HBCI4JavaFormat;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.MultipleClassLoader;

/**
 * Implementierung eines in Hibiscus existierenden RDH-Schluessels.
 */
public class RDHKeyImpl implements RDHKey
{

  private File file = null;
  private Settings settings = new Settings(RDHKey.class);

  /**
   * ct.
   * @param file Die Schluesseldatei.
   * @throws java.rmi.RemoteException
   */
  public RDHKeyImpl(File file) throws RemoteException
  {
    this.file = file;
  }

  @Override
  public Object getAttribute(String attribute) throws RemoteException
  {
    if ("file".equals(attribute))
      return getFilename();
		if ("enabled".equals(attribute))
			return Boolean.valueOf(isEnabled());
    if ("alias".equals(attribute))
      return getAlias();
    if ("format".equals(attribute))
      return getFormat().getName();
    return null;
  }

  @Override
  public String getDescription()
  {
    try
    {
      String name = this.getAlias();
      if (name != null && name.length() > 0)
        return name;
      return this.getFilename();
    }
    catch (Exception e)
    {
      Logger.error("unable to determine name",e);
      return file.getAbsolutePath();
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
    RDHKeyFactory.removeKey(this);
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"file","enabled","hbciversion","alias","shared"};
  }

  @Override
  public String getID() throws RemoteException
  {
    return getFilename();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "file";
  }

  @Override
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;
    return getID().equals(other.getID());
  }

  @Override
  public String getFilename() throws RemoteException
  {
    return file.getAbsolutePath();
  }

  @Override
  public String getHBCIVersion() throws RemoteException
  {
    return settings.getString(getID() + ".hbciversion",null);
  }

  @Override
  public void setHBCIVersion(String version) throws RemoteException
  {
    settings.setAttribute(getID() + ".hbciversion",version);
  }

  @Override
  public boolean isEnabled() throws RemoteException
  {
    return settings.getBoolean(getID() + ".enabled",true);
  }

  @Override
  public void setEnabled(boolean enabled) throws RemoteException
  {
  	settings.setAttribute(getID() + ".enabled",enabled);
  }

  @Override
  public void setFilename(String filename) throws RemoteException
  {
    this.file = new File(filename);
  }

  @Override
  public String getAlias() throws RemoteException
  {
    return settings.getString(getID() + ".alias",null);
  }

  @Override
  public void setAlias(String alias) throws RemoteException
  {
    settings.setAttribute(getID() + ".alias",alias);
  }

  @Override
  public Konto[] getKonten() throws RemoteException
  {
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
    }
    return (Konto[])konten.toArray(new Konto[0]);
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
  public HBCIPassport load() throws RemoteException, ApplicationException, OperationCanceledException
  {
    return getFormat().load(this);
  }
  
  /**
   * Liefert das Schluesselformat.
   * @return das Schluesselformat.
   * @throws RemoteException
   */
  private KeyFormat getFormat() throws RemoteException
  {
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      MultipleClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
      
      // Als Default nehmen wir das Eigenformat
      String s = settings.getString(getID() + ".format",HBCI4JavaFormat.class.getName());
      Class c = loader.load(s);
      return (KeyFormat) service.get(c);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to load key format",e);
    }
  }

  /**
   * Speichert das Format des Schluessels.
   * @param format Format des Schluessels.
   * @throws RemoteException
   */
  public void setFormat(KeyFormat format) throws RemoteException
  {
    settings.setAttribute(getID() + ".format",format.getClass().getName());
  }

}
