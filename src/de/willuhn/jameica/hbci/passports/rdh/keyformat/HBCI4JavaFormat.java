/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.keyformat;

import java.io.File;
import java.rmi.RemoteException;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.exceptions.InvalidPassphraseException;
import org.kapott.hbci.exceptions.NeedKeyAckException;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passports.rdh.InsertKeyDialog;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.rdh.server.RDHKeyImpl;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung des Schluesselformats von HBCI4Java.
 */
public class HBCI4JavaFormat extends AbstractKeyFormat
{
  protected static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#getName()
   */
  public String getName()
  {
    return i18n.tr("HBCI4Java/Hibiscus-Format (RDH)");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#hasFeature(int)
   */
  public boolean hasFeature(int feature)
  {
    switch (feature)
    {
      case KeyFormat.FEATURE_CREATE:
        return true;
      case KeyFormat.FEATURE_IMPORT:
        return true;
    }
    Logger.warn("unknown feature " + feature);
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#importKey(java.io.File)
   */
  public RDHKey importKey(File file) throws ApplicationException, OperationCanceledException
  {
    // Checken, ob die Datei lesbar ist.
    if (file == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"));
    
    if (!file.canRead() || !file.isFile())
      throw new ApplicationException(i18n.tr("Schlüsseldatei nicht lesbar"));
    
    // Das ist ein Hibiscus-Schluessel. Wir lassen den Schluessel gleich dort, wo er ist
    try
    {
      RDHKeyImpl key = new RDHKeyImpl(file);
      key.setFormat(this);
      return key;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to import key " + file.getAbsolutePath(),re);
      throw new ApplicationException(i18n.tr("Schlüsseldatei kann nicht importiert werden: {0}",re.getMessage()));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#createKey(java.io.File)
   */
  public RDHKey createKey(File file) throws ApplicationException, OperationCanceledException
  {
    HBCIHandler handler = null;
    RDHKeyImpl key      = null;
    
    try
    {
      key = new RDHKeyImpl(file);
      key.setFormat(this);

      // Wir machen den Handler einmal auf und wieder zu, damit
      // der Schluessel gleich initialisiert wird.
      HBCIPassport passport = load(key,true);
      passport.saveChanges();
      passport.syncSigId();
      passport.syncSysId();

      // Bei der Neuerstellung fragen wir immer den User nach der HBCI-Version
      // Wir fragen die HBCI-Version via Messaging ab, damit sie ggf. auch
      // (z.Bsp. vom Payment-Server) automatisch beantwortet werden kann.
      QueryMessage msg = new QueryMessage(passport);
      Application.getMessagingFactory().getMessagingQueue("hibiscus.passport.rdh.hbciversion").sendSyncMessage(msg);
      Object data = msg.getData();
      if (data == null || !(data instanceof String))
        throw new ApplicationException(i18n.tr("HBCI-Version nicht ermittelbar"));
      
      String version = (String)msg.getData();
      Logger.info("using hbci version: " + version);
      
      handler = new HBCIHandler(version,passport);
      handler.close();
      handler = null;
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schlüssel erfolgreich erstellt"), StatusBarMessage.TYPE_SUCCESS));
      return key;
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      OperationCanceledException oce = (OperationCanceledException) HBCIProperties.getCause(e,OperationCanceledException.class);
      if (oce != null)
        throw oce;
        
      ApplicationException ae = (ApplicationException) HBCIProperties.getCause(e,ApplicationException.class);
      if (ae != null)
        throw ae;

      NeedKeyAckException ack = (NeedKeyAckException) HBCIProperties.getCause(e,NeedKeyAckException.class);
      if (ack != null)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schlüssel erfolgreich erstellt"), StatusBarMessage.TYPE_SUCCESS));
        String msg = i18n.tr("Bitte senden Sie den INI-Brief an Ihre Bank\nund warten Sie auf die Freischaltung durch die Bank.");
        try
        {
          Application.getCallback().notifyUser(msg);
        }
        catch (Exception e2)
        {
          Logger.error("unable to notify user",e2);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg, StatusBarMessage.TYPE_SUCCESS));
        }
        return key;
      }
      
      Logger.error("unable to create key " + file.getAbsolutePath(),e);
      throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Schlüssels: {0}",e.getMessage()));
    }
    finally
    {
      try
      {
        if (handler != null)
          handler.close();
      }
      catch (Throwable t)
      {
        Logger.error("error while closing handler",t);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#load(de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey)
   */
  public HBCIPassport load(RDHKey key) throws ApplicationException, OperationCanceledException
  {
    return load(key,false);
  }
  
  /**
   * Liefert den Passport-Typ gemaess HBCI4Java.
   * @return Passport-Typ.
   */
  String getPassportType()
  {
    return "RDHNew"; 
  }

  /**
   * Laedt einen existierenden oder erstellt einen neuen Schluessel.
   * @param key der Schluessel.
   * @param create true, wenn ein neuer erstellt werden soll.
   * @return der Schluessel.
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  private HBCIPassport load(RDHKey key, boolean create) throws ApplicationException, OperationCanceledException
  {
    HBCICallback callback = null;
    try
    {
      String filename = key.getFilename();
      
      if (create)
      {
        Logger.info("create " + getPassportType() + " key " + filename);
      }
      else
      {
        Logger.info("load " + getPassportType() + " key " + filename);

        File f = new File(filename);
        if (!f.exists())
        {
          InsertKeyDialog kd = new InsertKeyDialog(f);
          Boolean b = (Boolean) kd.open();
          if (b == null || !b.booleanValue())
            throw new OperationCanceledException(i18n.tr("Schlüsseldatei nicht eingelegt oder nicht lesbar"));
        }
      }
      
      HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
      callback = plugin.getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl());
      else
        Logger.warn("unable to register current handle, callback: " + callback);
      
      String type = getPassportType();
      HBCIUtils.setParam("client.passport.default",type); // ist eigentlich nicht noetig
      HBCIUtils.setParam("client.passport." + type + ".filename",filename);
      HBCIUtils.setParam("client.passport." + type + ".init","1");
      return AbstractHBCIPassport.getInstance(type);
    }
    catch (Exception e)
    {
      OperationCanceledException oce = (OperationCanceledException) HBCIProperties.getCause(e,OperationCanceledException.class);
      if (oce != null)
        throw oce;

      DialogFactory.clearPINCache(null);
      
      ApplicationException ae = (ApplicationException) HBCIProperties.getCause(e,ApplicationException.class);
      if (ae != null)
        throw ae;

      NeedKeyAckException ack = (NeedKeyAckException) HBCIProperties.getCause(e,NeedKeyAckException.class);
      if (ack != null)
      {
        String text = i18n.tr("Bitte senden Sie den INI-Brief an Ihre Bank und warten Sie auf die Freischaltung durch die Bank.");
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_ERROR));
        throw new ApplicationException(text);
      }
      
      InvalidPassphraseException ipe = (InvalidPassphraseException) HBCIProperties.getCause(e,InvalidPassphraseException.class);
      if (ipe != null)
      {
        Logger.write(Level.TRACE,"password for key file seems to be wrong",e);
        String text = i18n.tr("Das Passwort für die Schlüsseldatei ist falsch.");
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_ERROR));
        throw new ApplicationException(text);
      }
      
      // Keine brauchbare Exception gefunden
      Logger.error("unable to load " + getPassportType() + " key",e);
      throw new ApplicationException(i18n.tr("Fehler beim Laden des Schlüssels: {0}",e.getMessage()),e);
    }
    finally
    {
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(null);
    }
  }
}
