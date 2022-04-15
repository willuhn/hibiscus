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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Addressbook book       = null;
  private TextInput search       = null;
  private KeyAdapter listener    = null;
  private AddressFilter filter   = null;
  private boolean createButton   = true;

  private MessageConsumer mcImport = null;
  private MessageConsumer mcChanged = null;
  
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
    this(action,filter,true);
  }

  /**
   * ct.
   * @param action
   * @param filter optionaler Filter.
   * @param createButton true, wenn ein Button zum Anlegen einer neuen Adresse mit angezeigt werden soll.
   * @throws RemoteException
   */
  public EmpfaengerList(Action action, AddressFilter filter, boolean createButton) throws RemoteException
  {
    super(action);
    
    this.filter = filter;
    this.listener = new DelayedAdapter();
    this.createButton = createButton;

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
          String name = HBCIProperties.getNameForBank(blz);
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
    addColumn(i18n.tr("IBAN"),"iban", new IbanFormatter());
    addColumn(i18n.tr("Gruppe"),"kategorie");
    addColumn(i18n.tr("Notiz"),"kommentar",new Formatter()
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
            item.setForeground(Color.FOREGROUND.getSWTColor());
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
    
    this.setRememberState(true);

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Adressen informiert werden.
    this.mcImport = new EmpfaengerImportMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mcImport);
    
    // Und noch ein Message-Consumer fuer geaenderte Adressen
    this.mcChanged = new EmpfaengerChangedMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mcChanged);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

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
        tab.addLabelPair(i18n.tr("Adressbuch"), select);
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
    tab.addLabelPair(i18n.tr("Suchbegriff"), this.search);
    this.search.getControl().addKeyListener(this.listener);

    if (this.createButton)
    {
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(i18n.tr("Neue Adresse"),new EmpfaengerNew(),null,true,"contact-new.png");
      buttons.paint(parent);
    }
    
    // Damit wir den MessageConsumer beim Schliessen wieder entfernen
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mcImport);
        Application.getMessagingFactory().unRegisterMessageConsumer(mcChanged);
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
  public class EmpfaengerImportMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public EmpfaengerImportMessageConsumer()
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
   * Message-Consumer der ueber geaenderte Adressen benachrichtigt wird.
   */
  private class EmpfaengerChangedMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ObjectChangedMessage.class};
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          try
          {
            ObjectMessage m = (ObjectMessage) message;
            Object o = m.getObject();
            updateItem(o,o);
          }
          catch (Exception e)
          {
            Logger.error("unable to update address",e);
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
   * Aktualisiert die Liste der angezeigten Elemente abhaengig vom
   * eingegebenen Suchtext und dem ggf. ausgewaehlten Adressbuch.
   */
  private synchronized void reload()
  {
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
          List<Address> found = EmpfaengerList.this.book.findAddresses(text);
          if (found == null)
            return;
          for (Address a : found)
          {
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
