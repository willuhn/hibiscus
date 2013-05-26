/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractSammelTransferList.java,v $
 * $Revision: 1.17 $
 * $Date: 2011/10/20 16:20:05 $
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
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.parts.columns.AusgefuehrtColumn;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sammel-Auftraegen.
 */
public abstract class AbstractSammelTransferList extends AbstractFromToList
{
  private MessageConsumer mc = null;
  private CheckboxInput pending = null;

  /**
   * ct.
   * @param action
   */
  public AbstractSammelTransferList(Action action)
  {
    super(action);
    setMulti(true);
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    final ReminderStorageProvider provider = service.get(ReminderStorageProviderHibiscus.class);

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        SammelTransfer l = (SammelTransfer) item.getData();
        if (l == null)
          return;

        try
        {
          Date termin = l.getTermin();
          boolean faellig = (termin.before(new Date()) && !l.ausgefuehrt());
          item.setFont(faellig ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          if (l.ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());

          // Checken, ob der Auftrag einen Reminder hat oder ob es ein geclonter Auftrag ist
          HibiscusDBObject o = (HibiscusDBObject) l;
          String uuid = o.getMeta("reminder.uuid",null);
          if (uuid != null)
          {
            try
            {
              Reminder r = provider.get(uuid);
              item.setImage(4,SWTUtil.getImage("stock_form-time-field.png"));
              item.setText(4,i18n.tr("ab {0}\n{1}",HBCI.DATEFORMAT.format(termin),r.getReminderInterval().toString()));
            }
            catch (Exception e)
            {
              Logger.error("unable to determine reminder",e);
            }
          }
          else if (o.getMeta("reminder.template",null) != null)
          {
            item.setImage(4,SWTUtil.getImage("edit-copy.png"));
          }
        }
        catch (RemoteException e)
        {
          Logger.error("unable to format line",e);
        }
      }
    });

    addColumn(new KontoColumn());
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    addColumn(i18n.tr("Anzahl Buchungen"),"anzahl");
    addColumn(i18n.tr("Summe"),"summe", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(new AusgefuehrtColumn());
    
    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Lastschriften informiert werden.
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
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
    return this.pending;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#hasChanged()
   */
  protected boolean hasChanged()
  {
    try
    {
      return (super.hasChanged() && pending != null && pending.hasChanged());
    }
    catch (Exception e)
    {
      Logger.error("unable to check change status",e);
      return true;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    
    DBIterator list = service.createList(getObjectType());
    if (from != null) list.addFilter("termin >= ?", new Object[]{new java.sql.Date(DateUtil.startOfDay(from).getTime())});
    if (to   != null) list.addFilter("termin <= ?", new Object[]{new java.sql.Date(DateUtil.endOfDay(to).getTime())});
    if (text != null && text.length() > 0)
    {
      list.addFilter("LOWER(bezeichnung) like ?", new Object[]{"%" + text.toLowerCase() + "%"});
    }
    
    if (konto != null && (konto instanceof Konto))
      list.addFilter("konto_id = " + ((Konto) konto).getID());
    else if (konto != null && (konto instanceof String))
      list.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) konto);

    boolean pending = ((Boolean) this.getPending().getValue()).booleanValue();
    if (pending)
      list.addFilter("ausgefuehrt = 0");

    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC, id DESC");
    return list;
  }
  
  /**
   * Liefert die Art der zu ladenden Objekte zurueck.
   * @return Art der zu ladenden Objekte.
   */
  protected abstract Class getObjectType();

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
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
      if (!(o instanceof SammelTransfer) && !(o instanceof SammelTransferBuchung))
        return;

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


/**********************************************************************
 * $Log: AbstractSammelTransferList.java,v $
 * Revision 1.17  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 * Revision 1.16  2011-06-30 16:29:41  willuhn
 * @N Unterstuetzung fuer neues UnreadCount-Feature
 *
 * Revision 1.15  2011-04-29 15:33:28  willuhn
 * @N Neue Spalte "ausgefuehrt_am", in der das tatsaechliche Ausfuehrungsdatum von Auftraegen vermerkt wird
 *
 * Revision 1.14  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.13  2010-11-01 23:00:32  willuhn
 * @N Ausgefuehrte Sammel-Auftraege in grau anzeigen
 *
 * Revision 1.12  2010-08-16 11:13:52  willuhn
 * @N In den Auftragslisten kann jetzt auch nach einem Text gesucht werden
 *
 * Revision 1.11  2010/03/24 14:06:45  willuhn
 * @B Uhrzeit in Termin-Spalte nicht anzeigen
 *
 * Revision 1.10  2009/03/01 22:26:19  willuhn
 * @B BUGZILLA 705
 *
 * Revision 1.9  2009/02/13 14:17:01  willuhn
 * @N BUGZILLA 700
 *
 * Revision 1.8  2008/06/30 13:04:10  willuhn
 * @N Von-Bis-Filter auch in Sammel-Auftraegen
 *
 * Revision 1.7  2008/02/04 18:56:45  willuhn
 * @B Bug 545
 *
 * Revision 1.6  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.5  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.4  2006/11/06 23:12:38  willuhn
 * @B Fehler bei Aktualisierung der Elemente nach Insert, Delete, Sort
 *
 * Revision 1.3  2006/10/17 00:04:31  willuhn
 * @N new Formatters in Transfer-Listen
 * @N merged UeberweisungList + LastschriftList into AbstractTransferList
 *
 * Revision 1.2  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/