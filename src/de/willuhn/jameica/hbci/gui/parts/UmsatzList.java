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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureShortcut;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Umsaetzen.
 */
public class UmsatzList extends TablePart implements Extendable
{
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Cache fuer die Filter-Einstellungen des Users fuer die Dauer der Sitzung.
  static Map cache = new HashMap();
  

  private MessageConsumer mcChanged = null;
  private MessageConsumer mcNew     = null;

  private UmsatzDaysInput days      = null;

  private Konto konto               = null;
  
  private KL kl                     = new KL();
  private boolean filter            = true;
  
  private boolean disposed          = false;
  
  private List<Umsatz> umsaetze     = null;
  
  /**
   * @param konto
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, Action action) throws RemoteException
  {
    this((GenericIterator) null, action);
    this.konto = konto;
  }

  /**
   * @param list
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(GenericIterator<Umsatz> list, Action action) throws RemoteException
  {
    super(list,action);
    
    // Wir arbeiten nur auf dieser Liste
    if (list != null)
      this.umsaetze = PseudoIterator.asList(list);
    
    this.addFeature(new FeatureShortcut()); // Wir unterstuetzen Shortcuts

    final DateFormatter df  = new DateFormatter(HBCI.DATEFORMAT);
    final DateFormatter dfs = new DateFormatter(HBCI.SHORTDATEFORMAT);
    final boolean bold = Settings.getBoldValues();
    
    setMulti(true);
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Umsatz u = (Umsatz) item.getData();
        if (u == null) return;

        try {
          item.setFont(NeueUmsaetze.isNew(u) ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());

          final Date datum = u.getDatum();
          final Date valuta = u.getValuta();
          if (!Objects.equals(datum,valuta))
            item.setText(4,df.format(datum) + " (" + dfs.format(valuta) + ")");
          
          if (bold)
            item.setFont(5,Font.BOLD.getSWTFont());

          if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
          else
          {
            ColorUtil.setForeground(item,5,u.getBetrag());
            ColorUtil.setForeground(item,6,u.getUmsatzTyp());

            // Saldo nicht mit einfaerben, dass irritiert sonst,
            // wenn die Buchung zwar einen negativen Betrag hat,
            // der Saldo aber einen positiven (und umgekehrt)
            item.setForeground(7,Color.FOREGROUND.getSWTColor());
          }

          item.setText(1,""); // Kein Text in den Flags - wir wollen nur das Bild
          if (u.hasFlag(Umsatz.FLAG_CHECKED))
            item.setImage(1,SWTUtil.getImage("emblem-default.png"));
          else
            item.setImage(1,null); // Image wieder entfernen. Noetig, weil wir auch bei Updates aufgerufen werden
        
        }
        catch (RemoteException e)
        {
          Logger.error("unable to format line",e);
        }
      }
    });

    // BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    addColumn("#","id-int");
    addColumn(i18n.tr("Geprüft"),                   "flags");
    addColumn(i18n.tr("Gegenkonto"),                "empfaenger");
    
    if (settings.getBoolean("usage.display.all",false))
      addColumn(i18n.tr("Verwendungszweck"),        "mergedzweck");
    else
      addColumn(i18n.tr("Verwendungszweck"),        Tag.SVWZ.name());
    
    addColumn(i18n.tr("Datum"),                     "datum_pseudo", df);
    addColumn(i18n.tr("Betrag"),                    "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("Kategorie"),                 "umsatztyp",null,false);
    // BUGZILLA 66 http://www.willuhn.de/bugzilla/show_bug.cgi?id=66
    addColumn(i18n.tr("Zwischensumme"),             "saldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("Notiz"),                     "kommentar",null,true);

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);
    
    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);
    
    // BUGZILLA 468 http://www.willuhn.de/bugzilla/show_bug.cgi?id=468
    setRememberState(true);

    // Wir erstellen noch Message-Consumer, damit wir ueber neu eintreffende
    // und geaenderte Umsaetze informiert werden.
    this.mcChanged = new UmsatzChangedMessageConsumer();
    this.mcNew     = new UmsatzNewMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mcChanged);
    Application.getMessagingFactory().registerMessageConsumer(this.mcNew);

    this.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        refreshSummary();
      }
    });
    
    this.addChangeListener(new TableChangeListener() {
      public void itemChanged(Object object, String attribute, String newValue) throws ApplicationException
      {
        try
        {
          Umsatz u = (Umsatz) object;
          BeanUtil.set(u,attribute,newValue);
          u.store();
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to apply changes",e);
          throw new ApplicationException(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()));
        }
      }
    });
    
    // Wir geben die Tabelle jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);
  }
  
  /**
   * Schaltet die Anzeige der Umsatzfilter an oder aus.
   * @param visible true, wenn die Umsatzfilter angezeigt werden sollen. Default: true.
   */
  public void setFilterVisible(boolean visible)
  {
    this.filter = visible;
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#getSummary()
   */
  @Override
  protected String getSummary()
  {
    try
    {
      Object o = this.getSelection();
      int size = this.size();

      // nichts markiert oder nur einer, dann liefern wir nur die Anzahl der Umsaetze
      if (o == null || size == 1 || !(o instanceof Umsatz[]))
      {
        if (size == 1)
          return i18n.tr("1 Umsatz");
        else
          return i18n.tr("{0} Umsätze",Integer.toString(size));
      }
      
      // Andernfalls berechnen wir die Summe
      double sum = 0.0d;
      double income = 0.0d;
      double expenses = 0.0d;
      Umsatz[] list = (Umsatz[]) o;
      String curr = null;
      for (Umsatz u:list)
      {
        if (curr == null)
          curr = u.getKonto().getWaehrung();
        
        double betrag = u.getBetrag();
        sum += betrag;
        
        if (betrag >= 0.01d)
          income += betrag;
        else
          expenses += betrag;
      }
      if (curr == null)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      //@formatter:off
      return i18n.tr("{0} Umsätze, {1} markiert, Summe: {2} {5}, Einnahmen: {3} {5}, Ausgaben: {4} {5}",
                     Integer.toString(size),
                     Integer.toString(list.length),
                     HBCI.DECIMALFORMAT.format(sum),
                     HBCI.DECIMALFORMAT.format(income),
                     HBCI.DECIMALFORMAT.format(Math.abs(expenses)),
                     curr);
      //@formatter:on
    }
    catch (Exception e)
    {
      Logger.error("error while updating summary",e);
    }
    return super.getSummary();
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzList(this.konto));

    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        disposed = true;
        Application.getMessagingFactory().unRegisterMessageConsumer(mcChanged);
        Application.getMessagingFactory().unRegisterMessageConsumer(mcNew);
        
        // Fuer den Fall, dass wir verlassen worden, bevor das letzte Aktualisierungstimeout
        // ausgelaufen war. Das wuerde sonst ausgeloest werden, obwohl die Widgets alle disposed sind
        if (kl != null && kl.timeout != null)
        {
          try
          {
            kl.timeout.interrupt();
          }
          catch (Exception ex)
          {
            // ignore
          }
        }
      }
    });
    
    if (this.filter)
    {
      Container c = new SimpleContainer(parent);
      this.days = new UmsatzDaysInput();
      this.days.addListener(new DelayedListener(300, new Listener() {
        public void handleEvent(Event event)
        {
          kl.process();
        }
      }));
      c.addInput(this.days);
    }
    
    // Und einmal starten bitte, wenn wir entweder einen Filter
    // haben oder ein Konto angegeben ist, von dem wir die Umsaetze on-the-fly laden 
    if (this.filter || this.konto != null)
      kl.process(true);

    super.paint(parent);

    // Machen wir explizit nochmal, weil wir die paint()-Methode ueberschrieben haben
    restoreState();
  }


  private class KL extends KeyAdapter
  {
    private boolean sleep = true;
    private Thread timeout = null;
    private Calendar cal = null;
   
    private KL() throws RemoteException
    {
      this.cal = Calendar.getInstance();
    }
    
    /**
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      // Wenn ein Timeout existiert, verlaengern wir einfach
      // nur dessen Wartezeit
      if (timeout != null)
      {
        sleep = true;
        return;
      }
      
      // Ein neuer Timer
      timeout = new Thread("UmsatzList")
      {
        public void run()
        {
          try
          {
            do
            {
              sleep = false;
              sleep(700l);
            }
            while (sleep); // Wir warten ggf. nochmal

            // Ne, wir wurden nicht gekillt. Also machen wir uns ans Werk
            process();

          }
          catch (InterruptedException e)
          {
            return;
          }
          finally
          {
            timeout = null;
          }
        }
      };
      timeout.start();
    }
    
    /**
     * 
     */
    private synchronized void process()
    {
      this.process(false);
    }

    /**
     * @param force true, wenn das Reload forciert werden soll.
     */
    private synchronized void process(final boolean force)
    {
      if (disposed)
        return;
      
      GUI.startSync(new Runnable()
      {
        public void run()
        {
          try
          {
            if (!force && !hasChanged())
              return;
              
            int t = days != null ? ((Integer) days.getValue()).intValue() : 0;

            if (konto != null)
            {
              // Umsaetze vom Konto neu laden
              removeAll();
              GenericIterator<Umsatz> list = konto.getUmsaetze(t);
              while (list.hasNext())
              {
                addItem(list.next());
              }
            }
            else if (umsaetze != null)
            {
              removeAll();
              Date date = null;
              Date limit = null;
              if (t > 0)
              {
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_YEAR,-t);
                cal.set(Calendar.HOUR_OF_DAY,0);
                cal.set(Calendar.MINUTE,0);
                cal.set(Calendar.SECOND,0);
                cal.set(Calendar.MILLISECOND,0);
                limit = cal.getTime();
              }
              

              for (int i=0;i<umsaetze.size();++i)
              {
                Umsatz u = (Umsatz) umsaetze.get(i);
                if (u.getID() == null) // Wurde zwischenzeitlich geloescht
                {
                  umsaetze.remove(i);
                  i--;
                  continue;
                }
                date = u.getDatum();

                // Wenn der Umsatz ein Datum hat, welches vor dem Limit liegt. Dann raus damit
                if (date != null && limit != null && date.before(limit))
                  continue;

                addItem(u);
              }
            }
            
            sort();
          }
          catch (Exception e)
          {
            Logger.error("error while loading umsatz",e);
          }
        }
      });
    }
  }

  private Integer lastDays = null;
  
  /**
   * Prueft, ob sich an den Such-Eingaben etwas geaendert hat.
   * @return true, wenn sich den Eingaben etwas geaendert hat.
   */
  private boolean hasChanged()
  {
    // Such-Filter ist ueberhaupt nicht aktiv
    if (!this.filter || this.days == null)
      return false;
    
    Integer i = (Integer) this.days.getValue();  // liefert nie null
    try
    {
      return !i.equals(this.lastDays);
    }
    finally
    {
      this.lastDays = i;
    }
  }

  
  /**
   * Hilfsklasse damit wir ueber importierte Umsaetze informiert werden.
   */
  public class UmsatzChangedMessageConsumer implements MessageConsumer
  {
    private List<Umsatz> bulk = new LinkedList<Umsatz>();
    private DelayedListener delay = new DelayedListener(70,new Listener()
    {
      public void handleEvent(Event event)
      {
        try
        {
          for (Umsatz u:bulk)
          {
            // Das geht, weil die Objekt-Referenz die selbe ist.
            // Nur die Properties haben sich geaendert. Und die sollen ja neu gezeichnet werden.
            updateItem(u,u);
          }
          // Summen-Zeile einmalig aktualisieren
          refreshSummary();
        }
        catch (Exception e)
        {
          Logger.error("unable to add object to list",e);
        }
        finally
        {
          bulk.clear();
        }
      }
    });
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{
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

      if (o == null || !(o instanceof Umsatz))
        return;
      
      // wir machen das Update in einer Bulk-Operation, damit die Summen-Zeile nicht
      // 100 mal aktualisiert werden muss, wenn 100 Umsaetze angefasst wurden
      this.bulk.add((Umsatz)o);
      this.delay.handleEvent(null);
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }


  /**
   * Hilfsklasse damit wir ueber importierte Umsaetze informiert werden.
   */
  public class UmsatzNewMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{
        ImportMessage.class,
      };
    }

    DelayedListener updateKontoListListener = new DelayedListener(new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        if (kl != null)
          kl.process(true);
      }
    });

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null)
        return;
      
      final GenericObject o = ((ObjectMessage)message).getObject();

      if (o == null || !(o instanceof Umsatz))
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            Umsatz newUmsatz = (Umsatz) o;
            
            // BUGZILLA 692 haben wir den schon?
            if (umsaetze != null)
            {
              for (Object u:getItems())
              {
                if (BeanUtil.equals(u,newUmsatz))
                  return;
              }
            }
            
            // Checken, ob der Umsatz ueberhaupt zum Konto passt
            // Wenn man waehrend der Synchronisierung in ein anderes Konto klickt, koennte es sonst
            // passieren, dass ein Umsatz hier temporaer in der Liste landet, weil halt zufaellig grad
            // diese View offen ist.
            if (konto != null)
            {
              if (!BeanUtil.equals(konto,newUmsatz.getKonto())) // ist von einem anderen Konto
                return;
            }

            if (umsaetze != null)
              umsaetze.add(newUmsatz);
            else
              addItem(newUmsatz);
            
            updateKontoListListener.handleEvent(null);
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
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return UmsatzList.class.getName();
  }
  
  
}
