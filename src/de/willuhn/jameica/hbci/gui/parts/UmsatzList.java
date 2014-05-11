/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzList.java,v $
 * $Revision: 1.83 $
 * $Date: 2012/04/23 21:03:41 $
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.ButtonInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureShortcut;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypNewDialog;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Umsaetzen.
 */
public class UmsatzList extends TablePart implements Extendable
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Cache fuer die Filter-Einstellungen des Users fuer die Dauer der Sitzung.
  static Map cache = new HashMap();
  

  private MessageConsumer mcChanged = null;
  private MessageConsumer mcNew     = null;

  private SearchInput search        = null;
  private CheckboxInput regex       = null;
  
  private UmsatzDaysInput days      = null;

  private Konto konto               = null;
  private List umsaetze             = null;
  
  private KL kl                     = null;
  private boolean filter            = true;
  
  private boolean disposed          = false;
  
  /**
   * @param konto
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, Action action) throws RemoteException
  {
    this(konto,0,action);
  }

  /**
   * @param konto
   * @param days
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, int days, Action action) throws RemoteException
  {
    this(konto.getUmsaetze(days), action);
    this.konto = konto;
  }

  /**
   * @param list
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(GenericIterator list, Action action) throws RemoteException
  {
    super(list,action);
    if (list != null)
      this.umsaetze = PseudoIterator.asList(list);
    else
      this.umsaetze = new ArrayList();
    
    this.addFeature(new FeatureShortcut()); // Wir unterstuetzen Shortcuts
    
    setMulti(true);
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Umsatz u = (Umsatz) item.getData();
        if (u == null) return;

        try {
          item.setFont(NeueUmsaetze.isNew(u) ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          
          if (!u.isBooked())
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
          else
          {
            item.setForeground(ColorUtil.getForeground(u.getBetrag()));

            // Saldo nicht mit einfaerben, dass irritiert sonst,
            // wenn die Buchung zwar einen negativen Betrag hat,
            // der Saldo aber einen positiven (und umgekehrt)
            item.setForeground(7,Color.FOREGROUND.getSWTColor());
          }

          item.setText(1,""); // Kein Text in den Flags - wir wollen nur das Bild
          if (u.isChecked())
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
    addColumn(i18n.tr("Flags"),                     "flags");
    addColumn(i18n.tr("Gegenkonto"),                "empfaenger");
    addColumn(i18n.tr("Verwendungszweck"),          "mergedzweck");
    addColumn(i18n.tr("Datum"),                     "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Betrag"),                    "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Kategorie"),                 "umsatztyp",null,false);
    // BUGZILLA 66 http://www.willuhn.de/bugzilla/show_bug.cgi?id=66
    addColumn(i18n.tr("Zwischensumme"),             "saldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
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
      Umsatz[] list = (Umsatz[]) o;
      String curr = null;
      for (Umsatz u:list)
      {
        if (curr == null)
          curr = u.getKonto().getWaehrung();
        sum += u.getBetrag();
      }
      if (curr == null)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      return i18n.tr("{0} Umsätze, {1} markiert, Summe: {2} {3}",new String[]{Integer.toString(size),
                                                                              Integer.toString(list.length),
                                                                              HBCI.DECIMALFORMAT.format(sum),
                                                                              curr});
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
      if (this.kl == null)
        this.kl = new KL();

      LabelGroup group = new LabelGroup(parent,i18n.tr("Anzeige einschränken"));

      this.days = new UmsatzDaysInput();
      this.days.addListener(new DelayedListener(300, new Listener() {
        public void handleEvent(Event event)
        {
          kl.process();
        }
      }));
      group.addInput(this.days);

      // Eingabe-Feld fuer die Suche mit Button hinten dran.
      this.search = new SearchInput();
      group.addLabelPair(i18n.tr("Zweck, Konto oder Kommentar enthält"), this.search);

      // Checkbox zur Aktivierung von regulaeren Ausdruecken
      Boolean b = (Boolean) cache.get("regex");
      this.regex = new CheckboxInput(b != null && b.booleanValue());
      this.regex.addListener(new Listener() {
        public void handleEvent(Event event)
        {
          cache.put("regex",regex.getValue());
          kl.process();
        }
      });
      group.addCheckbox(this.regex,i18n.tr("Suchbegriff ist ein regulärer Ausdruck"));
    }

    super.paint(parent);

    // Und einmal starten bitte
    if (this.filter)
      kl.process();
    
    // Machen wir explizit nochmal, weil wir die paint()-Methode ueberschrieben haben
    restoreState();
  }


  /**
   * Hilfsklasse fuer das Suchfeld.
   * @author willuhn
   */
  private class SearchInput extends ButtonInput
  {
    private Text text = null;

    /**
     * ct.
     */
    private SearchInput()
    {
      // Listener fuer den Button
      this.addButtonListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          Menu menu = new Menu(GUI.getShell(),SWT.POP_UP);
          MenuItem item = new MenuItem(menu, SWT.PUSH);
          item.setText(i18n.tr("Suchbegriff als Umsatz-Kategorie speichern..."));
          item.addListener(SWT.Selection, new Listener()
          {
            public void handleEvent (Event e)
            {
              try
              {
                String text = (String) search.getValue();
                if (text == null || text.length() == 0)
                  return;
                
                // Mal schauen, obs den Typ schon gibt
                DBIterator existing = Settings.getDBService().createList(UmsatzTyp.class);
                existing.addFilter("pattern = ?", new Object[]{text});
                UmsatzTyp typ = null; 
                if (existing.size() > 0)
                {
                  if (!Application.getCallback().askUser(i18n.tr("Eine Umsatz-Kategorie mit diesem Suchbegriff existiert bereits. Überschreiben?")))
                    return;
                  
                  // OK, ueberschreiben
                  typ = (UmsatzTyp) existing.next();
                }
                else
                {
                  UmsatzTypNewDialog d = new UmsatzTypNewDialog(UmsatzTypNewDialog.POSITION_MOUSE);
                  typ = (UmsatzTyp) d.open();
                }
                typ.setPattern(text);
                typ.setRegex(((Boolean)regex.getValue()).booleanValue());
                typ.store();
                GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz-Kategorie gespeichert"));
              }
              catch (ApplicationException ae)
              {
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
              }
              catch (OperationCanceledException oce)
              {
                Logger.info("operation cancelled");
                return;
              }
              catch (Exception ex)
              {
                Logger.error("unable to store umsatz filter",ex);
                GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Umsatz-Kategorie"));
              }
            }
          });
          
          new MenuItem(menu, SWT.SEPARATOR);
          try
          {
            DBIterator i = Settings.getDBService().createList(UmsatzTyp.class);
            i.addFilter("pattern is not null and pattern != ''"); // Wir wollen nur die mit Suchbegriff haben
            while (i.hasNext())
            {
              final UmsatzTyp ut = (UmsatzTyp) i.next();
              final String s    = ut.getName();
              final String p    = ut.getPattern();
              final boolean ir  = ut.isRegex();
              final MenuItem mi = new MenuItem(menu, SWT.PUSH);
              mi.setText(s);
              mi.addListener(SWT.Selection, new Listener()
              {
                public void handleEvent(Event event)
                {
                  Logger.debug("applying filter " + p);
                  regex.setValue(Boolean.valueOf(ir));
                  search.setValue(p);
                  search.focus();
                  kl.process();
                }
              });
            }
            
          }
          catch (Exception ex)
          {
            Logger.error("unable to load umsatz filter",ex);
            GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Umsatz-Kategorien"));
          }

          menu.setLocation(GUI.getDisplay().getCursorLocation());
          menu.setVisible(true);
          while (!menu.isDisposed() && menu.isVisible())
          {
            if (!GUI.getDisplay().readAndDispatch()) GUI.getDisplay().sleep();
          }
          menu.dispose();
        }
      });
    }

    /**
     * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
     */
    public Control getClientControl(Composite parent)
    {
      if (text != null)
        return text;

      text = GUI.getStyleFactory().createText(parent);
      // BUGZILLA 258
      
      String s = (String) cache.get("search");
      this.text.setText(s != null ? s : "");
      this.text.addKeyListener(kl);
      return this.text;
    }

    /**
     * @see de.willuhn.jameica.gui.input.Input#getValue()
     */
    public Object getValue()
    {
      String s = text == null ? null : text.getText();
      cache.put("search",s);
      return s;
    }

    /**
     * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
      if (text == null || value == null || text.isDisposed())
        return;
      text.setText(value.toString());
    }
    
  }
  
  // BUGZILLA 5
  private class KL extends KeyAdapter
  {
    private boolean sleep = true;
    private Thread timeout = null;
    private UmsatzTyp typ = null;
    private Calendar cal = null;
   
    private KL() throws RemoteException
    {
      DBService service = Settings.getDBService();
      this.typ = (UmsatzTyp) service.createObject(UmsatzTyp.class,null);
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
              
            // Erstmal alle rausschmeissen
            UmsatzList.this.removeAll();

            // Wir holen uns den aktuellen Text
            String text = (String) search.getValue();

            Umsatz u  = null;
            Date date = null;

            // BUGZILLA 217
            Date limit = null;
            int t = ((Integer) days.getValue()).intValue();
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
            
            boolean empty = text == null || text.length() == 0;

            if (!empty)
            {
              typ.setPattern(text);
              typ.setRegex(((Boolean)regex.getValue()).booleanValue());
            }
            
            for (int i=0;i<umsaetze.size();++i)
            {
              u = (Umsatz) umsaetze.get(i);
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

              // Steht ein Suchwort drin?
              // Nein? Dann sofort uebernehmen
              if (empty)
              {
                UmsatzList.this.addItem(u);
                continue;
              }

              if (typ.matches(u,true))
              {
                UmsatzList.this.addItem(u);
              }
            }
            UmsatzList.this.sort();
          }
          catch (PatternSyntaxException pe)
          {
            GUI.getView().setErrorText(pe.getLocalizedMessage());
          }
          catch (Exception e)
          {
            Logger.error("error while loading umsatz",e);
          }
        }
      });
    }
  }

  private String lastSearch = "";
  private Boolean lastRegex = Boolean.FALSE;
  private Integer lastDays = null;
  
  /**
   * Prueft, ob sich an den Such-Eingaben etwas geaendert hat.
   * @return true, wenn sich den Eingaben etwas geaendert hat.
   */
  private boolean hasChanged()
  {
    // Such-Filter ist ueberhaupt nicht aktiv
    if (!this.filter || this.search == null || this.regex == null || this.days == null)
      return false;
    
    String s  = (String) this.search.getValue(); // liefert nie null
    Boolean r = (Boolean) this.regex.getValue(); // liefert nie null
    Integer i = (Integer) this.days.getValue();  // liefert nie null
    try
    {
      return !r.equals(this.lastRegex) || !s.equals(this.lastSearch) || !i.equals(this.lastDays);
    }
    finally
    {
      this.lastSearch = s;
      this.lastRegex = r;
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
            for (Object u:umsaetze)
            {
              if (BeanUtil.equals(u,newUmsatz))
                return;
            }
            
            // Checken, ob er Umsatz ueberhaupt zum Konto passt
            // Wenn man waehrend der Synchronisierung in ein anderes Konto klickt, koennte es sonst
            // passieren, dass ein Umsatz hier temporaer in der Liste landet, weil halt zufaellig grad
            // diese View offen ist.
            if (konto != null)
            {
              if (!BeanUtil.equals(konto,newUmsatz.getKonto())) // ist von einem anderen Konto
                return;
            }

            umsaetze.add(newUmsatz);
            if (filter && kl != null)
              kl.process(true);
            else
            {
              addItem(newUmsatz);
              sort();
            }
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
