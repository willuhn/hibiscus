/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/PassportHandleImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/06/17 11:45:48 $
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

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportHandleImpl extends UnicastRemoteObject implements PassportHandle
{
  
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(PassportImpl.class);

	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;

	private Passport passport = null;
  
  private I18N i18n;

  /**
   * ct.
   * @param passport
   * @throws RemoteException
   */
  public PassportHandleImpl(Passport passport) throws RemoteException {
		this.passport = passport;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#open()
   */
  public HBCIHandler open() throws RemoteException, ApplicationException
  {

		if (isOpen())
			return handler;

		Logger.info("open ddv passport");
		try {
	
			Logger.info("using passport path " + Settings.getWorkPath() + "/passports/");
			Logger.info("using library path " + Settings.getLibPath());
			HBCIUtils.setParam("client.passport.DDV.path",Settings.getWorkPath() + "/passports/");
	
			File f = new File(Settings.getWorkPath() + "/passports/");
			if (!f.exists())
				f.mkdirs();

	
			String ctapiDriver = passport.getCTAPIDriver();

			if (ctapiDriver == null || ctapiDriver.length() == 0)
				throw new RemoteException(i18n.tr("CTAPI-Treiber nicht definiert"));

			String jniLib = passport.getJNILib();

			// Nichts im passport angegeben
			if (jniLib == null || jniLib.length() == 0)
        jniLib = AbstractReader.getDefaultJNILib();

      File jni = new File(jniLib);
      
      // Wert aus Passport ungueltig
      if (!jni.exists() || !jni.canRead() || !jni.isFile())
        jniLib = AbstractReader.getDefaultJNILib();

      // BUGZILLA 43 http://www.willuhn.de/bugzilla/show_bug.cgi?id=43
      jni = new File(jniLib);
      if (!jni.exists() || !jni.isFile() || !jni.canRead())
        throw new ApplicationException(i18n.tr("JNI-Bibliothek {0} konnte nicht geladen werden",jniLib)); 

      File ctapi = new File(ctapiDriver);
      if (!ctapi.exists() || !ctapi.isFile() || !ctapi.canRead())
        throw new ApplicationException(i18n.tr("CTAPI-Treiber-Datei \"{0}\" des Kartenlesers konnte nicht geladen werden",ctapiDriver)); 
      
			Logger.info("using JNI lib " + jniLib);
			HBCIUtils.setParam(Passport.JNILIB, jniLib);

			Logger.info("using CTAPI driver " + ctapiDriver);
			HBCIUtils.setParam(Passport.CTAPI, ctapiDriver);

			Logger.info("using Port " + passport.getPort());
			Logger.info("using Port ID " + passport.getPortForName(passport.getPort()));
			HBCIUtils.setParam(Passport.PORT, ""+passport.getPortForName(passport.getPort()));

			Logger.info("using ctnumber " + passport.getCTNumber());
			HBCIUtils.setParam(Passport.CTNUMBER,""+passport.getCTNumber());

			Logger.info("using biometric features " + passport.useBIO());
			HBCIUtils.setParam(Passport.USEBIO,	passport.useBIO() ? "1" : "0");

			Logger.info("using soft pin " + passport.useSoftPin());
			HBCIUtils.setParam(Passport.SOFTPIN,	passport.useSoftPin() ? "1" : "0");

			Logger.info("using entry index " + passport.getEntryIndex());
			HBCIUtils.setParam(Passport.ENTRYIDX,""+passport.getEntryIndex());
	
      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
      HBCICallback callback = ((HBCI)plugin).getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(this);

      hbciPassport = AbstractHBCIPassport.getInstance("DDV");
			Logger.info("ddv passport opened");

			Logger.info("using HBCI version " + passport.getHBCIVersion());
			handler=new HBCIHandler(passport.getHBCIVersion(),hbciPassport);
      
			return handler;
		}
    catch (RemoteException re)
    {
      close();
      throw re;
    }
    catch (ApplicationException ae)
    {
      close();
      throw ae;
    }
		catch (Exception e)
		{
			close();
			throw new RemoteException("error while opening chipcard",e);
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#isOpen()
   */
  public boolean isOpen() throws RemoteException {
		return handler != null && hbciPassport != null;
	}

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#close()
   */
  public void close() throws RemoteException {
		if (hbciPassport == null && handler == null)
			return;
		try {
			Logger.info("closing ddv passport");
			handler.close();
		}
		catch (Exception e) {/*useless*/}
		hbciPassport = null;
		handler = null;

    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    HBCICallback callback = ((HBCI)plugin).getHBCICallback();
    if (callback != null && (callback instanceof HBCICallbackSWT))
      ((HBCICallbackSWT)callback).setCurrentHandle(null);
    
    Logger.info("ddv passport closed");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#getKonten()
   */
  public Konto[] getKonten() throws RemoteException, ApplicationException
  {
  	Logger.info("reading accounts from ddv passport");
		try {
			open();
			org.kapott.hbci.structures.Konto[] konten = hbciPassport.getAccounts();
			if (konten == null || konten.length == 0)
			{
				Logger.info("no accounts found");
				return new Konto[]{};
			}

			ArrayList result = new ArrayList();
			Konto k = null;
			for (int i=0;i<konten.length;++i)
			{
				k = Converter.HBCIKonto2HibiscusKonto(konten[i], Passport.class);
				Logger.debug("found account " + k.getKontonummer());
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

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public boolean callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) throws Exception
  {
    
    switch (reason) {
    
      case HBCICallback.NEED_CHIPCARD:
        return handleCallback(i18n.tr("Bitte legen Sie die Chipkarte in das Lesegerät"),true,settings.getBoolean("waitfor.card.insert",false));

      case HBCICallback.HAVE_CHIPCARD:
        return handleCallback(i18n.tr("HBCI-Chipkarte wird ausgelesen."),false,false);

      case HBCICallback.NEED_HARDPIN:
        return handleCallback(i18n.tr("Bitte geben Sie die PIN in Ihren Chipkarten-Leser ein"),true,settings.getBoolean("waitfor.card.pin",false));

      case HBCICallback.HAVE_HARDPIN:
        return handleCallback(i18n.tr("PIN wurde eingegeben."),false,false);

      case HBCICallback.NEED_REMOVE_CHIPCARD:
        return handleCallback(i18n.tr("Bitte entfernen Sie die Chipkarte aus dem Lesegerät."),false,settings.getBoolean("waitfor.card.eject",false));
    }
    
    return false;
  }
  
  /**
   * Uebernimmt den Callback.
   * @param text ANzuzeigender Text.
   * @param displayKonto true, wenn auch das Konto noch angezeigt werden soll.
   * @param wait true, wenn die Anzeige in einem modalen Dialog erfolgen soll.
   * Der Vorgang wird in dem Fall erst dann fortgesetzt, wenn der User auf OK klickt.
   * @return true oder false wenn der Callback behandelt wurde oder nicht.
   * @throws Exception
   */
  private boolean handleCallback(String text, boolean displayKonto, boolean wait) throws Exception
  {
    if (displayKonto)
    {
      Konto konto = HBCIFactory.getInstance().getCurrentKonto();
      if (konto != null)
        text += ". " + konto.getLongName();
    }
    
    if (wait)
    {
      Application.getCallback().notifyUser(text);
    }
    else
    {
      HBCIFactory.getInstance().getProgressMonitor().setStatusText(text);
      GUI.getStatusBar().setSuccessText(text);
    }
    return true;
  }
}


/**********************************************************************
 * $Log: PassportHandleImpl.java,v $
 * Revision 1.4  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.35  2010/02/13 15:32:18  willuhn
 * @N BUGZILLA 823
 *
 * Revision 1.34  2010/02/07 22:25:27  willuhn
 * @N BUGZILLA #815
 *
 * Revision 1.33  2008/12/01 09:43:25  willuhn
 * @B Fallback auf Default-JNI Lib, wenn konfigurierte nicht lesbar
 *
 * Revision 1.32  2008/02/26 18:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2006/10/23 14:58:37  willuhn
 * @B reset current handle
 *
 * Revision 1.30  2006/08/21 12:27:11  willuhn
 * @N HBCICallbackSWT.setCurrentHandle
 *
 * Revision 1.29  2006/08/06 13:15:49  willuhn
 * @B bug 256
 *
 * Revision 1.28  2006/08/03 13:51:22  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.27  2006/04/05 21:01:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2006/04/05 15:15:42  willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 *
 * Revision 1.25  2006/01/08 22:22:37  willuhn
 * @B 176
 * @B 177
 *
 * Revision 1.24  2005/08/08 15:07:36  willuhn
 * @N added jnilib for mac os
 * @N os autodetection for mac os
 *
 * Revision 1.23  2005/08/01 23:28:20  web0
 * *** empty log message ***
 *
 * Revision 1.22  2005/06/27 11:24:30  web0
 * @N HBCI-Version aenderbar
 *
 * Revision 1.21  2005/06/21 20:19:09  web0
 * *** empty log message ***
 *
 * Revision 1.20  2005/04/05 21:22:33  web0
 * @B bug 43
 *
 * Revision 1.19  2004/11/12 18:25:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/10/29 16:09:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/28 23:19:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/19 21:58:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/17 14:06:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 * Revision 1.13  2004/07/27 18:58:06  willuhn
 * @B wrong filename for jni lib
 *
 * Revision 1.12  2004/07/19 22:37:28  willuhn
 * @B gna - Chipcard funktioniert ja doch ;)
 *
 * Revision 1.11  2004/07/18 15:49:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/16 17:13:33  willuhn
 * @B Fehler bei OS-Erkennung
 *
 * Revision 1.9  2004/07/14 23:47:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/08 23:34:51  willuhn
 * @N mehr debug ausgaben
 *
 * Revision 1.7  2004/07/08 23:20:23  willuhn
 * @C Redesign
 *
 * Revision 1.6  2004/07/05 21:27:56  willuhn
 * @B bug in OS detection
 *
 * Revision 1.5  2004/07/01 19:36:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/10 20:56:20  willuhn
 * @D javadoc comments fixed
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
