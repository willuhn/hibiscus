/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/PassportTest.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/06/21 20:11:10 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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

		GUI.getStatusBar().startProgress();
		GUI.getStatusBar().setSuccessText(i18n.tr("Teste Sicherheits-Medium..."));

		try
		{
			GUI.startSync(new Runnable() {
				public void run() {
					try {
						PassportHandle handle = null;
            if (context instanceof Passport)
              handle = ((Passport)context).getHandle();
            else
              handle = (PassportHandle) context;
						handle.open();
						handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
 													  // fehlschlaegt, ist nichts zum Schliessen da ;)
						GUI.getStatusBar().setSuccessText(i18n.tr("Sicherheits-Medium erfolgreich getestet."));
					}
          catch (ApplicationException ae)
          {
            GUI.getStatusBar().setErrorText(ae.getMessage());
          }
					catch (RemoteException e)
					{
            String msg = e.getMessage();
            if (msg != null && msg.length() > 0)
  						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Testen des Sicherheits-Mediums: {0}",msg));
            else
              GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Testen des Sicherheits-Mediums."));
						Logger.warn("error while testing passport: " + e.getMessage());

            // BUGZILLA 52 http://www.willuhn.de/bugzilla/show_bug.cgi?id=52
            if (Logger.getLevel().equals(Level.DEBUG))
              Logger.error("stacktrace for debugging purpose",e);
					}
				}
			});
		}
		finally
		{
			GUI.getStatusBar().stopProgress();
		}
  }

}


/**********************************************************************
 * $Log: PassportTest.java,v $
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