/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SynchronizeList.java,v $
 * $Revision: 1.7.4.1 $
 * $Date: 2010/12/27 23:21:40 $
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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.HBCIFactoryMessage;
import de.willuhn.jameica.hbci.messaging.HBCIFactoryMessage.Status;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
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
  private MessageConsumer mc = new MyMessageConsumer();

  /**
   * ct.
   * @throws RemoteException
   */
  public SynchronizeList() throws RemoteException
  {
    super(SynchronizeEngine.getInstance().getSynchronizeJobs(),new MyAction());
    addColumn(i18n.tr("Offene Synchronisierungsaufgaben"),"name");
    
    this.setSummary(false);
    this.setCheckable(true);

    // BUGZILLA 583
    this.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        try
        {
          if (item == null)
            return;
          SynchronizeJob job = (SynchronizeJob) item.getData();
          if (job == null)
            return;
          item.setFont(job.isRecurring() ? Font.DEFAULT.getSWTFont() : Font.BOLD.getSWTFont());
        }
        catch (Exception e)
        {
          Logger.error("unable to format text",e);
        }
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    super.paint(parent);
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
  
  /**
   * Hilfsklasse zum Abfragen der Status-Codes von der HBCI-Factory.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{HBCIFactoryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          try
          {
            HBCIFactoryMessage m = (HBCIFactoryMessage) message;
            Status status = m.getStatus();
            if (status == null || status != Status.STOPPED)
              return;
            
            // Liste der Jobs aktualisieren
            removeAll();
            
            GenericIterator i = SynchronizeEngine.getInstance().getSynchronizeJobs();
            while (i.hasNext())
              addItem(i.next());
          }
          catch (Exception e)
          {
            Logger.error("unable to reload sync jobs",e);
          }
        }
      });
    }
  }

}


/*********************************************************************
 * $Log: SynchronizeList.java,v $
 * Revision 1.7.4.1  2010/12/27 23:21:40  willuhn
 * @N Backports 0034, 0035
 * @N 1.12.3
 *
 * Revision 1.8  2010-12-27 22:47:52  willuhn
 * @N BUGZILLA 964
 *
 * Revision 1.7  2008/04/13 04:20:41  willuhn
 * @N Bug 583
 **********************************************************************/