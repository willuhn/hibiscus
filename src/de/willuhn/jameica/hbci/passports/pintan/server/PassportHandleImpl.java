/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/server/PassportHandleImpl.java,v $
 * $Revision: 1.5.2.1 $
 * $Date: 2010/12/16 16:31:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
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
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMech;
import de.willuhn.jameica.hbci.passports.pintan.PtSecMechDialog;
import de.willuhn.jameica.hbci.passports.pintan.SelectConfigDialog;
import de.willuhn.jameica.hbci.passports.pintan.TANDialog;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
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
          SelectConfigDialog d = new SelectConfigDialog(SelectConfigDialog.POSITION_CENTER);
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

      // Wir speichern die verwendete PIN/TAN-Config im Passport. Dann wissen wir
      // spaeter in den HBCI-Callbacks noch, aus welcher Config der Passport
      // erstellt wurde. Wird z.Bsp. vom Payment-Server benoetigt.
      ((AbstractHBCIPassport)hbciPassport).setPersistentData(CONTEXT_CONFIG,config);

      String hbciVersion = config.getHBCIVersion();
      if (hbciVersion == null || hbciVersion.length() == 0)
        hbciVersion = "plus";

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
      // BUGZILLA 62
      case HBCICallback.NEED_PT_TAN:
        
        TANDialog td = new TANDialog(config);
        td.setText(msg);
        retData.replace(0,retData.length(),(String)td.open());
        return true;

      // BUGZILLA 200
      case HBCICallback.NEED_PT_SECMECH:

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
    return false;
  }

}


/**********************************************************************
 * $Log: PassportHandleImpl.java,v $
 * Revision 1.5.2.1  2010/12/16 16:31:36  willuhn
 * @N BACKPORT 0033
 *
 * Revision 1.6  2010-12-15 13:17:25  willuhn
 * @N Code zum Parsen der TAN-Verfahren in PtSecMech ausgelagert. Wenn ein TAN-Verfahren aus Vorauswahl abgespeichert wurde, wird es nun nur noch dann automatisch verwendet, wenn es in der aktuellen Liste der TAN-Verfahren noch enthalten ist. Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=12545
 *
 * Revision 1.5  2010-10-27 10:25:10  willuhn
 * @C Unnoetiges Fangen und Weiterwerfen von Exceptions
 *
 * Revision 1.4  2010-09-29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 * Revision 1.3  2010-09-08 15:04:52  willuhn
 * @N Config des Sicherheitsmediums als Context in Passport speichern
 *
 * Revision 1.2  2010-09-07 15:17:08  willuhn
 * @N GUI-Cleanup
 *
 * Revision 1.1  2010/06/17 11:38:16  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.15  2010/03/10 15:42:14  willuhn
 * @N BUGZILLA 831
 **********************************************************************/