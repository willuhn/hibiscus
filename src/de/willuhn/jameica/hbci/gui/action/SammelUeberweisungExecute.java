/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungExecute.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/07/04 09:16:24 $
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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.views.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCISammelUeberweisungJob;
import de.willuhn.util.ApplicationException;

/**
 * Action, die zur Ausfuehrung einer Sammel-Ueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelUeberweisung</code> als Context.
 */
public class SammelUeberweisungExecute extends AbstractSammelTransferExecute
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSammelTransferExecute#execute(de.willuhn.jameica.hbci.rmi.SammelTransfer)
   */
  void execute(final SammelTransfer transfer) throws ApplicationException, RemoteException
  {
    // Wir merken uns die aktuelle Seite und aktualisieren sie nur,
    // wenn sie sich nicht geaendert hat.
    final AbstractView oldView = GUI.getCurrentView();

    HBCIFactory factory = HBCIFactory.getInstance();
    factory.addJob(new HBCISammelUeberweisungJob((SammelUeberweisung) transfer));
    factory.executeJobs(transfer.getKonto(), new Listener() {
      public void handleEvent(Event event)
      {
        final AbstractView newView = GUI.getCurrentView();
        if (oldView == newView && transfer == newView.getCurrentObject())
          GUI.startView(SammelUeberweisungNew.class,transfer);
      }
    });
  }

}


/**********************************************************************
 * $Log: SammelUeberweisungExecute.java,v $
 * Revision 1.2  2007/07/04 09:16:24  willuhn
 * @B Aktuelle View nach Ausfuehrung eines HBCI-Jobs nur noch dann aktualisieren, wenn sie sich zwischenzeitlich nicht geaendert hat
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.7  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.6  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.5  2005/03/30 23:28:13  web0
 * @B bug 31
 *
 * Revision 1.4  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.3  2005/03/05 19:19:48  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/