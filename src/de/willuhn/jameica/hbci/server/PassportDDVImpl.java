/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportDDVImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/12 23:46:46 $
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

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportDDVImpl
  extends PassportImpl
  implements PassportDDV {

	private HBCIPassport hbciPassport = null;
	private boolean opened = false;

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
		return "1".equals((String) getParam(PassportDDV.USEBIO));
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
		return "1".equals((String) getParam(PassportDDV.SOFTPIN));
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
  public void open() throws RemoteException {
		new Opener().start();
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Passport#isOpen()
	 */
	public boolean isOpen() throws RemoteException {
		return opened;
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#close()
   */
  public void close() throws RemoteException {
		if (hbciPassport == null || !opened)
			return;
		hbciPassport.close();
		Application.getLog().info("passport successfully closed");
  }

	private class Opener extends Thread
	{
	    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
			try {
				String path = Settings.getPath();
				File f = new File(path);
				String absolutePath = f.getAbsolutePath();

				HBCIUtils.setParam("client.passport.default","DDV");
				HBCIUtils.setParam("client.passport.DDV.path",absolutePath + File.separator + "passports");


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
				opened = true;					
				GUI.setActionText(I18N.tr("Chipkarte erfolgreich gelesen."));
				Application.getLog().info("passport successfully opened");
//			HBCIHandler handler=new HBCIHandler("210",passport);
	    }
      catch (Exception e)
      {
      	opened = false;
      	Application.getLog().error("error while opening chipcard",e);
				GUI.setActionText(I18N.tr("Fehler beim Lesen der Chipkarte."));
      }
    }
	}

}


/**********************************************************************
 * $Log: PassportDDVImpl.java,v $
 * Revision 1.2  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/