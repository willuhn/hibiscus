/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungExecute.java,v $
 * $Revision: 1.4 $
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
import de.willuhn.jameica.hbci.gui.dialogs.UeberweisungDialog;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action, die zur Ausfuehrung einer Ueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
 */
public class UeberweisungExecute implements Action
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public UeberweisungExecute()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
			throw new ApplicationException(i18n.tr("Keine Überweisung angegeben"));

		try
		{
			final Ueberweisung u = (Ueberweisung) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			UeberweisungDialog d = new UeberweisungDialog(u,UeberweisungDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Überweisung"));
				return;
			}

			GUI.startSync(new Runnable()
      {
        public void run()
        {
        	try
        	{
						GUI.getStatusBar().startProgress();
						GUI.getStatusBar().setStatusText(i18n.tr("Führe Überweisung aus..."));
						u.execute();
						GUI.getStatusBar().setSuccessText(i18n.tr("Überweisung erfolgreich ausgeführt"));
        	}
					catch (OperationCanceledException oce)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Ausführung der Überweisung abgebrochen"));
					}
					catch (ApplicationException ae)
					{
						GUI.getStatusBar().setErrorText(ae.getMessage());
					}
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
 * $Log: UeberweisungExecute.java,v $
 * Revision 1.4  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.3  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/