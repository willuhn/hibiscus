/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/server/RDHKeyImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/29 09:17:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.rdh.server;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.rdh.Detail;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.HBCI4JavaFormat;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.SizRdhDirectFormat;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.MultipleClassLoader;

/**
 * Implementierung eines in Hibiscus existierenden RDH-Schluessels.
 * @author willuhn
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

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attribute) throws RemoteException
  {
    if ("file".equals(attribute))
      return getFilename();
		if ("enabled".equals(attribute))
			return new Boolean(isEnabled());
    if ("alias".equals(attribute))
      return getAlias();
    if ("format".equals(attribute))
      return getFormat().getName();
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#getDescription()
   */
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

  /**
   * @see de.willuhn.jameica.hbci.passport.Configuration#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException
  {
    return Detail.class;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"file","enabled","hbciversion","alias","shared"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return getFilename();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "file";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;
    return getID().equals(other.getID());
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#getFilename()
   */
  public String getFilename() throws RemoteException
  {
    return file.getAbsolutePath();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#getHBCIVersion()
   */
  public String getHBCIVersion() throws RemoteException
  {
    return settings.getString(getID() + ".hbciversion",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#setHBCIVersion(java.lang.String)
   */
  public void setHBCIVersion(String version) throws RemoteException
  {
    settings.setAttribute(getID() + ".hbciversion",version);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#isEnabled()
   */
  public boolean isEnabled() throws RemoteException
  {
    return settings.getBoolean(getID() + ".enabled",true);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled) throws RemoteException
  {
  	settings.setAttribute(getID() + ".enabled",enabled);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#setFilename(java.lang.String)
   */
  public void setFilename(String filename) throws RemoteException
  {
    this.file = new File(filename);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#getAlias()
   */
  public String getAlias() throws RemoteException
  {
    return settings.getString(getID() + ".alias",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#setAlias(java.lang.String)
   */
  public void setAlias(String alias) throws RemoteException
  {
    settings.setAttribute(getID() + ".alias",alias);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#getKonten()
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
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#setKonten(de.willuhn.jameica.hbci.rmi.Konto[])
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
   * @see de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey#load()
   */
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
      MultipleClassLoader loader = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader();
      String s = settings.getString(getID() + ".format",null);

      // Migration. Wir schauen, ob fuer den Schluessel
      // schon ein Format hinterlegt ist. Falls ja, nehmen
      // wir das. Andernfalls weisen wir manuell das Format
      // zu welches dem entspricht, bevor das KeyFormat-Interface
      // existierte.

      if (s != null && s.length() > 0)
        return (KeyFormat) loader.load(s).newInstance();

      
      // Migration noetig
      Logger.info("determining key format for " + getFilename());
      boolean shared = settings.getBoolean(getID() + ".shared",false);
      s = shared ? SizRdhDirectFormat.class.getName() : HBCI4JavaFormat.class.getName();
      Logger.info("  is " + s);
        
      // Wir speichern das gleich ab, damit wir es kuenftig wissen
      KeyFormat format = (KeyFormat) loader.load(s).newInstance();
      setFormat(format);
        
      // bei der Gelegenheit loeschen wir gleich noch die alten Parameter weg
      settings.setAttribute(getID() + ".newkey",(String) null);
      settings.setAttribute(getID() + ".shared",(String) null);

      return format;
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

/*****************************************************************************
 * $Log: RDHKeyImpl.java,v $
 * Revision 1.2  2011/04/29 09:17:34  willuhn
 * @N Neues Standard-Interface "Configuration" fuer eine gemeinsame API ueber alle Arten von HBCI-Konfigurationen
 * @R Passports sind keine UnicastRemote-Objekte mehr
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.17  2008/07/25 12:56:50  willuhn
 * @B Bugfixing
 *
 * Revision 1.16  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.15  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 * Revision 1.14  2007/05/30 14:48:50  willuhn
 * @N Bug 314
*****************************************************************************/