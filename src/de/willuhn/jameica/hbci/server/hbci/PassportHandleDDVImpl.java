/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/Attic/PassportHandleDDVImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/19 22:05:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.jameica.hbci.rmi.hbci.PassportHandleDDV;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportHandleDDVImpl
  extends UnicastRemoteObject
  implements PassportHandleDDV {

	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;

	private PassportDDV passport = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportHandleDDVImpl(PassportDDV passport) throws RemoteException {
		this.passport = passport;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#open()
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
			{
				HBCIUtils.setParam("client.passport.DDV.libname.ddv",
					Settings.getLibPath() + "/libhbci4java-card-win32.dll");
				HBCIUtils.setParam("client.passport.DDV.libname.ctapi",
					Settings.getLibPath() + "/libtowitoko-2.0.7.dll");
			}
			else
			{
				HBCIUtils.setParam("client.passport.DDV.libname.ddv",
					Settings.getLibPath() + "/libhbci4java-card-linux.so");
				HBCIUtils.setParam("client.passport.DDV.libname.ctapi",
					Settings.getLibPath() + "/libtowitoko-2.0.7.so");
			}
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
	 * @see de.willuhn.jameica.hbci.rmi.Passport#isOpen()
	 */
	public boolean isOpen() throws RemoteException {
		return handler != null && hbciPassport != null;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#close()
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
   * @see de.willuhn.jameica.hbci.rmi.Passport#getKonten()
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
 * $Log: PassportHandleDDVImpl.java,v $
 * Revision 1.1  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/