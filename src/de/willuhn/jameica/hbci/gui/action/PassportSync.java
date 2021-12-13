/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;
import de.willuhn.logging.targets.Target;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Synchronisieren des Bankzugangs.
 */
public class PassportSync implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code> oder
   * <code>de.willuhn.jameica.hbci.passport.PassportHandle</code> oder
   */
  @Override
  public void handleAction(final Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || (!(context instanceof Passport) && !(context instanceof PassportHandle)))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen Bank-Zugang aus."));

    Logger.info("performing passport re-sync");

		BackgroundTask task = new BackgroundTask() {
      public void run(final ProgressMonitor monitor) throws ApplicationException
      {
        HBCIHandler handler = null;
        Target target       = null;
        try {
          monitor.setStatusText(i18n.tr("Synchronisiere Bank-Zugang..."));

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

          PassportHandle handle = null;
          if (context instanceof Passport)
            handle = ((Passport)context).getHandle();
          else
            handle = (PassportHandle) context;
          
          handler = handle.open();
          monitor.log("L�sche BPD");
          new PassportDeleteBPD().handleAction(handler.getPassport());
          monitor.log("Synchronisiere Bankzugang");
          handler.sync(true);
          handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
                          // fehlschlaegt, ist nichts zum Schliessen da ;)

          Logger.flush();
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);
          monitor.setStatusText(i18n.tr("Bank-Zugang erfolgreich synchronisiert."));
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bank-Zugang erfolgreich synchronisiert."), StatusBarMessage.TYPE_SUCCESS));
          removeTarget(target);
          
          if (!Application.getCallback().askUser(i18n.tr("Bank-Zugang synchronisiert. Konten automatisch anlegen?")))
            return;

          try
          {
            new KontoMerge().handleAction(handle.getKonten());
            // Wir starten die aktuelle View neu, damit die Liste der Konten
            // gleich aktualisiert wird
            GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
          }
          catch (Exception e)
          {
            // Das darf fehlschlagen. Zum Beispiel, wenn die Bank sowas nicht unterstuetzt
            Logger.error("unable to fetch accounts",e);
            monitor.log(i18n.tr("Automatisches Anlegen der Konten fehlgeschlagen. Bitte legen Sie sie manuell an"));
          }
        }
        catch (ApplicationException ae)
        {
          // Wenn ein Fehler auftrat, MUSS der PIN-Cache geloescht werden. Denn falls
          // es genau deshalb fehlschlug, WEIL der User eine falsche PIN eingegeben
          // hat, kriegt er sonst keine Chance, seine Eingabe zu korrigieren
          DialogFactory.dirtyPINCache(handler != null ? handler.getPassport() : null);
          
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setPercentComplete(100);
          monitor.setStatusText(ae.getMessage());
          removeTarget(target);
        }
        catch (Exception e)
        {
          Throwable cause = HBCIProperties.getCause(e);
          if (cause == null) cause = e; // NPE proof - man weiss ja nie ;)
          Logger.info("test of passport failed: " + cause.getClass() + ": " + cause.getMessage());
          
          // Den kompletten Stacktrace loggen wir nur auf DEBUG, weil der beim Testen bzw. Suchen nach
          // einem Kartenleser durchaus auftreten kann.
          Logger.write(Level.DEBUG,"error while testing passport",e);
          
          // Wenn ein Fehler auftrat, MUSS der PIN-Cache geloescht werden. Denn falls
          // es genau deshalb fehlschlug, WEIL der User eine falsche PIN eingegeben
          // hat, kriegt er sonst keine Chance, seine Eingabe zu korrigieren
          DialogFactory.dirtyPINCache(handler != null ? handler.getPassport() : null);

          // Wir entfernen das Ding vor dem Ausgeben der Fehlermeldungen.
          // die kommen sonst alle doppelt.
          removeTarget(target);

          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          String errorText = i18n.tr("Fehler beim Testen des Sicherheits-Mediums: {0}",cause.getMessage());
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(errorText, StatusBarMessage.TYPE_ERROR));
          monitor.setStatusText(errorText);

          monitor.log(i18n.tr("Aufgetretene Fehlermeldungen:"));
          monitor.log("-----------------------------");
          Throwable current = e;
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
        }
      }

      public void interrupt() {}
      public boolean isInterrupted()
      {
        return false;
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
      for (int k=0;k<stack.length;++k)
      {
        String ks = stack[k];
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
