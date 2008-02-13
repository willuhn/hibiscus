/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypList.java,v $
 * $Revision: 1.8 $
 * $Date: 2008/02/13 23:44:27 $
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
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit den existiernden Umsatz-Typen.
 */
public class UmsatzTypList extends TablePart implements Part
{

  private I18N i18n = null;
  private MessageConsumer mc = null;

  /**
   * ct.
   * @param action
   * @throws RemoteException
   */
  public UmsatzTypList(Action action) throws RemoteException
  {
    super(Settings.getDBService().createList(UmsatzTyp.class), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    addColumn(i18n.tr("Bezeichnung"),"name");
    addColumn(i18n.tr("Nummer"),"nummer");
    addColumn(i18n.tr("Zweck, Name oder Konto enth‰lt"),"pattern");
    addColumn(i18n.tr("Umsatzart"),"iseinnahme",new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return i18n.tr("unbekannt");
        Integer i = (Integer) o;
        return i.intValue() == 1 ? UmsatzTyp.EINNAHME : UmsatzTyp.AUSGABE;
      }
    });

    this.setMulti(true);
    this.setSummary(false);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzTypList());
    
    this.mc = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
  }
  
  /**
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
   * Hierueber werden wir ueber importierte Umsatz-Typen informiert und aktualisieren
   * die Tabelle.
   */
  private class MyMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
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
      final GenericObject data = ((ObjectMessage)message).getObject();

      if (data == null || !(data instanceof UmsatzTyp))
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            removeItem(data);
            addItem(data);
            sort();
          }
          catch (Exception e)
          {
            Logger.error("unable to add object to list",e);
          }
        }
      });

    }
    
  }
}


/**********************************************************************
 * $Log: UmsatzTypList.java,v $
 * Revision 1.8  2008/02/13 23:44:27  willuhn
 * @R Hibiscus-Eigenformat (binaer-serialisierte Objekte) bei Export und Import abgeklemmt
 * @N Import und Export von Umsatz-Kategorien im XML-Format
 * @B Verzaehler bei XML-Import
 *
 * Revision 1.7  2007/03/10 07:17:58  jost
 * Neu: Nummer f√ºr die Sortierung der Umsatz-Kategorien
 *
 * Revision 1.6  2006/11/24 00:07:09  willuhn
 * @C Konfiguration der Umsatz-Kategorien in View Einstellungen verschoben
 * @N Redesign View Einstellungen
 *
 * Revision 1.5  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.4  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 * Revision 1.3  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.2  2005/12/29 01:22:12  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.1  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 **********************************************************************/