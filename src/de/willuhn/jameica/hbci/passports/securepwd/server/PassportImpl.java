package de.willuhn.jameica.hbci.passports.securepwd.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.securepwd.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.I18N;

/**
 * @author sven
 * 
 * PassportKlasse für Plugins
 *
 */
public class PassportImpl  implements Passport
{
  private Settings settings = new Settings(PassportImpl.class);

  private Konto konto;

  private Class pluginclass;

  private String description;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   */
  public PassportImpl() {
    super();
  }

  
  /**
   * Installisiert die Passport-Klasse
   */
  @Override
  public void init(Konto k, Class pluginclass, String description) {
    this.konto = k;
    this.pluginclass = pluginclass;
    this.description = description;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getName()
   */
  public String getName() {
    return i18n.tr("Passwordverwaltung für Plugins");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getInfo()
   */
  public String getInfo() throws RemoteException
  {
    return "";
  }


  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException {
    return null; 
  }


  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getHandle()
   * @return null (Für HBCI wird diese Klasse nicht genutzt)
   */
  public PassportHandle getHandle() throws RemoteException
  {
    return null; 
  }

  /**
   * Liefert das aktuelle Konto.
   * @return Konto
   */
  protected Konto getKonto()
  {
    return konto;
  }

  /*
   * (non-Javadoc)
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigurations()
   */
  @Override
  public List<? extends Configuration> getConfigurations() throws RemoteException {
    ArrayList a = new ArrayList();
    for (String attr : settings.getAttributes()) {
      try
      {
        // Die Daten sind als Listen gespeichert und sind als als  attr.0, attr.1, usw. gespeichert
        // Nur das erste Attribut nehmen und passend kürzen
        if (!attr.endsWith(".0")) {
          continue;
        }
        String[] list = settings.getList(attr.substring(0, attr.length() - 2), null);
        Konto k = (Konto) de.willuhn.jameica.hbci.Settings.getDBService().createObject(Konto.class, list[3]);
        Class c = Class.forName(list[2].substring(6));
        a.add(new SecurePwdCfgImpl(k, c, list[0]));
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return a;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.securepwd.rmi.Passport#getConfig()
   */
  @Override
  public SecurePwdCfgImpl getConfig() throws Exception
  {
    String[] list = settings.getList(getSettingsName(), null); 
    if (list == null) {
      return null;
    }
    return new SecurePwdCfgImpl(konto, pluginclass, description);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.securepwd.rmi.Passport#createConfig()
   */
  @Override
  public SecurePwdCfgImpl createConfig() throws Exception
  {
    settings.setAttribute(getSettingsName(), 
        new String[] { description, pluginclass.getSimpleName().toString(), pluginclass.toString(), konto.getID() });
    return getConfig();

  }

  /**
   * 
   * @return Liefert den Namen, so wie er im Setting genutzt wird
   * @throws RemoteException
   */
  private String getSettingsName() throws RemoteException {
    return "config_" + pluginclass.getSimpleName() + "_" + konto.getID();
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#init(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public void init(Konto konto) throws RemoteException
  {
    this.konto = konto;
  }


}
