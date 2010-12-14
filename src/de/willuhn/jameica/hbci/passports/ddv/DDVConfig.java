/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/DDVConfig.java,v $
 * $Revision: 1.3.2.1 $
 * $Date: 2010/12/14 14:19:42 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.passports.ddv.server.CustomReader;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Konfiguration eines einzelnen Kartenlesers.
 */
public class DDVConfig
{
  /**
   * Moegliche Ports fuer den Leser.
   */
  public final static String[] PORTS = new String[] {"COM/USB","COM2/USB2","USB3","USB4","USB5","USB6"};
  
  private final static Settings settings = new Settings(DDVConfig.class);
  private String id = null;

  /**
   * ct.
   * @param id die ID der Config.
   */
  DDVConfig(String id)
  {
    this.id = id;
  }
  
  /**
   * Liefert die ID der Config.
   * @return die ID der Config.
   */
  public String getId()
  {
    return this.id;
  }
  
  /**
   * Liefert den Schluessel-Praefix fuer die Parameter.
   * @return Schluessel-Praefix fuer die Parameter.
   */
  private String getPrefix()
  {
    return "config." + this.getId() + ".";
  }
  
  /**
   * Liefert einen sprechenden Namen fuer die Config.
   * @return sprechender Name fuer die Config.
   */
  public String getName()
  {
    return settings.getString(this.getPrefix() + "name","default");
  }
  
  /**
   * Legt den Namen der Config fest.
   * @param name Name der Config.
   */
  public void setName(String name)
  {
    settings.setAttribute(this.getPrefix() + "name",name);
  }
  
  /**
   * Liefert die Port-Nummer des Kartenlesers.
   * @return die Port-Nummer des Kartenlesers.
   */
  public String getPort()
  {
    return settings.getString(this.getPrefix() + "port",DDVConfig.PORTS[0]);
  }

  /**
   * Speichert die Port-Nummer des Kartenlesers.
   * @param port die Portnummer des Kartenlesers.
   */
  public void setPort(String port)
  {
    settings.setAttribute(this.getPrefix() + "port",port);
  }

  /**
   * Liefert die Index-Nummer des Kartenlesers.
   * @return die Index-Nummer des Kartenlesers.
   */
  public int getCTNumber()
  {
    return settings.getInt(this.getPrefix() + "ctnumber",0);
  }

  /**
   * Speichert die Index-Nummer des Kartenlesers.
   * @param ctNumber die Index-Nummer des Kartenlesers.
   */
  public void setCTNumber(int ctNumber)
  {
    settings.setAttribute(this.getPrefix() + "ctnumber",ctNumber);
  }

  /**
   * Liefert true, wenn Biometrik verwendet werden soll.
   * @return true, wenn Biometrik verwendet werden soll.
   */
  public boolean useBIO()
  {
    return settings.getBoolean(this.getPrefix() + "usebio",false);
  }

  /**
   * Legt fest, ob Biometrik verwendet werden soll.
   * @param bio true, wenn Biometrik verwendet werden soll.
   */
  public void setBIO(boolean bio)
  {
    settings.setAttribute(this.getPrefix() + "usebio",bio);
  }

  /**
   * Liefert true, wenn die PC-Tastatur zur Eingabe von PINs verwendet werden soll.
   * @return true, wenn die PC-Tastatur zur Eingabe von PINs verwendet werden soll.
   */
  public boolean useSoftPin()
  {
    return settings.getBoolean(this.getPrefix() + "softpin",false);
  }

  /**
   * Legt fest, ob die PC-Tastatur zur Eingabe von PINs verwendet werden soll.
   * @param softPin true, wenn die PC-Tastatur zur Eingabe von PINs verwendet werden soll.
   */
  public void setSoftPin(boolean softPin)
  {
    settings.setAttribute(this.getPrefix() + "softpin",softPin);
  }

  /**
   * Liefert den Index des Slots auf der Karte.
   * @return Index des Slots auf der Karte.
   */
  public int getEntryIndex()
  {
    return settings.getInt(this.getPrefix() + "entryidx",1);
  }

  /**
   * Legt den Index des Slots auf der Karte fest.
   * @param index Indes des Slots auf der Karte.
   */
  public void setEntryIndex(int index)
  {
    settings.setAttribute(this.getPrefix() + "entryidx",index);
  }

  /**
   * Liefert Pfad und Dateiname des CTAPI-Treibers.
   * @return Pfad und Dateiname des CTAPI-Traibers.
   */
  public String getCTAPIDriver()
  {
    return settings.getString(this.getPrefix() + "ctapi","");
  }

  /**
   * Legt Pfad und Dateiname des CTAPI-Treibers fest.
   * @param file Pfad und Dateiname des CTAPI-Treibers.
   */
  public void setCTAPIDriver(String file)
  {
    settings.setAttribute(this.getPrefix() + "ctapi",file);
  }

  /**
   * Liefert das Kartenleser-Preset, auf dem diese Config basiert.
   * @return das Kartenleser-Preset, auf dem diese Config basiert.
   */
  public Reader getReaderPreset()
  {
    String s = settings.getString(this.getPrefix() + "readerpreset",CustomReader.class.getName());
    try
    {
      return (Reader) Application.getClassLoader().load(s).newInstance();
    }
    catch (Throwable t)
    {
      Logger.error("error while reading presets - you can ignore this error message",t);
    }
    return new CustomReader();
  }

  /**
   * Legt das Kartenleser-Preset fuer die Config fest.
   * @param reader das Kartenleser-Preset.
   */
  public void setReaderPreset(Reader reader)
  {
    settings.setAttribute(this.getPrefix() + "readerpreset",reader != null ? reader.getClass().getName() : null);
  }

  /**
   * Liefert die zu verwendende HBCI-Version.
   * @return die HBCI-Version.
   */
  public String getHBCIVersion()
  {
    // BUG: Wir hatten hier vergessen, den Prefix mit anzugeben.
    // Wir migrieren die bisherigen Werte gleich.
    return settings.getString(this.getPrefix() + "hbciversion",settings.getString("hbciversion","210"));
  }

  /**
   * Legt die zu verwendende HBCI-Version fest.
   * @param version die zu verwendende HBCI-Version.
   */
  public void setHBCIVersion(String version)
  {
    settings.setAttribute(this.getPrefix() + "hbciversion",version);
  }
  
  /**
   * Liefert eine Liste von fest verdrahteten Konten fuer die Kartenleser-Config.
   * @return Liste von fest verdrahteten Konten.
   * @throws RemoteException
   */
  public List<Konto> getKonten() throws RemoteException
  {
    // Und jetzt laden wir die Liste neu
    String[] ids = settings.getList(getPrefix() + "konto",null);
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
      settings.setAttribute(getPrefix() + "konto",fixedIds.toArray(new String[fixedIds.size()]));
    }
    return konten;
  }

  /**
   * Speichert eine Liste von fest zugeordneten Konten.
   * @param list Liste von fest zugeordneten Konten.
   * @throws RemoteException
   */
  public void setKonten(List<Konto> list) throws RemoteException
  {
    if (list == null || list.size() == 0)
    {
      settings.setAttribute(getPrefix() + "konto",(String[]) null);
      return;
    }
    
    String[] ids = new String[list.size()];
    for (int i=0;i<list.size();++i)
    {
      ids[i] = list.get(i).getID();
    }
    settings.setAttribute(getPrefix() + "konto",ids);
  }
  

  /**
   * Liefert die Portnummer fuer den angegebenen Port-Namen.
   * @param name der Port-Name.
   * @return die Port-Nummer.
   * @throws RemoteException
   */
  public static int getPortForName(String name) throws RemoteException
  {
    if (name == null || name.length() == 0)
      return 0;

    for (int i=0;i<DDVConfig.PORTS.length;++i)
    {
      if (DDVConfig.PORTS[i].equals(name))
        return i;
    }
    return 0;
  }

  /**
   * Erstellt eine Kopie der Config mit neuer ID.
   * @return Kopie der Config.
   */
  public DDVConfig copy()
  {
    DDVConfig copy = DDVConfigFactory.create();
    copy.setBIO(this.useBIO());
    copy.setCTAPIDriver(this.getCTAPIDriver());
    copy.setCTNumber(this.getCTNumber());
    copy.setEntryIndex(this.getEntryIndex());
    copy.setHBCIVersion(this.getHBCIVersion());
    copy.setName(this.getName());
    copy.setPort(this.getPort());
    copy.setReaderPreset(this.getReaderPreset());
    copy.setSoftPin(this.useSoftPin());
    return copy;
  }
  
  /**
   * Loescht die Einstellungen der Config.
   */
  void delete()
  {
    this.setCTAPIDriver(null);
    this.setHBCIVersion(null);
    this.setName(null);
    this.setPort(null);
    this.setReaderPreset(null);
    settings.setAttribute(this.getPrefix() + "usebio",(String) null);
    settings.setAttribute(this.getPrefix() + "softpin",(String) null);
    settings.setAttribute(this.getPrefix() + "ctnumber",(String) null);
    settings.setAttribute(this.getPrefix() + "entryidx",(String) null);
    settings.setAttribute(this.getPrefix() + "konto",(String[]) null);
  }
}



/**********************************************************************
 * $Log: DDVConfig.java,v $
 * Revision 1.3.2.1  2010/12/14 14:19:42  willuhn
 * @B BACKPORT 0028
 *
 * Revision 1.4  2010-12-01 21:59:00  willuhn
 * @B die HBCI-Version wurde nicht pro Config gespeichert sondern galt fuer alle Configs
 *
 * Revision 1.3  2010-09-08 15:04:52  willuhn
 * @N Config des Sicherheitsmediums als Context in Passport speichern
 *
 * Revision 1.2  2010-09-08 10:14:32  willuhn
 * @B Beim Loeschen einer DDV-Config auch die IDs der fest verdrahteten Konten loeschen
 *
 * Revision 1.1  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 **********************************************************************/