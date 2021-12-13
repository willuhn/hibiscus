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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.rdh.RDHKeyFactory;
import de.willuhn.jameica.hbci.passports.rdh.View;
import de.willuhn.jameica.hbci.passports.rdh.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports fuer Schluesseldatei.
 */
public class PassportImpl extends UnicastRemoteObject implements Passport
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Konto konto = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Schl�sseldatei");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getInfo()
   */
  public String getInfo() throws RemoteException
  {
    GenericIterator i = RDHKeyFactory.getKeys();
    return i18n.tr("vorhandene Schl�sseldateien: {0}",Integer.toString(i.size()));
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigurations()
   */
  public List<? extends Configuration> getConfigurations() throws RemoteException
  {
    GenericIterator i = RDHKeyFactory.getKeys();
    List<Configuration> configs = new ArrayList<Configuration>();
    while (i.hasNext())
      configs.add((Configuration) i.next());
    return configs;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getHandle()
   */
  public PassportHandle getHandle() throws RemoteException
  {
    return new PassportHandleImpl(this);
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException
  {
    return View.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#init(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void init(Konto konto) throws RemoteException
  {
  	this.konto = konto;
  }

	/**
	 * Liefert das Konto, fuer das der Passport gerade zustaendig ist.
   * @return Konto.
   */
  protected Konto getKonto()
	{
		return konto;
	}
}
