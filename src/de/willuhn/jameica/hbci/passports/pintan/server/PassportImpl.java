/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/server/PassportImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:38:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.View;
import de.willuhn.jameica.hbci.passports.pintan.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung der Persistenz des Passports vom Typ "PIN/TAN".
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

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getName()
   */
  public String getName() throws RemoteException {
    return i18n.tr("PIN/TAN");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getInfo()
   */
  public String getInfo() throws RemoteException
  {
    GenericIterator i = PinTanConfigFactory.getConfigs();
    return i18n.tr("vorhandene PIN/TAN-Konfigurationen: {0}",Integer.toString(i.size()));
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException {
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
   * @see de.willuhn.jameica.hbci.passport.Passport#getHandle()
   */
  public PassportHandle getHandle() throws RemoteException
  {
    return new PassportHandleImpl(this);
  }

	/**
	 * Liefert das aktuelle Konto.
   * @return Konto
   */
  protected Konto getKonto()
	{
		return konto;
	}
}


/**********************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.1  2010/06/17 11:38:16  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.9  2010/04/22 12:08:44  willuhn
 * @R "Backend" wieder entfernt - Offline-Support geht im Konto mit einem "FLAG_OFFLINE" doch bequemer
 *
 * Revision 1.8  2010/04/21 23:14:57  willuhn
 * @N Ralfs Patch fuer Offline-Konten
 * @N Neue Funktion "getBackend()" und erweitertes Build-Script mit "deploy"-Target zu Hibiscus
 *
 * Revision 1.7  2010/04/14 16:56:06  willuhn
 * @N BUGZILLA 471
 *
 * Revision 1.6  2010/04/14 16:51:00  willuhn
 * @N BUGZILLA 471
 *
 * Revision 1.5  2005/08/01 23:28:03  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/06/23 21:52:48  web0
 * @B Bug 80
 *
 * Revision 1.3  2005/03/11 02:43:59  web0
 * @N PIN/TAN works ;)
 *
 * Revision 1.2  2005/03/10 18:38:48  web0
 * @N more PinTan Code
 *
 * Revision 1.1  2005/03/07 12:06:12  web0
 * @N initial import
 *
 **********************************************************************/