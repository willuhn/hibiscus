/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.manager.MatrixCode;
import org.kapott.hbci.manager.QRCode;
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
import de.willuhn.jameica.hbci.passports.pintan.PhotoTANDialog;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMech;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMechDialog;
import de.willuhn.jameica.hbci.passports.pintan.SelectConfigDialog;
import de.willuhn.jameica.hbci.passports.pintan.TANDialog;
import de.willuhn.jameica.hbci.passports.pintan.TanMediaDialog;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.HBCIContext;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports vom Typ "PIN/TAN".
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
				hbciVersion = HBCIVersion.HBCI_300.getId();

      Logger.info("[PIN/TAN] url         : " + config.getURL());
      Logger.info("[PIN/TAN] blz         : " + config.getBLZ());
      Logger.info("[PIN/TAN] filter      : " + config.getFilterType());
      Logger.info("[PIN/TAN] HBCI version: " + hbciVersion);

      //////////////////////
      // BUGZILLA 831
      // Siehe auch Stefans Mail vom 10.03.2010 - Betreff "Re: [hbci4java] Speicherung des TAN-Verfahrens im PIN/TAN-Passport-File?"
      PtSecMech mech = config.getStoredSecMech();
      String secmech = mech != null ? StringUtils.trimToNull(mech.getId()) : null;

      Logger.info("[PIN/TAN] using stored tan sec mech: " + (mech != null ? mech.toString() : "<ask-user>"));
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
    catch (OperationCanceledException oce)
    {
      close();
      throw oce;
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
		  this.saveContextData();
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
   * Speichert gesammelte Context-Daten in der Konfiguration.
   */
  private void saveContextData()
  {
    if (hbciPassport == null)
      return;

    final AbstractHBCIPassport ap = (AbstractHBCIPassport) this.hbciPassport;
    if (this.config == null)
      this.config = (PinTanConfig) ap.getPersistentData(PassportHandle.CONTEXT_CONFIG);
    
    if (this.config == null)
      return;

    try
    {
      String s1 = (String) ap.getPersistentData(PassportHandle.CONTEXT_SECMECHLIST);
      if (s1 != null && s1.length() > 0) // sicherstellen, dass es nicht ueberschrieben wird, wenn nichts uebergeben wird
        this.config.setAvailableSecMechs(s1);
      
      String s2 = (String) ap.getPersistentData(PassportHandle.CONTEXT_TANMEDIALIST);
      if (s2 != null && s2.length() > 0) // sicherstellen, dass es nicht ueberschrieben wird, wenn nichts uebergeben wird
        this.config.setAvailableTanMedias(s2);
    }
    catch (Exception e)
    {
      Logger.error("unable to apply context data",e);
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

			ArrayList<Konto> result = new ArrayList<>();
			Konto k = null;
			for (org.kapott.hbci.structures.Konto konto : konten)
			{
				k = Converter.HBCIKonto2HibiscusKonto(konto, PassportImpl.class);
				Logger.debug("found account " + k.getKontonummer());
				result.add(k);
			}
			return result.toArray(new Konto[result.size()]);
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

      case HBCICallback.NEED_PT_PHOTOTAN:
      {
        Logger.debug("got phototan code, using phototan dialog");
        final MatrixCode code = new MatrixCode(retData.toString());
        TANDialog dialog = new PhotoTANDialog(config,code.getImage());
        dialog.setContext(this.getContext(passport));
        dialog.setText(msg);
        retData.replace(0,retData.length(),(String)dialog.open());
        return true;
      }

      case HBCICallback.NEED_PT_QRTAN:
      {
        Logger.debug("got QR tan code, using qrtan dialog");
        final QRCode code = new QRCode(retData.toString(),msg);
        TANDialog dialog = new PhotoTANDialog(config,code.getImage());
        dialog.setContext(this.getContext(passport));
        dialog.setText(code.getMessage());
        retData.replace(0,retData.length(),(String)dialog.open());
        return true;
      }

      case HBCICallback.NEED_PT_TAN:
      {
        TANDialog dialog = null;
        
        String flicker = retData.toString();
        if (flicker != null && flicker.length() > 0)
        {
          Logger.debug("got flicker code " + flicker);
          // Wir haben einen Flicker-Code. Also zeigen wir den Flicker-Dialog statt
          // dem normalen TAN-Dialog an
          Logger.info("using chiptan OPTIC/USB");
          dialog = new ChipTANDialog(config,flicker);
        }
        
        // regulaerer TAN-Dialog
        if (dialog == null)
        {
          Logger.info("using chiptan MANUAL");
          Logger.debug("using regular tan dialog");
          dialog = new TANDialog(config);
        }
        
        dialog.setContext(this.getContext(passport));
        dialog.setText(msg);
        retData.replace(0,retData.length(),(String)dialog.open());
        return true;
      }

      // BUGZILLA 200
      case HBCICallback.NEED_PT_SECMECH:
      {
        Logger.debug("GOT PIN/TAN secmech list: " + msg + " ["+retData.toString()+"]");
        ((AbstractHBCIPassport)passport).setPersistentData(PassportHandle.CONTEXT_SECMECHLIST,retData.toString());

        if (config != null)
        {
          PtSecMech mech = config.getStoredSecMech();
          String type = mech != null ? StringUtils.trimToNull(mech.getId()) : null;
          if (type != null)
          {
            // Wir checken vorher noch, ob es das TAN-Verfahren ueberhaupt noch gibt
            PtSecMech m = PtSecMech.contains(retData.toString(),type);
            if (m != null)
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
        Logger.debug("PIN/TAN media name requested: " + msg + " ["+retData.toString()+"]");
        ((AbstractHBCIPassport)passport).setPersistentData(PassportHandle.CONTEXT_TANMEDIALIST,retData.toString());
        
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
  
  /**
   * Versucht den zugehoerigen Auftrag zu ermitteln.
   * @param passport der Passport.
   * @return der Auftrag oder NULL, wenn er nicht ermittelbar war.
   */
  private HibiscusDBObject getContext(HBCIPassport passport)
  {
    String externalId = null;
    
    try
    {
      if (!(passport instanceof AbstractHBCIPassport))
        return null;
      
      externalId = (String) ((AbstractHBCIPassport)passport).getPersistentData("externalid");
      return HBCIContext.unserialize(externalId);
    }
    catch (Exception e)
    {
      Logger.error("unable to load transfer for external id: " + externalId,e);
    }
    
    return null;
  }

}
