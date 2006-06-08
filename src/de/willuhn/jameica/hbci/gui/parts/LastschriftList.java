/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/LastschriftList.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/06/08 22:29:47 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.io.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Lastschriften.
 */
public class LastschriftList extends TablePart implements Part
{
  private I18N i18n = null;

  private MessageConsumer mc = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public LastschriftList(Action action) throws RemoteException
  {
    super(init(), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Lastschrift l = (Lastschrift) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    addColumn(i18n.tr("Empfänger-Konto"),"konto_id");
    addColumn(i18n.tr("Zahlungspflichtiger"),"empfaenger_name");
    addColumn(i18n.tr("Belastetes Konto"),"empfaenger_konto");
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

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.LastschriftList());

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Lastschriften informiert werden.
    this.mc = new LastMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

  }

  // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
  /**
   * Initialisiert die Liste der Lastschriften.
   * @return Initialisiert die Liste der Lastschriften.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Lastschrift.class);
    list.setOrder("ORDER BY TONUMBER(termin) DESC");
    return list;
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
    super.paint(parent);
  }
  
  /**
   * Hilfsklasse damit wir ueber importierte Lastschriften informiert werden.
   */
  public class LastMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public LastMessageConsumer()
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
      
      if (o == null || !(o instanceof Lastschrift))
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

}


/**********************************************************************
 * $Log: LastschriftList.java,v $
 * Revision 1.7  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.6  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.5  2006/05/11 16:53:09  willuhn
 * @B bug 233
 *
 * Revision 1.4  2006/03/30 21:00:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.2  2005/06/23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/