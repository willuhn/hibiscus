/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SynchronizeList.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/03/17 00:51:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.server.SynchronizeEngine;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Vorgefertigte Liste mit den offenen Synchronisierungs-TODOs fuer ein Konto.
 */
public class SynchronizeList extends TablePart
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public SynchronizeList() throws RemoteException
  {
    super(SynchronizeEngine.getInstance().getSynchronizeJobs(),new MyAction());
    addColumn(i18n.tr("Aufgabe"),"name");
}
  
  /**
   * Hilfsklasse zum Reagieren auf Doppelklicks in der Liste.
   * Dort stehen naemlich ganz verschiedene Datensaetze drin.
   * Daher muss der Datensatz selbst entscheiden, was beim
   * Klick auf ihn gesehen soll.
   */
  private static class MyAction implements Action
  {

    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof SynchronizeJob))
        return;
      try
      {
        ((SynchronizeJob)context).configure();
      }
      catch (RemoteException e)
      {
        Logger.error("unable to configure synchronize job",e);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Öffnen des Synchronisierungs-Auftrags"));
      }
    }
    
  }

}


/*********************************************************************
 * $Log: SynchronizeList.java,v $
 * Revision 1.1  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/