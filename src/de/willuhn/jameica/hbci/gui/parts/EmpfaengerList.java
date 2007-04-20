/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/EmpfaengerList.java,v $
 * $Revision: 1.16 $
 * $Date: 2007/04/20 14:55:31 $
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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
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
  private KeyAdapter keyListener = null;

  private MessageConsumer mc = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public EmpfaengerList(Action action) throws RemoteException
  {
    super(action);
    
    this.setMulti(true);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.EmpfaengerList());

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);
    
    setMulti(true);
    
    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);

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
    this.search = new TextInput("");
    group.addLabelPair(i18n.tr("Name. Konto oder BLZ enthält"), this.search);

    this.keyListener = new KL();
    this.search.getControl().addKeyListener(this.keyListener);

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
      
      if (o == null || !(o instanceof Adresse))
        return;
      
      // Falls ein ganzer Pulk von Update kommt, machen wir
      // nicht jedesmal ein Reload - sondern feuern einfach
      // nur das Event fuer "Text in Suchfeld eingegeben".
      // Das wartet vorm Update immer noch ein paar Millisekunden
      // Sollten ganz viele Updates schnell hintereinander
      // kommen, wird das Update damit naemlich erst nach
      // der letzten Aenderung durchgefuehrt.
      if (EmpfaengerList.this.keyListener != null)
        EmpfaengerList.this.keyListener.keyReleased(null);
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
    GUI.getDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        try
        {

          // Erstmal leer machen
          EmpfaengerList.this.removeAll();
          
          // Jetzt fragen wir das aktuelle Adressbuch nach den gesuchten Adressen
          GenericIterator found = EmpfaengerList.this.book.findAddresses((String) EmpfaengerList.this.search.getValue());
          if (found == null)
            return;
          while (found.hasNext())
            EmpfaengerList.this.addItem(found.next());

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
      timeout = new Thread("AddressList Reload")
      {
        public void run()
        {
          try
          {
            // Wir warten 900ms. Vielleicht gibt der User inzwischen weitere
            // Sachen ein.
            sleep(700l);

            // Ne, wir wurden nicht gekillt. Also machen wir uns ans Werk
            reload();

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
  }
  
  
}


/**********************************************************************
 * $Log: EmpfaengerList.java,v $
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