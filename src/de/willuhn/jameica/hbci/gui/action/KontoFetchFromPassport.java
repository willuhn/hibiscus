/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoFetchFromPassport.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/10/25 22:39:14 $
 * $Author: willuhn $
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
import de.willuhn.jameica.hbci.gui.views.KontoListe;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

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
								found = true;
								Logger.info("  konto exists, skipping");
								break;
							}
					
						}
						existing.begin();
						if (!found)
						{
							// Konto neu anlegen
							Logger.info("saving new konto");
							try {
								konten[i].setPassport(p); // wir speichern den ausgewaehlten Passport.
								konten[i].store();
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
					GUI.startView(KontoListe.class.getName(),null);
					GUI.getStatusBar().setSuccessText(i18n.tr("Konten erfolgreich ausgelesen"));
				}
				catch (Throwable t)
				{
					Logger.error("error while reading data from passport",t);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Konto-Daten. Bitte prüfen Sie die Einstellungen des Mediums."));
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