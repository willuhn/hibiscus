/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/keyformat/HBCI4JavaFormat.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/07/06 08:00:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
import de.willuhn.jameica.hbci.passports.rdh.InsertKeyDialog;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.rdh.server.RDHKeyImpl;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Implementierung des Schluesselformats von HBCI4Java.
 */
public class HBCI4JavaFormat implements KeyFormat
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#getName()
   */
  public String getName()
  {
    return i18n.tr("HBCI4Java/Hibiscus-Format");
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
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      OperationCanceledException oce = (OperationCanceledException) HBCIFactory.getCause(e,OperationCanceledException.class);
      if (oce != null)
        throw oce;
        
      ApplicationException ae = (ApplicationException) HBCIFactory.getCause(e,ApplicationException.class);
      if (ae != null)
        throw ae;

      NeedKeyAckException ack = (NeedKeyAckException) HBCIFactory.getCause(e,NeedKeyAckException.class);
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
            throw new OperationCanceledException(i18n.tr("Schlüsseldiskette nicht eingelegt oder nicht lesbar"));
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
      OperationCanceledException oce = (OperationCanceledException) HBCIFactory.getCause(e,OperationCanceledException.class);
      if (oce != null)
        throw oce;

      ApplicationException ae = (ApplicationException) HBCIFactory.getCause(e,ApplicationException.class);
      if (ae != null)
        throw ae;

      NeedKeyAckException ack = (NeedKeyAckException) HBCIFactory.getCause(e,NeedKeyAckException.class);
      if (ack != null)
      {
        String text = i18n.tr("Bitte senden Sie den INI-Brief an Ihre Bank und warten Sie auf die Freischaltung durch die Bank.");
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_ERROR));
        throw new ApplicationException(text);
      }
      
      InvalidPassphraseException ipe = (InvalidPassphraseException) HBCIFactory.getCause(e,InvalidPassphraseException.class);
      if (ipe != null)
      {
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


/**********************************************************************
 * $Log: HBCI4JavaFormat.java,v $
 * Revision 1.4  2011/07/06 08:00:18  willuhn
 * @N Debug-Output
 *
 * Revision 1.3  2011-05-24 09:23:26  willuhn
 * @C Exception-Handling
 *
 * Revision 1.2  2010/06/17 17:20:58  willuhn
 * @N Exception-Handling beim Laden der Schluesseldatei ueberarbeitet - OperationCancelledException wird nun sauber behandelt - auch wenn sie in HBCI_Exceptions gekapselt ist
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.10  2010/06/14 22:47:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2009/11/02 23:05:55  willuhn
 * @B RDHX-Schluessel wurden als RDHNew angelegt - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=62310#62310
 *
 * Revision 1.8  2009/04/14 08:37:52  willuhn
 * @N Neuer Schluessel im RDHNew-Format liess sich nicht mehr erstellen, da nach der Datei gefragt wurde
 *
 * Revision 1.7  2009/03/29 22:25:56  willuhn
 * @B Warte-Dialog wurde nicht angezeigt, wenn Schluesseldiskette nicht eingelegt
 *
 * Revision 1.6  2008/08/29 17:06:25  willuhn
 * @N InvalidPassphraseException beruecksichtigen
 *
 * Revision 1.5  2008/07/28 09:31:13  willuhn
 * @N Abfrage der HBCI-Version via Messaging
 *
 * Revision 1.4  2008/07/25 12:56:50  willuhn
 * @B Bugfixing
 *
 * Revision 1.3  2008/07/25 11:34:56  willuhn
 * @B Bugfixing
 *
 * Revision 1.2  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.1  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 **********************************************************************/
