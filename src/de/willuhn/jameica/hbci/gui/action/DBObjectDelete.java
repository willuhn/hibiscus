/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBObjectNode;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Generische Action fuer das Loeschen von Datensaetzen.
 */
public class DBObjectDelete implements Action
{
  private I18N i18n = null;
  
  /**
   * ct.
   */
  public DBObjectDelete()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Erwartet ein Objekt vom Typ <code>DBObject</code> oder <code>DBObject[]</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
      throw new ApplicationException(i18n.tr("Keine zu löschenden Daten ausgewählt"));

    if (!(context instanceof DBObject) && !(context instanceof DBObject[]))
    {
      Logger.warn("wrong type to delete: " + context.getClass());
      return;
    }

    boolean array = (context instanceof DBObject[]);
    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Daten löschen"));
      d.setText(i18n.tr("Wollen Sie diese {0} Datensätze wirklich löschen?",""+((DBObject[])context).length));
    }
    else
    {
      d.setTitle(i18n.tr("Daten löschen"));
      d.setText(i18n.tr("Wollen Sie diesen Datensatz wirklich löschen?"));
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting objects",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Datensatzes"));
      return;
    }

    DBObject[] list = null;
    if (array)
      list = (DBObject[]) context;
    else
      list = new DBObject[]{(DBObject)context}; // Array mit einem Element
    
    Worker worker = new Worker(list);

// Das machen wir nicht mehr. Durch die dauernden Wechsel im Event Dispatcher wird das schweinelangsam
//    if (list.length > 100)
//      Application.getController().start(worker);
//    else
    worker.run(null);
  }
  
  /**
   * Damit koennen wir lange Loeschvorgaenge ggf. im Hintergrund laufen lassen
   */
  private class Worker implements BackgroundTask
  {
    private boolean cancel = false;
    private DBObject[] list = null;

    /**
     * ct.
     * @param list
     */
    private Worker(DBObject[] list)
    {
      this.list = list;
    }
    
    /**
     * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
     */
    public void interrupt()
    {
      this.cancel = true;
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
     */
    public boolean isInterrupted()
    {
      return this.cancel;
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
     */
    public void run(ProgressMonitor monitor) throws ApplicationException
    {
      try
      {
        if (monitor != null)
          monitor.setStatusText(i18n.tr("Lösche {0} Datensätze",""+list.length));

        double factor = 100d / list.length;
        
        // Wenn es eine Liste von Node-Objekten ist, sortieren wir sie nach
        // Einrückungstiefe - unten beginnend. Das stellt sicher, dass wir
        // zuerst die Kinder löschen, bevor die Eltern gelöscht werden.
        if (list instanceof DBObjectNode[])
        {
          Arrays.sort((DBObjectNode[])list,(o1,o2) -> {
            try {
              return Integer.compare(o2.getPath().size(),o1.getPath().size());
            }
            catch (RemoteException re) {}
            return 0;
          });
        }
        
        for (int i=0;i<list.length;++i)
        {
          if (monitor != null && i % 4 == 0)
            monitor.setPercentComplete((int)((i+4) * factor));

          if (list[i].isNewObject())
            continue; // muss nicht geloescht werden

          // ok, wir loeschen das Objekt
          final String id = list[i].getID();
          if (id == null)
            continue;
          
          list[i].delete();
          Application.getMessagingFactory().sendMessage(new ObjectDeletedMessage(list[i],id));
        }
        
        if (monitor != null)
          monitor.setPercentComplete(100);
        
        String text = i18n.tr("Datensatz gelöscht.");
        if (list.length > 1)
          text = i18n.tr("{0} Datensätze gelöscht.",""+list.length);
        
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
        if (monitor != null)
        {
          monitor.setStatusText(text);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
        }

      }
      catch (RemoteException e)
      {
        Logger.error("error while deleting objects",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der Datensätze."), StatusBarMessage.TYPE_ERROR));

        if (monitor != null)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(i18n.tr("Fehler beim Löschen der Daten"));
          monitor.log(e.toString());
        }
      }
      catch (ApplicationException ae)
      {
        if (monitor != null)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(ae.getMessage());
        }
        throw ae;
      }
    }
  }
}
