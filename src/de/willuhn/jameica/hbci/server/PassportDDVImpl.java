/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportDDVImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/03/30 22:07:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.PassportDDV;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportDDVImpl
  extends PassportImpl
  implements PassportDDV {

	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;

  /**
   * @throws RemoteException
   */
  public PassportDDVImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#getPort()
   */
  public int getPort() throws RemoteException {
		try {
			return new Integer(getParam(PassportDDV.PORT)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 0;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setPort(int)
   */
  public void setPort(int port) throws RemoteException {
  	setParam(PassportDDV.PORT,""+port);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#getCTNumber()
   */
  public int getCTNumber() throws RemoteException {
		try {
			return new Integer(getParam(PassportDDV.CTNUMBER)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 0;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setCTNumber(int)
   */
  public void setCTNumber(int ctNumber) throws RemoteException {
		setParam(PassportDDV.CTNUMBER,""+ctNumber);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#useBIO()
   */
  public boolean useBIO() throws RemoteException {
		return "1".equals(getParam(PassportDDV.USEBIO));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setBIO(boolean)
   */
  public void setBIO(boolean bio) throws RemoteException {
		setParam(PassportDDV.USEBIO,bio ? "1" : "0");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException {
		return "1".equals(getParam(PassportDDV.SOFTPIN));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setSoftPin(boolean)
   */
  public void setSoftPin(boolean softPin) throws RemoteException {
		setParam(PassportDDV.SOFTPIN,softPin ? "1" : "0");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#getEntryIndex()
   */
  public int getEntryIndex() throws RemoteException {
		try {
			return new Integer(getParam(PassportDDV.ENTRYIDX)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 1;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setEntryIndex(int)
   */
  public void setEntryIndex(int index) throws RemoteException {
		setParam(PassportDDV.ENTRYIDX,""+index);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#open()
   */
  public HBCIHandler open() throws RemoteException {

		if (isOpen())
			return handler;

		try {
			String path = Settings.getPath();
			File f = new File(path);
			String absolutePath = f.getAbsolutePath();
	
			HBCIUtils.setParam("client.passport.default","DDV");
			HBCIUtils.setParam("client.passport.DDV.path",absolutePath + "/passports/");
	
	
			String os = System.getProperty("os.name");
	
			if ("win32".equals(os))
			{
				HBCIUtils.setParam("client.passport.DDV.libname.ddv",
					absolutePath + File.separator +
					"lib" + File.separator +
					"libhbci4java-card-win32.dll");
				HBCIUtils.setParam("client.passport.DDV.libname.ctapi",
					absolutePath + File.separator +
					"lib" + File.separator +
					"libtowitoko-2.0.7.dll");
			}
			else
			{
				HBCIUtils.setParam("client.passport.DDV.libname.ddv",
					absolutePath + File.separator +
					"lib" + File.separator +
					"libhbci4java-card-linux.so");
				HBCIUtils.setParam("client.passport.DDV.libname.ctapi",
					absolutePath + File.separator +
					"lib" + File.separator +
					"libtowitoko-2.0.7.so");
			}
			HBCIUtils.setParam("client.passport.DDV.port",		""+getPort());
			HBCIUtils.setParam("client.passport.DDV.ctnumber",""+getCTNumber());
			HBCIUtils.setParam("client.passport.DDV.usebio",	useBIO() ? "1" : "0");
			HBCIUtils.setParam("client.passport.DDV.softpin",	useSoftPin() ? "1" : "0");
			HBCIUtils.setParam("client.passport.DDV.entryidx",""+getEntryIndex());
	
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
 * $Log: PassportDDVImpl.java,v $
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