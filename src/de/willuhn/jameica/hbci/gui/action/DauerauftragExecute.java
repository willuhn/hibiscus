/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DauerauftragExecute.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/25 17:58:56 $
 * $Author: willuhn $
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
import de.willuhn.jameica.hbci.gui.dialogs.DauerauftragDialog;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action zur Ausfuehrung eines neu angelegten Dauerauftrag.
 */
public class DauerauftragExecute implements Action
{

  /**
   * Erwartet einen Dauerauftrag als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {

		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Keine Überweisung angegeben"));

		try
		{
			final Dauerauftrag d = (Dauerauftrag) context;
			
			if (d.isNewObject())
				d.store(); // wir speichern bei Bedarf selbst.

			DauerauftragDialog dd = new DauerauftragDialog(d,DauerauftragDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)dd.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Dauerauftrages"));
				return;
			}

			GUI.startSync(new Runnable()
			{
				public void run()
				{
					try
					{
						GUI.getStatusBar().startProgress();
						if (d.isActive())
							GUI.getStatusBar().setStatusText(i18n.tr("Aktualisiere Dauerauftrag..."));
						else
							GUI.getStatusBar().setStatusText(i18n.tr("Führe Dauerauftrag aus..."));
						
						// d.execute(); // TODO HIER GEHTS WEITER
						GUI.getStatusBar().setSuccessText(i18n.tr("Dauerauftrag erfolgreich ausgeführt"));
					}
					catch (OperationCanceledException oce)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Ausführung des Dauerauftrages abgebrochen"));
					}
//					catch (ApplicationException ae)
//					{
//						GUI.getStatusBar().setErrorText(ae.getMessage());
//					}
					catch (RemoteException e)
					{
						Logger.error("error while executing ueberweisung",e);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Überweisung"));
					}
				}
			});

		}
		catch (RemoteException e)
		{
			Logger.error("error while executing ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Überweisung"));
		}
		finally
		{
			GUI.getStatusBar().stopProgress();
			GUI.getStatusBar().setStatusText("");
		}
  }

}


/**********************************************************************
 * $Log: DauerauftragExecute.java,v $
 * Revision 1.2  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.1  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 **********************************************************************/