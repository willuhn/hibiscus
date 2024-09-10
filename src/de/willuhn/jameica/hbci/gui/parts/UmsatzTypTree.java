/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines fix und fertig vorkonfigurierten Trees mit den existiernden Umsatz-Typen.
 */
public class UmsatzTypTree extends TreePart
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private DelayedListener delayed = new DelayedListener(new UpdateListener());
  
  /**
   * Initialisiert die Liste der Root-Elemente.
   * @return Liste der Root-Elemente.
   * @throws RemoteException
   */
  private final static GenericIterator init() throws RemoteException
  {
    return UmsatzTypUtil.getRootElements();
  }
  
  /**
   * ct.
   * @param action
   * @throws RemoteException
   */
  public UmsatzTypTree(Action action) throws RemoteException
  {
    super(init(), action);
    addColumn(i18n.tr("Bezeichnung"),"name");
    addColumn(i18n.tr("Reihenfolge"),"nummer"); // BUGZILLA 554/988
    addColumn(i18n.tr("Suchbegriff"),"pattern"); // BUGZILLA 756
    addColumn(i18n.tr("Umsatzart"),"umsatztyp",new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return i18n.tr("egal");
        return UmsatzTypUtil.getNameForType(((Integer) o).intValue());
      }
    });
    addColumn(i18n.tr("Konto"),"dummy");
    addColumn(i18n.tr("Kommentar"),"kommentar");
    
    this.setFormatter(new TreeFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TreeFormatter#format(org.eclipse.swt.widgets.TreeItem)
       */
      public void format(TreeItem item)
      {
        if (item == null)
          return;

        try
        {
          UmsatzTyp ut = (UmsatzTyp) item.getData();
          if (ut == null)
            return;
          
          final String kat = ut.getKontoKategorie();
          final Konto k = ut.getKonto();
          if (k != null)
            item.setText(4,KontoUtil.toString(k));
          else if (kat != null)
            item.setText(4,kat);

          ColorUtil.setForeground(item,-1,ut);
        }
        catch (Exception e)
        {
          Logger.error("unable to apply custom color",e);
        }
      }
    });

    this.setMulti(true);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(true);
    this.setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzTypList());
  }
  
  
  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    final MessageConsumer mc = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(mc);
    parent.addDisposeListener(new DisposeListener()
    {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
  }

  /**
   * Hilfsklasse, um ueber das Loeschen von Kategorien benachrichtigt zu werden.
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
      return new Class[]{ObjectDeletedMessage.class,ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      ObjectMessage msg = (ObjectMessage) message;
      GenericObject o = msg.getObject();
      if (!(o instanceof UmsatzTyp))
        return;

      delayed.handleEvent(null);
    }
  }
  
  /**
   * Listener, der das Aktualisieren des Tree übernimmt.
   */
  private class UpdateListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          try
          {
            setList(init());
          }
          catch (RemoteException re)
          {
            Logger.error("unable to reload list",re);
          }
        }
      });
    }
  }
}
