/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.passports.ddv.server.CustomReader;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Konfiguration eines einzelnen Kartenlesers.
 */
public class DDVConfig implements Configuration
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
   * @see de.willuhn.jameica.hbci.passport.Configuration#getDescription()
   */
  public String getDescription()
  {
    return this.getReaderPreset().getName() + " (" + this.getName() + ")";
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException
  {
    return Detail.class;
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
   * Liefert den Kartenleser-Namen, wenn es einer via javax.smartcardio ist.
   * @return der Kartenleser-Name.
   */
  public String getPCSCName()
  {
    return settings.getString(this.getPrefix() + "pcscname",null);
  }

  /**
   * Legt den Kartenleser-Namen von javax.smartcardio-Kartenlesern fest.
   * @param javaname der Name des Kartenlesers in javax.smartcardio.
   */
  public void setPCSCName(String javaname)
  {
    settings.setAttribute(this.getPrefix() + "pcscname",javaname);
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
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class c = Class.forName(s);
      return (Reader) service.get(c);
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
    return settings.getString(this.getPrefix() + "hbciversion",settings.getString("hbciversion",getReaderPreset().getDefaultHBCIVersion()));
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
    for (final String accountId : ids)
    {
      try
      {
        konten.add((Konto) de.willuhn.jameica.hbci.Settings.getDBService().createObject(Konto.class, accountId));
        fixedIds.add(accountId); // Wenn das Konto geladen wurde, bleibt es erhalten
      }
      catch (ObjectNotFoundException noe)
      {
        Logger.warn("account " + accountId + " does not exist, removing from list");
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
    copy.setCTAPIDriver(this.getCTAPIDriver());
    copy.setCTNumber(this.getCTNumber());
    copy.setEntryIndex(this.getEntryIndex());
    copy.setHBCIVersion(this.getHBCIVersion());
    copy.setName(this.getName());
    copy.setPort(this.getPort());
    copy.setReaderPreset(this.getReaderPreset());
    copy.setSoftPin(this.useSoftPin());
    copy.setPCSCName(this.getPCSCName());
    return copy;
  }
  
  /**
   * Loescht die Einstellungen der Config.
   */
  void deleteProperties()
  {
    this.setCTAPIDriver(null);
    this.setHBCIVersion(null);
    this.setName(null);
    this.setPort(null);
    this.setReaderPreset(null);
    settings.setAttribute(this.getPrefix() + "softpin",(String) null);
    settings.setAttribute(this.getPrefix() + "ctnumber",(String) null);
    settings.setAttribute(this.getPrefix() + "entryidx",(String) null);
    settings.setAttribute(this.getPrefix() + "javaname",(String) null);
    settings.setAttribute(this.getPrefix() + "konto",(String[]) null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#delete()
   */
  public void delete() throws ApplicationException
  {
    DDVConfigFactory.delete(this);
  }
}



/**********************************************************************
 * $Log: DDVConfig.java,v $
 * Revision 1.9  2011/09/06 11:54:25  willuhn
 * @C JavaReader in PCSCReader umbenannt - die PIN-Eingabe fehlt noch
 *
 * Revision 1.8  2011-09-01 12:16:08  willuhn
 * @N Kartenleser-Suche kann jetzt abgebrochen werden
 * @N Erster Code fuer javax.smartcardio basierend auf dem OCF-Code aus HBCI4Java 2.5.8
 *
 * Revision 1.7  2011-09-01 09:40:53  willuhn
 * @R Biometrie-Support bei Kartenlesern entfernt - wurde nie benutzt
 *
 * Revision 1.6  2011-06-17 08:49:19  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.5  2011-04-29 09:17:35  willuhn
 * @N Neues Standard-Interface "Configuration" fuer eine gemeinsame API ueber alle Arten von HBCI-Konfigurationen
 * @R Passports sind keine UnicastRemote-Objekte mehr
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