/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/PassportImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/05/04 23:07:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passports.ddv.View;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportHandle;
import de.willuhn.util.I18N;

/**
 * Implementierung der Persistenz des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportImpl extends UnicastRemoteObject implements Passport
{

	private de.willuhn.jameica.Settings settings;
	private I18N i18n;
	
  /**
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
    settings = new de.willuhn.jameica.Settings(this.getClass());
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getPort()
   */
  public int getPort() throws RemoteException {
		return settings.getInt(Passport.PORT,0);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setPort(int)
   */
  public void setPort(int port) throws RemoteException {
		settings.setAttribute(Passport.PORT,port);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getCTNumber()
   */
  public int getCTNumber() throws RemoteException {
		return settings.getInt(Passport.CTNUMBER,0);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setCTNumber(int)
   */
  public void setCTNumber(int ctNumber) throws RemoteException {
		settings.setAttribute(Passport.CTNUMBER,ctNumber);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#useBIO()
   */
  public boolean useBIO() throws RemoteException {
		return settings.getBoolean(Passport.USEBIO,false);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setBIO(boolean)
   */
  public void setBIO(boolean bio) throws RemoteException {
		settings.setAttribute(Passport.USEBIO,bio);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException {
  	return settings.getBoolean(Passport.SOFTPIN,true);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setSoftPin(boolean)
   */
  public void setSoftPin(boolean softPin) throws RemoteException {
		settings.setAttribute(Passport.SOFTPIN,softPin);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getEntryIndex()
   */
  public int getEntryIndex() throws RemoteException {
		return settings.getInt(Passport.ENTRYIDX,1);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setEntryIndex(int)
   */
  public void setEntryIndex(int index) throws RemoteException {
		settings.setAttribute(Passport.ENTRYIDX,index);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getHandle()
   */
  public PassportHandle getHandle() throws RemoteException {
    return new PassportHandleImpl(this);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException {
		return settings.getString(Passport.CTAPI,Settings.getLibPath() + "/libtowitoko-2.0.7.so");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setCTAPIDriver(java.lang.String)
   */
  public void setCTAPIDriver(String file) throws RemoteException {
		settings.setAttribute(Passport.CTAPI,file);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getName()
   */
  public String getName() throws RemoteException {
    return i18n.tr("Chipkarte (DDV)");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException {
    return View.class;
  }


}


/**********************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.3  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.2  2004/04/27 22:28:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/27 22:23:55  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.12  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.11  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.9  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.6  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.5  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/