package de.willuhn.jameica.hbci.passports.securepwd.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.passports.securepwd.rmi.SecurePwdCfg;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

/**
 * 
 * @author sven
 *
 */
public class SecurePwdCfgImpl implements SecurePwdCfg
{

  private Wallet wallet;
  private String description;
  private Konto konto;
  private Class pluginclass;

  /**
   * 
   * @param k Konto
   * @param c Plugin-Klasse
   * @param desc Name des Plugin (Wird für die Darstellung bei den Zugängen genutzt)
   * @throws Exception
   */
  public SecurePwdCfgImpl(Konto k, Class c, String desc) throws Exception
  {
    this.konto = k;
    this.description = desc;
    this.pluginclass = c;
    wallet = new Wallet(c);
  }

  /*
   * (non-Javadoc)
   * @see de.willuhn.jameica.hbci.passports.securepwd.rmi.SecurePwdCfg#getString(java.lang.String)
   */
  @Override
  public String getString(String attribute) throws ApplicationException {
    try {
      return wallet.get(getPrefix() + attribute).toString();
    } catch (RemoteException e) {
      e.printStackTrace();
      throw new ApplicationException("Fehler beim Auslesen des Strings: ", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see de.willuhn.jameica.hbci.passports.securepwd.rmi.SecurePwdCfg#setString(java.lang.String, java.lang.String)
   */
  @Override
  public void setString(String attribute, String value) throws ApplicationException {
    try {
      wallet.set(getPrefix() + attribute, value);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ApplicationException("Fehler beim Setzen des Strings: ", e);
    }
  }

  /**
   * 
   * @return Prefix, dass für dieses Konto genutzt werden soll
   * @throws RemoteException
   */
  private String getPrefix() throws RemoteException {
    return konto.getID() + "_";
  }
  
  /*
   * (non-Javadoc)
   * @see de.willuhn.jameica.hbci.passport.Configuration#getDescription()
   */
  @Override
  public String getDescription()
  {
    return description;
  }

  /*
   * (non-Javadoc)
   * @see de.willuhn.jameica.hbci.passport.Configuration#getConfigDialog()
   */
  @Override
  public Class getConfigDialog() throws RemoteException
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see de.willuhn.jameica.hbci.passport.Configuration#delete()
   */
  @Override
  public void delete() throws ApplicationException
  {
    try {
      // KOnkrete Einträge in der Wallet löschen
      wallet.deleteAll(getPrefix());
      
      // Setting Eintrag löschen
      Settings settings = new Settings(PassportImpl.class);
      settings.setAttribute(getSettingsName(), new String[] {null, null, null, null}); // TODO Setting löschen und nicht nur überschreiben
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new ApplicationException("Fehler beim Setzen des Strings: ", e);
    }
  }

  /**
   * Lifert den Eintragsnamen für das Setting zurück
   * @return
   * @throws RemoteException
   */
  private String getSettingsName() throws RemoteException {
    return "config_" + pluginclass.getSimpleName() + "_" + konto.getID();
  }
  
}

