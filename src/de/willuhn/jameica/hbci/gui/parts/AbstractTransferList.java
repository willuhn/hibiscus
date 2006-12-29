/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractTransferList.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/12/29 14:28:47 $
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Transfers.
 */
public abstract class AbstractTransferList extends TablePart implements Part
{
  private I18N i18n           = null;

  private MessageConsumer mc  = null;
  
  private Input from          = null;
  private Input to            = null;
  
  private GenericIterator list  = null;
  private ArrayList transfers   = null;

  private de.willuhn.jameica.system.Settings settings = null;
  

  /**
   * ct.
   * @param list Liste der anzuzeigenden Transfers.
   * @param action
   */
  public AbstractTransferList(GenericIterator list, Action action)
  {
    super(list, action);
    this.list = list;
    this.i18n     = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Terminable l = (Terminable) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
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
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(i18n.tr("Gegenkonto BLZ"),"empfaenger_blz", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        String name = HBCIUtils.getNameForBLZ(blz);
        if (name != null && name.length() > 0)
          blz += " [" + name + "]";
        return blz;
      }
    
    });
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
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
  
    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);
    
    setMulti(true);

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Transfers informiert werden.
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

  }

  /**
   * Ueberschrieben, um einen DisposeListener an das Composite zu haengen.
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

    LabelGroup group = new LabelGroup(parent,i18n.tr("Filter"));

    // Als End-Datum nehmen wir keines.
    // Es sei denn, es ist ein aktuelles gespeichert
    Date dTo = null;
    String sTo = settings.getString("transferlist.filter.to",null);
    if (sTo != null && sTo.length() > 0)
    {
      try
      {
        dTo = HBCI.DATEFORMAT.parse(sTo);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + sTo,e);
      }
    }

    // Als Startdatum nehmen wir den ersten des aktuellen Monats
    // Es sei denn, es ist eines gespeichert
    Date dFrom = null;
    String sFrom = settings.getString("transferlist.filter.from",null);
    if (sFrom != null && sFrom.length() > 0)
    {
      try
      {
        dFrom = HBCI.DATEFORMAT.parse(sFrom);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + sFrom,e);
      }
    }

    if (dFrom == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH,1);
      dFrom = HBCIProperties.startOfDay(cal.getTime());
    }

    Listener l = new ChangedListener();
    
    from = new DateInput(dFrom, HBCI.DATEFORMAT);
    from.addListener(l);
    to = new DateInput(dTo, HBCI.DATEFORMAT);
    to.addListener(l);
    
    group.addLabelPair(i18n.tr("Aufträge von"),from);
    group.addLabelPair(i18n.tr("Aufträge bis"),to);
   
    super.paint(parent);

    // Wir kopieren den ganzen Kram in eine ArrayList, damit die
    // Objekte beim Filter geladen bleiben
    transfers = new ArrayList();
    list.begin();
    while (list.hasNext())
    {
      Terminable t = (Terminable) list.next();
      transfers.add(t);
    }
    
    // einmal ausloesen
    l.handleEvent(null);

  }
  
  /**
   * Wird ausgeloest, wenn das Datum geaendert wird.
   */
  private class ChangedListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try
      {
        Date dfrom = (Date) from.getValue();
        Date dto   = (Date) to.getValue();
        
        if (dfrom != null && dto != null && dfrom.after(dto))
        {
          GUI.getView().setErrorText(i18n.tr("End-Datum muss sich nach dem Start-Datum befinden"));
          return;
        }
        GUI.getView().setErrorText("");
        
        AbstractTransferList.this.removeAll();

        for (int i=0;i<transfers.size();++i)
        {
          Terminable t = (Terminable) transfers.get(i);
          if (((GenericObject)t).getID() == null) // Wurde zwischenzeitlich geloescht
          {
            transfers.remove(i);
            i--;
            continue;
          }
          Date termin = t.getTermin();
          if (termin == null || (dfrom == null && dto == null))
          {
            AbstractTransferList.this.addItem((Transfer)t);
            continue;
          }
          boolean match = (dfrom == null || termin.after(dfrom) || termin.equals(dfrom)); // Entweder kein Start-Datum oder dahinter
          match &= (dto == null || termin.before(dto) || termin.equals(dto)); // oder kein End-Datum oder davor
          if (match)
            AbstractTransferList.this.addItem((Transfer)t);
        }
        
        // Sortierung wiederherstellen
        AbstractTransferList.this.sort();

      
        // Wir speichern die Datums-Eingaben
        // Das From-Datum speichern wir immer
        if (dfrom != null)
          settings.setAttribute("transferlist.filter.from",HBCI.DATEFORMAT.format(dfrom));
        
        // Das End-Datum speichern wir nur, wenn es nicht das aktuelle Datum ist
        if (dto != null && !HBCIProperties.startOfDay(new Date()).equals(dto))
          settings.setAttribute("transferlist.filter.to",HBCI.DATEFORMAT.format(dto));
        else
          settings.setAttribute("transferlist.filter.to",(String)null);
        
      
      }
      catch (RemoteException re)
      {
        Logger.error("unable to apply filter",re);
      }
    }
    
  }

  /**
   * Hilfsklasse damit wir ueber importierte Transfers informiert werden.
   */
  public class TransferMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public TransferMessageConsumer()
    {
      super();
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null || !(message instanceof ImportMessage))
        return;
      final GenericObject o = ((ImportMessage)message).getImportedObject();
      
      if (o == null || !(o instanceof Transfer))
        return;
      
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            transfers.add(o);
            sort();
          }
          catch (Exception e)
          {
            Logger.error("unable to add object to list",e);
          }
        }
      });
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
 * $Log: AbstractTransferList.java,v $
 * Revision 1.11  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.10  2006/12/27 17:56:49  willuhn
 * @B Bug 341
 *
 * Revision 1.9  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.8  2006/11/16 12:21:02  willuhn
 * @B NPE
 *
 * Revision 1.7  2006/11/15 00:47:39  willuhn
 * @C Bug 325
 *
 * Revision 1.6  2006/11/06 23:12:38  willuhn
 * @B Fehler bei Aktualisierung der Elemente nach Insert, Delete, Sort
 *
 * Revision 1.5  2006/10/31 22:59:03  willuhn
 * @B Bis-Datum wurde nicht korrekt uebernommen
 *
 * Revision 1.4  2006/10/31 22:54:46  willuhn
 * @N Ausgefuehrte Transfers eingrauen
 *
 * Revision 1.3  2006/10/17 23:50:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/17 01:01:21  willuhn
 * @N Filter fuer Ueberweisungen und Lastschriften
 *
 * Revision 1.1  2006/10/17 00:04:31  willuhn
 * @N new Formatters in Transfer-Listen
 * @N merged UeberweisungList + LastschriftList into AbstractTransferList
 *
 **********************************************************************/