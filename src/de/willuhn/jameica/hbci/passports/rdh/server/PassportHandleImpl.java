/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.PassportProcessCode3072;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.rdh.InsertKeyDialog;
import de.willuhn.jameica.hbci.passports.rdh.KeyPasswordSaveDialog;
import de.willuhn.jameica.hbci.passports.rdh.RDHKeyFactory;
import de.willuhn.jameica.hbci.passports.rdh.SelectSizEntryDialog;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class PassportHandleImpl extends UnicastRemoteObject implements PassportHandle
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private PassportImpl passport = null;
	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;
	private RDHKey key = null;


  /**
   * @param passport
   * @throws RemoteException
   */
  protected PassportHandleImpl(PassportImpl passport) throws RemoteException
  {
    super();
    this.passport = passport;
  }

  /**
   * @param key
   * @throws RemoteException
   */
  public PassportHandleImpl(RDHKey key) throws RemoteException
  {
    super();
    this.key = key;
  }

  /**
   * @throws RemoteException
   */
  public PassportHandleImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#open()
   */
  public HBCIHandler open() throws RemoteException, ApplicationException
  {
		if (isOpen())
			return handler;

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		Logger.info("open rdh passport");
		try {
	
      RDHKey activeKey = this.key != null ? this.key : RDHKeyFactory.findByKonto(passport != null ? passport.getKonto() : null);
      
      if (activeKey == null)
        throw new ApplicationException(i18n.tr("Keine Schlüsseldatei für dieses Konto definiert"));

      String filename = activeKey.getFilename();
      
      File f = new File(filename);
      if (!f.exists())
      {
        InsertKeyDialog kd = new InsertKeyDialog(f);
        Boolean b = (Boolean) kd.open();
        if (b == null || !b.booleanValue())
          throw new OperationCanceledException(i18n.tr("Schlüsseldatei nicht eingelegt oder nicht lesbar"));
      }
      
      Logger.info("using passport file " + filename);

      String hbciVersion = activeKey.getHBCIVersion();
      if (hbciVersion == null)
      {
        // Bei der Neuerstellung fragen wir immer den User nach der HBCI-Version
        // Wir fragen die HBCI-Version via Messaging ab, damit sie ggf. auch
        // (z.Bsp. vom Payment-Server) automatisch beantwortet werden kann.
        QueryMessage msg = new QueryMessage(passport);
        Application.getMessagingFactory().getMessagingQueue("hibiscus.passport.rdh.hbciversion").sendSyncMessage(msg);
        Object data = msg.getData();
        if (data == null || !(data instanceof String))
          throw new ApplicationException(i18n.tr("HBCI-Version nicht ermittelbar"));
        hbciVersion = (String) msg.getData();
        
        // Wir merken uns die Auswahl damit wir den User nicht immer wieder fragen muessen
        // Siehe auch http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=14883
        if (hbciVersion != null)
        {
          Logger.info("storing hbci [" + hbciVersion + "] version for key " + filename);
          activeKey.setHBCIVersion(hbciVersion);
        }
      }

      hbciPassport = activeKey.load();
      
      // Wir speichern die verwendete PIN/TAN-Config im Passport. Dann wissen wir
      // spaeter in den HBCI-Callbacks noch, aus welcher Config der Passport
      // erstellt wurde. Wird z.Bsp. vom Payment-Server benoetigt.
      ((AbstractHBCIPassport)hbciPassport).setPersistentData(CONTEXT_CONFIG,activeKey);

			Logger.info("using HBCI version " + hbciVersion);
			handler = new HBCIHandler(hbciVersion,hbciPassport);
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
      Logger.error("error while opening key",e);
      throw new ApplicationException(i18n.tr("Fehler beim Öffnen des Schlüssels: {0}",e.getMessage()));
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#close()
   */
  public void close() throws RemoteException
  {
		if (hbciPassport == null && handler == null)
			return;

		try
		{
	    this.handleCode3072();
		}
		finally
		{
	    try {
	      Logger.info("closing rdh passport");
	      handler.close();
	    }
	    catch (Exception e) {/*useless*/}
	    hbciPassport = null;
	    handler = null;

	    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
	    HBCICallback callback = ((HBCI)plugin).getHBCICallback();
	    if (callback != null && (callback instanceof HBCICallbackSWT))
	      ((HBCICallbackSWT)callback).setCurrentHandle(null);

	    Logger.info("rdh passport closed");
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
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#isOpen()
   */
  public boolean isOpen() throws RemoteException
  {
		return handler != null && hbciPassport != null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#getKonten()
   */
  public Konto[] getKonten() throws RemoteException, ApplicationException
  {
		Logger.info("reading accounts from rdh passport");
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
  public boolean callback(HBCIPassport p, int reason, String msg, int datatype, StringBuffer retData) throws Exception
  {
    switch (reason)
    {
      case HBCICallback.NEED_SIZENTRY_SELECT:
      {
        SelectSizEntryDialog e = new SelectSizEntryDialog(SelectSizEntryDialog.POSITION_CENTER,retData.toString());
        retData.replace(0,retData.length(),(String)e.open());
        return true;
      }

      case HBCICallback.NEED_PASSPHRASE_LOAD:
      {
        retData.replace(0,retData.length(),DialogFactory.getKeyPassword(p));
        return true;
      }

      case HBCICallback.NEED_PASSPHRASE_SAVE:
      {
        KeyPasswordSaveDialog dialog = new KeyPasswordSaveDialog(AbstractDialog.POSITION_CENTER,p);
        String password = (String) dialog.open();
        retData.replace(0,retData.length(),password);
        return true;
      }
    }
    return false;
  }

}
