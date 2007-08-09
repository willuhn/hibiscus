/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzList.java,v $
 * $Revision: 1.54 $
 * $Date: 2007/08/09 11:01:38 $
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
import java.util.List;
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
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypNewDialog;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
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
  private MessageConsumer mc    = null;

  private SearchInput search    = null;
  private CheckboxInput regex   = null;
  
  private UmsatzDaysInput days  = null;

  private Konto konto           = null;
  private List umsaetze         = null;
  
  private KL kl                 = null;
  private boolean filter        = true;
  
  protected static de.willuhn.jameica.system.Settings mySettings = new de.willuhn.jameica.system.Settings(UmsatzList.class);

  private I18N i18n;
  
  /**
   * @param konto
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, Action action) throws RemoteException
  {
    this(konto,0,action);
    this.konto = konto;
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
    
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setMulti(true);
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Umsatz u = (Umsatz) item.getData();
        if (u == null) return;
        try {
          if (u.getBetrag() < 0.0)
          {
            item.setForeground(Settings.getBuchungSollForeground());
          }
          else
          {
            item.setForeground(Settings.getBuchungHabenForeground());
          }
        }
        catch (RemoteException e)
        {
        }
      }
    });

    // BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    addColumn("#","id-int");
    addColumn(i18n.tr("Gegenkonto"),                "empfaenger");
    addColumn(i18n.tr("Verwendungszweck"),          "zweck");
    addColumn(i18n.tr("Datum"),                     "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Betrag"),                    "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Kategorie"),                 "umsatztyp");
    // BUGZILLA 66 http://www.willuhn.de/bugzilla/show_bug.cgi?id=66
    addColumn(i18n.tr("Zwischensumme"), "saldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);
    
    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Umsaetze informiert werden.
    this.mc = new UmsMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

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
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzList(this.konto));

    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });

    if (this.filter)
    {
      // TODO: Koennte man auch mal noch auf DelayedListener umstellen, ist aber etwas umstaendlich hier
      if (this.kl == null)
        this.kl = new KL();

      LabelGroup group = new LabelGroup(parent,i18n.tr("Anzeige einschränken"));

      this.days = new UmsatzDaysInput();
      this.days.addListener(new Listener() {
        public void handleEvent(Event event)
        {
          kl.process();
        }
      });
      group.addLabelPair(i18n.tr("Nur Umsätze des Zeitraumes"), this.days);

      // Eingabe-Feld fuer die Suche mit Button hinten dran.
      this.search = new SearchInput();
      group.addLabelPair(i18n.tr("Zweck, Konto oder Kommentar enthält"), this.search);

      // Checkbox zur Aktivierung von regulaeren Ausdruecken
      this.regex = new CheckboxInput(mySettings.getBoolean("regex",false));
      this.regex.addListener(new Listener() {
        public void handleEvent(Event event)
        {
          boolean b = ((Boolean)regex.getValue()).booleanValue();
          mySettings.setAttribute("regex",b);
          kl.process();
        }
      });
      group.addCheckbox(this.regex,i18n.tr("Suchbegriff ist ein regulärer Ausdruck"));
    }

    super.paint(parent);

    // Und einmal starten bitte
    if (this.filter)
      kl.process();
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
                  regex.setValue(new Boolean(ir));
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

//      this.text = GUI.getStyleFactory().createText(parent);
      text = new Text(parent,SWT.NONE | SWT.SINGLE);
      text.setForeground(Color.WIDGET_FG.getSWTColor());
      text.setBackground(Color.WIDGET_BG.getSWTColor());
      // BUGZILLA 258
      this.text.setText(mySettings.getString("search",""));
      this.text.addKeyListener(kl);
      return this.text;
    }

    /**
     * @see de.willuhn.jameica.gui.input.Input#getValue()
     */
    public Object getValue()
    {
      String s = text == null ? null : text.getText();
      mySettings.setAttribute("search",s);
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
    
    private synchronized void process()
    {
      GUI.getView().setLogoText(i18n.tr("Aktualisiere Daten..."));
      GUI.startSync(new Runnable()
      {
        public void run()
        {
          try
          {
            if (!hasChanged())
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
              date = u.getValuta();

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

              if (typ.matches(u))
              {
                // ggf. vorher geworfene Fehlermeldung wieder entfernen
                GUI.getView().setErrorText("");
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
          finally
          {
            GUI.getView().setLogoText("");
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
  public class UmsMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public UmsMessageConsumer()
    {
      super();
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

      if (o == null || !(o instanceof Umsatz))
        return;

      // Ein Umsatz wurde geaendert
      if (message instanceof ObjectChangedMessage)
      {
        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              int index = removeItem(o);
              if (index == -1)
                return; // Objekt war nicht in der Tabelle
              
              // Aktualisieren, in dem wir es neu an der gleichen Position eintragen
             addItem(o,index);
             
             // Wir markieren es noch in der Tabelle
             select(o);
            }
            catch (Exception e)
            {
              Logger.error("unable to add object to list",e);
            }
          }
        });
        return;
      }
      // Neuer Umsatz hinzugekommen
      if (message instanceof ImportMessage)
      {
        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              umsaetze.add(o);
              if (filter && kl != null)
                kl.process();
              else
              {
                addItem(o);
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
    return this.getClass().getName();
  }
  
  
}


/**********************************************************************
 * $Log: UmsatzList.java,v $
 * Revision 1.54  2007/08/09 11:01:38  willuhn
 * @B Bug 462
 *
 * Revision 1.53  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.52  2007/04/27 15:30:44  willuhn
 * @N Kontoauszug-Liste in TablePart verschoben
 *
 * Revision 1.51  2007/04/26 23:08:13  willuhn
 * @C Umstellung auf DelayedListener
 *
 * Revision 1.50  2007/04/26 18:27:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.49  2007/04/26 12:20:12  willuhn
 * @B In Umsatzsuche nur die Kategorien mit Pattern anzeigen
 *
 * Revision 1.48  2007/04/25 14:06:57  willuhn
 * @C Vermeidung paralleler Datenhaltung
 *
 * Revision 1.47  2007/04/25 12:40:12  willuhn
 * @N Besseres Warteverhalten nach Texteingabe in Umsatzliste und Adressbuch
 *
 * Revision 1.46  2007/04/18 14:51:09  willuhn
 * @C removed 2 warnings
 *
 * Revision 1.45  2007/03/21 16:56:56  willuhn
 * @N Online-Hilfe aktualisiert
 * @N Bug 337 (Stichtag in Sparquote)
 * @C Refactoring in Sparquote
 *
 * Revision 1.44  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.42  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.41  2006/12/28 15:38:42  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.40  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 * Revision 1.39  2006/11/29 00:40:37  willuhn
 * @N Keylistener in Umsatzlist nur dann ausfuehren, wenn sich wirklich etwas geaendert hat
 * @C UmsatzTyp.matches matcht jetzt bei leeren Pattern nicht mehr
 *
 * Revision 1.38  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.36  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.35  2006/11/06 23:19:45  willuhn
 * @B Fehler bei Aktualisierung der Elemente nach Insert, Delete, Sort
 *
 * Revision 1.34  2006/11/06 23:12:38  willuhn
 * @B Fehler bei Aktualisierung der Elemente nach Insert, Delete, Sort
 *
 * Revision 1.33  2006/10/23 22:31:15  willuhn
 * @R removed debug output
 *
 * Revision 1.32  2006/10/23 22:30:43  willuhn
 * @C recompile ($LANG)
 *
 * Revision 1.31  2006/10/17 23:50:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.30  2006/10/09 23:50:00  willuhn
 * @N extendable
 *
 * Revision 1.29  2006/08/23 09:45:13  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.28  2006/08/08 21:18:21  willuhn
 * @B Bug 258
 *
 * Revision 1.27  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.26  2006/08/02 17:49:44  willuhn
 * @B Bug 255
 * @N Erkennung des Kontos beim Import von Umsaetzen aus dem Kontextmenu heraus
 *
 * Revision 1.25  2006/06/19 16:05:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 * Revision 1.23  2006/05/22 12:55:54  willuhn
 * @N bug 235 (thanks to Markus)
 *
 * Revision 1.22  2006/05/11 16:53:09  willuhn
 * @B bug 233
 *
 * Revision 1.21  2006/03/30 22:22:32  willuhn
 * @B bug 217
 *
 * Revision 1.20  2006/03/30 21:00:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2006/02/20 22:57:22  willuhn
 * @N Suchfeld in Adress-Liste
 *
 * Revision 1.18  2006/02/06 23:08:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2006/02/06 23:03:23  willuhn
 * @B Sortierung der Spalte "#"
 *
 * Revision 1.16  2006/02/06 14:53:39  willuhn
 * @N new column "#" in umsatzlist
 *
 * Revision 1.15  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.14  2005/12/16 16:35:31  willuhn
 * @N Filter UmsatzList width regular expressions
 *
 * Revision 1.13  2005/12/13 00:06:26  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.12  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.11  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.10  2005/11/18 17:39:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/11/14 21:41:02  willuhn
 * @B bug 5
 *
 * Revision 1.8  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.7  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.6  2005/06/21 20:15:33  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.3  2005/05/09 12:24:20  web0
 * @N Changelog
 * @N Support fuer Mehrfachmarkierungen
 * @N Mehere Adressen en bloc aus Umsatzliste uebernehmen
 *
 * Revision 1.2  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/