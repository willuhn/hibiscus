/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/EmpfaengerList.java,v $
 * $Revision: 1.25 $
 * $Date: 2010/04/14 17:44:10 $
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
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Adressen.
 */
public class EmpfaengerList extends TablePart implements Part
{
  private Addressbook book       = null;
  private TextInput search       = null;
  private I18N i18n              = null;
  private KeyAdapter listener    = null;
  private AddressFilter filter   = null;

  private MessageConsumer mc = null;
  
  private static Settings mySettings = new Settings(EmpfaengerList.class);

  /**
   * ct.
   * @param action
   * @throws RemoteException
   */
  public EmpfaengerList(Action action) throws RemoteException
  {
    this(action,null);
  }
  
  /**
   * ct.
   * @param action
   * @param filter optionaler Filter.
   * @throws RemoteException
   */
  public EmpfaengerList(Action action, AddressFilter filter) throws RemoteException
  {
    super(action);
    
    this.filter = filter;
    
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.listener = new DelayedAdapter();

    addColumn(i18n.tr("Name"),"name");
    addColumn(i18n.tr("Kontonummer"),"kontonummer");
    addColumn(i18n.tr("Bankleitzahl"),"blz", new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return null;
        try
        {
          String blz = o.toString();
          String name = HBCIUtils.getNameForBLZ(blz);
          if (name == null || name.length() == 0)
            return blz;
          return blz + " [" + name + "]";
        }
        catch (Exception e)
        {
          Logger.error("error while formatting blz",e);
          return o.toString();
        }
      }
    });
    addColumn(i18n.tr("IBAN"),"iban");
    addColumn(i18n.tr("Kategorie"),"kategorie");
    addColumn(i18n.tr("Kommentar"),"kommentar",new Formatter()
    {
      public String format(Object o)
      {
        if (o == null)
          return null;
        String s = o.toString();

        s = s.replaceAll("\\n|\\r",", ");

        if (s.length() < 30)
          return s;
        return s.substring(0,29) + "...";
      }
    });
    
    this.setFormatter(new TableFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TableItem item)
      {
        try
        {
          Object o = item.getData();
          if (o == null || !(o instanceof Address))
            return;
          if (o instanceof HibiscusAddress)
            item.setForeground(Color.WIDGET_FG.getSWTColor());
          else
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("unable to format address",e);
        }
      }
    });
    
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.EmpfaengerList());

    this.setMulti(true);

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    this.setRememberOrder(true);
    
    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    this.setRememberColWidths(true);

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Adressen informiert werden.
    this.mc = new EmpfaengerMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Anzeige einschränken"));

    /////////////////////////////////////////////////////////////////
    // Mal schauen, ob wir mehrere Adressbuecher haben. Ist das der Fall,
    // dann zeigen wir eine Auswahlbox an.
    try
    {
      AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      this.book = service; // Wir machen das gleich zum Default-Adressbuch
      if (service.hasExternalAddressbooks())
      {
        // Es existieren mehrere. Wir zeigen eine Auswahl an
        final SelectInput select = new SelectInput(service.getAddressbooks(),null);
        select.setAttribute("name");
        select.addListener(new Listener() {
          public void handleEvent(Event event)
          {
            Object value = select.getValue();
            if (value == null || !(value instanceof Addressbook))
              return;
            EmpfaengerList.this.book = (Addressbook) value;
            reload(); // Anzeige aktualisieren
          }
        });
        group.addLabelPair(i18n.tr("Adressbuch"), select);
      }
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to load addressbook service",e);
    }
    /////////////////////////////////////////////////////////////////

    // Eingabe-Feld fuer die Suche mit Button hinten dran.
    this.search = new TextInput(mySettings.getString("search",null));
    group.addLabelPair(i18n.tr("Suchbegriff"), this.search);
    this.search.getControl().addKeyListener(this.listener);

    
    // Damit wir den MessageConsumer beim Schliessen wieder entfernen
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });

    // Wir machen ein initiales Reload fuer die Erstbefuellung
    reload();

    // Und jetzt kann sich die Tabelle malen
    super.paint(parent);
    super.sort();
  }

  
  /**
   * Hilfsklasse damit wir ueber importierte Empfaenger informiert werden.
   */
  public class EmpfaengerMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public EmpfaengerMessageConsumer()
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
      final GenericObject o = ((ImportMessage)message).getObject();
      
      if (o == null || !(o instanceof Address))
        return;
      
      // Falls ein ganzer Pulk von Update kommt, machen wir
      // nicht jedesmal ein Reload - sondern feuern einfach
      // nur das Event fuer "Text in Suchfeld eingegeben".
      // Das wartet vorm Update immer noch ein paar Millisekunden
      // Sollten ganz viele Updates schnell hintereinander
      // kommen, wird das Update damit naemlich erst nach
      // der letzten Aenderung durchgefuehrt.
      if (listener != null)
        listener.keyReleased(null);
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
   * Aktualisiert die Liste der angezeigten Elemente abhaengig vom
   * eingegebenen Suchtext und dem ggf. ausgewaehlten Adressbuch.
   */
  private synchronized void reload()
  {
    GUI.getView().setLogoText(i18n.tr("Aktualisiere Daten..."));
    GUI.startSync(new Runnable() // Sanduhr anzeigen
    {
      public void run()
      {
        try
        {

          // Erstmal leer machen
          EmpfaengerList.this.removeAll();
          
          // Jetzt fragen wir das aktuelle Adressbuch nach den gesuchten Adressen
          String text = (String) EmpfaengerList.this.search.getValue();
          mySettings.setAttribute("search",text);
          List found = EmpfaengerList.this.book.findAddresses(text);
          if (found == null)
            return;
          for (int i=0;i<found.size();++i)
          {
            Address a = (Address) found.get(i);
            if (filter == null || filter.accept(a))
              EmpfaengerList.this.addItem(a);
          }

          // Fertig. Jetzt nochmal neu sortieren
          EmpfaengerList.this.sort();
        }
        catch (Exception e)
        {
          Logger.error("error while reloading addresses",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der Adressen"), StatusBarMessage.TYPE_ERROR));
        }
        finally
        {
          GUI.getView().setLogoText("");
        }
      }
    });
  }
  
  /**
   * Da KeyAdapter/KeyListener nicht von swt.Listener abgeleitet
   * sind, muessen wir leider dieses schraege Konstrukt verenden,
   * um den DelayedListener verwenden zu koennen
   */
  private class DelayedAdapter extends KeyAdapter
  {
    private Listener forward = new DelayedListener(700,new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        // hier kommt dann das verzoegerte Event an.
        reload();
      }
    
    });

    /**
     * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      forward.handleEvent(null); // Das Event-Objekt interessiert uns eh nicht
    }
  }
}


/**********************************************************************
 * $Log: EmpfaengerList.java,v $
 * Revision 1.25  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.24  2010/04/11 21:57:08  willuhn
 * @N Anzeige der eigenen Konten im Adressbuch als "virtuelle" Adressen. Basierend auf Ralfs Patch.
 *
 * Revision 1.23  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.22  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.21  2009/02/19 23:42:01  willuhn
 * @N Filter fuer Adressbuch zum Ausblenden von Adressen (z.Bsp. bei Auslandsueberweisungen alle ausblenden, die keine IBAN haben)
 *
 * Revision 1.20  2007/04/26 23:08:13  willuhn
 * @C Umstellung auf DelayedListener
 *
 * Revision 1.19  2007/04/26 15:02:36  willuhn
 * @N Optisches Feedback beim Neuladen der Daten
 *
 * Revision 1.18  2007/04/25 12:40:12  willuhn
 * @N Besseres Warteverhalten nach Texteingabe in Umsatzliste und Adressbuch
 *
 * Revision 1.17  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.16  2007/04/20 14:55:31  willuhn
 * @C s/findAddress/findAddresses/
 *
 * Revision 1.15  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.14  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.13  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.12  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.11  2006/10/17 23:50:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 * Revision 1.9  2006/08/05 20:44:39  willuhn
 * @B Bug 256
 *
 * Revision 1.8  2006/05/11 16:53:09  willuhn
 * @B bug 233
 *
 * Revision 1.7  2006/03/30 22:22:32  willuhn
 * @B bug 217
 *
 * Revision 1.6  2006/02/20 22:57:22  willuhn
 * @N Suchfeld in Adress-Liste
 *
 * Revision 1.5  2006/02/06 15:31:00  willuhn
 * @N Anzeige des Banknamens in Adressbuch-Liste
 *
 * Revision 1.4  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.3  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.2  2005/05/09 12:24:20  web0
 * @N Changelog
 * @N Support fuer Mehrfachmarkierungen
 * @N Mehere Adressen en bloc aus Umsatzliste uebernehmen
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/