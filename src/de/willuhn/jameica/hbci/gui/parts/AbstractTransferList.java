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
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.input.InputCompat;
import de.willuhn.jameica.hbci.gui.parts.columns.AusgefuehrtColumn;
import de.willuhn.jameica.hbci.gui.parts.columns.BlzColumn;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Transfers.
 */
public abstract class AbstractTransferList extends AbstractFromToList
{
  private MessageConsumer mc = null;
  private CheckboxInput pending = null;

  /**
   * ct.
   * @param action
   */
  public AbstractTransferList(Action action)
  {
    super(action);
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    final ReminderStorageProvider provider = service.get(ReminderStorageProviderHibiscus.class);

    final boolean bold = Settings.getBoldValues();
    
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Terminable l = (Terminable) item.getData();
        if (l == null)
          return;

        try
        {
          if (bold)
            item.setFont(4,Font.BOLD.getSWTFont());

          Date termin = l.getTermin();
          boolean faellig = l.ueberfaellig() && !l.ausgefuehrt();
          item.setFont(faellig ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          if (l.ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());

          // Checken, ob der Auftrag einen Reminder hat oder ob es ein geclonter Auftrag ist
          HibiscusDBObject o = (HibiscusDBObject) l;
          String uuid = MetaKey.REMINDER_UUID.get(o);
          if (uuid != null)
          {
            try
            {
              Reminder r = provider.get(uuid);
              item.setImage(5,SWTUtil.getImage("preferences-system-time.png"));
              Date end = r.getEnd();
              if (end != null)
                item.setText(5,i18n.tr("{0} - {1} \n{2}",HBCI.DATEFORMAT.format(termin),HBCI.DATEFORMAT.format(end),r.getReminderInterval().toString()));
              else
                item.setText(5,i18n.tr("ab {0} \n{1}",HBCI.DATEFORMAT.format(termin),r.getReminderInterval().toString()));
            }
            catch (Exception e)
            {
              Logger.error("unable to determine reminder",e);
            }
          }
          else if (MetaKey.REMINDER_TEMPLATE.get(o) != null)
          {
            item.setImage(5,SWTUtil.getImage("edit-copy.png"));
          }
        }
        catch (RemoteException e)
        {
          Logger.error("unable to format line",e);
        }
        
      }
    });

    initColums();
    setMulti(true);

    // Damit werden wir informiert, wenn Ueberweisungen/Lastschriften
    // importiert wurden. Wir koennen sie dann gleich zur Tabelle hinzufuegen
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

  }
  
  /**
   * Initialisiert die Spalten
   */
  protected void initColums()
  {
    addColumn(new KontoColumn());
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(new BlzColumn("empfaenger_blz",i18n.tr("Gegenkonto BLZ")));
    addColumn(i18n.tr("Verwendungszweck"),"mergedzweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(new AusgefuehrtColumn());
  }

  /**
   * Liefert eine Checkbox mit der festgelegt werden kann, ob nur offene Auftraege angezeigt werden sollen.
   * @return Checkbox.
   */
  protected CheckboxInput getPending()
  {
    if (this.pending != null)
      return this.pending;
    
    this.pending = new CheckboxInput(settings.getBoolean("transferlist.filter.pending",false));
    this.pending.setName(i18n.tr("Nur offene Aufträge anzeigen"));
    this.pending.addListener(this.listener);
    this.pending.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        settings.setAttribute("transferlist.filter.pending",((Boolean)pending.getValue()).booleanValue());
      }
    });
    return this.pending;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#hasChanged()
   */
  protected boolean hasChanged()
  {
    return InputCompat.valueHasChanged(super.hasChanged(), pending);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    // Damit wir den MessageConsumer beim Schliessen wieder entfernen
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    super.paint(parent);
    
    this.getLeft().addInput(this.getPending());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(java.lang.Object, java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    int filterCount = 0;
    
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    
    DBIterator list = service.createList(getObjectType());
    if (from != null)
    {
      list.addFilter("termin >= ?", new java.sql.Date(DateUtil.startOfDay(from).getTime()));
      filterCount++;
    }
    if (to != null)
    {
      list.addFilter("termin <= ?", new java.sql.Date(DateUtil.endOfDay(to).getTime()));
      filterCount++;
    }

    if (konto != null)
    {
      if (konto instanceof Konto)
        list.addFilter("konto_id = " + ((Konto) konto).getID());
      else if (konto instanceof String)
        list.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) konto);
      
      filterCount++;
    }
    
    boolean pending = ((Boolean) this.getPending().getValue()).booleanValue();
    if (pending)
    {
      list.addFilter("ausgefuehrt = 0");
      filterCount++;
    }

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC, id DESC");

    if (filterCount > 0)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Suchkriterien: {0}",Integer.toString(filterCount)),StatusBarMessage.TYPE_INFO));

    return list;
  }
  
  /**
   * Liefert die Art der zu ladenden Objekte zurueck.
   * @return Art der zu ladenden Objekte.
   */
  protected abstract Class getObjectType();


  /**
   * Hilfsklasse damit wir ueber importierte Transfers informiert werden.
   */
  public class TransferMessageConsumer implements MessageConsumer
  {
    private DelayedListener delayed = null;
    
    /**
     * ct.
     */
    private TransferMessageConsumer()
    {
      if (listener != null)
        this.delayed = new DelayedListener(listener);
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{
        ImportMessage.class,
        ObjectChangedMessage.class
      };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null)
        return;
      
      final GenericObject o = ((ObjectMessage)message).getObject();
      
      if (o == null)
        return;
      
      // Checken, ob uns der Transfer-Typ interessiert
      if (!getObjectType().isAssignableFrom(o.getClass()))
        return;
      
      if (message instanceof ObjectChangedMessage)
      {
        GUI.startSync(new Runnable() {
          public void run()
          {
            try
            {
              updateItem(o,o);
            }
            catch (Exception e)
            {
              Logger.error("unable to update item",e);
            }
          }
        });
        return;
      }

      // Wir forcieren das Reload. Da in den Eingabefeldern
      // nichts geaendert wurde, wuerde das Reload sonst nicht
      // stattfinden.
      if (delayed != null)
        delayed.handleEvent(null);
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
