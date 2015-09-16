/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.AbstractPinTanPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.PassportProcessCode3072;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.pintan.ChipTANDialog;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMech;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMechDialog;
import de.willuhn.jameica.hbci.passports.pintan.SelectConfigDialog;
import de.willuhn.jameica.hbci.passports.pintan.TANDialog;
import de.willuhn.jameica.hbci.passports.pintan.TanMediaDialog;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportHandleImpl extends UnicastRemoteObject implements PassportHandle
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;

	private PassportImpl passport = null;
  private PinTanConfig config   = null;

  /**
   * ct.
   * @param passport
   * @throws RemoteException
   */
  public PassportHandleImpl(PassportImpl passport) throws RemoteException {
    super();
		this.passport = passport;
  }

  /**
   * @param config
   * @throws RemoteException
   */
  public PassportHandleImpl(PinTanConfig config) throws RemoteException {
    super();
    this.config = config;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#open()
   */
  public HBCIHandler open() throws RemoteException, ApplicationException
  {

		if (isOpen())
			return handler;

		Logger.info("open pin/tan passport");
		try {
	
      if (config == null && this.passport == null)
        throw new ApplicationException(i18n.tr("Keine Konfiguration oder Konto ausgewählt"));

      if (config == null && this.passport != null && this.passport.getKonto() != null)
        config = PinTanConfigFactory.findByKonto(this.passport.getKonto());


      // Mh, nichts da zum Laden, dann fragen wir mal den User
      if (config == null)
      {
        GenericIterator list = PinTanConfigFactory.getConfigs();

        if (list == null || list.size() == 0)
          throw new ApplicationException(i18n.tr("Bitte legen Sie zuerst eine PIN/TAN-Konfiguration an"));
        
        // Wir haben nur eine Config, dann brauchen wir den User nicht fragen
        if (list.size() == 1)
        {
          config = (PinTanConfig) list.next();
        }
        else
        {
          SelectConfigDialog d = new SelectConfigDialog(SelectConfigDialog.POSITION_CENTER,list);
          try
          {
            config = (PinTanConfig) d.open();
          }
          catch (OperationCanceledException oce)
          {
            throw oce;
          }
          catch (Exception e)
          {
            Logger.error("error while choosing config",e);
            throw new ApplicationException(i18n.tr("Fehler bei der Auswahl der PIN/TAN-Konfiguration"));
          }
        }
      }

      if (config == null)
        throw new ApplicationException(i18n.tr("Keine PIN/TAN-Konfiguration für dieses Konto definiert"));
      
			Logger.debug("using passport file " + config.getFilename());

      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
      HBCICallback callback = ((HBCI)plugin).getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(this);

      hbciPassport = config.getPassport();

      {
        AbstractHBCIPassport ap = (AbstractHBCIPassport) hbciPassport;
        
        // Wir speichern die verwendete PIN/TAN-Config im Passport. Dann wissen wir
        // spaeter in den HBCI-Callbacks noch, aus welcher Config der Passport
        // erstellt wurde. Wird z.Bsp. vom Payment-Server benoetigt.
        ap.setPersistentData(CONTEXT_CONFIG,config);
        
        String cannationalacc = config.getCustomProperty("cannationalacc");
        if (cannationalacc != null)
          ap.setPersistentData("cannationalacc",cannationalacc);
      }

			String hbciVersion = config.getHBCIVersion();
			if (hbciVersion == null || hbciVersion.length() == 0)
				hbciVersion = "300";

      Logger.info("[PIN/TAN] url         : " + config.getURL());
      Logger.info("[PIN/TAN] blz         : " + config.getBLZ());
      Logger.info("[PIN/TAN] filter      : " + config.getFilterType());
      Logger.info("[PIN/TAN] HBCI version: " + hbciVersion);

      //////////////////////
      // BUGZILLA 831
      // Siehe auch Stefans Mail vom 10.03.2010 - Betreff "Re: [hbci4java] Speicherung des TAN-Verfahrens im PIN/TAN-Passport-File?"
      String secmech = config.getSecMech();
      if (secmech != null && secmech.trim().length() == 0)
        secmech = null; // nur um sicherzustellen, dass kein Leerstring drinsteht

      Logger.info("[PIN/TAN] tan sec mech: " + secmech);
      ((AbstractPinTanPassport)hbciPassport).setCurrentTANMethod(secmech);
      //////////////////////


			handler=new HBCIHandler(hbciVersion,hbciPassport);
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
			Logger.error("error while opening pin/tan passport",e);
			throw new RemoteException("error while opening pin/tan passport",e);
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

		try
		{
	    this.handleCode3072();
		}
		finally
		{
	    try {
	      Logger.info("closing pin/tan passport");
	      handler.close();
	    }
	    catch (Exception e) {/*useless*/}
	    hbciPassport = null;
	    handler = null;

	    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
	    HBCICallback callback = ((HBCI)plugin).getHBCICallback();
	    if (callback != null && (callback instanceof HBCICallbackSWT))
	      ((HBCICallbackSWT)callback).setCurrentHandle(null);
	    
	    Logger.info("pin/tan passport closed");
		}
		
  }
  
  /**
   * Behandelt die GAD-spezifische Rueckmeldung zur Aenderung der Kundenkennung
   */
  private void handleCode3072()
  {
    if (hbciPassport == null)
      return;
    
    try
    {
      new PassportProcessCode3072().handleAction(hbciPassport);
    }
    catch (Exception e)
    {
      Logger.error("error while applying new user-/customer data",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der geänderten Zugangsdaten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#getKonten()
   */
  public Konto[] getKonten() throws RemoteException, ApplicationException
  {
  	Logger.info("reading accounts from pin/tan passport");
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
				k = Converter.HBCIKonto2HibiscusKonto(konten[i], PassportImpl.class);
				Logger.debug("found account " + k.getKontonummer());
				result.add(k);
			}
			return (Konto[]) result.toArray(new Konto[result.size()]);
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
    switch (reason)
    {
      case HBCICallback.NEED_PT_PIN:
      {
        retData.replace(0,retData.length(),DialogFactory.getPIN(passport));
        return true;
      }

      // BUGZILLA 62
      case HBCICallback.NEED_PT_TAN:
      {
        TANDialog dialog = null;
        
        String flicker = retData.toString();
        if (flicker != null && flicker.length() > 0)
        {
          // Wir haben einen Flicker-Code. Also zeigen wir den Flicker-Dialog statt
          // dem normalen TAN-Dialog an
          Logger.debug("got flicker code " + flicker + ", using optical chiptan dialog");
          dialog = new ChipTANDialog(config,flicker);
        }
        
        // regulaerer TAN-Dialog
        if (dialog == null)
        {
          Logger.debug("using regular tan dialog");
          dialog = new TANDialog(config);
        }
        
        dialog.setText(msg);
        retData.replace(0,retData.length(),(String)dialog.open());
        return true;
      }

      // BUGZILLA 200
      case HBCICallback.NEED_PT_SECMECH:
      {
        if (config != null)
        {
          String type = config.getSecMech();
          if (type != null && type.length() > 0)
          {
            // Wir checken vorher noch, ob es das TAN-Verfahren ueberhaupt noch gibt
            PtSecMech mech = PtSecMech.contains(retData.toString(),type);
            if (mech != null)
            {
              // Jepp, gibts noch
              retData.replace(0,retData.length(),type);
              return true;
            }
          }
        }
        
        PtSecMechDialog ptd = new PtSecMechDialog(config,retData.toString());
        retData.replace(0,retData.length(),(String) ptd.open());
        return true;
      }
        
      // BUGZILLA 827
      case HBCICallback.NEED_PT_TANMEDIA:
      {
        // Wenn wir eine Medienbezeichnung von HBCI4Java gekriegt haben und das genau
        // eine einzige ist. Dann uebernehmen wir diese ohne Rueckfrage. Der User
        // hat hier sonst eh keine andere Wahl.
        String media = retData.toString();
        if (media.length() > 0 && !media.contains("|"))
        {
          Logger.info("having exactly one TAN media name (provided by institute) - automatically using this: " + media);
          retData.replace(0,retData.length(),media);
          return true;
        }

        // Falls wir eine PIN/TAN-Config haben, in der die Medienbezeichnung
        // hinterlegt ist, dann nehmen wir die.
        if (config != null)
        {
          media =  config.getTanMedia();
          if (media != null && media.length() > 0)
          {
            Logger.info("having a stored TAN media name (provided by user) - automatically using this: " + media);
            retData.replace(0,retData.length(),media);
            return true;
          }
        }

        Logger.info("asking user for TAN media (options provided by institute: " + media + ")");
        TanMediaDialog tmd = new TanMediaDialog(config,retData.toString());
        retData.replace(0,retData.length(),(String) tmd.open());
        return true;
      }
    }
    
    return false;
  }

}
