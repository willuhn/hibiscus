/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzList.java,v $
 * $Revision: 1.15 $
 * $Date: 2005/12/30 00:14:45 $
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
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
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
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.ButtonInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypEdit;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypNewDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Umsaetzen.
 */
public class UmsatzList extends TablePart
{

  private SearchInput search    = null;
  private CheckboxInput regex   = null;

  
  private GenericIterator list  = null;
  private ArrayList umsaetze    = null;
  
  private KL kl                 = new KL();

  private I18N i18n;

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
  }

  /**
   * @param list
   * @param action
   */
  public UmsatzList(GenericIterator list, Action action)
  {
    super(list, action);
    
    this.list = list;
    
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
          // Waehrung des Kontos dranpappen
          item.setText(3,item.getText(3) + " " + u.getKonto().getWaehrung());
          item.setText(4,item.getText(4) + " " + u.getKonto().getWaehrung());
        }
        catch (RemoteException e)
        {
        }
      }
    });

    // BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    // BUGZILLA 86 http://www.willuhn.de/bugzilla/show_bug.cgi?id=86
    addColumn(i18n.tr("Gegenkonto"),                "empfaenger");
    addColumn(i18n.tr("Verwendungszweck"),          "zweck");
    addColumn(i18n.tr("Valuta"),                    "valuta", new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Betrag"),                    "betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
    // BUGZILLA 66 http://www.willuhn.de/bugzilla/show_bug.cgi?id=66
    addColumn(i18n.tr("Saldo zu diesem Zeitpunkt"), "saldo",  new CurrencyFormatter("",HBCI.DECIMALFORMAT));

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzList());
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Filter"));

    // Eingabe-Feld fuer die Suche mit Button hinten dran.
    this.search = new SearchInput();
    group.addLabelPair(i18n.tr("Zweck, Name oder Konto enthält"), this.search);

    // Checkbox zur Aktivierung von regulaeren Ausdruecken
    this.regex = new CheckboxInput(false);
    this.regex.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        kl.process();
      }
    });
    group.addCheckbox(this.regex,i18n.tr("Suchbegriff ist ein regulärer Ausdruck"));

    super.paint(parent);

    // Wir kopieren den ganzen Kram in eine ArrayList, damit die
    // Objekte beim Filter geladen bleiben
    umsaetze = new ArrayList();
    list.begin();
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      umsaetze.add(u);
    }
  }


  /**
   * Hilfsklasse fuer das Suchfeld.
   * @author willuhn
   */
  private class SearchInput extends ButtonInput
  {
    private Text text         = null;

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
          item.setText(i18n.tr("Als Umsatz-Filter speichern..."));
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
                existing.addFilter("pattern = '" + text + "'");
                UmsatzTyp typ = null; 
                if (existing.size() > 0)
                {
                  if (!Application.getCallback().askUser(i18n.tr("Umsatz-Filter existiert bereits. Überschreiben?")))
                    return;
                  
                  // Wenn wir ihn ueberschreiben, verwenden wir den Namen nochmal.
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
                GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz-Filter gespeichert"));
              }
              catch (Exception ex)
              {
                Logger.error("unable to store umsatz filter",ex);
                GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Umsatz-Filters"));
              }
            }
          });
          
          try
          {
            DBIterator i = Settings.getDBService().createList(UmsatzTyp.class);
            if (i.size() > 0)
            {
              MenuItem edit = new MenuItem(menu, SWT.PUSH);
              edit.setText(i18n.tr("Umsatz-Filter bearbeiten..."));
              edit.addListener(SWT.Selection,new Listener() {
                public void handleEvent(Event event)
                {
                  try
                  {
                    new UmsatzTypEdit().handleAction(null);
                  }
                  catch (Exception e)
                  {
                    Logger.error("error while editing umsatz filter",e);
                    GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Bearbeiten der Umsatz-Filter"));
                  }
                }
              });
              new MenuItem(menu, SWT.SEPARATOR);
              
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
            
          }
          catch (Exception ex)
          {
            Logger.error("unable to load umsatz filter",ex);
            GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Umsatz-Filters"));
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
      
      // Listener fuer das Text-Eingabe-Feld.
      this.addListener(new Listener()
      {
        public void handleEvent(Event event)
        {
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

      this.text = GUI.getStyleFactory().createText(parent);
      this.text.addKeyListener(kl);
      return this.text;
    }

    /**
     * @see de.willuhn.jameica.gui.input.Input#getValue()
     */
    public Object getValue()
    {
      return text == null ? null : text.getText();
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
    private Thread timeout = null;
   
    /**
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      // Mal schauen, ob schon ein Thread laeuft. Wenn ja, muessen wir den
      // erst killen
      if (timeout != null)
      {
        timeout.interrupt();
        timeout = null;
      }
      
      // Ein neuer Timer
      timeout = new Thread("UmsatzList")
      {
        public void run()
        {
          try
          {
            // Wir warten 900ms. Vielleicht gibt der User inzwischen weitere
            // Sachen ein.
            sleep(700l);

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
      GUI.getDisplay().syncExec(new Runnable()
      {
        public void run()
        {
          try
          {
            // Erstmal alle rausschmeissen
            removeAll();

            // Wir holen uns den aktuellen Text
            String text = (String) search.getValue();

            Umsatz u = null;

            UmsatzTyp typ = null;
            
            if (text != null && text.length() > 0)
            {
              DBService service = Settings.getDBService();
              typ = (UmsatzTyp) service.createObject(UmsatzTyp.class,null);
              typ.setPattern(text);
              typ.setRegex(((Boolean)regex.getValue()).booleanValue());
            }

            for (int i=0;i<umsaetze.size();++i)
            {
              u = (Umsatz) umsaetze.get(i);

              // Was zum Filtern da?
              if (text == null || text.length() == 0)
              {
                // ne
                addItem(u);
                continue;
              }

              if (typ.matches(u))
              {
                // ggf. vorher geworfene Fehlermeldung wieder entfernen
                GUI.getStatusBar().setErrorText("");
                addItem(u);
              }
            }
          }
          catch (PatternSyntaxException pe)
          {
            GUI.getStatusBar().setErrorText(pe.getLocalizedMessage());
          }
          catch (Exception e)
          {
            Logger.error("error while loading umsatz",e);
          }
        }
      });
    }
  }

}


/**********************************************************************
 * $Log: UmsatzList.java,v $
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