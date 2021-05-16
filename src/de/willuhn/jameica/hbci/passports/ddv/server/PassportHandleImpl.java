/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.DDVConfig;
import de.willuhn.jameica.hbci.passports.ddv.DDVConfigFactory;
import de.willuhn.jameica.hbci.passports.ddv.SelectConfigDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.services.BeanService;
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
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(DDVConfig.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N(); 

	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;

	private Konto konto      = null;
  private DDVConfig config = null;
  
  /**
   * ct.
   * @param konto
   * @throws RemoteException
   */
  public PassportHandleImpl(Konto konto) throws RemoteException
  {
    super();
		this.konto = konto;
  }

  /**
   * ct.
   * @param config
   * @throws RemoteException
   */
  public PassportHandleImpl(DDVConfig config) throws RemoteException
  {
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

		Logger.info("open ddv passport");
		try
		{
		  // Wenn keine Config, dafuer aber ein Konto angegeben ist, versuchen
		  // wir die Config anhand des Kontos zu ermitteln
      if (config == null && this.konto != null)
        config = DDVConfigFactory.findByKonto(this.konto);

      // Wenn wir immer noch keine Config haben, muss der User waehlen
      if (config == null)
      {
        List<DDVConfig> list = DDVConfigFactory.getConfigs();

        if (list == null || list.size() == 0)
          throw new ApplicationException(i18n.tr("Bitte legen Sie zuerst eine Kartenleser-Konfiguration an"));
        
        // Wir haben nur eine Config, dann brauchen wir den User nicht fragen
        if (list.size() == 1)
        {
          config = (DDVConfig) list.get(0);
        }
        else
        {
          SelectConfigDialog d = new SelectConfigDialog(SelectConfigDialog.POSITION_CENTER);
          try
          {
            config = (DDVConfig) d.open();
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

      // Immer noch keine Config. Dann eben nicht.
      if (config == null)
        throw new ApplicationException(i18n.tr("Keine Kartenleser-Konfiguration vorhanden"));
      
      Logger.debug("using config " + config.getName());

      // Handle im Callback vermerken
      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
      HBCICallback callback = ((HBCI)plugin).getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(this);

      hbciPassport = DDVConfigFactory.createPassport(config);

      // Wir speichern die verwendete DDV-Config im Passport. Dann wissen wir
      // spaeter in den HBCI-Callbacks noch, aus welcher Config der Passport
      // erstellt wurde. Wird z.Bsp. vom Payment-Server benoetigt, um die
      // PIN (basierend auf der UUID der DDV-Config) speichern zu koennen
      ((AbstractHBCIPassport)hbciPassport).setPersistentData(CONTEXT_CONFIG,config);

      Logger.info("ddv passport opened");

      Logger.info("  hbci version: " + config.getHBCIVersion());
			handler = new HBCIHandler(config.getHBCIVersion(),hbciPassport);
      Logger.info("ddv handler opened");
      
			return handler;
		}
    catch (RemoteException re)
    {
      close();
      throw re;
    }
    catch (OperationCanceledException oce)
    {
      close();
      throw oce;
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
    
    switch (reason) {
    
      case HBCICallback.NEED_SOFTPIN:
      {
        retData.replace(0,retData.length(),DialogFactory.getPIN(passport));
        return true;
      }

      case HBCICallback.NEED_CHIPCARD:
        return handleCallback(i18n.tr("Bitte legen Sie die Chipkarte in das Lesegerät"),true,settings.getBoolean("waitfor.card.insert",false), StatusBarMessage.TYPE_INFO);

      case HBCICallback.HAVE_CHIPCARD:
        return handleCallback(i18n.tr("HBCI-Chipkarte wird ausgelesen."),false,false, StatusBarMessage.TYPE_SUCCESS);

      case HBCICallback.NEED_HARDPIN:
        return handleCallback(i18n.tr("Bitte geben Sie die PIN in Ihren Chipkarten-Leser ein"),true,settings.getBoolean("waitfor.card.pin",false), StatusBarMessage.TYPE_INFO);

      case HBCICallback.HAVE_HARDPIN:
        return handleCallback(i18n.tr("PIN wurde eingegeben."),false,false, StatusBarMessage.TYPE_SUCCESS);

      case HBCICallback.NEED_REMOVE_CHIPCARD:
        return handleCallback(i18n.tr("Bitte entfernen Sie die Chipkarte aus dem Lesegerät."),false,settings.getBoolean("waitfor.card.eject",false), StatusBarMessage.TYPE_INFO);
    }
    
    return false;
  }
  
  /**
   * Uebernimmt den Callback.
   * @param text ANzuzeigender Text.
   * @param displayKonto true, wenn auch das Konto noch angezeigt werden soll.
   * @param wait true, wenn die Anzeige in einem modalen Dialog erfolgen soll.
   * Der Vorgang wird in dem Fall erst dann fortgesetzt, wenn der User auf OK klickt.
   * @param type Typ der Message. Zum Beispiel {@link StatusBarMessage#TYPE_INFO}
   * @return true oder false wenn der Callback behandelt wurde oder nicht.
   * @throws Exception
   */
  private boolean handleCallback(String text, boolean displayKonto, boolean wait, int type) throws Exception
  {
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();

    if (displayKonto)
    {
      Konto konto = session != null ? session.getKonto() : null;
      
      if (konto != null)
        text += ". " + konto.getLongName();
    }
    
    if (wait)
    {
      Application.getCallback().notifyUser(text);
    }
    else
    {
      if (session != null)
        session.getProgressMonitor().setStatusText(text);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,type));
    }
    return true;
  }
}


/**********************************************************************
 * $Log: PassportHandleImpl.java,v $
 * Revision 1.13  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.12  2010-10-27 10:25:10  willuhn
 * @C Unnoetiges Fangen und Weiterwerfen von Exceptions
 *
 * Revision 1.11  2010-10-17 21:58:56  willuhn
 * @C Aendern der Bankdaten auf der Karte auch dann moeglich, wenn auf dem Slot ungueltige Daten stehen
 *
 * Revision 1.10  2010-09-29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 * Revision 1.9  2010-09-10 15:47:37  willuhn
 * @R Kein direkter GUI-Code im Handle
 *
 * Revision 1.8  2010-09-08 15:04:52  willuhn
 * @N Config des Sicherheitsmediums als Context in Passport speichern
 *
 * Revision 1.7  2010-09-08 11:24:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.5  2010-07-22 22:36:24  willuhn
 * @N Code-Cleanup
 *
 * Revision 1.4  2010-06-17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.35  2010/02/13 15:32:18  willuhn
 * @N BUGZILLA 823
 *
 * Revision 1.34  2010/02/07 22:25:27  willuhn
 * @N BUGZILLA #815
 **********************************************************************/