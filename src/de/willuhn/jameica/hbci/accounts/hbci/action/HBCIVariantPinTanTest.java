/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.action;

import java.io.File;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIAccountPinTan;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.passports.pintan.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.pintan.server.PinTanConfigImpl;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;
import de.willuhn.logging.targets.Target;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Testet den erfassten Bank-Zugang.
 */
public class HBCIVariantPinTanTest implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ {@link HBCIAccountPinTan}.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof HBCIAccountPinTan))
      throw new ApplicationException(i18n.tr("Keine Zugangsdaten angegeben"));
    
    final HBCIAccountPinTan account = (HBCIAccountPinTan) context;

    BackgroundTask task = new BackgroundTask() {
      
      private boolean stop = false;
      
      public void run(final ProgressMonitor monitor) throws ApplicationException
      {
        PinTanConfig conf     = null;
        HBCIHandler handler   = null;
        HBCICallback callback = null;
        Target target         = null;

        File f = null;
        try
        {
          monitor.setStatusText(i18n.tr("Teste Bank-Zugang..."));
          
          // Log-Ausgaben temporaer auch mit im Progressbar-Fenster
          // ausgeben
          target = new Target() {
            public void write(Message msg) throws Exception
            {
              monitor.addPercentComplete(2);
              format(monitor,msg.getText());
            }
            public void close() throws Exception
            {
            }
          };
          Logger.addTarget(target);

          AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
          callback = ((HBCI)plugin).getHBCICallback();
          if (callback != null && (callback instanceof HBCICallbackSWT))
            ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl((PinTanConfig)null) {
              @Override
              public boolean callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) throws Exception
              {
                
                if (stop)
                  throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
                
                monitor.addPercentComplete(3);

                switch (reason)
                {
                  case HBCICallback.NEED_COUNTRY:
                    retData.replace(0,retData.length(),"DE");
                    return true;
  
                  case HBCICallback.NEED_BLZ:
                    retData.replace(0,retData.length(),account.getBlz());
                    return true;
  
                  case HBCICallback.NEED_HOST:
                    retData.replace(0,retData.length(),account.getUrl());
                    return true;
  
                  case HBCICallback.NEED_PORT:
                    retData.replace(0,retData.length(),"443");
                    return true;
  
                  case HBCICallback.NEED_FILTER:
                    retData.replace(0,retData.length(),"Base64");
                    return true;
  
                  case HBCICallback.NEED_USERID:
                    retData.replace(0,retData.length(),account.getUsername());
                    return true;
  
                  case HBCICallback.NEED_CUSTOMERID:
                    retData.replace(0,retData.length(),account.getCustomer() != null ? account.getCustomer() : account.getUsername());
                    return true;
                }
                return super.callback(passport,reason,msg,datatype,retData);
              }
            });

          f = PinTanConfigFactory.createFilename();

          conf = new PinTanConfigImpl(PinTanConfigFactory.load(f),f);
          conf.setBezeichnung(account.getBlz());
          
          PinTanConfigFactory.store(conf);
          
          if (stop)
            throw new OperationCanceledException();
          
          PassportHandle handle = new PassportHandleImpl(conf);
          handler = handle.open();
          handle.close();
          
          // Test erfolgreich. Bankzugang uebernehmen
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          String text = i18n.tr("Bank-Zugang erfolgreich angelegt");
          monitor.setStatusText(text);
          monitor.setPercentComplete(100);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(text, StatusBarMessage.TYPE_SUCCESS));
        }
        catch (Throwable t)
        {
          if (conf != null)
            PinTanConfigFactory.delete(conf);
          
          if ( f != null && f.exists())
            f.delete();
          
          Throwable cause = HBCIProperties.getCause(t);
          if (cause == null) cause = t; // NPE proof - man weiss ja nie ;)
          Logger.error("account test failed: " + cause.getClass() + ": " + cause.getMessage(),t);
          
          // Wenn ein Fehler auftrat, MUSS der PIN-Cache geloescht werden. Denn falls
          // es genau deshalb fehlschlug, WEIL der User eine falsche PIN eingegeben
          // hat, kriegt er sonst keine Chance, seine Eingabe zu korrigieren
          DialogFactory.dirtyPINCache(handler != null ? handler.getPassport() : null);

          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          String errorText = i18n.tr("Fehler beim Testen des Bank-Zugangs: {0}",cause.getMessage());
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(errorText, StatusBarMessage.TYPE_ERROR));
          monitor.setStatusText(errorText);

          monitor.log(i18n.tr("Aufgetretene Fehlermeldungen:"));
          monitor.log("-----------------------------");
          Throwable current = t;
          for (int i=0;i<10;++i)
          {
            if (current == null)
              break; // Wir sind oben angekommen
            format(monitor,current.getMessage()); // Loggen
            Throwable parent = current.getCause();
            if (parent == null || parent == current)
              break; // oben angekommen
            current = parent;
          }
          monitor.log("-----------------------------");
          monitor.setPercentComplete(100);
        }
        finally
        {
          if (!Settings.getCachePin())
            DialogFactory.clearPINCache(handler != null ? handler.getPassport() : null);

          // Wir entfernen das Ding vor dem Ausgeben der Fehlermeldungen.
          // die kommen sonst alle doppelt.
          removeTarget(target);

          if (callback != null && (callback instanceof HBCICallbackSWT))
            ((HBCICallbackSWT)callback).setCurrentHandle(null);
        }
      }

      public void interrupt() {
        this.stop = true;
      }
      public boolean isInterrupted()
      {
        return this.stop;
      }
    };
    
    Application.getController().start(task);
  }
  
  /**
   * @param t
   */
  private void removeTarget(final Target t)
  {
    if (t == null)
      return;
    Thread thread = new Thread() {
      public void run()
      {
        Logger.removeTarget(t);
        Logger.info("log target removed");
      }
    };
    thread.start();
  }

  /**
   * Schneidet Stacktrace-Elemente aus dem Text raus.
   * @param monitor Monitor, an den geloggt werden soll.
   * @param msg Die Nachricht.
   */
  private void format(ProgressMonitor monitor, String msg)
  {
    if (msg == null || msg.length() == 0)
      return;
    // Wenn der Fehlertext ein Mehrzeiler ist, ignoren wir alle Stracktrace-Elemente
    String[] stack = msg.split(System.getProperty("line.separator","\n"));
    if (stack != null && stack.length > 1)
    {
      for (String ks : stack)
      {
        if (ks == null || ks.length() == 0)
          continue;
        if (ks.matches("\\tat.*")) // Stacktrace-Elemente
          continue;
        if (ks.matches("\\t\\.\\.\\..*")) // Stacktrace-Elemente
          continue;
        if (ks.matches("HBCI4Java stacktrace END ---"))
          continue;
        if (ks.matches("HBCI4Java Exception END ---"))
          continue;
        ks = ks.replaceAll("HBCI4Java Exception BEGIN ---","");
        ks = ks.replaceAll("HBCI4Java stacktrace BEGIN ---org.kapott.hbci.exceptions.HBCI_Exception: ","");
        ks = ks.replaceAll("Caused by: ","");
        ks = ks.replaceAll(".*?Exception:","");
        if (ks.length() == 0)
          continue;
        monitor.log("  " + ks);
      }
    }
    else
    {
      monitor.log("  " + msg);
    }
  }


}


