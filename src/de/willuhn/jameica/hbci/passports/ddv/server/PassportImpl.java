/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/PassportImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/27 22:28:54 $
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

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.hbci.PassportHandle;

/**
 * Implementierung der Persistenz des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportImpl
  extends de.willuhn.jameica.hbci.server.PassportImpl
  implements Passport {

  /**
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getPort()
   */
  public int getPort() throws RemoteException {
		try {
			return new Integer(getParam(Passport.PORT)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 0;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setPort(int)
   */
  public void setPort(int port) throws RemoteException {
  	setParam(Passport.PORT,""+port);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getCTNumber()
   */
  public int getCTNumber() throws RemoteException {
		try {
			return new Integer(getParam(Passport.CTNUMBER)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 0;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setCTNumber(int)
   */
  public void setCTNumber(int ctNumber) throws RemoteException {
		setParam(Passport.CTNUMBER,""+ctNumber);
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#useBIO()
   */
  public boolean useBIO() throws RemoteException {
		return "1".equals(getParam(Passport.USEBIO));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setBIO(boolean)
   */
  public void setBIO(boolean bio) throws RemoteException {
		setParam(Passport.USEBIO,bio ? "1" : "0");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException {
		return "1".equals(getParam(Passport.SOFTPIN));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setSoftPin(boolean)
   */
  public void setSoftPin(boolean softPin) throws RemoteException {
		setParam(Passport.SOFTPIN,softPin ? "1" : "0");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#getEntryIndex()
   */
  public int getEntryIndex() throws RemoteException {
		try {
			return new Integer(getParam(Passport.ENTRYIDX)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 1;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setEntryIndex(int)
   */
  public void setEntryIndex(int index) throws RemoteException {
		setParam(Passport.ENTRYIDX,""+index);
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
		String s = getParam(Passport.CTAPI);
		if (s != null)
			return s;
		return Settings.getLibPath() + "/libtowitoko-2.0.7.so";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Passport#setCTAPIDriver(java.lang.String)
   */
  public void setCTAPIDriver(String file) throws RemoteException {
		setParam(Passport.CTAPI,file);
  }

}


/**********************************************************************
 * $Log: PassportImpl.java,v $
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