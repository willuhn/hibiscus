/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractSammelTransferList.java,v $
 * $Revision: 1.13 $
 * $Date: 2010/11/01 23:00:32 $
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
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sammel-Auftraegen.
 */
public abstract class AbstractSammelTransferList extends AbstractFromToList
{
  private MessageConsumer mc = null;

  /**
   * ct.
   * @param action
   */
  public AbstractSammelTransferList(Action action)
  {
    super(action);
    setMulti(true);
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        SammelTransfer l = (SammelTransfer) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
            item.setForeground(Settings.getUeberfaelligForeground());
          if (l.ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (RemoteException e) { /*ignore */}
      }
    });

    addColumn(i18n.tr("Konto"),"konto_id", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null || !(o instanceof Konto))
          return null;
        Konto k = (Konto) o;
        try
        {
          String s = k.getKontonummer();
          String name = k.getBezeichnung();
          if (name != null && name.length() > 0)
            s += " [" + name + "]";
          return s;
        }
        catch (RemoteException r)
        {
          Logger.error("unable to display konto",r);
          return null;
        }
      }
    
    });
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    addColumn(i18n.tr("Anzahl Buchungen"),"anzahl");
    addColumn(i18n.tr("Summe"),"summe", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
        try {
          int i = ((Integer) o).intValue();
          return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
        }
        catch (Exception e) {}
        return ""+o;
      }
    });
    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Lastschriften informiert werden.
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Date from, Date to, String text) throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    
    DBIterator list = service.createList(getObjectType());
    if (from != null) list.addFilter("termin >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(from).getTime())});
    if (to   != null) list.addFilter("termin <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(to).getTime())});
    if (text != null && text.length() > 0)
    {
      list.addFilter("LOWER(bezeichnung) like ?", new Object[]{"%" + text.toLowerCase() + "%"});
    }
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
 * Revision 1.13  2010/11/01 23:00:32  willuhn
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