/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoFetchFromPassport.java,v $
 * $Revision: 1.11 $
 * $Date: 2005/08/01 23:27:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.KontoList;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
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

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {

		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Passport))
			throw new ApplicationException(i18n.tr("Kein Sicherheitsmedium ausgewählt oder keines verfügbar"));

		final Passport p = (Passport) context;

		GUI.startSync(new Runnable()
		{
			public void run() {
				try {

					GUI.getStatusBar().startProgress();
					GUI.getStatusBar().setStatusText(i18n.tr("Medium wird ausgelesen..."));

					DBIterator existing = Settings.getDBService().createList(Konto.class);
					Konto check = null;
					Konto[] konten = p.getHandle().getKonten();

					int created = 0;
          int skipped = 0;
          for (int i=0;i<konten.length;++i)
					{
						Logger.info("found konto " + konten[i].getKontonummer());
						// Wir checken, ob's das Konto schon gibt
						boolean found = false;
						Logger.info("  checking if allready exists");
						while (existing.hasNext())
						{
							check = (Konto) existing.next();
							if (check.getBLZ().equals(konten[i].getBLZ()) &&
								check.getKontonummer().equals(konten[i].getKontonummer()))
							{
                // Jetzt noch checken, ob die Passports uebereinstimmen
                String pp = check.getPassportClass();
                Logger.info("  checking for same passport");
                if (pp == null || pp.equals(p.getClass().getName()))
                {
                  found = true;
                  Logger.info("  konto exists, skipping");
                  skipped++;
                  break;
                }
							}
					
						}
						existing.begin();
						if (!found)
						{
							// Konto neu anlegen
							Logger.info("saving new konto");
							try {
								konten[i].setPassportClass(p.getClass().getName()); // wir speichern den ausgewaehlten Passport.
								konten[i].store();
                created++;
								Logger.info("konto saved successfully");
							}
							catch (Exception e)
							{
								// Wenn ein Konto fehlschlaegt, soll nicht gleich der ganze Vorgang abbrechen
								Logger.error("error while storing konto",e);
								GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Anlegen des Kontos") + " " + konten[i].getKontonummer());
							}
						}
				
					}
					GUI.startView(KontoList.class,null);
          String[] values = new String[] {""+created,""+skipped};
					GUI.getStatusBar().setSuccessText(i18n.tr("Konten erfolgreich ausgelesen. Angelegt: {0}, Übersprungen: {1}",values));
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
					GUI.getStatusBar().setStatusText("");
				}
			}
		});
  }
}


/**********************************************************************
 * $Log: KontoFetchFromPassport.java,v $
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