/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.NachrichtOpen;
import de.willuhn.jameica.hbci.gui.parts.NachrichtList;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * BUGZILLA 331
 * Zeigt neue System-nachrichten der Bank an.
 */
public class NachrichtBox extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private NachrichtList list = null;
  private MessageConsumer mc = new MyMessageConsumer();

  @Override
  public boolean getDefaultEnabled()
  {
    return true;
  }

  @Override
  public int getDefaultIndex()
  {
    return 0;
  }

  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("System-Nachrichten der Bank");
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    DBIterator iterator = Settings.getDBService().createList(Nachricht.class);
    iterator.setOrder("order by datum desc"); // Neueste zuerst
    iterator.addFilter("gelesen is null or gelesen = 0");
    
    this.list = new NachrichtList(iterator,new NachrichtOpen());
    this.list.setSummary(false);
    this.list.paint(parent);
    
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    parent.addDisposeListener(new DisposeListener() {
      
      @Override
      public void widgetDisposed(DisposeEvent e)
      {
        list = null;
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    
  }

  @Override
  public boolean isActive()
  {
    return super.isActive() && isEnabled(); // Nicht konfigurierbar
  }

  @Override
  public boolean isEnabled()
  {
    try
    {
      DBIterator iterator = Settings.getDBService().createList(Nachricht.class);
      iterator.addFilter("gelesen is null or gelesen = 0");
      return iterator.hasNext(); // Wenn Nachrichten vorliegen, wird die Box automatisch aktiviert
    }
    catch (Exception e)
    {
      Logger.error("unable to check for new messages",e);
    }
    return super.isEnabled();
  }
  
  /**
   * Wird benachrichtigt, wenn neue Nachrichten der Bank eintreffen.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    @Override
    public boolean autoRegister()
    {
      return false;
    }
    
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ImportMessage.class};
    }
    
    @Override
    public void handleMessage(Message message) throws Exception
    {
      ImportMessage im = (ImportMessage) message;
      GenericObject o = im.getObject();
      if (!(o instanceof Nachricht))
        return;
      
      final Nachricht n = (Nachricht) o;
      
      GUI.getDisplay().asyncExec(new Runnable() {
        
        @Override
        public void run()
        {
          if (list == null)
            return;
          
          try
          {
            list.addItem(n);
          }
          catch (Exception e)
          {
            Logger.error("unable to add message",e);
          }
        }
      });
    }
  }
  
  @Override
  public int getHeight()
  {
    return 120;
  }
}
