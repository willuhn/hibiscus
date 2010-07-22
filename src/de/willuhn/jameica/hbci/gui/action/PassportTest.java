/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/PassportTest.java,v $
 * $Revision: 1.10 $
 * $Date: 2010/07/22 22:36:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;
import de.willuhn.logging.targets.Target;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action, die die Funktionsfaehigkeit eines Passports via oeffnen und schliessen testet.
 */
public class PassportTest implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code> oder
   * <code>de.willuhn.jameica.hbci.passport.PassportHandle</code> oder
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(final Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || (!(context instanceof Passport) && !(context instanceof PassportHandle)))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Sicherheits-Medium aus."));

		BackgroundTask task = new BackgroundTask() {
      public void run(final ProgressMonitor monitor) throws ApplicationException
      {
        Target target = null;
        try {
          monitor.setStatusText(i18n.tr("Teste Sicherheits-Medium..."));

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
          handle.open();
          handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
                          // fehlschlaegt, ist nichts zum Schliessen da ;)

          Logger.flush();
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);
          monitor.setStatusText(i18n.tr("Sicherheits-Medium erfolgreich getestet."));
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sicherheits-Medium erfolgreich getestet."), StatusBarMessage.TYPE_SUCCESS));
          removeTarget(target);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setPercentComplete(100);
          monitor.setStatusText(ae.getMessage());
          removeTarget(target);
        }
        catch (Exception e)
        {
          // Wir entfernen das Ding vor dem Ausgeben der Fehlermeldungen.
          // die kommen sonst alle doppelt.
          removeTarget(target);

          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          String errorText = i18n.tr("Fehler beim Testen des Sicherheits-Mediums.");
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


/**********************************************************************
 * $Log: PassportTest.java,v $
 * Revision 1.10  2010/07/22 22:36:24  willuhn
 * @N Code-Cleanup
 *
 * Revision 1.9  2008/05/05 10:22:18  willuhn
 * @B MACOS - Log-Target am Ende in einem separaten Thread entfernen. Ich bin mir nicht sicher, ob das was bringt. Muss das mal noch auf'm Mac testen
 *
 * Revision 1.8  2007/12/05 10:58:43  willuhn
 * @N Lesbarere und ausfuehrlichere Fehlermeldungen beim Testen des Sicherheitsmediums
 *
 * Revision 1.7  2006/01/23 00:36:29  willuhn
 * @N Import, Export und Chipkartentest laufen jetzt als Background-Task
 *
 * Revision 1.6  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.5  2005/04/27 00:31:36  web0
 * @N real test connection
 * @N all hbci versions are now shown in select box
 * @C userid and customerid are changable
 *
 * Revision 1.4  2005/04/12 23:19:29  web0
 * @B Bug 52
 *
 * Revision 1.3  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/