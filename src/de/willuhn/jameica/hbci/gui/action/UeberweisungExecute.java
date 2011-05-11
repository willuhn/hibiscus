/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungExecute.java,v $
 * $Revision: 1.16 $
 * $Date: 2011/05/11 10:05:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.UeberweisungDialog;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCIUeberweisungJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Ueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
 */
public class UeberweisungExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Ueberweisung))
			throw new ApplicationException(i18n.tr("Keine Überweisung angegeben"));

		try
		{
			final Ueberweisung u = (Ueberweisung) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			// BUGZILLA 559
			Date termin = DateUtil.startOfDay(u.getTermin());
			Date now    = DateUtil.startOfDay(new Date());
			if (!u.isTerminUeberweisung() && (termin.getTime() - now.getTime()) >= (24 * 60 * 60 * 1000))
			{
			  String q = i18n.tr("Der Termin liegt mindestens 1 Tag in Zukunft.\n" +
			  		               "Soll der Auftrag stattdessen als bankseitige Termin-Überweisung " +
			  		               "ausgeführt werden?");
			  if (Application.getCallback().askUser(q))
			  {
			    u.setTerminUeberweisung(true);
			    u.store();
			  }
			}
			
			UeberweisungDialog d = new UeberweisungDialog(u,UeberweisungDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
      catch (OperationCanceledException oce)
      {
        Logger.info(oce.getMessage());
        return;
      }
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Überweisung"));
				return;
			}

      // Wir merken uns die aktuelle Seite und aktualisieren sie nur,
      // wenn sie sich nicht geaendert hat.
      final AbstractView oldView = GUI.getCurrentView();

      HBCIFactory factory = HBCIFactory.getInstance();
      factory.addJob(new HBCIUeberweisungJob(u));
      factory.executeJobs(u.getKonto(), new Listener() {
        public void handleEvent(Event event)
        {
          final AbstractView newView = GUI.getCurrentView();
          if (oldView == newView && u == newView.getCurrentObject())
            GUI.startView(UeberweisungNew.class,u);
        }
      }); 

		}
		catch (ApplicationException ae)
		{
		  throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while executing ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Überweisung: {0}",e.getMessage()));
		}
  }

}


/**********************************************************************
 * $Log: UeberweisungExecute.java,v $
 * Revision 1.16  2011/05/11 10:05:32  willuhn
 * @N OCE fangen
 *
 * Revision 1.15  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.14  2010-10-03 22:07:47  willuhn
 * @C Dialog sollte natuerlich nur dann angezeigt werden, wenn es noch KEINE Termin-Ueberweisung ist
 *
 * Revision 1.13  2010-10-03 21:50:34  willuhn
 * @N BUGZILLA 559 - wenn der Termin der Ueberweisung mind. 1 Tag in der Zukunft liegt, dann vorschlagen, ihn als bankseitige Termin-Ueberweisung auszufuehren
 *
 * Revision 1.12  2007/07/04 09:16:23  willuhn
 * @B Aktuelle View nach Ausfuehrung eines HBCI-Jobs nur noch dann aktualisieren, wenn sie sich zwischenzeitlich nicht geaendert hat
 *
 * Revision 1.11  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.10  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.9  2005/03/30 23:28:13  web0
 * @B bug 31
 *
 * Revision 1.8  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.7  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
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