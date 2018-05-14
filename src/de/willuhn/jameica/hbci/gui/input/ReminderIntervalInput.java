/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LinkInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.gui.action.OpenReminderTemplate;
import de.willuhn.jameica.hbci.gui.dialogs.ReminderIntervalDialog;
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.reminder.ReminderUtil;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines fertig konfigurierten Auswahlfeldes fuer das Intervall des Reminders.
 */
public class ReminderIntervalInput implements Input
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Terminable order              = null;
  private Input input                   = null;
  private ReminderIntervalDialog dialog = null;
  private boolean containsInterval      = false;
  private Date end                      = null;
  
  /**
   * ct.
   * @param terminable der Auftrag.
   * @param termin das Start-Datum des Intervalls.
   * @throws RemoteException
   */
  public ReminderIntervalInput(Terminable terminable, Date termin) throws RemoteException
  {
    this.order = terminable;

    final HibiscusDBObject bean = (HibiscusDBObject) order;
    
    // Fuer Auftraege, die bereits selbst via Reminder erzeugt wurden, duerfen keine
    // neuen Reminder angelegt werden. Daher nehmen wir hier ein LinkInput, welches
    // auf die Kopier-Vorlage verlinkt.
    if (MetaKey.REMINDER_TEMPLATE.get(bean) != null)
    {
      this.input = new LinkInput(i18n.tr("von dieser <a>Vorlage</a>"));
      this.input.setName(i18n.tr("Wiederholung"));
      this.input.addListener(new Listener() {
        public void handleEvent(Event event)
        {
          try
          {
            new OpenReminderTemplate().handleAction(order);
          }
          catch (ApplicationException ae)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
      
      // Raus. Nichts weiter zu tun.
      return;
    }
    
    this.containsInterval = true;
    String uuid = MetaKey.REMINDER_UUID.get(bean);
    ReminderInterval ri = null;
    
    if (uuid != null)
    {
      try
      {
        // Wir holen den Termin direkt aus unserem Provider. Denn wir
        // speichern ja auch nur dort.
        BeanService service = Application.getBootLoader().getBootable(BeanService.class);
        ReminderStorageProvider provider = service.get(ReminderStorageProviderHibiscus.class);
        Reminder reminder = provider.get(uuid);
        if (reminder != null)
        {
          ri = reminder.getReminderInterval();
          end = reminder.getEnd();
        }
      }
      catch (RemoteException re)
      {
        throw re;
      }
      catch (Exception e)
      {
        throw new RemoteException("unable to load reminder",e);
      }
    }
    
    this.input = new DialogInput(this.toString(ri,end));
    this.input.setName(i18n.tr("Wiederholung"));
    this.input.setValue(ri);
    ((DialogInput)this.input).disableClientControl(); // Freitext-Eingabe gibts nicht.

    this.dialog = new ReminderIntervalDialog(ri,termin,end,ReminderIntervalDialog.POSITION_CENTER);
    this.dialog.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (event.detail == SWT.CANCEL)
          return; // Wurde abgebrochen
        ReminderInterval ri = (ReminderInterval) event.data;
        end = dialog.getEnd();
        ((DialogInput)input).setText(ReminderIntervalInput.this.toString(ri,end));
      }
    });
    ((DialogInput)this.input).setDialog(this.dialog);

    // Wenn der Auftrag bereits ausgefuehrt ist, uebernehmen wir die Aenderungen
    // sofort - ohne Klick auf Speichern
    if (this.order.ausgefuehrt())
    {
      this.input.addListener(new Listener() {
        public void handleEvent(Event event)
        {
          try
          {
            if (!input.hasChanged())
              return;
            ReminderInterval ri = (ReminderInterval) input.getValue();
            ReminderUtil.apply(bean,ri,dialog.getEnd());
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(ri != null ? "Wiederholung gespeichert" : "Wiederholung entfernt"),StatusBarMessage.TYPE_SUCCESS));
          }
          catch (ApplicationException ae)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
          }
          catch (Exception e)
          {
            Logger.error("unable to apply reminder interval",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }
  
  }
  
  /**
   * Liefert eine String-Repraesentation des Intervalls.
   * @param ri das Intervall.
   * @param end optionales Ende-Datum.
   * @return String-Repraesentation.
   */
  private String toString(ReminderInterval ri, Date end)
  {
    if (ri == null)
      return "<" + i18n.tr("keine") + ">";
    
    String text = ri.toString();
    if (end != null)
      text += ", " + i18n.tr("bis {0}",HBCI.DATEFORMAT.format(end));
    return text;
  }
  
  /**
   * Liefert true, wenn es sich hier tatsaechlich um ein Auswahlfeld
   * fuer das Intervall handelt. Ist der Auftrag jedoch ein Clone, liefert
   * die Funktion false.
   * @return true, wenn bei dem Auftrag tatsaechlich das Intervall eingestellt werden kann.
   */
  public boolean containsInterval()
  {
    return this.containsInterval;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return this.input.getValue();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    this.input.setValue(value);
  }
  
  /**
   * Liefert das optionale End-Datum.
   * @return das optionale End-Datum.
   */
  public Date getEnd()
  {
    return this.end;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#hasChanged()
   */
  public boolean hasChanged()
  {
    return this.input.hasChanged();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    return this.input.getControl();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#addListener(org.eclipse.swt.widgets.Listener)
   */
  public void addListener(Listener l)
  {
    this.input.addListener(l);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setComment(java.lang.String)
   */
  public void setComment(String comment)
  {
    this.input.setComment(comment);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent)
  {
    this.input.paint(parent);
    if (!(this.input instanceof DialogInput))
      return;
    
    // wir registrieren hier noch einen Message-Consumer, damit wir benachrichtigt werden,
    // wenn das Datum geaendert wurde.
    final MessageConsumer mc = new DateChangedConsumer();
    final MessagingQueue queue = Application.getMessagingFactory().getMessagingQueue(TerminInput.QUEUE_TERMIN_CHANGED);
    queue.registerMessageConsumer(mc);
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        queue.unRegisterMessageConsumer(mc);
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite, int)
   */
  public void paint(Composite parent, int width)
  {
    this.input.paint(parent,width);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.input.focus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    this.input.disable();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    this.input.enable();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.input.setEnabled(enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.input.isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setMandatory(boolean)
   */
  public void setMandatory(boolean mandatory)
  {
    this.input.setMandatory(mandatory);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isMandatory()
   */
  public boolean isMandatory()
  {
    return this.input.isMandatory();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.input.setName(name);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getName()
   */
  public String getName()
  {
    return this.input.getName();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setData(java.lang.String, java.lang.Object)
   */
  public void setData(String key, Object data)
  {
    this.input.setData(key,data);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getData(java.lang.String)
   */
  public Object getData(String key)
  {
    return this.input.getData(key);
  }

  /**
   * Wird benachrichtigt, wenn das Datum geaendert wurde.
   */
  private class DateChangedConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (dialog == null)
        return;
      
      QueryMessage msg = (QueryMessage) message;
      Object data = msg.getData();
      if (!(data instanceof Date))
        return;
      
      dialog.setDate((Date) data);
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }

}



/**********************************************************************
 * $Log: ReminderIntervalInput.java,v $
 * Revision 1.2  2011/12/13 22:59:24  willuhn
 * @B Beim erneuten Speichern eines Auftrages (ohne Oeffnen des Intervall-Dialogs) ging das Intervall verloren
 *
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/