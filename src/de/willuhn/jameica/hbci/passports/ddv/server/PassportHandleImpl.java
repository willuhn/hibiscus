/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/PassportHandleImpl.java,v $
 * $Revision: 1.2 $
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

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.PassportHandle;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportHandleImpl
  extends UnicastRemoteObject
  implements PassportHandle {

	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;

	private Passport passport = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportHandleImpl(Passport passport) throws RemoteException {
		this.passport = passport;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.PassportHandle#open()
   */
  public HBCIHandler open() throws RemoteException {

		if (isOpen())
			return handler;

		try {
	
			Application.getLog().info("using passport path " + Settings.getWorkPath() + "/passports/");
			Application.getLog().info("using library path " + Settings.getLibPath());
			HBCIUtils.setParam("client.passport.default","DDV");
			HBCIUtils.setParam("client.passport.DDV.path",Settings.getWorkPath() + "/passports/");
	
			File f = new File(Settings.getWorkPath() + "/passports/");
			if (!f.exists())
				f.mkdirs();

			String os = System.getProperty("os.name");
	
			if ("win32".equals(os))
				HBCIUtils.setParam("client.passport.DDV.libname.ddv",  Settings.getLibPath() + "/libhbci4java-card-win32.dll");
			else
				HBCIUtils.setParam("client.passport.DDV.libname.ddv",  Settings.getLibPath() + "/libhbci4java-card-linux.so");

			HBCIUtils.setParam("client.passport.DDV.libname.ctapi",passport.getCTAPIDriver());

			HBCIUtils.setParam("client.passport.DDV.port",		""+passport.getPort());
			HBCIUtils.setParam("client.passport.DDV.ctnumber",""+passport.getCTNumber());
			HBCIUtils.setParam("client.passport.DDV.usebio",	passport.useBIO() ? "1" : "0");
			HBCIUtils.setParam("client.passport.DDV.softpin",	passport.useSoftPin() ? "1" : "0");
			HBCIUtils.setParam("client.passport.DDV.entryidx",""+passport.getEntryIndex());
	
			hbciPassport = AbstractHBCIPassport.getInstance();
			Application.getLog().info("passport opened");
			handler=new HBCIHandler("210",hbciPassport);
			return handler;
		}
		catch (Exception e)
		{
			close();
			Application.getLog().error("error while opening chipcard",e);
			throw new RemoteException("error while opening chipcard",e);
		}
  }

	/**
   * @see de.willuhn.jameica.hbci.rmi.hbci.PassportHandle#isOpen()
   */
  public boolean isOpen() throws RemoteException {
		return handler != null && hbciPassport != null;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.PassportHandle#close()
   */
  public void close() throws RemoteException {
		if (hbciPassport == null && handler == null)
			return;
		try {
			handler.close();
		}
		catch (Exception e) {/*useless*/}
		hbciPassport = null;
		handler = null;
		Application.getLog().info("passport closed");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.PassportHandle#getKonten()
   */
  public Konto[] getKonten() throws RemoteException {
		try {
			open();
			org.kapott.hbci.structures.Konto[] konten = hbciPassport.getAccounts();
			if (konten == null || konten.length == 0)
				return new Konto[]{};

			ArrayList result = new ArrayList();
			Konto k = null;
			for (int i=0;i<konten.length;++i)
			{
				k = (Konto) Settings.getDatabase().createObject(Konto.class,null);
				k.setBLZ(konten[i].blz);
				k.setKontonummer(konten[i].number);
				k.setKundennummer(konten[i].customerid);
				k.setName(konten[i].name);
				k.setBezeichnung(konten[i].type);
				k.setWaehrung(konten[i].curr);
				result.add(k);
			}
			return (Konto[]) result.toArray(new Konto[result.size()]);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		finally
		{
			try {
				close();
			}
			catch (RemoteException e2) {/*useless*/}
		}
  }

}


/**********************************************************************
 * $Log: PassportHandleImpl.java,v $
 * Revision 1.2  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.1  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.2  2004/04/25 17:41:05  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/