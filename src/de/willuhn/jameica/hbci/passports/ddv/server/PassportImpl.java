/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.DDVConfig;
import de.willuhn.jameica.hbci.passports.ddv.DDVConfigFactory;
import de.willuhn.jameica.hbci.passports.ddv.View;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportImpl extends UnicastRemoteObject implements Passport
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Konto konto = null;

  /**
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
  }

  @Override
  public PassportHandle getHandle() throws RemoteException {
    return new PassportHandleImpl(this.konto);
  }

  @Override
  public String getName() throws RemoteException {
    return i18n.tr("Chipkartenleser");
  }

  @Override
  public String getInfo() throws RemoteException
  {
    List<DDVConfig> list = DDVConfigFactory.getConfigs();
    if (list.size() == 0)
      return "";

    // Wenn nur eine Config vorhanden ist, zeigen wir das Kartenleser-Preset an
    if (list.size() == 1)
      return i18n.tr("vorhandener Kartenleser: {0}",list.get(0).getReaderPreset().getName());

    // Andernfalls die Anzahl der Konfigurationen
    return i18n.tr("vorhandene Kartenleser-Konfigurationen: {0}",Integer.toString(list.size()));
  }

  @Override
  public List<? extends Configuration> getConfigurations() throws RemoteException
  {
    return DDVConfigFactory.getConfigs();
  }

  @Override
  public Class getConfigDialog() throws RemoteException {
    return View.class;
  }

  @Override
  public void init(Konto konto) throws RemoteException
  {
    this.konto = konto;
  }
}


/**********************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.8  2011/04/29 09:17:35  willuhn
 * @N Neues Standard-Interface "Configuration" fuer eine gemeinsame API ueber alle Arten von HBCI-Konfigurationen
 * @R Passports sind keine UnicastRemote-Objekte mehr
 *
 * Revision 1.7  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.6  2010-07-22 22:36:24  willuhn
 * @N Code-Cleanup
 *
 * Revision 1.5  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.23  2010/04/22 12:08:42  willuhn
 * @R "Backend" wieder entfernt - Offline-Support geht im Konto mit einem "FLAG_OFFLINE" doch bequemer
 *
 * Revision 1.22  2010/04/21 23:14:55  willuhn
 * @N Ralfs Patch fuer Offline-Konten
 * @N Neue Funktion "getBackend()" und erweitertes Build-Script mit "deploy"-Target zu Hibiscus
 *
 * Revision 1.21  2010/04/14 16:57:58  willuhn
 * @N BUGZILLA 471
 *
 * Revision 1.20  2010/04/14 16:50:55  willuhn
 * @N BUGZILLA 471
 **********************************************************************/