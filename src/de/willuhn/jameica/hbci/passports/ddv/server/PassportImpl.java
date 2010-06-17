/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/PassportImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/06/17 11:45:49 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.View;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung der Persistenz des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportImpl extends UnicastRemoteObject implements Passport
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	private de.willuhn.jameica.system.Settings settings;
	
  /**
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getPort()
   */
  public String getPort() throws RemoteException {
		return settings.getString(Passport.PORT,PORTS[0]);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setPort(java.lang.String)
   */
  public void setPort(String port) throws RemoteException {
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
   * @see de.willuhn.jameica.hbci.passport.Passport#getHandle()
   */
  public PassportHandle getHandle() throws RemoteException {
    return new PassportHandleImpl(this);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException {
		return settings.getString(Passport.CTAPI,"");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setCTAPIDriver(java.lang.String)
   */
  public void setCTAPIDriver(String file) throws RemoteException {
		settings.setAttribute(Passport.CTAPI,file);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getJNILib()
   */
  public String getJNILib() throws RemoteException
  {
    // BUGZILLA 646
    return settings.getString(Passport.JNILIB,null);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setJNILib(java.lang.String)
   */
  public void setJNILib(String file) throws RemoteException
  {
    settings.setAttribute(Passport.JNILIB,file);
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getName()
   */
  public String getName() throws RemoteException {
    return i18n.tr("Chipkarte (DDV)");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getInfo()
   */
  public String getInfo() throws RemoteException
  {
    String name = "-";
    String s = settings.getString("readerpreset",null);
    if (s != null)
      name = this.getReaderPresets().getName();
    return i18n.tr("konfigurierter Kartenleser: {0}",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException {
    return View.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getPortForName(java.lang.String)
   */
  public int getPortForName(String name) throws RemoteException
  {
  	if (name == null || name.length() == 0)
	    return 0;

		for (int i=0;i<Passport.PORTS.length;++i)
		{
			if (Passport.PORTS[i].equals(name))
				return i;
		}
	  return 0;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getReaderPresets()
   */
  public Reader getReaderPresets() throws RemoteException
  {
		String s = settings.getString("readerpreset",CustomReader.class.getName());
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
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setReaderPresets(de.willuhn.jameica.hbci.passports.ddv.rmi.Reader)
   */
  public void setReaderPresets(Reader reader) throws RemoteException
  {
  	if (reader == null)
  		return;
  	settings.setAttribute("readerpreset",reader.getID());
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#init(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void init(Konto konto) throws RemoteException
  {
  	// brauchen wir bei DDV nicht.
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getHBCIVersion()
   */
  public String getHBCIVersion() throws RemoteException
  {
    // Default ist jetzt "210", weil sonst ein Nullpointer bei der ersten Verwendung auftritt
    return settings.getString("hbciversion","210");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setHBCIVersion(java.lang.String)
   */
  public void setHBCIVersion(String version) throws RemoteException
  {
    settings.setAttribute("hbciversion",version);
  }
}


/**********************************************************************
 * $Log: PassportImpl.java,v $
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
 *
 * Revision 1.19  2008/11/02 22:48:16  willuhn
 * @C BUGZILLA 646
 *
 * Revision 1.18  2006/04/05 21:01:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2006/04/05 15:15:42  willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 *
 * Revision 1.16  2005/06/27 11:24:30  web0
 * @N HBCI-Version aenderbar
 *
 * Revision 1.15  2005/02/20 19:04:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2005/02/06 17:46:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2005/02/01 18:26:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/12 18:25:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/17 14:06:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/27 23:40:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/07/27 23:39:29  willuhn
 * @N Reader presets
 *
 * Revision 1.8  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 * Revision 1.7  2004/07/25 15:05:40  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:19  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/07/19 22:37:28  willuhn
 * @B gna - Chipcard funktioniert ja doch ;)
 *
 * Revision 1.3  2004/05/05 22:21:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/05 22:14:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/04 23:24:34  willuhn
 * @N separated passports into eclipse project
 *
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