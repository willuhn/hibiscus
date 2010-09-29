/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoFetchFromPassport.java,v $
 * $Revision: 1.14 $
 * $Date: 2010/09/29 23:43:34 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.views.KontoList;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, welche die Konten aus einem Passport ermittelt und abspeichert.
 */
public class KontoFetchFromPassport implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof Passport))
			throw new ApplicationException(i18n.tr("Kein Sicherheitsmedium ausgewählt oder keines verfügbar"));

		final Passport p = (Passport) context;

		GUI.startSync(new Runnable()
		{
			public void run() {
				try {

					GUI.getStatusBar().startProgress();
					GUI.getStatusBar().setSuccessText(i18n.tr("Medium wird ausgelesen..."));
					new KontoMerge().handleAction(p.getHandle().getKonten());

					// Konto-Liste neu laden
					GUI.startView(KontoList.class,null);
				}
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
				catch (Throwable t)
				{
					Logger.error("error while reading data from passport",t);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Konto-Daten. Bitte prüfen Sie die Einstellungen des Sicherheits-Mediums."));
				}
				finally
				{
					GUI.getStatusBar().stopProgress();
				}
			}
		});
  }
}


/**********************************************************************
 * $Log: KontoFetchFromPassport.java,v $
 * Revision 1.14  2010/09/29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 * Revision 1.13  2007/04/15 22:23:16  willuhn
 * @B Bug 338
 *
 * Revision 1.12  2006/03/15 16:25:48  willuhn
 * @N Statusbar refactoring
 *
 * Revision 1.11  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 * Revision 1.9  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.8  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.7  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/21 13:59:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/