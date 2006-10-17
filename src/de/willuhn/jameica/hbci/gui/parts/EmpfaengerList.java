/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/EmpfaengerList.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/10/17 23:50:20 $
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.io.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Adressen.
 */
public class EmpfaengerList extends TablePart implements Part
{
  private TextInput search      = null;

  private GenericIterator list  = null;
  
  private I18N i18n = null;

  private MessageConsumer mc = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public EmpfaengerList(Action action) throws RemoteException
  {
    this(Settings.getDBService().createList(Adresse.class), action);
  }
  
  /**
   * @param list
   * @param action
   */
  public EmpfaengerList(GenericIterator list, Action action)
  {
    super(list,action);

    this.list = list;
    
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
    addColumn(i18n.tr("Kommentar"),"kommentar",new Formatter() {
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
    // Empfaengerninformiert werden.
    this.mc = new EmpfaengerMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Filter"));

    // Eingabe-Feld fuer die Suche mit Button hinten dran.
    this.search = new TextInput("");
    group.addLabelPair(i18n.tr("Name. Konto oder BLZ enthält"), this.search);

    this.search.getControl().addKeyListener(new KL());

    // Damit wir den MessageConsumer beim Schliessen wieder entfernen
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });

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
      final GenericObject o = ((ImportMessage)message).getImportedObject();
      
      if (o == null || !(o instanceof Adresse))
        return;
      
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            addItem(o);
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
      timeout = new Thread("AddressList")
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
            EmpfaengerList.this.removeAll();

            Adresse a = null;

            // Wir holen uns den aktuellen Text
            String text = (String) search.getValue();

            boolean empty = text == null || text.length() == 0;
            if (!empty) text = text.toLowerCase();

            list.begin();
            while (list.hasNext())
            {
              a = (Adresse) list.next();

              // Was zum Filtern da?
              if (empty)
              {
                // ne
                EmpfaengerList.this.addItem(a);
                continue;
              }

              String s1 = a.getName();
              String s2 = a.getKontonummer();
              String s3 = a.getBLZ();
              
              s1 = s1 == null ? "" : s1.toLowerCase();
              s2 = s2 == null ? "" : s2.toLowerCase();
              s3 = s3 == null ? "" : s3.toLowerCase();

              if (s1.indexOf(text) != -1 || s2.indexOf(text) != -1 || s3.indexOf(text) != -1)
              {
                EmpfaengerList.this.addItem(a);
              }
            }
            EmpfaengerList.this.sort();
          }
          catch (Exception e)
          {
            Logger.error("error while loading address",e);
          }
        }
      });
    }
  }
  
  
}


/**********************************************************************
 * $Log: EmpfaengerList.java,v $
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